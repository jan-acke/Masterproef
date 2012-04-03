
class lily::server {
  require lily::repository
  package { "lily":
    ensure => latest, #or use a specific version eg: ensure => "1.1.2-2"
    
  }

  package { "lily-server":
    ensure => latest,
  }
}
