package com.ngdata;
import java.util.List;
import java.util.Map;


public interface IConfigReader {

	public List<Map<String,String>> getInstances();
	public Map<String,String> getGeneral();
}