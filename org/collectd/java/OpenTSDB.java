package org.collectd.java;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.net.*;

import org.collectd.api.Collectd;
import org.collectd.api.ValueList;
import org.collectd.api.DataSet;
import org.collectd.api.DataSource;
import org.collectd.api.CollectdConfigInterface;
import org.collectd.api.CollectdInitInterface;
import org.collectd.api.CollectdWriteInterface;
import org.collectd.api.OConfigValue;
import org.collectd.api.OConfigItem;


public class OpenTSDB implements CollectdWriteInterface,
    CollectdInitInterface,
    CollectdConfigInterface
{
    private String      server = "localhost";
    private String      port   = "4242";
    private PrintStream _out;
    private Socket      socket;

    public OpenTSDB ()
    {
        Collectd.registerInit   ("OpenTSDB", this);
        Collectd.registerWrite  ("OpenTSDB", this);
        Collectd.registerConfig ("OpenTSDB", this);
    }

    public int init ()
    {
        try {
          Collectd.logInfo ("OpenTSDB plugin: server: " + server + ", port: " + port);
          socket = new Socket (server, 4242);
          _out   = new PrintStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
          Collectd.logError ("Couldn't establish connection: " + e.getMessage());
          System.exit(1);
        } catch (IOException e) {
          Collectd.logError ("Couldn't send data: " + e.getMessage());
          System.exit(1);
        }


        /*
        out.close();
        in.close();
        stdIn.close();
        echoSocket.close();
        */

        return(0);
    }

  public int write (ValueList vl)
  {
    List<DataSource> ds = vl.getDataSource();
    List<Number> values = vl.getValues();
    int size            = values.size();
    StringBuffer sb = new StringBuffer();

    for (int i=0; i<size; i++) {
        // Buffer
        sb.setLength(0);
        sb.append("put ");

        // Metric name
        String    name, pointName,
                  plugin, pluginInstance,
                  type, typeInstance;
        ArrayList<String> parts = new ArrayList<String>();

        plugin         = vl.getPlugin();
        pluginInstance = vl.getPluginInstance();
        type           = vl.getType();
        typeInstance   = vl.getTypeInstance();

        // FIXME: refactor to switch?
        if ( plugin != null && !plugin.isEmpty() ) {
            parts.add(plugin);
        }
        if ( pluginInstance != null && !plugin.isEmpty() ) {
            parts.add(pluginInstance);
        }
        if ( type != null ) {
            parts.add(type);
        }
        if ( typeInstance != null && !plugin.isEmpty() ) {
            parts.add(typeInstance);
        }

        pointName = ds.get(i).getName();
        if (!pointName.equals("value")) {
          parts.add(pointName);
        }

        name = join(parts, ".");

        sb.append(name).append(' ');

        // Time
        long time = vl.getTime() / 1000;
        sb.append(time).append(' ');

        // Value
        Number val = values.get(i);
        sb.append(val).append(' ');

        // Host
        String host = vl.getHost();
        sb.append("host=").append(host).append(" ");

        // Meta
        sb.append("source=collectd");

        String output = sb.toString();

        // Send to OpenTSDB
        _out.println(output);
    }

    return(0);
  }

  public static String join(Collection<String> s, String delimiter) {
      StringBuffer buffer = new StringBuffer();
      Iterator<String> iter = s.iterator();
      while (iter.hasNext()) {
          buffer.append(iter.next());
          if (iter.hasNext()) {
              buffer.append(delimiter);
          }
      }
      return buffer.toString();
  }

  public int config (OConfigItem ci) /* {{{ */
  {
    List<OConfigItem> children;
    int i;

    Collectd.logDebug ("OpenTSDB plugin: config: ci = " + ci + ";");

    children = ci.getChildren ();
    for (i = 0; i < children.size (); i++)
    {
      List<OConfigValue> values;
      OConfigItem child;
      String key;

      child = children.get (i);
      key   = child.getKey ();
      if (key.equalsIgnoreCase ("Server"))
      {
        values = child.getValues();
        if (values.size () != 2)
        {
            Collectd.logError ("OpenTSDB plugin: " + key +
                "configuration option needs exactly two arguments: server + port");
            return (1);
        } else {
            server = values.get(0).toString();
            port   = values.get(1).toString();
        }
      }
      else
      {
        Collectd.logError ("OpenTSDB plugin: Unknown config option: " + key);
      }
    } /* for (i = 0; i < children.size (); i++) */

    return (0);
  } /* }}} int config */
}
