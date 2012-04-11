
class cdh3::environment {
  #core-site.xml
  $cdh3_namenode = "hdfs://mail.outernet:8020"

  #hdfs-site.xml
  $cdh3_dfs_permissions = "false"
  $cdh3_dfs_name_dir = "/data/nn"
  $cdh3_dfs_data_dir = "/data/dn"

  #mapred-site.xml
  $cdh3_mapred_job_tracker = "mail.outernet:54311"
  $cdh3_mapred_local_dir = "/data/mapred/local" 
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
