

class cdh3 {
  require cdh3::repository
  require mcollective::server::service
  require cdh3::environment
}

class cdh3::repository {
  include apt
  
  apt::key {"Cloudera":
      ensure => present,
      source  => "http://archive.cloudera.com/debian/archive.key",
    }
  
  apt::sources_list { "cloudera":
    ensure => present,
    content => "deb http://archive.cloudera.com/debian lucid-cdh3 contrib",
  }
}
