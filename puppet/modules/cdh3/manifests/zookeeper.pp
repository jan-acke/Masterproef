
class cdh3::zookeeper {
  require java
  require cdh3
  
  include cdh3::zookeeper::install,cdh3::zookeeper::service
  
}

class cdh3::zookeeper::install {

  $zookeeper = $cdh3::environment::zookeeper
  
  package { "hadoop-zookeeper":
    ensure => latest,
    require => Class["cdh3::zookeeper"],
  }

  file { "zk.dataDir":
    path => "${cdh3::environment::zookeeper['dataDir']}",
    ensure => directory,
    owner => zookeeper,
    group => zookeeper,
    require => Package["hadoop-zookeeper"],
  }

  # Does not work with the fact: FACTER_zooid requires difficult java configuration
  #                              Custom facts are loaded at puppet startup, it only evaluates correctly after zoo.cfg is in place
  exec { "myid":
    command => "grep ${hostname} /etc/zookeeper/zoo.cfg | cut -d'=' -f1 | cut -d'.' -f2 | tee -a ${cdh3::environment::zookeeper['dataDir']}/myid",
    creates => "${cdh3::environment::zookeeper['dataDir']}/myid",
    require => File["zk.dataDir","/etc/zookeeper/zoo.cfg"],
  }

    
  file { "/etc/zookeeper/zoo.cfg":
    content => template("cdh3/zookeeper/zoo.cfg.erb"),
    owner => zookeeper,
    group => zookeeper,
    require => Package["hadoop-zookeeper"],
  }
}

class cdh3::zookeeper::service {
  require cdh3::zookeeper::install
  
  package { "hadoop-zookeeper-server":
    ensure => present,
  }

}
