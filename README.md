collectd-opentsdb
=================

collectd writer plugin for OpenTSDB.

Install
-------

    # Install collectd.
    apt-get install collectd
    # Clone the plugin repo.
    git clone git://github.com/auxesis/collectd-opentsdb.git
    # Compile the plugin
    cd collectd-opentsdb
    javac -classpath /usr/share/collectd-core/java/collectd-api.jar org/collectd/java/OpenTSDB.java


Insert this into your `collectd.conf` (likely at `/etc/collectd/collectd.conf`):

    LoadPlugin java
    <Plugin java>
      JVMArg "-Djava.class.path=/usr/share/collectd-core/java/collectd-api.jar:/path/to/collectd-opentsdb/"

      LoadPlugin "org.collectd.java.OpenTSDB"
      <Plugin "OpenTSDB">
        Server "localhost" "4242"
      </Plugin>
    </Plugin>

Restart collectd.
