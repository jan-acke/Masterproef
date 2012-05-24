package com.ngdata.jajc.puppetconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ngdata.jajc.exception.JajcException;


public class ConfigurationManager {
	
	
	private static ConfigurationManager manager;
	
	public static ConfigurationManager getConfigurationManager() {
		if (manager == null)
			manager =  new ConfigurationManager();
		return manager;
		
	}
	
	private Map<String,PuppetConfiguration> configs;
	
	private ConfigurationManager(){
		configs = new HashMap<String,PuppetConfiguration>();
	}
	
	public ConfigurationManager add(PuppetConfiguration pc){
		configs.put(pc.getModuleName(),pc);
		return this;
	}
	
	public PuppetConfiguration getConfiguration(String name) {
		return configs.get(name);
		
	}
	
	public void build() throws JajcException {
		List<PuppetConfiguration> tmp = new ArrayList<PuppetConfiguration>(configs.values());
		Collections.sort(tmp);
		for (PuppetConfiguration pc : tmp){
			pc.build();
		}
	}

	public Map<String,PuppetConfiguration> getConfigurations() {
		return configs;
	}
	


}
