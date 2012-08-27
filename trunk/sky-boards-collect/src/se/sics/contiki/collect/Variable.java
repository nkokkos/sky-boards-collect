/*
 * Variable
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 22 jun 2012
 */

package se.sics.contiki.collect;

public class Variable {
	private String name;
	private double value;

	public Variable(String name, double value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

	public void setName(String newname) {
		name = newname;
	}

	public void setValue(double newvalue) {
		value = newvalue;
	}
}
