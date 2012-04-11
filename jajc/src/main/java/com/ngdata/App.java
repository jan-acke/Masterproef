package com.ngdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.io.Payloads;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;


import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Module;
import com.ngdata.bo.Instance;
import com.ngdata.bo.Roles;
import com.ngdata.exception.JaJcLogicalConfigException;

/**
 * Hello world!
 *
 */

public class App 
{	
	
	private ComputeServiceContext context;
	
	
	public App(String configFileName) throws Exception {
		IConfig config = YamlConfigReader.createYAMLConfig(configFileName);
		Iterable<NodeMetadata> nodes = null;
		if (config.getInstances().size() == 0)
			throw new JaJcLogicalConfigException("no instances defined in " + configFileName);
		
		if (Roles.getInstances("puppetmaster") == null || Roles.getInstances("puppetmaster").size() > 1 )
			throw new JaJcLogicalConfigException("Need at least one puppetmaster" + configFileName);
				
		String provider = config.getGeneral().getProvider();
		if ( "byon".equals(provider) ) {
			nodes = createByonContext(config);
		}
		
		else if ( "awsec2".equals(provider)) {
			nodes = createAwsec2Context(config);
		}
		
		
		NodeMetadata debug = nodes.iterator().next();
		System.out.println("Username : " + debug.getCredentials().getUser());
		System.out.println("Password : " + debug.getCredentials().getPassword());
		System.out.println("Pkey     : " + debug.getCredentials().getPrivateKey());
		
		//install puppet and puppet.conf on all the nodes
		Collection<String> lines = new ArrayList<String>();
		lines.add("[master]");
		//lines.add("modulepath = /vagrant/mytests/Masterproef/puppet/modules");
		lines.add("[main]");
		lines.add("logdir = /var/lib/puppet/log");
		lines.add("vardir = /var/lib/puppet");
		lines.add("factpath = $vardir/lib/facter");
		lines.add("ssldir = /var/lib/puppet/ssl");
		lines.add("rundir = /var/run/puppet");
		lines.add("pluginsync = true");
		
		
		ScriptBuilder sb = new ScriptBuilder();
		sb.addStatement(Statements.exec("apt-get install -y rubygems"));
		sb.addStatement(Statements.exec("apt-get install -y tar"));
		sb.addStatement(Statements.exec("gem install puppet facter --no-ri --no-rdoc"));
		sb.addStatement(Statements.exec("groupadd puppet"));
		sb.addStatement(Statements.exec("useradd -g puppet -G admin puppet"));
		sb.addStatement(Statements.exec("export PATH=$PATH:/opt/ruby/bin")); //puppet in PATH
		sb.addStatement(Statements.createOrOverwriteFile("/etc/puppet/puppet.conf", lines));
		
		System.out.println("Installing necessary components for puppet");
		//om ec2 te doen werken moet hier de Pr ingesteld worden, lukt NIET door bij het Template dit in te stellen
		//context.getComputeService().runScriptOnNode(debug.getId(), sb, RunScriptOptions.Builder.overrideLoginPrivateKey(config.getProviderSpecificInfo("awsec2").get("private_key")));
		
		context.getComputeService().runScriptOnNodesMatching(Predicates.<NodeMetadata> alwaysTrue(), sb);
		
		System.out.println("Configuring puppet master");
		
		//Placing puppet modules in puppetmaster and generate manifest
		NodeMetadata pm = Iterables.filter(nodes,new Predicate<NodeMetadata>() {
			@Override public boolean apply(NodeMetadata nd) {
				return nd.getTags().contains("puppetmaster");
			} } ).iterator().next();
		
		ScriptBuilder master = new ScriptBuilder();
		master.addStatement(Statements.exec("mkdir -p /etc/puppet/manifests"));
		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/manifests/site.pp", ManifestBuilder.createPuppetManifests(nodes)));
		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/autosign.conf", Arrays.asList("*") ));
		context.getComputeService().runScriptOnNode( pm.getId() , master);
					
		SshClient ssh = context.utils().sshForNode().apply(NodeMetadataBuilder.fromNodeMetadata(pm).build());
		
		try {
			ssh.connect();
			ssh.put("/tmp/modules.tgz", Payloads.newFilePayload(new File("/home/jacke/Documents/eindwerk/Masterproef/jajc/zever")));
			System.out.println(ssh.exec("sudo tar xzf /tmp/modules.tgz -C /etc/puppet/"));
			System.out.println(ssh.exec("sudo /opt/ruby/bin/puppet master"));
			System.out.println(ssh.exec("echo 'certname = '" + pm.getHostname() + "| sudo tee -a /etc/puppet/puppet.conf")); //must be in the main section of the puppet master only 
		}
		finally {
			if (ssh != null)
				ssh.disconnect();
		}
		
		System.out.println("Starting puppet agents");
		Map<? extends NodeMetadata, ExecResponse> resp3 = context.getComputeService()
				.runScriptOnNodesMatching(Predicates.<NodeMetadata> alwaysTrue()
							, Statements.exec("/opt/ruby/bin/puppet agent --server " + pm.getHostname()));
		for ( NodeMetadata entry : resp3.keySet()) {
			System.out.println(entry.getHostname() + " ---> " + resp3.get(entry));
			
		}
		
		context.close();
			
	}
	
