
class cdh3::hbase {
  require cdh3::hadoop
  require cdh3::zookeeper
  
  package { "hadoop-hbase":
    ensure => latest,
  }

  file { "/etc/hbase/conf":
    owner => root,
    group => root,
    source => "puppet:///modules/cdh3/hbase/conf",
    recurse => true,
    require => Package["hadoop-hbase"],
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
