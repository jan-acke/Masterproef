
#testopstelling
Exec {
  path => [ "/bin" , "/sbin" , "/usr/bin" , "/usr/sbin" ]
}

node "puppet.outernet" {
  include cdh3::hadoop::datanode,cdh3::hadoop::tasktracker,cdh3::zookeeper
}

node "mail.outernet" {
  include cdh3::hadoop::namenode,cdh3::hadoop::jobtracker,cdh3::zookeeper
}

node "web.outernet" {
  include cdh3::hadoop::datanode,cdh3::hadoop::tasktracker,cdh3::hadoop::secondarynamenode,cdh3::zookeeper
}
