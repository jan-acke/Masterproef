package com.ngdata.jajc.puppetconfiguration;

import java.util.Map;
import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.base.Function;
import com.ngdata.jajc.exception.JajcException;

public class LilyPuppetConfiguration extends AbstractPuppetConfiguration {

	private String content;
	private Cdh3PuppetConfiguration cdh3;
	
	
	public static final String DEFAULT_LILY_PORT = "12020";
	
	public void build() throws JajcException {
		cdh3 = (Cdh3PuppetConfiguration) ConfigurationManager.getConfigurationManager().getConfiguration("cdh3");
		
		if (cdh3 == null)
			throw new JajcException("Cdh3PuppetConfiguration was not initialized");
		PuppetFile pf = new PuppetFile(getModuleName(),"environment");
		Map<String,String> lilyCredentials = getUserConfiguration("lily");
		String u = lilyCredentials.get("username");
		String p = lilyCredentials.get("password");
		
		if ( u == null || p ==null)
			throw new JajcException("'username' and 'password' for the lily repository are " +
					"required in the lily section of the userconfiguration"); 
		
		
		pf.addProperty("username", u);
		pf.addProperty("password", p);
		pf.addHash("lilyMapReduce", createLilyMapReduceProperties());
		pf.addHash("lilyHbase", createLilyHbaseProperties());
		pf.addHash("lilyZooKeeper", createLilyZooKeeperProperties());
		pf.addHash("lilyRepository", createLilyRepositoryProperties());
		content = pf.toString();
	}
	
	@Override
	public String getModuleName() {
		return "lily";
	}

	@Override
	public String getEnvironmentContent() {
		return content;
	}

	private Map<String, String> createLilyMapReduceProperties() {
		Map<String,String> properties = getUserConfiguration("lilyMapReduce");
		createOrOverwriteProperty(properties, "mapred.job.tracker", cdh3.getJobtracker());
		createOrOverwriteProperty(properties, "fs.default.name", cdh3.getNamenode());
		return properties;
	}

	private Map<String, String> createLilyZooKeeperProperties() {
		Function<NodeMetadata,String> f = new Function<NodeMetadata,String>() {
			@Override
			public String apply(NodeMetadata nm) {
				return nm.getHostname() + ":" + Cdh3PuppetConfiguration.DEFAULT_ZK_PORT;
			}
		};
		Map<String,String> properties = getUserConfiguration("lilyZooKeeper");
		createOrOverwriteProperty(properties, "connectString", 
				PuppetFile.iterableToString(cdh3.getZookeeperQuorum(), f));
		createNoOverwriteProperty(properties, "sessionTimeout", "20000");
		createNoOverwriteProperty(properties, "startUpTimeout", "600000");
		return properties;
	}
	
	private Map<String,String> createLilyRepositoryProperties() {
		Map<String,String> properties = getUserConfiguration("lilyRepository");
		createOrOverwriteProperty(properties, "blobFileSystem", cdh3.getNamenode() + "/lily/blobs");
		createNoOverwriteProperty(properties, "port", DEFAULT_LILY_PORT);
		createNoOverwriteProperty(properties, "maxServerThreads", "35");
		return properties;
		
	}

	private Map<String, String> createLilyHbaseProperties() {
		
		Function<NodeMetadata,String> f = new Function<NodeMetadata,String>() {
			@Override
			public String apply(NodeMetadata nm) {
				return nm.getHostname();
			}
		};
		Map<String,String> properties = getUserConfiguration("lilyHbase");
		createOrOverwriteProperty(properties, "hbase.zookeeper.quorum", PuppetFile.
				iterableToString(cdh3.getZookeeperQuorum(), f));
		createOrOverwriteProperty(properties, "hbase.zookeeper.property.clientPort", 
				Cdh3PuppetConfiguration.DEFAULT_ZK_PORT);
		return properties;
		
	}

	@Override
	public int getPriority() {
		return 50;
	}
}
