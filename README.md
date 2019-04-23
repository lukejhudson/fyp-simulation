# A Teaching Tool for Basic Particle Physics
The aim of this project was to create a software teaching tool that allows the user to investigate the properties of an ideal gas through the use of a particle simulation. The initial simulation, along with the mechanisms for the user to control the simulation, was created in Java, and allowed the user to investigate the properties of ideal gases. The functionality of this simulation was then extended to explain the more advanced concepts of heat engines and activation energy. This was achieved by including tools and graphics to relate the behaviour of the particles to the to the properties of the gas as a whole. 

Read more about the project in the report, located at /doc/report.pdf.


![Heat Engines](screenshots/Heat-Engines.png)

The above screenshot shows the software in the heat engines mode. Particles collide with the walls and each other in the container located in the centre of the software. The handle attached to the right wall allows the user to move this wall. The parameters of the simulation can be altered using the control bar at the bottom of the software. 

In this mode there are three graphs: a histogram showing the distribution of the speeds of the particles; a pressure (y-axis) against volume (x-axis) graph; and a temperature (y-axis) against entropy (x-axis) graph. 

The pressure-volume and temperature-entropy graphs are primarily used to demonstrate the Carnot heat engine cycle. This can be done by using the “Single Carnot” or “Continual Carnot” buttons, which create either one cycle or as many as the user would like. 


![Heat Engines Help](screenshots/Heat-Engines-Help.png)

The help screen above describes and explains the basics of heat engines and the Carnot cycle. This highlights the crucial information that a user needs to understand to experiment with the simulation in confidence. The diagrams included specify the model of the Carnot cycle as well as relate the description of the cycle to a more realistic representation and to the simulation itself.


![Activation Energy](screenshots/Activation-Energy.png)

When in activation energy mode, particles are by default coloured red when they have energies greater than the activation energy. 

In the activation energy mode there are also three graphs: a histogram showing the distribution of the speeds of the particles; a histogram showing the distribution of the energies of the particles; and a graph of Boltzmann factor (y-axis) against reactions per iteration (x-axis). 

The energy histogram is included to observe the comparison between the distribution of the particles’ energies and their speeds. The bars that are highlighted red represent particles that have energies greater than the activation energy.

![Activation Energy Help](screenshots/Activation-Energy-Help.png)

The help screen above describes and explains the basics of activation energy. Since the basic concept of activation energy is relatively simple, this help screen goes through some examples of processes that are triggered when particles reach the activation energy, in addition to providing an understanding of how the activation energy relates to the Boltzmann factor. 


![Information](screenshots/Information.png)

To quickly find out about a component the user can hold their mouse cursor over it and a tooltip will pop up. To find more detailed information and some explanations of how best to use the software, an additional information screen is provided when pressing the “INFO” button in the top right of the simulation. 