package com.ngdata;

import java.util.List;
import java.util.Map;

import com.ngdata.bo.General;
import com.ngdata.bo.Instance;

public class YAMLConfig implements IConfig {

	private General general;
	private List<Instance> instances;
	private Map<String,Map<String,String>> providerSpecificInfo;
	
	public YAMLConfig() {}
	
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

	
	
}
