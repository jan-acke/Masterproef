
class lily::solr {
  require lily::repository
  package { [ "lily-solr-3.5" , "lily-solr-3.5-server" ]:
    ensure => latest,
  }
}
