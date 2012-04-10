package com.ngdata.bo;

public class General {
	private String provider;
	private String clustername;
	private String accesskeyid;
	private String secretkey;
	
	public General(){
		
	} 
	
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public String getClustername() {
		return clustername;
	}
	public void setClustername(String clustername) {
		this.clustername = clustername;
	}

	public String getAccesskeyid() {
		return accesskeyid;
	}

	public void setAccesskeyid(String accesskeyid) {
		this.accesskeyid = accesskeyid;
	}

	public String getSecretkey() {
		return secretkey;
	}

	public void setSecretkey(String secretkey) {
		this.secretkey = secretkey;
	}
	
	
}
