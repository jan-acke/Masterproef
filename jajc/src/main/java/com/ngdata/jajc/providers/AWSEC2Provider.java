package com.ngdata.jajc.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.ngdata.jajc.bo.Instance;
import com.ngdata.jajc.exception.JajcException;

public class AWSEC2Provider extends AbstractProvider {

	private ComputeServiceContext context;
	private LoginCredentials credentials;
	private Set<NodeMetadata> nodes;
	
	public AWSEC2Provider() {}
	
	public void build() throws JajcException {
		Map<String,String> awsec2config = getConfiguration().getProviderSpecificInfo("awsec2");
    	context =  new ComputeServiceContextFactory().
            	createContext("aws-ec2",awsec2config.get("accesskeyid"), awsec2config.get("secretkey"),
            			ImmutableSet.<Module> of(new SshjSshClientModule()));
    	TemplateBuilder tb = context.getComputeService().templateBuilder();
    	Map<Template,Integer> templates = new HashMap<Template,Integer>();
    	for (Instance i : getConfiguration().getInstances()){
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
    		t.getOptions().overrideLoginPrivateKey(awsec2config.get("private_key"))
    				.tags(i.getRoles())
    				.as(AWSEC2TemplateOptions.class).securityGroups(awsec2config.get("security_group"))
    				.as(AWSEC2TemplateOptions.class).keyPair(awsec2config.get("keypair_name"));
    		templates.put(t,number);
    		credentials = new LoginCredentials(awsec2config.get("username"),null,awsec2config.get("private_key"),false);
    		
    	}
    	
    	nodes = new HashSet<NodeMetadata>();
    	for (Template template : templates.keySet()){
    		 Set<? extends NodeMetadata> tmp;
			try {
				tmp = context.getComputeService().createNodesInGroup(getConfiguration().getGeneral().getClustername(), templates.get(template), template);
			} catch (RunNodesException e) {
				e.printStackTrace();
				throw new JajcException("Provider error: Failed to create nodes");
				
			}
    		 nodes.addAll(tmp);
    	}

	}
		
	@Override
	public Iterable<NodeMetadata> getNodes() {
		return nodes;
	}


	@Override
	public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(
			Predicate<NodeMetadata> filter, Statement runScript) throws RunScriptOnNodesException {
		return context.getComputeService().runScriptOnNodesMatching(filter, runScript, new RunScriptOptions().overrideLoginCredentials(credentials));
		
	}

	@Override
	public ExecResponse runScriptOnNode(String id, Statement runScript) {
		return context.getComputeService().runScriptOnNode(id, runScript, new RunScriptOptions().overrideLoginCredentials(credentials));
	}

	@Override
	public void closeContext() {
		context.close();
		
	}

	@Override
	public SshClient getSshClient(NodeMetadata node) {
		return context.utils().sshForNode().apply(NodeMetadataBuilder.fromNodeMetadata(node).credentials(credentials).build());
	}
	
	

}
