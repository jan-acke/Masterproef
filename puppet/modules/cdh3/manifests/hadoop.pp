
class cdh3::hadoop {
  require java
  require cdh3

  #make variables local so templates can use them
  $hdfs = $cdh3::environment::hdfs
  $mapred = $cdh3::environment::mapred
  $core = $cdh3::environment::core
  
  package { "hadoop-0.20":
    ensure => latest,
    
  }
  
  #mkdir -p in puppet is not possible and manuallly splitting and creating a variable string with the necessary directories
  #seems a bit far fetched eg [ "/data" , "/data/subdir" , "/data/subdir/dfsdata" , "/data/subdir/dfsnamedir"]
  exec { "create_directories":
    command => "mkdir -p ${cdh3::environment::hdfs['dfs.data.dir']} ${cdh3::environment::hdfs['dfs.name.dir']} ${cdh3::environment::mapred['mapred.local.dir']}",
    require => Package["hadoop-0.20"],
  }

  file { [ $cdh3::environment::hdfs['dfs.data.dir'] , $cdh3::environment::hdfs['dfs.name.dir'] ] :
    ensure => directory,
    owner => hdfs,
    group => hadoop,
    mode => 711,
    require => Exec["create_directories"],
  }

  file { $cdh3::environment::mapred['mapred.local.dir'] :
    ensure => directory,
    owner => mapred,
    group => hadoop,
    mode => 755,
    require => Exec["create_directories"],
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
    refreshonly => true,
    command => "update-alternatives --install /etc/hadoop-0.20/conf hadoop-0.20-conf /etc/hadoop-0.20/conf.mine/ 50",
  }
  
}

class cdh3::hadoop::namenode {
  require cdh3::hadoop
  include cdh3::hadoop::namenode::postinstall
  
  package { "hadoop-0.20-namenode":
    ensure => latest,
  }

  #Interactive command :S
  exec { "format_namenode":
    onlyif => "test ! -e ${cdh3::environment::hdfs['dfs.name.dir']}/current/VERSION",
    command => "yes Y | hadoop-0.20 namenode -format",
    user => "hdfs",
    require => Package["hadoop-0.20-namenode"],
  }
}


#namenode can be safely started since it has no dependencies
class cdh3::hadoop::namenode::service {
  require cdh3::hadoop::namenode
  service { "hadoop-0.20-namenode":
    ensure => running,
    hasrestart => true,
  }
}

#formats the namenode and creates some directories in the hdfs
class cdh3::hadoop::namenode::postinstall {
  require cdh3::hadoop::namenode::service
  exec { "hadoop fs -mkdir /tmp && hadoop fs -chmod 1777 /tmp":
    user => hdfs,
    subscribe => Exec["format_namenode"],
    refreshonly => true,
  }

  exec { "hadoop fs -mkdir ${cdh3::environment::mapred['mapred.system.dir']} && hadoop fs -chown mapred:hadoop ${cdh3::environment::mapred['mapred.system.dir']}":
    refreshonly => true,
    user => hdfs,
    subscribe => Exec["format_namenode"],
  }
}


class cdh3::hadoop::datanode {
  require cdh3::hadoop
  include cdh3::hadoop::datanode::service
  package { "hadoop-0.20-datanode":
    ensure => latest,
  }
}

class cdh3::hadoop::datanode::service {
  require cdh3::hadoop::datanode
  service { "hadoop-0.20-datanode":
    ensure => running,
    hasrestart => true,
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
  include cdh3::hadoop::jobtracker::service
  package { "hadoop-0.20-jobtracker":
    ensure => latest,
  }

}


class cdh3::hadoop::jobtracker::service {
  require cdh3::hadoop::jobtracker
  
  service { "hadoop-0.20-jobtracker":
    ensure => running,
    hasrestart => true,
    require => Class["cdh3::hadoop::namenode::postinstall"],
  }
}


class cdh3::hadoop::tasktracker {
  require cdh3::hadoop
  include cdh3::hadoop::tasktracker::service
  package { "hadoop-0.20-tasktracker":
    ensure => latest,
  }

}


class cdh3::hadoop::tasktracker::service {
  require cdh3::hadoop::tasktracker

  service { "hadoop-0.20-tasktracker":
    ensure => running,
    hasrestart => true,
  }
}
