
class lily::solr {
  require lily::repository
  package { "lily-solr-3.5":
    ensure => latest,
  }
}

class lily::solr::service {
  require lily::solr
  package { "lily-solr-3.5-server":
    ensure => latest,
  }
}
  
