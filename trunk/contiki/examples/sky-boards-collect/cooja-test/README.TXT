How to run sky-boards-collect example in Cooja, the Contiki Network Simulator.

PRE:

Make sure /contiki/examples/sky-boards-collect/collect-ar1000/ exists
Make sure /contiki/examples/sky-boards-collect/collect-ds1000/ exists

1. Open CollectView plugin source file

/contiki/tools/cooja/apps/collect-view/java/se/sics/se/cooja/plugins/CollectView.java

2. Find this line 

String jarFile = contikiPath + "/tools/collect-view/dist/collect-view.jar";

3. Edit line to set sky-boards-collect.jar location. Eg:

String jarFile = contikiPath + "/tools/sky-boards-collect/sky-boards-collect.jar";

4. Open Cooja and go to file>Open simulation>Browse, 
then open example-sky-boards-collect.csc. Wait for Cooja to setup simulation.

5. Click Start in "Simulation Control" window.
6. Click Start collect & Send command to nodes in collect view 

It is possible to adjust conversion expression for simulated nodes
It is possible to send all data from simulated network nodes to Cosm/sen.se too
