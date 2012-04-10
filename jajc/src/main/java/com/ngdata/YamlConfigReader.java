package com.ngdata;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.yaml.snakeyaml.Yaml;


// TODO validate config file
public class YamlConfigReader {

	private static YAMLConfig config;
	
	private YamlConfigReader() {
	}
	
	public static IConfig createYAMLConfig(String filename) {
		try {
			Yaml yaml = new Yaml();
			config = yaml.loadAs(new FileInputStream(new File(filename)), YAMLConfig.class);
		}
		catch (FileNotFoundException ex) {
			// TODO logger
			config = null;
		}
		return config;
		
	}

}
