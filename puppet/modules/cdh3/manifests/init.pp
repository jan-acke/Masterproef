

class cdh3::repository {
  include apt
  
  apt::key {"cloudera":
      source  => "http://archive.cloudera.com/debian/archive.key",
    }
  
  apt::sources_list { "cloudera":
    ensure => present,
    content => "deb http://archive.cloudera.com/debian lucid-cdh3 contrib",
  }
}
