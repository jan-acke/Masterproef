#!/bin/bash

#start services, followed guidelines on 
#https://ccp.cloudera.com/display/CDHDOC/CDH3+Deployment+on+a+Cluster

mco service -C cdh3::hadoop::namenode hadoop-0.20-namenode start
mco service -C cdh3::hadoop::datanode hadoop-0.20-datanode start
mco service -C cdh3::hadoop::tasktracker hadoop-0.20-tasktracker start
mco service -C cdh3::hadoop::jobtracker hadoop-0.20-jobtracker start
mco service -C cdh3::zookeeper hadoop-zookeeper-server start
mco service -C cdh3::hbase::master hadoop-hbase-master start
mco service -C cdh3::hbase::regionserver hadoop-hbase-regionserver start
mco service -C lily::solr lily-solr start
mco service -C lily::server lily-server start
