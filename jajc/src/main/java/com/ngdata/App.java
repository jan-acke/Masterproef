package com.ngdata;

import static org.jclouds.compute.options.RunScriptOptions.Builder.wrapInInitScript;
import static org.jclouds.scriptbuilder.domain.Statements.exec;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.domain.Credentials;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.scriptbuilder.InitScript;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.Statements;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;


import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Hello world!
 *
 */

public class App 
{	
	public App(String configFileName) throws RunScriptOnNodesException, IOException {
		IConfigReader config = new YamlConfigReader(configFileName);
		
		if ( "byon".equals(config.getGeneral().get("provider"))) {
			Properties byonProps = new Properties();
			byonProps.setProperty("byon.endpoint", "file:///home/jacke/Documents/eindwerk/Masterproef/jajc/nodes-byon.yml");
			ComputeServiceContext context = new ComputeServiceContextFactory().createContext("byon","bla","bla",
					ImmutableSet.<Module> of (new SshjSshClientModule() ) , byonProps);
			
			//run script on all nodes
			ScriptBuilder sb = new ScriptBuilder();
			sb.addStatement(Statements.exec("apt-get install -y rubygems"));
			sb.addStatement(Statements.exec("gem install puppet facter --no-ri --no-rdoc"));
			sb.addStatement(Statements.exec("groupadd puppet"));
			sb.addStatement(Statements.exec("useradd -g puppet -G admin puppet"));
			//sb.addStatement(Statements.exec("export PATH=$PATH:/opt/ruby/bin")); //puppet in PATH
			
			
			context.getComputeService().runScriptOnNodesMatching(Predicates.<NodeMetadata> alwaysTrue(), sb);
			
			context.close();
		}
			
	}
    public static void main( String[] args ) throws Exception {
    	new App(args[0]);
    }
    
}
