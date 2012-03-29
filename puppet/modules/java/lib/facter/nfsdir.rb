
Facter.add("nfsdir") do 
  setcode do 
    Facter::util::Resolution.exec('/bin/uname')
  end
end
