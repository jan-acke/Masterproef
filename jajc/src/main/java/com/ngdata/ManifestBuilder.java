package com.ngdata;

import java.util.ArrayList;
import java.util.Collection;

import org.jclouds.compute.domain.NodeMetadata;

public class ManifestBuilder {
	
	public static Iterable<String> createPuppetManifests(Iterable<NodeMetadata> instances) {
		
		Collection<String> lines = new ArrayList<String>();
		lines.add("Exec  { path => [ '/bin' , '/sbin' , '/usr/bin' , '/usr/sbin' ] }");
		for ( NodeMetadata nm : instances) {
			lines.add("\n");
			lines.add("node '" + nm.getHostname() + "' {");
			StringBuilder sb = new StringBuilder(100);
			for ( String tag : nm.getTags())
				if (!"puppetmaster".equals(tag))
					sb.append(tag).append(",");
			sb.deleteCharAt(sb.length()-1);
			lines.add("include " + sb);
			lines.add("}");
		}
		
		return lines;
	}
}
