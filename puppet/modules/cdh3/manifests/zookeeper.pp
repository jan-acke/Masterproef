
class cdh3::zookeeper {
  require java
  require cdh3::environment
  require cdh3::repository
  include cdh3::zookeeper::install,cdh3::zookeeper::service
  
}

class cdh3::zookeeper::install {
  $zookeeper = $cdh3::environment::zookeeper
    notice("Trying to create zk myid file with with zooid = ${zooid}")
  
  package { "hadoop-zookeeper-server":
    ensure => latest,
    require => Class["cdh3::zookeeper"],
  }

  file { "zk.dataDir":
    path => "${cdh3::environment::zookeeper['dataDir']}",
    ensure => directory,
    owner => zookeeper,
    group => zookeeper,
    require => Package["hadoop-zookeeper-server"],
  }

  # Does not work with the fact: FACTER_zooid requires difficult java configuration
  #                              Custom facts are loaded at puppet startup, it only evaluates correctly after zoo.cfg is in place
  exec { "myid":
    command => "grep ${hostname} /etc/zookeeper/zoo.cfg | cut -d'=' -f1 | cut -d'.' -f2 | tee -a ${cdh3::environment::zookeeper['dataDir']}/myid",
    creates => "${cdh3::environment::zookeeper['dataDir']}/myid",
    require => File["zk.dataDir","/etc/zookeeper/zoo.cfg"],
  }

  service { "stopzookeeper":
    name => "hadoop-zookeeper-server",
    ensure => stopped,
    require => Package["hadoop-zookeeper-server"],
  }
    
  
  file { "/etc/zookeeper/zoo.cfg":
    content => template("cdh3/zookeeper/zoo.cfg.erb"),
    owner => zookeeper,
    group => zookeeper,
    require => Service["stopzookeeper"],
  }
}

class cdh3::zookeeper::service {
  
  service { "hadoop-zookeeper-server":
    hasrestart => true,
    restart => "restart",
    ensure => running,
    require => Class["cdh3::zookeeper::install"],
    subscribe => File["/etc/zookeeper/zoo.cfg"],
  }

}
