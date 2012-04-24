
Facter.add("zooid") do
  setcode do
    result = ""
    if File.exists?("/etc/zookeeper/zoo.cfg")
      command = "grep " + Facter.hostname + " /etc/zookeeper/zoo.cfg | cut -d'=' -f1 | cut -d'.' -f2"
      result = Facter::Util::Resolution.exec(command).chomp
    else
      result = "/etc/zookeeper/zoo.cfg does not exist"
    end
    result
  end
end
