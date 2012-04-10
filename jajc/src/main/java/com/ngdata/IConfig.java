package com.ngdata;
import java.util.List;
import java.util.Map;

import com.ngdata.bo.General;
import com.ngdata.bo.Instance;


public interface IConfig {

	//Properties of an instance
	public List<Instance> getInstances();
	public General getGeneral();
	public Map<String, String> getProviderSpecificInfo(String provider);

}