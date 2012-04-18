
class cdh3::zookeeper {
  require java
  require cdh3::environment
  require cdh3::repository
  include cdh3::zookeeper::install #,cdh3::zookeeper::service
  
}

class cdh3::zookeeper::install {
  $zookeeper = $cdh3::environment::zookeeper
  
  package { "hadoop-zookeeper-server":
    ensure => latest,
    require => Class["cdh3::zookeeper"],
  }
  
  file { "zk.dataDir":
    path => "${cdh3::environment::zookeeper['dataDir']}",
    ensure => directory,
    owner => root,
    group => root,
    require => Package["hadoop-zookeeper-server"],
  }

  file { "${cdh3::environment::zookeeper['dataDir']}/myid":
    content => $zooid,
    owner => root,
    group => root,
    require => File["zk.dataDir"],
  }

  file { "/etc/zookeeper/zoo.cfg":
    content => template("cdh3/zookeeper/zoo.cfg.erb"),
    owner => root,
    group => root,
    require => Package["hadoop-zookeeper-server"],
  }
}

class cdh3::zookeeper::service {

  service { "hadoop-zookeeper-server":
    ensure => running,
    require => Class["cdh3::zookeeper::install"],

  }  
}
