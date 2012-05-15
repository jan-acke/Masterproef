package com.ngdata.jajc.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jclouds.compute.options.TemplateOptions.Builder.*; 
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

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import com.ngdata.jajc.bo.Instance;
import com.ngdata.jajc.exception.JajcException;

public class AWSEC2Provider extends AbstractProvider {

	private ComputeServiceContext context;
	private LoginCredentials credentials;
	private Set<NodeMetadata> nodes;
	
	//make sure we don't try to access other nodes not in the cluster we try to create
	private Predicate<NodeMetadata> filter;
	
	
	public AWSEC2Provider() {
		filter = new Predicate<NodeMetadata>() {
			@Override
			public boolean apply(NodeMetadata arg0) {
				return nodes.contains(arg0);
			}};
	}
	
	public void build() throws JajcException {
		Map<String,String> awsec2config = getConfiguration().getProviderSpecificInfo("awsec2");
    	context =  new ComputeServiceContextFactory().
            	createContext("aws-ec2",awsec2config.get("accesskeyid"), awsec2config.get("secretkey"),
            			ImmutableSet.<Module> of(new SshjSshClientModule()));
    	TemplateBuilder tb = context.getComputeService().templateBuilder();
    	Map<Template,Integer> templates = new HashMap<Template,Integer>();
    	credentials = new LoginCredentials(awsec2config.get("username"),null,awsec2config.get("private_key"),false);
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
    		
    		Map<String,String> mp = new HashMap<String,String>();
    		mp.put("roles", Joiner.on(",").join(i.getRoles()));
    		
    		Template t = tb.hardwareId(hardware)
    					   .imageId(location + "/" + image)
    					   .options(userMetadata(mp)
    							    //.as(AWSEC2TemplateOptions.class).securityGroups(awsec2config.get("security_group"))
    							    //.as(AWSEC2TemplateOptions.class).keyPair(awsec2config.get("keypair_name"))
    							   )
    						.build();		
    		t.getOptions()
    				.as(AWSEC2TemplateOptions.class).securityGroups(awsec2config.get("security_group"))
    				.as(AWSEC2TemplateOptions.class).keyPair(awsec2config.get("keypair_name"));
    				
    		
    		templates.put(t,number);
    		
    		
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
			Predicate<NodeMetadata> filter2, Statement runScript) throws RunScriptOnNodesException {
		return context.getComputeService().runScriptOnNodesMatching(addLocalFilter(filter2), runScript, new RunScriptOptions().overrideLoginCredentials(credentials));
		
	}

	private Predicate<NodeMetadata> addLocalFilter(Predicate<NodeMetadata> filter2) {
		return Predicates.and(filter, filter2);
	}

	@Override
	public ExecResponse runScriptOnNode(String id, Statement runScript) {
		return context.getComputeService().runScriptOnNode(id, runScript, overrideLoginCredentials(credentials));
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
