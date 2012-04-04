package com.ngdata;

import java.io.File;

// TODO validate config file

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

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
	public List<Map<String, String>> getInstances() {
		List<Map<String,String>> instances = (List<Map<String, String>>) config.get("instances");
		return instances;
	}

	@Override
	public Map<String, String> getGeneral() {
		
		return (Map<String, String>) config.get("general");
	}

}
