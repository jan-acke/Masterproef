package com.ngdata;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import com.ngdata.bo.Instance;


// TODO validate config file
@SuppressWarnings("unchecked")
public class YamlConfigReader implements IConfigReader {

	private Map<String,Object> config;
	
	public YamlConfigReader(String filename) {
		try {
			Yaml yaml = new Yaml();
			config = (Map<String, Object>) yaml.load(new FileInputStream(new File(filename)));
		}
		catch (FileNotFoundException ex) {
			// TODO logger
		}
		
	}
	
	@Override
	public Set<Instance> getInstances() {
		
		Set<Instance> instances = new HashSet<Instance>();
		for ( Map<String,String> configInstance : (List<Map<String,String>>) config.get("instances") ) {
			Instance i = new Instance(configInstance.get("hostname"));
			String [] rolenames = configInstance.get("roles").split(" ");
			for ( String rolename : rolenames)
				i.addRole(rolename);
			instances.add(i);
		}
		return instances;
	}

	@Override
	public Map<String, String> getGeneral() {
		
		return (Map<String, String>) config.get("general");
	}


}
