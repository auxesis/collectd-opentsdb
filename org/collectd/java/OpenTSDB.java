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
import org.collectd.api.CollectdInitInterface;
import org.collectd.api.CollectdWriteInterface;


public class OpenTSDB implements CollectdWriteInterface, CollectdInitInterface
{
    private PrintStream _out;
    private Socket      socket;

    public OpenTSDB ()
    {
        Collectd.registerInit  ("OpenTSDB", this);
        Collectd.registerWrite ("OpenTSDB", this);
    }

    public int init ()
    {
        try {
          socket = new Socket("127.0.0.1", 4242);
          _out   = new PrintStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
          System.out.println("Couldn't establish connection!");
          System.out.println(e);
          System.exit(1);
        } catch (IOException e) {
          System.out.println("Couldn't send data!");
          System.out.println(e);
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

    for (int i=0; i<size; i++) {
        // Buffer
        StringBuffer sb = new StringBuffer();
        sb.append("put ");

        // Metric name
        String    name, pointName,
                  plugin, pluginInstance,
                  type, typeInstance;
        ArrayList parts = new ArrayList();

        plugin         = vl.getPlugin();
        pluginInstance = vl.getPluginInstance();
        type           = vl.getType();
        typeInstance   = vl.getTypeInstance();

        // FIXME: refactor to switch?
        if ( plugin != null ) {
            parts.add(plugin);
        }
        if ( pluginInstance != null ) {
            parts.add(pluginInstance);
        }
        if ( type != null ) {
            parts.add(type);
        }
        if ( typeInstance != null) {
            parts.add(typeInstance);
        }

        pointName = ds.get(i).getName();
        if (!pointName.equals("value")) {
          parts.add(pointName);
        }

        // Consolidate the list of labels
        parts.removeAll(Collections.singletonList(null));
        parts.removeAll(Collections.singletonList(""));
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

  public static String join(Collection s, String delimiter) {
      StringBuffer buffer = new StringBuffer();
      Iterator iter = s.iterator();
      while (iter.hasNext()) {
          buffer.append(iter.next());
          if (iter.hasNext()) {
              buffer.append(delimiter);
          }
      }
      return buffer.toString();
  }
}
