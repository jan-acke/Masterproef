
class cdh3::zookeeper {
  require cdh3::repository
  include cdh3::zookeeper::install,cdh3::zookeeper::service

}

class cdh3::zookeeper::install {
  package { "hadoop-zookeeper-server":
    ensure => latest,
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
    source => "puppet:///modules/cdh3/zoo.cfg",
    owner => root,
    group => root,
    require => Package["hadoop-zookeeper-server"],
  }
}

class cdh3::zookeeper::service {
  
  exec { "start-zk":
    command => "/usr/lib/zookeeper/bin/zkServer.sh start",
  }
}
