/*
 * CosmRow
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 04 Oct 2012
 */
package se.sics.contiki.collect.gui;

import java.util.ArrayList;
import java.util.Hashtable;

class CosmRow {
  ArrayList<Object> row;
  private static Object[] classes = { "", new Hashtable<String, String>(), "",
      "", "", true };
  public static final int IDX_NODE = 0;
  public static final int IDX_DATASTREAMS = 1;
  public static final int IDX_FEEDID = 2;
  public static final int IDX_FEEDTITLE = 3;
  public static final int IDX_CONV = 4;
  public static final int IDX_SEND = 5;

  public CosmRow(String node, Hashtable<String, String> dataStreams,
      String feedId, String feedTitle, String conv, boolean send) {
    row = new ArrayList<Object>(6);
    row.add(node);
    row.add(dataStreams);
    row.add(feedId);
    row.add(feedTitle);
    row.add(conv);
    row.add(send);
  }

  public Object getField(int index) {
    return row.get(index);
  }

  public void setField(int index, Object value) {
    row.set(index, value);
  }

  public static Class<? extends Object> getClass(int c) {
    return classes[c].getClass();
  }
}