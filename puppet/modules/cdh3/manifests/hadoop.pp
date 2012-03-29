

class cdh3::hadoop {
  require java
  require cdh3::repository

  package { "hadoop-0.20":
    ensure => latest,
    
  }
}

class cdh3::hadoop::namenode {
  require cdh3::hadoop

  package { "hadoop-0.20-namenode":
    ensure => latest,
  }
}

class cdh3::hadoop::datanode {
  require cdh3::hadoop

  package { "hadoop-0.20-datanode":
    ensure => latest,
  }
}

class cdh3::hadoop::secondarynamenode {
  require cdh3::hadoop

  package { "hadoop-0.20-secondarynamenode":
    ensure => latest,
  }
}

class cdh3::hadoop::jobtracker {
  require cdh3::hadoop

  package { "hadoop-0.20-jobtracker":
    ensure => latest,
  }
}


class cdh3::hadoop::tasktracker {
  require cdh3::hadoop

  package { "hadoop-0.20-datanode":
    ensure => latest,
  }
}
