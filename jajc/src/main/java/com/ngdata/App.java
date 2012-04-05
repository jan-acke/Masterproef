package com.ngdata;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.domain.NodeMetadata;
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
import com.ngdata.bo.Roles;
import com.ngdata.exception.JaJcLogicalConfigException;

/**
 * Hello world!
 *
 */

public class App 
{	
	
	public App(String configFileName) throws Exception {
		IConfigReader config = new YamlConfigReader(configFileName);
		
		if (config.getInstances().size() == 0)
			throw new JaJcLogicalConfigException("no instances defined in " + configFileName);
		
		if (Roles.getInstances("puppetmaster") == null || Roles.getInstances("puppetmaster").size() != 1 )
			throw new JaJcLogicalConfigException("Need exactly one puppet master in " + configFileName);
				
		if ( "byon".equals(config.getGeneral().get("provider"))) {
			Properties byonProps = new Properties();
			byonProps.setProperty("byon.endpoint", "file:///home/jacke/Documents/eindwerk/Masterproef/jajc/nodes-byon.yml");
			ComputeServiceContext context = new ComputeServiceContextFactory().createContext("byon","foo","bar",
					ImmutableSet.<Module> of (new SshjSshClientModule() ) , byonProps);
			
			
			//install puppet and puppet.conf on all the nodes
			Collection<String> lines = new ArrayList<String>();
			lines.add("[master]");
			lines.add("modulepath = /vagrant/mytests/Masterproef/puppet/modules");
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
			sb.addStatement(Statements.createOrOverwriteFile("/etc/puppet.conf", lines));
			
			context.getComputeService().runScriptOnNodesMatching(Predicates.<NodeMetadata> alwaysTrue(), sb);
			
			//Placing puppet modules in puppetmaster and generate manifest
			@SuppressWarnings("unchecked")
			Iterable<NodeMetadata> nodes = (Iterable<NodeMetadata>) context.getComputeService().listNodes();
			NodeMetadata pm = Iterables.filter(nodes,new Predicate<NodeMetadata>() {
				@Override public boolean apply(NodeMetadata nd) {
					return nd.getTags().contains("puppetmaster");
				} } ).iterator().next();
			
			ScriptBuilder master = new ScriptBuilder();
			master.addStatement(Statements.exec("mkdir -p /etc/puppet/manifests"));
			master.addStatement(Statements.exec("tar xzf /tmp/modules.tgz -C /etc/puppet"));
			master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/manifests/site.pp", ManifestBuilder.createPuppetManifests(nodes)));
			master.addStatement(Statements.createOrOverwriteFile("/etc/puppet/autosign.conf", Arrays.asList("*") ));
			context.getComputeService().runScriptOnNode( pm.getId() , master);
			
			SshClient ssh = context.utils().sshForNode().apply(pm);
			try {
				ssh.connect();
				ssh.put("/tmp/modules.tgz", Payloads.newFilePayload(new File("/home/jacke/Documents/eindwerk/Masterproef/jajc/modules.tgz")));
				ssh.exec("puppet master");
				ssh.exec("echo 'certname = '" + pm.getHostname() + "| tee -a /etc/puppet/puppet.conf"); //must be in the main section of the puppet master only 
			}
			finally {
				if (ssh != null)
					ssh.disconnect();
			}
			
			System.out.println("Starting puppet agents");
			context.getComputeService().runScriptOnNodesMatching(Predicates.<NodeMetadata> alwaysTrue(), Statements.exec("puppet agent --server" + pm.getHostname()));
						
			
			context.close();
			
			
		}
			
	}
    public static void main( String[] args ) throws Exception {
    	new App(args[0]);
    }
    
    
}