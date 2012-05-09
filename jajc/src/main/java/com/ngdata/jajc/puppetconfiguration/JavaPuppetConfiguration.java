package com.ngdata.jajc.puppetconfiguration;

import java.util.Map;

import com.ngdata.jajc.exception.JajcException;

public class JavaPuppetConfiguration extends AbstractPuppetConfiguration {

	
	private PuppetFile content;
	
	private String filename;
	private String path;
	
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
		//TODO log this
		if (path == null)
			path = "/opt/jdk6";
		if (filename == null)
			throw new JajcException("The value of bin_filename must be set in the java part of the user configuration, it corresponds to the file in puppet/modules/java/files");
		
		content.addProperty("bin_filename", filename);
		content.addProperty("install_path", path);
	}
	
	public String getPathName() {
		return path;
	}
	

}
