package com.ngdata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jclouds.aws.ec2.compute.AWSEC2TemplateOptions;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.domain.TemplateBuilder;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.ngdata.bo.Instance;
import com.ngdata.exception.JaJcLogicalConfigException;

public class AWSEC2Provider extends Provider {

	private ComputeServiceContext context;
	
	private Set<NodeMetadata> nodes;
	
	public AWSEC2Provider() throws JaJcLogicalConfigException, RunNodesException {
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
    		t.getOptions().overrideLoginPrivateKey(awsec2config.get("private_key"))
    				.tags(i.getRoles())
    				.as(AWSEC2TemplateOptions.class).securityGroups("evert-upgrade-3")
    				.as(AWSEC2TemplateOptions.class).keyPair("jacke");
    		templates.put(t,number);
    		
    	}
    	
    	nodes = new HashSet<NodeMetadata>();
    	for (Template template : templates.keySet()){
    		 Set<? extends NodeMetadata> tmp = context.getComputeService().createNodesInGroup("jan-jclouds", templates.get(template), template);
    		 nodes.addAll(tmp);
    	}
    	
	}
	
	
	
	@Override
	public Iterable<NodeMetadata> getNodes() {
		return nodes;
	}

	@Override
	public ComputeServiceContext getContext() {
		return context;
	}

}
