package com.ngdata.jajc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.io.Payloads;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.ssh.SshClient;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.ngdata.jajc.bo.Roles;
import com.ngdata.jajc.exception.JajcException;
import com.ngdata.jajc.providers.Provider;
import com.ngdata.jajc.providers.AbstractProvider;
import com.ngdata.jajc.puppetconfiguration.Cdh3PuppetConfiguration;
import com.ngdata.jajc.puppetconfiguration.ConfigurationManager;
import com.ngdata.jajc.puppetconfiguration.JavaPuppetConfiguration;
import com.ngdata.jajc.puppetconfiguration.LilyPuppetConfiguration;
import com.ngdata.jajc.puppetconfiguration.MCollectiveConfiguration;
import com.ngdata.jajc.puppetconfiguration.PuppetConfiguration;

public class Main 
{	
	
	private Iterable<NodeMetadata> nodes;
	private NodeMetadata pm;
	private Provider provider;
	private String java_home;
	
	
	public Main(String configFileName) throws Exception {
		
		IConfig configuration = YamlConfigReader.createYAMLConfig(configFileName);
		
		if (configuration.getInstances().size() == 0)
			throw new com.ngdata.jajc.exception.JaJcLogicalConfigException("No instances defined in " + configFileName);
		
		if (Roles.getInstances("puppetmaster") == null || Roles.getInstances("puppetmaster").size() > 1 )
			throw new com.ngdata.jajc.exception.JaJcLogicalConfigException("Need at least one puppetmaster" + configFileName);
		
		provider = AbstractProvider.createProvider(configuration);
		nodes = provider.getNodes();
		
		pm = Iterables.filter(nodes,new Predicate<NodeMetadata>() {
			@Override public boolean apply(NodeMetadata nd) {
				return AbstractProvider.extractRolesFromNode(nd).contains("puppetmaster");
			} } ).iterator().next();
		
		
		System.out.println("Installing necessary components for puppet");
		initializeAllNodes();
		System.out.println("Configuring puppet master");
		initializePuppetMaster();
		System.out.println("Starting puppet agents");
		startPuppetAgents();
		
		provider.closeContext();
			
	}
	
	private void startPuppetAgents() throws RunScriptOnNodesException, JajcException {
		AbstractProvider.getInstance().runScriptOnNodesMatching(Predicates.<NodeMetadata>alwaysTrue(), 
				Statements.exec("export JAVA_HOME=" + java_home + " && /var/lib/gems/1.8/bin/puppet agent"));
		
	}

	private void initializeAllNodes() throws RunScriptOnNodesException {
		
		//Create puppet.conf
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
		lines.add("pluginsync = true");
		
		//Script to run
		ScriptBuilder sb = new ScriptBuilder();
		sb.addStatement(Statements.exec("apt-get install -y rubygems"));
		sb.addStatement(Statements.exec("apt-get install -y tar"));
		sb.addStatement(Statements.exec("mkdir -p /opt/gems"));
		sb.addStatement(Statements.exec("/usr/bin/gem install puppet --no-ri --no-rdoc")); // --install-dir /opt/gems"));
		sb.addStatement(Statements.exec("groupadd puppet"));
		sb.addStatement(Statements.exec("useradd -g puppet -G admin puppet"));
		sb.addStatement(Statements.exec("mkdir -p /etc/puppet"));
		//sb.addStatement(Statements.exec("echo \"export PATH=$PATH:/opt/gems/bin\" | tee -a /etc/environment")); //puppet in PATH
		//sb.addStatement(Statements.exec("echo \"export JAVA_HOME=" + java_home + " | tee -a /etc/environment"));
		sb.addStatement(Statements.createOrOverwriteFile("/etc/puppet/puppet.conf", lines));
		sb.addStatement(Statements.exec("apt-get install -y libopenssl-ruby"));
		
		
		//TODO log output
		provider.runScriptOnNodesMatching(Predicates.<NodeMetadata>alwaysTrue(), sb);
		
	}
	
	private void initializePuppetMaster() throws JajcException {
		ScriptBuilder master = new ScriptBuilder();
		master.addStatement(Statements.exec("mkdir -p /etc/puppet/manifests"));
		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/manifests/site.pp", ManifestBuilder.createPuppetManifest(nodes)));
		master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/autosign.conf", Arrays.asList("*") ));
		provider.runScriptOnNode(pm.getId(), master);
					
		
		ConfigurationManager cm = ConfigurationManager.getConfigurationManager();
		//TODO centralize module name !!
		cm.add("mcollective", new MCollectiveConfiguration());
		cm.add("java", new JavaPuppetConfiguration());
		cm.add("cdh3", new Cdh3PuppetConfiguration());
		cm.add("lily", new LilyPuppetConfiguration());
		cm.build();
		Map<String,PuppetConfiguration> pcs = cm.getConfigurations();
		java_home = ((JavaPuppetConfiguration) cm.getConfigurations().get("java")).getPathName();
		
		
		SshClient ssh = AbstractProvider.getInstance().getSshClient(pm);
		try {
			ssh.connect();
			ssh.put("/tmp/modules.tgz", Payloads.newFilePayload(new File("/home/jacke/Documents/eindwerk/Masterproef/jajc/modules.tgz")));
			System.out.println(ssh.exec("sudo tar xzf /tmp/modules.tgz -C /etc/puppet/"));
			for ( String module : pcs.keySet() ) {
				ssh.put("/tmp/environment.pp",Payloads.newStringPayload( pcs.get(module).getEnvironmentContent()));
				ssh.exec("sudo cp /tmp/environment.pp /etc/puppet/modules/" + module +"/manifests/");
			}
			System.out.println(ssh.exec("sudo /var/lib/gems/1.8/bin/puppet master"));
			
		}
		finally {
			if (ssh != null)
				ssh.disconnect();
		}
	}

	public static void main( String[] args ) throws Exception {
		new Main("setup.yml");
		
    }
    
    
}