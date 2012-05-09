package com.ngdata.jajc;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.yaml.snakeyaml.Yaml;


// TODO validate config file
public class YamlConfigReader {

	private static YamlConfig config;
	
	private YamlConfigReader() {
	}
	
	public static IConfig createYAMLConfig(String filename) {
		try {
			Yaml yaml = new Yaml();
			config = yaml.loadAs(new FileInputStream(new File(filename)), YamlConfig.class);
		}
		catch (FileNotFoundException ex) {
			// TODO logger
			config = null;
		}
		return config;
		
	}

}
