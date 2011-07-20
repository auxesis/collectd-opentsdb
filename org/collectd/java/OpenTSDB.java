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
    private DataOutputStream _out;
    private Socket           socket;

    public OpenTSDB ()
    {
        Collectd.registerInit  ("OpenTSDB", this);
        Collectd.registerWrite ("OpenTSDB", this);
    }

    public int init ()
    {
        try {
          socket = new Socket("127.0.0.1", 4243);
          _out   = new DataOutputStream(socket.getOutputStream());
          _out.writeBytes("stats\n");
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
        String    name, pointName;
        ArrayList parts = new ArrayList();

        parts.add(vl.getPlugin());
        parts.add(vl.getPluginInstance());
        parts.add(vl.getType());
        parts.add(vl.getTypeInstance());

        pointName = ds.get(i).getName();
        if (!pointName.equals("value")) {
          parts.add(pointName);
        }

        // Consolidate the list of labels
        ArrayList uniques = new ArrayList(new LinkedHashSet(parts));
        uniques.removeAll(Collections.singletonList(null));
        uniques.removeAll(Collections.singletonList(""));
        name = join(uniques, ".");

        sb.append(name).append(' ');

        // Time
        long time = vl.getTime();
        sb.append(time).append(' ');

        // Value
        Number val = values.get(i);
        sb.append(val).append(' ');

        // Host
        String host = vl.getHost();
        sb.append("host=").append(host).append(",");

        // Meta
        sb.append("source=collectd");
        sb.append("\n");

        String output = sb.toString();

        // Send to OpenTSDB
        try {
          _out.writeBytes(output);
        } catch (java.io.IOException e) {
          System.out.println("Couldn't send data!");
          System.out.println(e);
        //} catch (java.net.SocketException e) {
        }
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
