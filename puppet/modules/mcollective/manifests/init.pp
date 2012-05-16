
class mcollective {


}


class mcollectvie::activemq {
  include util::tar
  require java

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
    subscribe File["${destination}","/tmp/activemq.tgz"],
  }

  
  file { "${destination}/conf/activemq.xml":
    owner => root,
    group => root,
    require => Exec["installmq"],
    content => template("mcollective/activemq.xml.erb"),
  }

  #Might be better to copy the binary to /etc/init.d so we can use the puppet service resource type
  #this would require extra configuration
  exec { "${destination}/bin/activemq restart":
    subscribe => File["${destination}/conf/activemq.xml"],
    refreshonly => true,
  }
  
}

class mcollective::server {
  require mcollective::activemq
}
