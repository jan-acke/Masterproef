package com.ngdata.jajc.bo;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class Instance {
	
	private List<String> hostnames;
	private Map<String,String> options;
	private Set<String> roles;
		
	public Instance() {
		//roles = new HashSet<String>();
		
	}
	
	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		for (String rolename : roles)
			Roles.removeInstance(rolename,this);
		
		this.roles = roles;
				
		for (String rolename : roles)
			Roles.addInstance(rolename, this);
	}
	
	public void addRole(String rolename) {
		if (rolename != null ) {
			roles.add(rolename);
			Roles.addInstance(rolename, this);
		}
	}

	public List<String> getHostnames() {
		return hostnames;
	}

	public void setHostnames(List<String> hostnames) {
		this.hostnames = hostnames;
	}

	public Map<String,String> getOptions() {
		return options;
	}

	public void setOptions(Map<String,String> options) {
		this.options = options;
	}
	
}
