
class cdh3::zookeeper {
  require cdh3::repository
  
  package { "hadoop-zookeeper":
    ensure => latest,
  }
}
