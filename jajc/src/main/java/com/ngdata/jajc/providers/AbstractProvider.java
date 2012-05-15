package com.ngdata.jajc.providers;

import java.util.Map;
import java.util.Set;

import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.collect.Sets;
import com.ngdata.jajc.IConfig;
import com.ngdata.jajc.exception.JaJcLogicalConfigException;
import com.ngdata.jajc.exception.JajcException;

public abstract class AbstractProvider implements Provider {
	
	private IConfig configuration;
	private static AbstractProvider p;
	
	public static Provider getInstance() throws JajcException {
		if (p == null)
			throw new JajcException("Provider is not initialized, use createProvider(IConfig) to create one");
		else
			return p;
	}
	
	public static Provider createProvider(IConfig configuration) throws JajcException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		String providerType = configuration.getGeneral().getProvider();
		if (providerType.indexOf("Provider") == -1)
			p = (AbstractProvider) Class.forName( "com.ngdata.jajc.providers." + providerType.toUpperCase() + "Provider").newInstance();
		else
			p = (AbstractProvider) Class.forName(providerType).newInstance();
		if (p == null)
			throw new JajcException("Could not instantiate provider : " + providerType);
		p.setConfiguration(configuration);
		p.build();
		return p;
		
	}
	
	
	public static Set<String> extractRolesFromNode(NodeMetadata nm) {
		String roles = nm.getUserMetadata().get("roles");
		return Sets.newHashSet(roles.split(","));
		
	}
		
	
	@Override
	public IConfig getConfiguration() {
		return configuration;
	}
		
	private void setConfiguration(IConfig configuration) {
		this.configuration = configuration;
	}
	
	public abstract void build() throws JajcException;
	
	protected String getValue(Map<String,String> lowerPriority , Map<String,String> higherPriority, String key) throws JaJcLogicalConfigException {
    	return getValue(lowerPriority, higherPriority, key,false);
    }
    
    protected String getValue (Map<String,String> lowerPriority , Map<String,String> higherPriority, String key, boolean required ) throws JaJcLogicalConfigException {
    	String value;
    	try {
    		value = higherPriority.get(key) == null ? lowerPriority.get(key) : higherPriority.get(key);
    		//catching errors in case integer values are used in the configuration file TODO log this
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
