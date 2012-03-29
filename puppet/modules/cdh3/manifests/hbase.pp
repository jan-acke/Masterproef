
class cdh3::hbase {
  require cdh3::hadoop
  require cdh3::zookeeper
  
  package { "hadoop-hbase":
    ensure => latest,
    notify => Class["cdh3::hbase::configuration"],
  }
}

class cdh3::hbase::master {
  require cdh3::hbase

  package { "hadoop-hbase-master":
    ensure => latest,
  }
}

class cdh3::hbase::regionserver {
  require cdh3::hbase

  package { "hadoop-hbase-regionserver":
    ensure => latest,
  }
}
