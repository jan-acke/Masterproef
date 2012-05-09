package com.ngdata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeMetadataBuilder;

import com.google.common.collect.Iterables;
import com.ngdata.jajc.puppetconfiguration.PuppetFile;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
    	
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	List<String> list = new ArrayList<String>();
    	list.add("een en nog wat");
    	list.add("twee\"zever");
    	list.add("drie");
    	assertEquals("[\"een en nog wat\" ,\"twee\"zever\" ,\"drie\" ]", PuppetFile.iterableToString(list));
    	
       
        List<String> tags = new ArrayList<String>();
        tags.add("cdh3::zookeeper");
        tags.add("zookeeper");
        tags.add("cdh3::zookeeper::service");
        tags.add("zoOkeeper");
        
        for ( int i=0;i<tags.size();i++) {
        	String s = tags.get(i);
        	if ( i < 3) //Pattern.compile(".*zookeeper.*").matcher(s).matches())
        		assertEquals(true, s.matches(".*zookeeper.*"));
        	else
        		assertEquals(false, s.matches(".*zookeeper.*"));	
        }
        
    }
}
