#!/bin/bash

#stop services, followed guidelines on 
#https://ccp.cloudera.com/display/CDHDOC/CDH3+Deployment+on+a+Cluster


echo "Stopping Lily"
mco service -C lily::server lily-server stop

echo "Stopping Lily solr"
mco service -C lily::solr lily-solr stop

echo "Stopping hbase master"
mco service -C cdh3::hbase::master hadoop-hbase-master stop

echo "Stopping hbase regionserver(s)"
mco service -C cdh3::hbase::regionserver hadoop-hbase-regionserver stop

echo "Stopping zookeeper(s)"
mco service -C cdh3::zookeeper hadoop-zookeeper-server stop

echo "Stopping hadoop jobtracker"
mco service -C cdh3::hadoop::jobtracker hadoop-0.20-jobtracker stop

echo "Stopping hadoop tasktracker(s)"
mco service -C cdh3::hadoop::tasktracker hadoop-0.20-tasktracker stop

echo "Stopping hadoop namenode"
mco service -C cdh3::hadoop::namenode hadoop-0.20-namenode stop

echo "Stopping hadoop datanode(s)"
mco service -C cdh3::hadoop::datanode hadoop-0.20-datanode stop


