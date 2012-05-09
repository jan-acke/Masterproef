package com.ngdata.jajc.bo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Roles {
	

	private static Map<String,Set<Instance>> roles = new HashMap<String,Set<Instance>>();
	
	
	public static Set<Instance> getInstances(String rolename) {
		return roles.get(rolename);
		
	}
	
	protected static boolean addInstance(String rolename,Instance i) {
		Set<Instance> s = roles.get(rolename);
		boolean exists = s != null;
		if (! exists) {
			roles.put(rolename, new HashSet<Instance>());
		}
		roles.get(rolename).add(i);
		return ! exists;
	}
	

	protected static boolean removeInstance(String rolename, Instance i) {
		if (roles.get(rolename) != null )
			return roles.get(rolename).remove(i);
		return false;
		
	}
	
	private Roles() {
	}

	

}
