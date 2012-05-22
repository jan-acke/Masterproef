
class java {
  require java::environment

  if $java::environment::installopenjdk {
    require java::openjdk
  }
  else {
    require java::install
  }

}


#mogelijk nog problemen met JAVA_HOME
class java::openjdk {

  package { "openjdk-6-jdk":
    ensure => latest,
  }
}

class java::install {

  require java::environment

  $filename = $java::environment::bin_filename
  
  file { "/tmp/${filename}":
    mode => 550,
    owner => root,
    group => root,
    source => "puppet:///modules/java/${filename}",
  }
  
  exec { "execute_bin":
    command => "/tmp/${filename}",
    require => File["/tmp/${filename}"],
    onlyif => "test ! -s ${java::environment::install_path}/bin",
    cwd => "/tmp",
  }

  #The .bin file creates a jdk1.6.* file, there're no options to set
  #the default name :/. Also at first this was subsribed to the execute_bin
  #resoource but sometimes this would not be executed immediately after the
  #execute_bin, which is what we want
  exec { "mv /tmp/jdk1.6* ${java::environment::install_path}":
    refreshonly => true,
    subscribe => Exec["execute_bin"],
  }
}
