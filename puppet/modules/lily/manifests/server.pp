
class lily::server {
  require lily::repository
  package { "lily":
    ensure => latest, #or use a specific version eg: ensure => "1.1.2-2"
    
  }

  package { "lily-server":
    ensure => latest,
  }


  #hashes used in configuration files
  $lilyHbase = $liy::environment::lilyHbase
  $lilyZooKeeper = $lily::environment::lilyZooKeeper
  $lilyMapReduce = $lily::environment::lilyMapReduce
  $lilyRepository = $lily::environment::lilyRepository


  $location = "/usr/lib/lily/conf"
  file { "${location}":
    ensure => directory,
    require => Package["lily-server"],
    source => "puppet:///modules/lily/lily-server/conf",
    owner => root,
    group => root,
    recurse => true
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
