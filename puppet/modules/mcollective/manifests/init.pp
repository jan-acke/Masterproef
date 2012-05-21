
class mcollective::activemq {
  require java
  require mcollective::environment
  
  $username = $mcollective::environment::username
  $password = $mcollective::environment::password
  
  file { "/tmp/activemq.tgz":
    mode => 550,
    owner => root,
    group => root,
    source => "puppet:///modules/mcollective/activemq.tgz",
  }

  $destination = "/usr/lib/activemq"
  
  file { "$destination":
    ensure => directory,
  }
  
  exec { "installamq":
    command => "tar xzf /tmp/activemq.tgz --strip-components=1 --directory=${destination}",
    refreshonly => true,
    subscribe => File["${destination}","/tmp/activemq.tgz"],
  }

  
  file { "${destination}/conf/activemq.xml":
    owner => root,
    group => root,
    require => Exec["installamq"],
    content => template("mcollective/activemq.xml.erb"),
  }


}

class mcollective::activemq::service {
  require mcollective::activemq
  
  #Might be better to copy the binary to /etc/init.d so we can use the puppet service resource type
  #however this would require extra configuration so it can find the activemq.xml file, for now using
  #refreshonly with service restart is ok.
  
  exec { "${destination}/bin/activemq restart":
    subscribe => File["${destination}/conf/activemq.xml"],
    refreshonly => true,
  }
  
}

class mcollective::server {
  require java
  require mcollective::environment

  $username = $mcollective::environment::username
  $password = $mcollective::environment::password
  $identity = $::hostname
  $stomphost = $mcollective::environment::stomphost
  
  package { "mcollective":
    ensure => latest,
  }

  file { "/etc/mcollective/server.cfg":
    owner => root,
    group => root,
    require => Package["mcollective"],
    content => template("mcollective/server.erb"),
  }

  service { "mcollective":
    hasrestart => true,
    ensure => running,
    require => File["/etc/mcollective/server.cfg"],
  }


}
