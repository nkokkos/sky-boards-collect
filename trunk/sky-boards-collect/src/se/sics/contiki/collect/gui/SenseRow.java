/*
 * SenseRow
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 04 Oct 2012
 */
package se.sics.contiki.collect.gui;

import java.util.ArrayList;

class SenseRow {
  ArrayList<Object> row;
  private static Object[] classes = { "", "", "", "", true };
  public static final int IDX_NODE = 0;
  public static final int IDX_SENSOR = 1;
  public static final int IDX_FEEDID = 2;
  public static final int IDX_CONV = 3;
  public static final int IDX_SEND = 4;

  public SenseRow(String node, String sensor, String feedId, String conv,
      boolean send) {
    row = new ArrayList<Object>(5);
    row.add(node);
    row.add(sensor);
    row.add(feedId);
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

  public SenseRow clone() {
    return new SenseRow((String) row.get(0), (String) row.get(1),
        (String) row.get(2), (String) row.get(3), (boolean) row.get(4));
  }
}