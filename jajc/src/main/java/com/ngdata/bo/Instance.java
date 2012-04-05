package com.ngdata.bo;

import java.util.HashSet;
import java.util.Set;


public class Instance {
	
	private String hostname;
	private Set<String> roles;
		
	public Instance(String hostname) {
		this.setHostname(hostname);
		roles = new HashSet<String>();
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
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
	
}
