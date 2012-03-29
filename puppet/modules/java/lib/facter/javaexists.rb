Facter.add("javaexists") do
  setcode do
    File::exists?("/opt/jdk1.6.0_31/bin/java")
  end
end
