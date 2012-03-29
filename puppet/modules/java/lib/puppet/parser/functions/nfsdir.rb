
#filename relative to nfsdir, destination absolute
module Puppet::Parser::Functions
  newfunction(:copy_from_nfsdir) do |args|
    filename = args[0]
    dest = args[1]
    %x[cp /vagrant/#{filename} #{dest}]
  end
end
