
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
  #the default name :/
  exec { "mv /tmp/jdk1.6* ${java::environment::install_path}":
    require => Exec["execute_bin"],
    refreshonly => true,
    subscribe => Exec["execute_bin"],
  }
}

class java {
    include java::install

}
