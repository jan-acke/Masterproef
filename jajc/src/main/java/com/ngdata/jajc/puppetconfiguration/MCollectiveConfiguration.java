package com.ngdata.jajc.puppetconfiguration;

import java.util.Map;

import org.jclouds.compute.domain.NodeMetadata;

import com.ngdata.jajc.exception.JajcException;

public class MCollectiveConfiguration extends  AbstractPuppetConfiguration {

	private String content;

	@Override
	public String getModuleName() {
		return "mcollective";
	}

	@Override
	public String getEnvironmentContent() {
		return content;
	}

	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public void build() throws JajcException {
		
		NodeMetadata nm = getNodesByRole("puppetmaster").iterator().next();
		
		PuppetFile pf = new PuppetFile(getModuleName(),"environment");
		
		Map<String,String> properties = getUserConfiguration(getModuleName());
		
		
		String username = properties.get("username");
		String password = properties.get("password");
		
		if (username == null)
			username = "mcollective";
		if (password == null)
			password = "marionette";
		
		pf.addProperty("username", username);
		pf.addProperty("password", password);
		pf.addProperty("stomphost", nm.getHostname());
		
		content = pf.toString();
	}

}
