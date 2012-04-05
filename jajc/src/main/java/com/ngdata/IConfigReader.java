package com.ngdata;
import java.util.Map;
import java.util.Set;

import com.ngdata.bo.Instance;


public interface IConfigReader {

	//Properties of an instance
	public Set<Instance> getInstances();
	public Map<String,String> getGeneral();

}