
class cdh3 {

  #variables used in different configs
  $namenode = "hdfs://mail.outernet:8020"
  $zk_quorum =  [  "puppet", "mail" , "web" ]

  #hashes contstrueren
  $hdfs = {
    "dfs.permissions" => "false",
    "dfs.name.dir" => "/data/nn",
    "dfs.data.dir" => "/data/dn"
  }

  $mapred = {
    "mapred.job.tracker" => "mail.outernet:54311",
    "mapred.local.dir"   => "/data/mapred/local",
    "mapred.system.dir"  => "/mapred/system"
  }

  $core = {
    "fs.default.name" => $namenode
  }

  $zookeeper = {
    "ticktime" => "2000",
    "dataDir"  => "/var/zookeeper",
    "clientPort" => "2181",
    "initLimit" => "5",
    "syncLimit" => "2",
    "servers" => $zk_quorum
  }

  $hbase = {
    "hbase.cluster.distributed" => "true",
    "hbase.rootdir" => "${namemode}/hbase",
    "hbase.zookeeper.quorum" => $zk_quorum
  }
  
}

class cdh3::repository {
  include apt
  
  apt::key {"Cloudera":
      source  => "http://archive.cloudera.com/debian/archive.key",
    }
  
  apt::sources_list { "cloudera":
    ensure => present,
    content => "deb http://archive.cloudera.com/debian lucid-cdh3 contrib",
  }
}
