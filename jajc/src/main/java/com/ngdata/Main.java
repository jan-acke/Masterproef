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


public class Main 
{	
	
	private ComputeServiceContext context;
	
	
	public Main(String configFileName) throws Exception {
		IConfig config = YamlConfigReader.createYAMLConfig(configFileName);
		
		if (config.getInstances().size() == 0)
			throw new JaJcLogicalConfigException("no instances defined in " + configFileName);
		
		if (Roles.getInstances("puppetmaster") == null || Roles.getInstances("puppetmaster").size() > 1 )
			throw new JaJcLogicalConfigException("Need at least one puppetmaster" + configFileName);
				
		
		
		
		Provider provider = Provider.createProvider(config);
		Iterable<NodeMetadata> nodes = provider.getNodes();
		context = provider.getContext();
		
		//System.out.println(CDH3ConfigurationBuilder.createConfiguration(nodes, config));
		
		NodeMetadata pm = Iterables.filter(nodes,new Predicate<NodeMetadata>() {
			@Override public boolean apply(NodeMetadata nd) {
				return nd.getTags().contains("puppetmaster");
			} } ).iterator().next();
		
//		NodeMetadata debug = nodes.iterator().next();
//		System.out.println("Username : " + debug.getCredentials().getUser());
//		System.out.println("Password : " + debug.getCredentials().getPassword());
//		System.out.println("Pkey     : " + debug.getCredentials().getPrivateKey());
		
		//install puppet and puppet.conf on all the nodes
		Collection<String> lines = new ArrayList<String>();
		lines.add("[master]");
		lines.add("certname=" + pm.getHostname());
		lines.add("[main]");
		lines.add("logdir = /var/lib/puppet/log");
		lines.add("vardir = /var/lib/puppet");
		lines.add("factpath = $vardir/lib/facter");
		lines.add("ssldir = /var/lib/puppet/ssl");
		lines.add("rundir = /var/run/puppet");
		lines.add("server = " + pm.getHostname());
		
		
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
		
		
		ScriptBuilder master = new ScriptBuilder();
		master.addStatement(Statements.exec("mkdir -p /etc/puppet/manifests"));
		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/manifests/site.pp", ManifestBuilder.createPuppetManifests(nodes)));
		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/autosign.conf", Arrays.asList("*") ));
		context.getComputeService().runScriptOnNode( pm.getId() , master);
					
		SshClient ssh = context.utils().sshForNode().apply(NodeMetadataBuilder.fromNodeMetadata(pm).build());
		
		try {
			ssh.connect();
			ssh.put("/tmp/modules.tgz", Payloads.newFilePayload(new File("/home/jacke/Documents/eindwerk/Masterproef/jajc/modules.tgz")));
			System.out.println(ssh.exec("sudo tar xzf /tmp/modules.tgz -C /etc/puppet/"));
			ssh.put("/tmp/environment.pp",Payloads.newStringPayload(CDH3ConfigurationBuilder.createConfiguration(nodes, config)));
			ssh.exec("sudo cp /tmp/environment.pp /etc/puppet/modules/cdh3/manifests/");
			System.out.println(ssh.exec("sudo /opt/ruby/bin/puppet master"));
			
			//System.out.println(ssh.exec("echo 'certname = '" + pm.getHostname() + "| sudo tee -a /etc/puppet/puppet.conf")`); //must be in the main section of the puppet master only 
		}
		finally {
			if (ssh != null)
				ssh.disconnect();
		}
		
		System.out.println("Starting puppet agents");
		Map<? extends NodeMetadata, ExecResponse> resp3 = context.getComputeService()
				.runScriptOnNodesMatching(Predicates.<NodeMetadata> alwaysTrue()
							, Statements.exec("/opt/ruby/bin/puppet agent"));
		for ( NodeMetadata entry : resp3.keySet()) {
			System.out.println(entry.getHostname() + " ---> " + resp3.get(entry));
			
		}
		
		context.close();
			
	}
	
	
	public static void main( String[] args ) throws Exception {
		new Main("setup.yml");
		
    }
    
    
}