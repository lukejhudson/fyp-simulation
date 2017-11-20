package ljh590;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ControlPanel extends JComponent {

	private Simulation sim;
	private SimModel model;
	private GraphView view;
	private JFrame frame;
	private SimComponent comp;
	private JLabel currT;
	private JLabel currP;

	public ControlPanel(Simulation sim, JFrame frame) {
		super();
		this.sim = sim;
		this.frame = frame;
		
		this.model = new SimModel(sim);
		this.view = new GraphView(model);
		model.addObserver(view);
		
		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel UI = new JPanel(new BorderLayout());
		
		JPanel sliders = sliderBar();
		JPanel buttons = buttonBar();
		comp = new SimComponent(model, frame, currT, currP);
		
		UI.add(sliders, BorderLayout.CENTER);
		UI.add(buttons, BorderLayout.EAST);
		
		System.out.println(model.getContainer().getWidth() + 600);
		view.setMinimumSize(new Dimension(200, frame.getHeight()));
		view.setPreferredSize(new Dimension(200, frame.getHeight()));
//		view.setMaximumSize(new Dimension(200, frame.getHeight()));
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1.0;
		c.weighty = 1.0;
//		c.ipady = 0;
//		c.ipadx = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		frame.add(view, c); // Graphs
		comp.setMinimumSize(new Dimension(model.getContainer().getWidth() + 300, model.getContainer().getHeight()));
		comp.setPreferredSize(new Dimension(model.getContainer().getWidth() + 300, model.getContainer().getHeight()));
//		c.fill = GridBagConstraints.BOTH;
//		c.ipady = 0;
//		c.ipadx = 0;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		frame.add(comp, c); // Simulation
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 0;
		c.ipadx = 20;
		c.gridx = 1;
		c.gridy = 1;
		frame.add(UI, c); // Bottom bar
	}

	private JPanel sliderBar() {
		// Whole thing
		JPanel sliderBar = new JPanel(new GridLayout(1, 5));
		// Temp slider, label and value
		JPanel tempComp = new JPanel(new BorderLayout());
		// FPS slider, label and value
		JPanel fps = new JPanel(new BorderLayout());
		// Current T and P
		JPanel stats = new JPanel(new BorderLayout());
		// NumParticles slider, label and value
		JPanel numParticles = new JPanel(new BorderLayout());
		// SizeParticles slider, label and value
		JPanel sizeParticles = new JPanel(new BorderLayout());
		
		JLabel numParticlesLabel = new JLabel("Number of particles", SwingConstants.CENTER);
		JSlider numParticlesSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 500, 250);
		numParticlesSlider.setMajorTickSpacing(100);
		numParticlesSlider.setMinorTickSpacing(25);
		numParticlesSlider.setPaintTicks(true);
		numParticlesSlider.setPaintLabels(true);
		JLabel numParticlesValue = new JLabel("250");
		numParticlesSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setNumParticles(numParticlesSlider.getValue());
				numParticlesValue.setText(Integer.toString(numParticlesSlider.getValue()));
			}
		});
		numParticles.add(numParticlesLabel, BorderLayout.NORTH);
		numParticles.add(numParticlesSlider, BorderLayout.CENTER);
		numParticles.add(numParticlesValue, BorderLayout.EAST);
		
		JLabel sizeParticlesLabel = new JLabel("Particle size", SwingConstants.CENTER);
		JSlider sizeParticlesSlider = new JSlider(SwingConstants.HORIZONTAL, 2, 100, 10);
		sizeParticlesSlider.setMajorTickSpacing(10);
		sizeParticlesSlider.setMinorTickSpacing(5);
		sizeParticlesSlider.setPaintTicks(true);
		sizeParticlesSlider.setPaintLabels(true);
		JLabel sizeParticlesValue = new JLabel("10");
		sizeParticlesSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setParticleSize(sizeParticlesSlider.getValue());
				sizeParticlesValue.setText(Integer.toString(sizeParticlesSlider.getValue()));
			}
		});
		sizeParticles.add(sizeParticlesLabel, BorderLayout.NORTH);
		sizeParticles.add(sizeParticlesSlider, BorderLayout.CENTER);
		sizeParticles.add(sizeParticlesValue, BorderLayout.EAST);
		
		JSlider tempSlider = new JSlider(SwingConstants.HORIZONTAL, 200, 4000, 300);
		JLabel tempValue = new JLabel("300");
		tempSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setT(tempSlider.getValue());
				tempValue.setText(Integer.toString(tempSlider.getValue()));
			}
		});
		JLabel tempLabel = new JLabel("Wall temperature", SwingConstants.CENTER);
		tempSlider.setMajorTickSpacing(600);
		tempSlider.setMinorTickSpacing(100);
//		tempSlider.setPaintTicks(true);
//		tempSlider.setPaintLabels(true);
		tempComp.add(tempLabel, BorderLayout.NORTH);
		tempComp.add(tempSlider, BorderLayout.CENTER);
		tempComp.add(tempValue, BorderLayout.EAST);
		
		currT = new JLabel("<html>Average temperature<br>of particles: </html>");
		currP = new JLabel("<html>Average pressure<br>on container: </html>");
		stats.add(currT, BorderLayout.NORTH);
		stats.add(currP, BorderLayout.SOUTH);
		
		JSlider fpsSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 500, 60);
		JLabel fpsLabel = new JLabel("Iterations per second", SwingConstants.CENTER);
		JLabel fpsValue = new JLabel("60");
		fpsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				comp.setFps(fpsSlider.getValue());
				fpsValue.setText(Integer.toString(fpsSlider.getValue()));
			}
		});
		fpsSlider.setMajorTickSpacing(100);
		fpsSlider.setMinorTickSpacing(25);
		fpsSlider.setPaintTicks(true);
		fpsSlider.setPaintLabels(true);
		fps.add(fpsSlider, BorderLayout.CENTER);
		fps.add(fpsLabel, BorderLayout.NORTH);
		fps.add(fpsValue, BorderLayout.EAST);
		
		
		sliderBar.add(stats, 0, 0);
		sliderBar.add(tempComp, 0, 1);
		sliderBar.add(sizeParticles, 0, 2);
		sliderBar.add(numParticles, 0, 3);
		sliderBar.add(fps, 0, 4);
		
		return sliderBar;
	}
	
	private JPanel buttonBar() {
		JPanel buttons = new JPanel(new BorderLayout());
		
		JButton restart = new JButton("Restart");
		restart.addActionListener(e -> model.restartSim());
		JButton pause = new JButton("Pause");
		pause.addActionListener(e -> model.pauseSim());
		JButton resume = new JButton("Resume");
		resume.addActionListener(e -> model.resumeSim());
		
		buttons.add(restart, BorderLayout.NORTH);
		buttons.add(pause, BorderLayout.CENTER);
		buttons.add(resume, BorderLayout.SOUTH);
		return buttons;
	}
}
