
class lily::admin {
  require lily::repository
  package { "lilyadmin":
    ensure => latest,
    
  }
}
