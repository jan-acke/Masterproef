
class mcollective::repository {
  include apt
  
  apt::sources_list { "puppet":
    ensure => present,
    content => "deb http://apt.puppetlabs.com lucid main"
  }
  
  apt::key {"Puppet":
      ensure => present,
      source  => "http://apt.puppetlabs.com/pubkey.gpg",
  }

}



#Manual upload/install since no packages available on lucid, creating a simple .deb
#file (not too many dependencies since puppet can take care of that) and
#downloading it from a (private) repository is definitely a better alternative
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
  
  exec { "${mcollective::activemq::destination}/bin/activemq restart":
    subscribe => File["${mcollective::activemq::destination}/conf/activemq.xml"],
    refreshonly => true,
    cwd => "${mcollective::activemq::destination}",
  }
  
}

class mcollective::common {
  require java
  require mcollective::environment
  require mcollective::repository
  require apt
  
  package { "mcollective-common":
    ensure => latest,
    
  }
}



class mcollective::server {
  require mcollective::common
  
  #requirement for the mcollective
  package { "stomp":
    ensure => latest,
    provider => gem,
  }
  
  #rename these since the template uses them
  $username = $mcollective::environment::username
  $password = $mcollective::environment::password
  $identity = $::hostname
  $stomphost = $mcollective::environment::stomphost
  
  package { "mcollective":
    ensure => latest,
    require => Package["stomp"],
  }

  file { "/etc/mcollective/server.cfg":
    owner => root,
    group => root,
    require => Package["mcollective"],
    content => template("mcollective/server.erb"),
  }

  #installing mcollective service agent, these files come from:
  #https://github.com/puppetlabs/mcollective-plugins/tree/master/agent/service/

  #mcollective searches in the mcollective subdirectory of libdir which is
  #/usr/share/mcollective/plugins when installing the package, that's where
  #we need to place the agent files
  $libdir = "/usr/share/mcollective/plugins/mcollective"

    
  file { "$libdir/agent/service.rb":
    mode => 644,
    owner => root,
    group => root,
    source => "puppet:///modules/mcollective/service.rb",
    require => Package["mcollective"],
  }

  file { "$libdir/spec":
    ensure => directory,
    require => Package["mcollective"],
    owner => root,
    group => root,
    source => "puppet:///modules/mcollective/spec",
    recurse => true,
  }

  
}


class mcollective::server::service {
  require mcollective::server
  notice["I'm over here"]
  service { "mcollective":
    hasrestart => true,
    ensure => running,
    subscribe => File["/etc/mcollective/server.cfg"],
    
  }

}
