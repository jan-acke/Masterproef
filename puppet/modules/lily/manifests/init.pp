

class lily::repository {
  include apt

  apt::key {"lily-repo-pubkey":
    ensure => present,
    source  => "http://${user}:${password}@lilyproject.org/enterprise/packages/1.2/lilyproject.packages.pubkey",
  }
  
  apt::sources_list { "lily":
    ensure => present,
    content => "deb http://${user}:${password}@lilyproject.org/enterprise/packages/1.2/ubuntu/ lucid contrib",
  }
}
