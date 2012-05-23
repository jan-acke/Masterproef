
class lily::solr {
  require lily
  include lily::solr::service
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
  
