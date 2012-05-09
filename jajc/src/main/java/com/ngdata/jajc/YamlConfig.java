package com.ngdata.jajc;

import java.util.List;
import java.util.Map;

import com.ngdata.jajc.bo.General;
import com.ngdata.jajc.bo.Instance;

public class YamlConfig implements IConfig {

	private General general;
	private List<Instance> instances;
	private Map<String,Map<String,String>> providerSpecificInfo;
	private Map<String, Map<String, String>> userConfig;
	
	public YamlConfig() {}
	
	public General getGeneral() {
		return general;
	}
	public void setGeneral(General general) {
		this.general = general;
	}
	public List<Instance> getInstances() {
		return instances;
	}
	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public Map<String,Map<String,String>> getProviderSpecificInfo() {
		return providerSpecificInfo;
	}

	public void setProviderSpecificInfo(Map<String,Map<String,String>> providerSpecificInfo) {
		this.providerSpecificInfo = providerSpecificInfo;
	}

	@Override
	public Map<String, String> getProviderSpecificInfo(String provider) {
		return providerSpecificInfo.get(provider);
		
	}

	@Override
	public Map<String, Map<String, String>> getUserConfig() {
		return userConfig;
	}

	public void setUserConfig(Map<String, Map<String, String>> userConfig) {
		this.userConfig = userConfig;
	}

	
	
}