    private Iterable<NodeMetadata> createAwsec2Context(IConfig config) throws RunNodesException, JaJcLogicalConfigException {
    	Map<String,String> awsec2config = config.getProviderSpecificInfo("awsec2");
    	context =  new ComputeServiceContextFactory().
            	createContext("aws-ec2",awsec2config.get("accesskeyid"), awsec2config.get("secretkey"),
            			ImmutableSet.<Module> of(new SshjSshClientModule()));
    	TemplateBuilder tb = context.getComputeService().templateBuilder();
    	Map<Template,Integer> templates = new HashMap<Template,Integer>();
    	for (Instance i : config.getInstances()){
    		Map<String, String> options = i.getOptions();
    		int number; 
    		try {
    			number = Integer.parseInt(options.get("number"));
    		} catch (Exception ex) {
    			System.out.println("Invalid or missing number value in configuration of instances, eg. number = '2'. Assuming 1 for now");
    			number = 1;
    		}
    		String hardware = getValue(awsec2config, options, "hardware");
    		String image = getValue(awsec2config, options, "image");
    		String location = getValue(awsec2config,options,"location");
    		
    		Template t = tb.hardwareId(hardware).imageId(location + "/" + image).build();
    		//t.getOptions().overrideLoginCredentials(new LoginCredentials("ubuntu", null, awsec2config.get("private_key"), false));
    		t.getOptions().overrideLoginPrivateKey(awsec2config.get("private_key"));
    		t.getOptions().tags(i.getRoles());
    		t.getOptions().as(AWSEC2TemplateOptions.class).securityGroups("evert-upgrade-3");
    		t.getOptions().as(AWSEC2TemplateOptions.class).keyPair("jacke");
    		templates.put(t,number);
    		
    	}

		Set<NodeMetadata> nodes = new HashSet<NodeMetadata>();
    	for (Template template : templates.keySet()){
    		 Set<? extends NodeMetadata> tmp = context.getComputeService().createNodesInGroup("jan-jclouds", templates.get(template), template);
    		 nodes.addAll(tmp);
    	}
    	return nodes;
		
    }

	@SuppressWarnings("unchecked")
	private Iterable<NodeMetadata> createByonContext(IConfig config) throws JaJcLogicalConfigException {
    	Properties byonProps = new Properties();
    	Map<String,String> byonConfig = config.getProviderSpecificInfo("byon");
    	
    	StringBuilder yaml = new StringBuilder(); 
    	yaml.append("nodes:");
    	for ( Instance i : config.getInstances()) {
    		Map<String,String> instanceConfig = i.getOptions();
    		String os_arch = getValue(byonConfig, instanceConfig, "os_arch");
    		String os_family = getValue(byonConfig, instanceConfig, "os_family");
    		String os_description = getValue(byonConfig, instanceConfig, "os_description");
    		String os_version = getValue(byonConfig,instanceConfig,"os_version");
    		String username = getValue(byonConfig,instanceConfig,"username",true);
    		String sudo_password = getValue(byonConfig,instanceConfig,"sudo_password",true);
    		String credential = getValue(byonConfig,instanceConfig,"credential");
    		String credential_url = getValue(byonConfig, instanceConfig, "credential_url");
    		if (credential == "" && credential_url == "")
    			throw new JaJcLogicalConfigException("Need one of these options : credential , credential_url");
    		if ( i.getHostnames() == null || i.getHostnames().size() == 0)
    			throw new JaJcLogicalConfigException("Hostnames are required for byon configuration");
    		
        	
    		for (String hostname : i.getHostnames()) {
    			yaml.append("\n    - id: ").append(hostname);
    			//yaml.append("\n      name: ").append(hostname);
    			yaml.append("\n      hostname: ").append(hostname);
    			yaml.append("\n      os_arch: ").append(os_arch);
    			yaml.append("\n      os_family: ").append(os_family);
    			yaml.append("\n      os_description: ").append(os_description);
    			yaml.append("\n      os_version: ").append(os_version);
    			yaml.append("\n      group: ").append(config.getGeneral().getClustername());
    			yaml.append("\n      username: ").append(username);
    			yaml.append("\n      sudo_password: ").append(sudo_password);
    			if (credential == null)
    				yaml.append("\n      credential_url: file://").append(credential_url);
    			else
    				yaml.append("\n      credential: ").append(credential);
    			yaml.append("\n      tags:");
    			for (String role : i.getRoles())
    				yaml.append("\n        - " + role);
    			yaml.append("\n");
    		}
    	}

		//byonProps.setProperty("byon.endpoint", "file:///home/jacke/Documents/eindwerk/Masterproef/jajc/nodes-byon.yml");
    	//System.out.println(yaml);
    	byonProps.setProperty("byon.nodes", yaml.toString());
    	context = new ComputeServiceContextFactory().createContext("byon","foo","bar",
				ImmutableSet.<Module> of (new SshjSshClientModule() ) , byonProps);
    	return (Iterable<NodeMetadata>) context.getComputeService().listNodes();
    	
		
	}
    
    private String getValue(Map<String,String> lowerPriority , Map<String,String> higherPriority, String key) throws JaJcLogicalConfigException {
    	return getValue(lowerPriority, higherPriority, key,false);
    }
    
    private String getValue (Map<String,String> lowerPriority , Map<String,String> higherPriority, String key, boolean required ) throws JaJcLogicalConfigException {
    	String value;
    	try {
    		value = higherPriority.get(key) == null ? lowerPriority.get(key) : higherPriority.get(key);
    		//ugly hack because we only want to deal with strings TODO reevaluate
    	} catch (ClassCastException ex){
    		value = "" + higherPriority.get(key) == null ? lowerPriority.get(key) : higherPriority.get(key);
    	}
    	if (value == null && required)
    		throw new JaJcLogicalConfigException("Missing " + key + " option");
    	else if (value == null)
    		value = "";
    	return value;
    }

	public static void main( String[] args ) throws Exception {
    	new App(args[0]);
    }
    
    
}