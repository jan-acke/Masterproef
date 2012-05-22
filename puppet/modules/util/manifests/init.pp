
class util {}

define util::fact($value="", $ensure=present) {
  $filename = "/etc/mcollective/facts.yaml"
  case $ensure {
    default: { err ("unkown value for ensure: ${ensure}") }
    present: {
      exec {"remove_current":
        command => "/bin/sed -i '/${name}/d' ${filename}"
      }
      
      exec { "/bin/echo '${name}: ${value}' >> ${filename}":
        require => Exec["remove_current"],
      }
    }

    absent: {
      exec { "/bin/sed -i '/${name}/' ${filename}":

      }
    }
  }
}
