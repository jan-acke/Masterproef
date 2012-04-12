
#testopstelling
Exec {
  path => [ "/bin" , "/sbin" , "/usr/bin" , "/usr/sbin" ]
}

node "puppet.outernet" {
  include cdh3::hadoop::datanode::service,cdh3::hadoop::tasktracker::service,cdh3::zookeeper,cdh3::hbase::regionserver::service
}

node "mail.outernet" {
  include cdh3::hadoop::namenode::postinstall,cdh3::hadoop::jobtracker::service,cdh3::zookeeper,cdh3::hbase::master::service
}

node "web.outernet" {
  include cdh3::hadoop::datanode::service,cdh3::hadoop::tasktracker::service,cdh3::hadoop::secondarynamenode,cdh3::zookeeper,cdh3::hbase::regionserver::service
}
