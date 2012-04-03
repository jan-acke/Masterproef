
class java::install {

  file { "/tmp/sun-jdk6.bin":
    mode => 550,
    owner => root,
    group => root,
    source => "puppet:///modules/java/jdk-6u31-linux-i586.bin",
  }
  
  exec { "/tmp/sun-jdk6.bin":
    require => File["/tmp/sun-jdk6.bin"],
    creates => "/opt/jdk1.6.0_31",
    cwd => "/opt",
  }
  
}

class java {
    include java::install

}
