package com.ngdata.jajc.puppetconfiguration;

import java.util.HashMap;
import java.util.Map;


import org.apache.log4j.Logger;
import org.jclouds.compute.domain.NodeMetadata;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.ngdata.jajc.exception.JajcException;
import com.ngdata.jajc.providers.AbstractProvider;


public abstract class AbstractPuppetConfiguration implements PuppetConfiguration {
	
	private static Logger log = Logger.getLogger(AbstractPuppetConfiguration.class.getName());	
	
	public AbstractPuppetConfiguration()  {
	
	}
	/*
	 * Use this to override user-defined settings, log a warning for the user
	 */
	protected <T> void createOrOverwriteProperty(Map<String,T> properties, String name, T value) {
		if (properties.get(name) != null)
			log.warn("Current value for " + name + " property (" + properties.get(name) + ") will be overridden by " + value);
		properties.put(name, value);
	}
	
	/*
	 * Use this to set necessary defaults, it will not override user-defined settings
	 * @param Map in which to add the key and value if the key doesn't exist
	 */
	protected <T> void createNoOverwriteProperty(Map<String,T> properties, String name, T value) {
		if (properties.get(name) == null){
			properties.put(name, value);
		}
	}
	
	/*
	 * @param This method uses regular expressions to find the nodes
	 */
	protected Iterable<NodeMetadata> getNodesByRole(final String simplifiedTagname)  {
		final String regex = ".*" + simplifiedTagname + ".*";
		try {
			return Iterables.filter(AbstractProvider.getInstance().getNodes(), new Predicate<NodeMetadata>() {
				@Override
				public boolean apply(NodeMetadata nm) {
					//Naive but it's clear what we're doing :
					for ( String tag : AbstractProvider.extractRolesFromNode(nm) ){
						if (tag.matches(regex))
							return true;
					}
					return false;
				}
			});
		} catch (JajcException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * @returns The user defined configuration (a map) based on a key, if it doesn't exist this method will 
	 * return an empty hashmap
	 */
	protected Map<String,String>getUserConfiguration(String key) {
		Map<String, String> tmp = null;
		try {
			tmp = AbstractProvider.getInstance().getConfiguration().getUserConfig().get(key);
		} catch (JajcException e) {
			log.debug("No user-configuration found for " + key + ". Created an empty properties Map");
		}
		if ( tmp == null)
			return new HashMap<String,String>();
		else 
			return tmp;
	}
	
	@Override
	public int compareTo(PuppetConfiguration o) {
		if (this.getPriority() < o.getPriority())
			return -1;
		else if (this.getPriority() > o.getPriority())
			return 1;
		else
			return 0;
	}
	
	
}
