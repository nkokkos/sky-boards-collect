How to run sky-boards-collect example in Cooja, the Contiki Network Simulator.

PRE:

Make sure /contiki/examples/sky-boards-collect/collect-ar1000/ exists
Make sure /contiki/examples/sky-boards-collect/collect-ds1000/ exists

1. Edit CollectView.java line 120 to set your sky-boards-collect.jar file location

2. Copy CollectView.java into

/contiki/tools/cooja/apps/collect-view/java/se/sics/cooja/plugins/collectview/

(rename old file to, eg, collectview-bk.java)

3. Open Cooja and go to file>Open simulation>Browse, 
then open example-sky-boards-collect.csc. Wait for Cooja to setup simulation.

4. Click Start in "Simulation Control" window.
5. Click Start collect & Send command to nodes in collect view 

It is possible to adjust conversion expression for simulated nodes
It is possible to send all data from simulated network nodes to Cosm/sen.se too
