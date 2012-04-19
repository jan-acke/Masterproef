
#variables puppet can use

class lily::environment {
  $lilyHbase = {
    "hbase.zookeeper.quorum" => [ "mail" , "puppet" , "web" ],
    "hbase.zookeeper.property.clientPort" => "2181"
  }

  $lilyZooKeeper = {
    "connectString" => [ "mail:2181" , "puppet:2181" , "web:2181" ],
    "sessionTimeout" => "20000",
    "startUpTimeout" => "600000"
  }

  $lilyMapReduce = {
    "mapred.job.tracker" => "mail:9001",
    "fs.default.name" => "hdfs://mail:8020"
  }

  $lilyRepository = {
    "blobFileSystem" => "hdfs://mail:8020/lily/blobs",
    "port" => "12020",
    "maxServerThreads" => "35"
  }

  
}
