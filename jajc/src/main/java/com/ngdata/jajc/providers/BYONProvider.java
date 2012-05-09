package com.ngdata.jajc.providers;

import java.util.Map;
import java.util.Properties;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.ngdata.jajc.bo.Instance;
import com.ngdata.jajc.exception.JaJcLogicalConfigException;

public class BYONProvider extends AbstractProvider {
	
	private ComputeServiceContext context;

	public BYONProvider() {
	}
	
	public void build() throws JaJcLogicalConfigException {
		Properties byonProps = new Properties();
    	Map<String,String> byonConfig = getConfiguration().getProviderSpecificInfo("byon");
    	
    	StringBuilder yaml = new StringBuilder(); 
    	yaml.append("nodes:");
    	for ( Instance i : getConfiguration().getInstances()) {
    		Map<String,String> instanceConfig = i.getOptions();
    		String os_arch = getValue(byonConfig, instanceConfig, "os_arch");
    		String os_family = getValue(byonConfig, instanceConfig, "os_family");
    		String os_description = getValue(byonConfig, instanceConfig, "os_description");
    		String os_version = getValue(byonConfig,instanceConfig,"os_version");
    		String username = getValue(byonConfig,instanceConfig,"username",true);
    		String sudo_password = getValue(byonConfig,instanceConfig,"sudo_password");
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
    			yaml.append("\n      group: ").append(getConfiguration().getGeneral().getClustername());
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
    	
	}


	@SuppressWarnings("unchecked")
	@Override
	public Iterable<NodeMetadata> getNodes() {
		return (Iterable<NodeMetadata>) context.getComputeService().listNodes();
	}

	@Override
	public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(
			Predicate<NodeMetadata> filter, Statement runScript) throws RunScriptOnNodesException {
		return context.getComputeService().runScriptOnNodesMatching(filter, runScript);
	}

	@Override
	public ExecResponse runScriptOnNode(String id, Statement runScript) {
		return context.getComputeService().runScriptOnNode(id, runScript);
	}

	@Override
	public void closeContext() {
		context.close();
		
	}

	@Override
	public SshClient getSshClient(NodeMetadata node) {
		return context.utils().sshForNode().apply(NodeMetadataBuilder.fromNodeMetadata(node).build());
		
	}



}
