package com.ngdata.jajc.puppetconfiguration;

import java.util.Map;

import org.apache.log4j.Logger;

import com.ngdata.jajc.exception.JajcException;

public class JavaPuppetConfiguration extends AbstractPuppetConfiguration {

	
	private PuppetFile content;
	
	private String filename;
	private String path;
	private String installOpenjdk;
	
	private Logger log = Logger.getLogger(JavaPuppetConfiguration.class.getName());
	
	@Override
	public String getModuleName() {
		return "java";
	}

	@Override
	public String getEnvironmentContent() {
		return content.toString();
	}

	@Override
	public int getPriority() {
		return 5;
	}

	@Override
	public void build() throws JajcException {
		content = new PuppetFile(getModuleName(),"environment");
		Map<String,String> properties = getUserConfiguration("java");
		
		path = properties.get("install_path");
		filename = properties.get("bin_filename");
		installOpenjdk = properties.get("installopenjdk");
		
		if (installOpenjdk == null){
			installOpenjdk = "false";
			log.warn("installOpenjdk in java section of setup.yml is missing, assuming false");
		}
		
		if (path == null) {
			if (installOpenjdk.equals("true")) {
				path = "/usr/lib/jvm/java-6-openjdk";
				log.warn("Openjdk will be installed but the install_path (JAVA_HOME location) is missing, assuming Ubuntu lucid's: " + path);
			}
			else {
				path = "/opt/jdk6";
				log.warn("No install_path property found in the java section of setup.yml, assuming " + path);
			}
		}
		
		if (filename == null && installOpenjdk.equals("false"))
			throw new JajcException("The value of bin_filename must be set when not installing openjdk in the java part of the user configuration, it corresponds to the file in puppet/modules/java/files");
		
		else  
			content.addProperty("bin_filename", filename);
		
		
		content.addProperty("install_path", path);
		content.addProperty("installopenjdk", installOpenjdk);
	}
	
	public String getPathName() {
		return path;
	}

	public String getBinFilename() {
		return filename;
	}


}
