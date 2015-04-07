# Introduction #

This guide briefly shows how to add platforms into sky-boards-collect. Despite the project name, platforms doesn't need to be sky based.


# Details #

1. **Platform firmware**

To define contiki-collect firmware for a platform, create `collect-view-platform.[ch]`. See:

  * `collect-view.[ch]`
  * `/svn/trunk/contiki/examples/`, for examples

After that, edit `SensorInfo.java` to reflect the platform message format in `CollectView` tool.

---


2. **Sensors classes**

A sensor class implements the sensor conversion expressions. See:

  * `/svn/trunk/sky-boards-collect/src/se/sics/contiki/collect/sensor`

Take existing sensor classes as reference.

---


3. **Platform class**

The platform class extends the Node class, creates its sensors instances, and map the sensor identifiers to the specific collect message position. See:

  * `/svn/trunk/sky-boards-collect/src/se/sics/contiki/collect/sensor`

Take existing platform classes as reference. If a sensor that didn't previously exist in the application is introduced along with the defined platform, sensor identifier should be create in `SensorIdentifier.java`. eg:

`public static final String PRESSURE_SENSOR = "Pressure";`

---


4. **Add instances**

In main class, `CollectServer.java`, add platform instance in `getNode` method.

If a sensor that didn't previously exist in the application is introduced along with the defined platform, `TimeChartPanel` instance should be added into `visualizers[]` structure (defined in class constructor). Take existing code as reference.