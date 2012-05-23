
class lily::server {
  require lily
  
  include lily::server::service
  
  package { "lily":
    ensure => latest, #or use a specific version eg: ensure => "1.1.2-2"
    
  }

  #redeclare variables so we can use them in templates
  $lilyHbase = $lily::environment::lilyHbase
  $lilyZooKeeper = $lily::environment::lilyZooKeeper
  $lilyMapReduce = $lily::environment::lilyMapReduce
  $lilyRepository = $lily::environment::lilyRepository


  $location = "/usr/lib/lily/conf"

  #Lily configuration files are downloaded with the package and change when newer versions
  #are released => only override the ones we need to configure
  file { "${location}":
    ensure => directory,
    require => Package["lily"],
    owner => root,
    group => root,
  }

  file { "${location}/general/hbase.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("lily/lily-server/conf/general/hbase.xml.erb")
  }

  file { "${location}/general/zookeeper.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("lily/lily-server/conf/general/zookeeper.xml.erb")
  }

  file { "${location}/general/mapreduce.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("lily/lily-server/conf/general/mapreduce.xml.erb")
  }

  file { "${location}/repository/repository.xml":
    owner => root,
    group => root,
    require => File[ $location ],
    content => template("lily/lily-server/conf/repository/repository.xml.erb")
  }


}


class lily::server::service  {
  require lily::server
  package { "lily-server":
    ensure => latest,
  }
}
