
class java::install {

  file { "/tmp/sun-jdk6.bin":
    mode => 550,
    owner => root,
    group => root,
    source => "puppet:///modules/java/jdk-6u31-linux-i586.bin",
  }
  
  exec { "/tmp/sun-jdk6.bin":
    require => File["/tmp/sun-jdk6.bin"],
    cwd => "/opt",
  }
  
}

class java {
  if ( $javaexists != "true" ){
    include java::install
  }
}
