
class cdh3::hadoop {
  require java
  require cdh3::repository
  
  package { "hadoop-0.20":
    ensure => latest,
    
  }
  
  file { ["/data","/data/nn","/data/dn"]:
    ensure => directory,
    owner => hdfs,
    group => hadoop,
    mode => 711,
    require => Package["hadoop-0.20"],
  }

  file { ["/data/mapred","/data/mapred/local" ]:
    ensure => directory,
    owner => mapred,
    group => hadoop,
    mode => 755,
    require => Package["hadoop-0.20"],
  }

  $location = "/etc/hadoop-0.20/conf.mine"
  file { $location :
    ensure => directory,
    require => Package["hadoop-0.20"],
    owner => root,
    group => root,
    source => "puppet:///modules/cdh3/conf.mine",
    recurse => true,
  }

  exec { "update-alternatives":
    require => File[$location],
    command => "update-alternatives --install /etc/hadoop-0.20/conf hadoop-0.20-conf /etc/hadoop-0.20/conf.mine/ 50",
  }
  
}

class cdh3::hadoop::namenode {
  require cdh3::hadoop

  package { "hadoop-0.20-namenode":
    ensure => latest,
  }

  #Interactive command :S
  # exec { "hadoop-0.20 namenode -format":
  #   user => "hdfs",
  #   subscribe => Package["hadoop-0.20-namenode"],
  # }
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
