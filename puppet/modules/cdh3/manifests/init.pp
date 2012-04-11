
class cdh3 {
  #core-site.xml
  $namenode = "hdfs://mail.outernet:8020"

  #hdfs-site.xml
  $dfs_permissions = "false"
  $dfs_name_dir = "/data/nn"
  $dfs_data_dir = "/data/dn"

  #mapred-site.xml
  $mapred_job_tracker = "mail.outernet:54311"
  $mapred_local_dir = "/data/mapred/local"

  #hbase-site.xml & zoo.cfg
  $hbase_zookeeper_quorum =  [ "mail" , "puppet" , "web"]
}

class cdh3::repository {
  include apt
  
  apt::key {"cloudera":
      source  => "http://archive.cloudera.com/debian/archive.key",
    }
  
  apt::sources_list { "cloudera":
    ensure => present,
    content => "deb http://archive.cloudera.com/debian lucid-cdh3 contrib",
  }
}
