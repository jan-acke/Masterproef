#!/bin/bash

#start services, followed guidelines on 
#https://ccp.cloudera.com/display/CDHDOC/CDH3+Deployment+on+a+Cluster


echo "Starting hadoop namenode"
mco service -C cdh3::hadoop::namenode hadoop-0.20-namenode start

echo "Starting hadoop datanode(s)"
mco service -C cdh3::hadoop::datanode hadoop-0.20-datanode start

echo "Starting hadoop jobtracker"
mco service -C cdh3::hadoop::tasktracker hadoop-0.20-tasktracker start

echo "Starting hadoop tasktracker(s)"
mco service -C cdh3::hadoop::jobtracker hadoop-0.20-jobtracker start

echo "Starting zookeeper(s)"
mco service -C cdh3::zookeeper hadoop-zookeeper-server start

echo "Starting hbase master"
mco service -C cdh3::hbase::master hadoop-hbase-master start

echo "Starting hbase regionserver(s)"
mco service -C cdh3::hbase::regionserver hadoop-hbase-regionserver start

echo "Starting Lily solr"
mco service -C lily::solr lily-solr start

echo "Starting Lily"
mco service -C lily::server lily-server start
