package com.ngdata.jajc.puppetconfiguration;

import java.util.Map;

import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.ngdata.jajc.exception.JaJcLogicalConfigException;
import com.ngdata.jajc.exception.JajcException;

/*
 * This will create the content of a puppet file with variables based on the cluster- and user configuration for 
 * the cdh3 services
 */
public class Cdh3PuppetConfiguration extends AbstractPuppetConfiguration {
	
	//used as read-only properties since other PuppetConfigurations might need these
	private Iterable<NodeMetadata> zookeeper;
	private String jobtracker;
	private String namenode;
	
	private String content;
	public static final String DEFAULT_NN_PORT = "8020";
	public static final String DEFAULT_ZK_PORT = "2181";
	public static final String DEFAULT_JT_PORT = "9001";
	
	public void build() throws JajcException {
		
		zookeeper = getNodesByTag("zookeeper");
		if ( Iterables.size(zookeeper) < 2 )
			throw new JaJcLogicalConfigException("At least two zookeeper servers are required");
		
		if ( Iterables.size(getNodesByTag("jobtracker")) != 1 )
			throw new JaJcLogicalConfigException("One jobtracker server is required");
		jobtracker = getNodesByTag("jobtracker").iterator().next().getHostname() + ":" + DEFAULT_JT_PORT;
					
		if ( Iterables.size(getNodesByTag("namenode")) != 1 )
			throw new JaJcLogicalConfigException("One namenode server is required");
		namenode = "hdfs://" + getNodesByTag("namenode").iterator().next().getHostname() + ":" + DEFAULT_NN_PORT;
		
		Function<NodeMetadata,String> f = new Function<NodeMetadata,String>() {
			@Override
			public String apply(NodeMetadata nm) {
				return nm.getHostname();
			}
		};
				
		PuppetFile pf = new PuppetFile(getModuleName(), "environment");
		
		pf.addProperty("namenode", namenode);
		pf.addProperty("zk_quorum", PuppetFile.iterableToString(zookeeper,f));
		pf.addHash("hdfs", createHdfsProperties());
		pf.addHash("core", createCoreProperties());
		pf.addHash("mapred", createMapredProperties());
		pf.addHash("zookeeper", createZookeeperProperties());
		pf.addHash("hbase",createHbaseProperties());
		
		content = pf.toString();
	}
	
	@Override
	public String getModuleName() {
		return "cdh3";
	}

	@Override
	public String getEnvironmentContent() {
		return content;
	}

	String getJobtracker() {
		return jobtracker;
	}

	String getNamenode() {
		return namenode;
	}
	
	Iterable<NodeMetadata> getZookeeperQuorum() {
		return zookeeper;
	}
	
	private Map<String, String> createZookeeperProperties() {
		Map<String,String> properties = getUserConfiguration("zookeeper");
		createNoOverwriteProperty(properties, "tickTime", "2000");
		createNoOverwriteProperty(properties, "dataDir", "/var/zookeeper");
		createNoOverwriteProperty(properties, "clientPort", DEFAULT_ZK_PORT);
		createNoOverwriteProperty(properties, "initLimit", "5");
		createNoOverwriteProperty(properties, "syncLimit", "2");
		createOrOverwriteProperty(properties, "servers", "$zk_quorum"); //refers to the existing zk_quorum variable
		return properties;
	}

	private Map<String, String> createHbaseProperties() {
		Map<String,String> properties = getUserConfiguration("hbase");
		createOrOverwriteProperty(properties, "hbase.cluster.distributed", "true");
		createOrOverwriteProperty(properties, "hbase.rootdir", "${namenode}/hbase");
		createOrOverwriteProperty(properties, "hbase.zookeeper.quorum", "$zk_quorum");
		return properties;
	}

	private Map<String, String> createMapredProperties() {
		Map<String,String> properties = getUserConfiguration("mapred");
		createOrOverwriteProperty(properties, "mapred.job.tracker", jobtracker);
		createNoOverwriteProperty(properties, "mapred.local.dir", "/data/mapred/local");
		createNoOverwriteProperty(properties, "mapred.system.dir", "/mapred/system");
		return properties;
	}

	private Map<String, String> createCoreProperties() {
		Map<String,String> properties = getUserConfiguration("core");
		createOrOverwriteProperty(properties, "fs.default.name", "$namenode" );
		return properties;
	}

	private Map<String, String> createHdfsProperties() {
		Map<String,String> properties = getUserConfiguration("hdfs");
		createNoOverwriteProperty(properties, "dfs.name.dir", "/data/nn");
		createNoOverwriteProperty(properties, "dfs.data.dir", "/data/dn");
		return properties;
	}

	@Override
	public int getPriority() {
		return 20;
	}

	




}
