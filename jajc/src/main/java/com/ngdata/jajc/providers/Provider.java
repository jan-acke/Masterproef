package com.ngdata.jajc.providers;

import java.util.Map;

import org.jclouds.compute.RunScriptOnNodesException;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.scriptbuilder.domain.Statement;
import org.jclouds.ssh.SshClient;

import com.google.common.base.Predicate;
import com.ngdata.jajc.IConfig;

public interface Provider {
	
	
	
	public Iterable<NodeMetadata> getNodes();
		
	/*
	 * Wrappers for jclouds functions, some providers require extra runscriptoptions
	 */
	public Map<? extends NodeMetadata, ExecResponse> runScriptOnNodesMatching(Predicate<NodeMetadata> filter, Statement runScript) throws RunScriptOnNodesException;
	public ExecResponse runScriptOnNode(String id, Statement runScript);
	public SshClient getSshClient(NodeMetadata data);
		
	public IConfig getConfiguration();
	
	public void closeContext();
}
