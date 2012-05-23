#!/bin/bash

#stop services, followed guidelines on 
#https://ccp.cloudera.com/display/CDHDOC/CDH3+Deployment+on+a+Cluster

mco service -C lily::server lily-server stop
mco service -C lily::solr lily-solr stop
mco service -C cdh3::hbase::master hadoop-hbase-master stop
mco service -C cdh3::hbase::regionserver hadoop-hbase-regionserver stop
mco service -C cdh3::zookeeper hadoop-zookeeper-server stop
mco service -C cdh3::hadoop::jobtracker hadoop-0.20-jobtracker stop
mco service -C cdh3::hadoop::tasktracker hadoop-0.20-tasktracker stop
mco service -C cdh3::hadoop::namenode hadoop-0.20-namenode stop
mco service -C cdh3::hadoop::datanode hadoop-0.20-datanode stop


