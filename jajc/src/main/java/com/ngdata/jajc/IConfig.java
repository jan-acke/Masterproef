package com.ngdata.jajc;
import java.util.List;
import java.util.Map;

import com.ngdata.jajc.bo.General;
import com.ngdata.jajc.bo.Instance;


public interface IConfig {

	//Properties of an instance
	public List<Instance> getInstances();
	public General getGeneral();
	public Map<String, String> getProviderSpecificInfo(String provider);
	public Map<String, Map<String, String>> getUserConfig();

}