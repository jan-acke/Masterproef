package com.ngdata.jajc.puppetconfiguration;

import com.ngdata.jajc.exception.JajcException;

public interface PuppetConfiguration extends Comparable<PuppetConfiguration>{

	/*
	 * @return The name of the puppet module
	 */
	public String getModuleName();
	
	/*
	 * @return A string that represents the content of the environment.pp file
	 */
	public String getEnvironmentContent();
	
	/*
	 * Configurations can be dependent of each other, lowest priority gets built first
	 */
	public int getPriority();
	
	public void build() throws JajcException;
}
