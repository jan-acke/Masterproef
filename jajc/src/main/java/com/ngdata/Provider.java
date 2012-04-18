package com.ngdata;

import java.util.Map;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;

import com.ngdata.exception.JaJcLogicalConfigException;

public abstract class Provider {
	
	protected static IConfig config;
	
	public static Provider createProvider(IConfig configuration) throws JaJcLogicalConfigException, RunNodesException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		config = configuration;
		String providerType = config.getGeneral().getProvider();
		if (providerType.indexOf("Provider") == -1)
			return (Provider) Class.forName( "com.ngdata." + providerType.toUpperCase() + "Provider").newInstance();
		else
			return (Provider) Class.forName(providerType).newInstance();
		
	}
	
	public abstract Iterable<NodeMetadata> getNodes();
	
	public abstract ComputeServiceContext getContext();
	
	
	
	protected String getValue(Map<String,String> lowerPriority , Map<String,String> higherPriority, String key) throws JaJcLogicalConfigException {
    	return getValue(lowerPriority, higherPriority, key,false);
    }
    
    protected String getValue (Map<String,String> lowerPriority , Map<String,String> higherPriority, String key, boolean required ) throws JaJcLogicalConfigException {
    	String value;
    	try {
    		value = higherPriority.get(key) == null ? lowerPriority.get(key) : higherPriority.get(key);
    		//ugly hack because we only want to deal with strings TODO reevaluate
    	} catch (ClassCastException ex){
    		value = "" + higherPriority.get(key) == null ? lowerPriority.get(key) : higherPriority.get(key);
    	}
    	if (value == null && required)
    		throw new JaJcLogicalConfigException("Missing " + key + " option");
    	else if (value == null)
    		value = "";
    	return value;
    }
}
