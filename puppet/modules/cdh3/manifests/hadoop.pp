
class cdh3::hadoop {
  require java
  require cdh3::repository
  require cdh3

  #declaring classified variables as local variables for template usage since templates don't know classified variables and
  #we don't want to use scope.lookupvar("varname") for every variable
  $namenode = $cdh3::namenode
  $dfs_permissions = $cdh3::permissions
  $dfs_name_dir = $cdh3::dfs_name_dir
  $dfs_data_dir = $cdh3::dfs_data_dir
  $mapred_job_tracker = $cdh3::mapred_job_tracker
  $mapred_local_dir = $cdh3::mapred_local_dir
  
  package { "hadoop-0.20":
    ensure => latest,
    
  }
  
  #mkdir -p in puppet is not possible and manuallly splitting and creating a variable string with the necessary directories
  #seems a bit far fetched eg [ "/data" , "/data/subdir" , "/data/subdir/dfsdata" , "/data/subdir/dfsnamedir"]
  exec { "create_directories":
    command => "mkdir -p $cdh3::dfs_data_dir $cdh3::dfs_name_dir $cdh3::mapred_local_dir",
    require => Package["hadoop-0.20"],
  }

  file { [ $cdh3::dfs_data_dir , $cdh3::dfs_name_dir ] :
    ensure => directory,
    owner => hdfs,
    group => hadoop,
    mode => 711,
    require => Exec["create_directories"],
  }

  file { $cdh3::mapred_local_dir :
    ensure => directory,
    owner => mapred,
    group => hadoop,
    mode => 755,
    require => Package["hadoop-0.20"],
  }

  $location = "/etc/hadoop-0.20/conf.mine"

  file { "${location}" :
    ensure => directory,
    require => Package["hadoop-0.20"],
    owner => root,
    group => root,
    source => "puppet:///modules/cdh3/conf.mine",
    recurse => true,
  }

  file { "${location}/hdfs-site.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("cdh3/conf.mine/hdfs-site.xml.erb")
  }

  file { "${location}/core-site.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("cdh3/conf.mine/core-site.xml.erb")
  }

  file { "${location}/mapred-site.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("cdh3/conf.mine/mapred-site.xml.erb")
  }
  
  
  exec { "update-alternatives":
    subscribe => File[$location],
    command => "update-alternatives --install /etc/hadoop-0.20/conf hadoop-0.20-conf /etc/hadoop-0.20/conf.mine/ 50",
  }
  
}

class cdh3::hadoop::namenode {
  require cdh3::hadoop

  package { "hadoop-0.20-namenode":
    ensure => latest,
  }

  #Interactive command :S
  exec { "yes Y | hadoop-0.20 namenode -format":
    user => "hdfs",
    require => Package["hadoop-0.20-namenode"],
    logoutput => true,
  }
}

class cdh3::hadoop::namenode::service {
  require cdh3::hadoop::namenode
  service { "hadoop-0.20-namenode":
    ensure => running,
    hasrestart => true,
  }
}

class cdh3::hadoop::datanode {
  require cdh3::hadoop
  package { "hadoop-0.20-datanode":
    ensure => latest,
  }
}

class cdh3::hadoop::secondarynamenode {
  require cdh3::hadoop
  package { "hadoop-0.20-secondarynamenode":
    ensure => latest,
  }
}

class cdh3::hadoop::jobtracker {
  require cdh3::hadoop

  package { "hadoop-0.20-jobtracker":
    ensure => latest,
  }
}


class cdh3::hadoop::tasktracker {
  require cdh3::hadoop

  package { "hadoop-0.20-tasktracker":
    ensure => latest,
  }
}
