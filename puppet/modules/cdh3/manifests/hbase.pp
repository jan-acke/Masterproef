
class cdh3::hbase {
  require cdh3::hadoop
  require cdh3::zookeeper

  #declaring classified variables as local variables for template usage since templates don't know classified variables and
  #we don't want to use scope.lookupvar("varname") for every variable
  $hbase = $cdh3::hbase
  
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

  file { "/etc/hbase/conf/hbase-site.xml":
    owner => root,
    group => root,
    content => template("cdh3/hbase/conf/hbase-site.xml.erb"),
    require => Package["hadoop-hbase"],
  }
}

class cdh3::hbase::master {
  require cdh3::hbase

  package { "hadoop-hbase-master":
    ensure => latest,
  }

  exec { "hadoop fs -mkdir /hbase && hadoop fs -chown hbase /hbase":
    user => hdfs,
    refreshonly => true,
    subscribe => Exec["format_namenode"],
  }
}

class cdh3::hbase::master::service {
  require cdh3::hbase::master
  require cdh3::hadoop::jobtracker::service
  require cdh3::zookeeper::service
  service { "hadoop-hbase-master":
    ensure => running,
    hasrestart => true,
    require => Service["hadoop-0.20-namenode"]
  }
}

class cdh3::hbase::regionserver {
  require cdh3::hbase

  package { "hadoop-hbase-regionserver":
    ensure => latest,
  }
}

class cdh3::hbase::regionserver::service {
  require cdh3::hbase::regionserver
  service { "hadoop-hbase-regionserver":
    ensure => running,
    hasrestart => true,
  }
}
