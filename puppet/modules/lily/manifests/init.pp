

class lily::repository {
  include apt
  require lily::environment

  $user = $lily::environment::username
  $password = $lily::environment::password
  
  apt::key {"lily-repo-pubkey":
    ensure => present,
    source  => "http://${user}:${password}@lilyproject.org/enterprise/packages/1.2/lilyproject.packages.pubkey",
  }
  
  apt::sources_list { "lily":
    ensure => present,
    content => "deb http://${user}:${password}@lilyproject.org/enterprise/packages/1.2/ubuntu/ lucid contrib",
  }
}
