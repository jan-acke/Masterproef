
class cdh3::zookeeper {
  require java
  require cdh3::repository
  include cdh3::zookeeper::install,cdh3::zookeeper::service
  
}

class cdh3::zookeeper::install {
  $hbase_zookeeper_quorum = $cdh3::hbase_zookeeper_quorum
  
  package { "hadoop-zookeeper-server":
    ensure => latest,
    require => Class["cdh3::zookeeper"],
  }
  
  file { "/var/zookeeper":
    ensure => directory,
    owner => root,
    group => root,
    require => Package["hadoop-zookeeper-server"],
  }

  file { "/var/zookeeper/myid":
    content => $zooid,
    owner => root,
    group => root,
    require => File["/var/zookeeper"]
  }

  file { "/etc/zookeeper/zoo.cfg":
    content => template("cdh3/zookeeper/zoo.cfg.erb"),
    owner => root,
    group => root,
    require => Package["hadoop-zookeeper-server"],
  }
}

class cdh3::zookeeper::service {
  
  exec { "start-zk":
    command => "/usr/lib/zookeeper/bin/zkServer.sh start",
    require => Class["cdh3::zookeeper::install"],
  }
}
