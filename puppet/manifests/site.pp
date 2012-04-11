
#testopstelling
Exec {
  path => [ "/bin" , "/sbin" , "/usr/bin" , "/usr/sbin" ]
}

node "puppet.outernet" {
  include cdh3::hadoop::datanode::service,cdh3::hadoop::tasktracker,cdh3::zookeeper,cdh3::hbase::regionserver
}

node "mail.outernet" {
  include cdh3::hadoop::namenode::postinstall,cdh3::hadoop::jobtracker,cdh3::zookeeper,cdh3::hbase::master
}

node "web.outernet" {
  include cdh3::hadoop::datanode::service,cdh3::hadoop::tasktracker,cdh3::hadoop::secondarynamenode,cdh3::zookeeper,cdh3::hbase::regionserver
}
