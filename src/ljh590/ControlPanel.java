package ljh590;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ljh590.GraphView.Mode;

@SuppressWarnings("serial")
public class ControlPanel extends JComponent {

	private Simulation sim;
	private SimModel model;
	private GraphView view;
	private JFrame frame;
	private SimComponent comp;
	private JLabel currT;
	private JLabel currP;
	
	private JCheckBox colourParticlesAtActEnergy;
	private JCheckBox particlesDisappearAtActEnergy;
	
	private boolean pause = false;

	public ControlPanel(Simulation sim, JFrame frame) {
		super();
		this.sim = sim;
		this.frame = frame;

		this.model = new SimModel(sim);
		this.view = new GraphView(model);
		model.addObserver(view);

		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		String[] modes = { "Heat Engines", "Activation Energy" };
		JComboBox<String> menu = new JComboBox<String>(modes);
		menu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// JComboBox cb = (JComboBox)e.getSource();
				String m = (String) menu.getSelectedItem();
				if (m.equals("Heat Engines")) {
					model.changeMode(Mode.HeatEngines);
					colourParticlesAtActEnergy.setSelected(false);
				} else if (m.equals("Activation Energy")) {
					model.changeMode(Mode.ActivationEnergy);
					colourParticlesAtActEnergy.setSelected(true);
				}
			}
		});
		menu.setMinimumSize(new Dimension(200, 40));

		JPanel UI = new JPanel(new BorderLayout());

		JPanel sliders = sliderBar();
		JPanel buttons = buttonBar();
		comp = new SimComponent(model, frame, currT, currP);

		UI.add(sliders, BorderLayout.CENTER);
		UI.add(buttons, BorderLayout.EAST);

		view.setMinimumSize(new Dimension(200, frame.getHeight() - 50));
		view.setPreferredSize(new Dimension(200, frame.getHeight()));
		// view.setMaximumSize(new Dimension(200, frame.getHeight()));
		c.anchor = GridBagConstraints.FIRST_LINE_START; // Stick to top left
		c.ipadx = 0;
		c.ipady = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		// c.gridx = 0;
		// c.gridy = 0;
		// c.gridheight = 2;

		JPanel graphView = new JPanel(new GridBagLayout());
		graphView.setMinimumSize(new Dimension(200, frame.getHeight()));
		graphView.setPreferredSize(new Dimension(200, frame.getHeight()));

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		graphView.add(menu, c);

		c.fill = GridBagConstraints.BOTH;
		c.gridy = 1;
		graphView.add(view, c);

		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;

		// graphView.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		frame.add(graphView, c); // Graphs

		comp.setMinimumSize(
				new Dimension(model.getContainer().getWidth() + 300, model.getContainer().getHeight() + 10));
		comp.setPreferredSize(
				new Dimension(model.getContainer().getWidth() + 300, model.getContainer().getHeight() + 10));
		// c.fill = GridBagConstraints.BOTH;
		// c.ipadx = 0;
		// c.ipady = 0;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		frame.add(comp, c); // Simulation

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_END; // Stick to bottom
		c.ipadx = 20;
		c.ipady = 0;
		c.gridx = 1;
		c.gridy = 1;
		UI.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		frame.add(UI, c); // Bottom bar

		// System.out.println("SIZE: " + );
	}

	private JPanel sliderBar() {
		// Whole thing
		JPanel sliderBar = new JPanel(new GridLayout(0, 5));
		// Temp slider, label and value
		JPanel tempComp = new JPanel(new BorderLayout());
		// FPS slider, label and value
		JPanel fps = new JPanel(new BorderLayout());
		// Current T and P
		JPanel stats = new JPanel(new BorderLayout());
		// NumParticles slider, label and value
		JPanel numParticles = new JPanel(new BorderLayout());
		// SizeParticles slider, label and value
		// JPanel sizeParticles = new JPanel(new BorderLayout());
		// ActivationEnergy slider, label and value
		JPanel actEnergy = new JPanel(new BorderLayout());

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

		// JLabel sizeParticlesLabel = new JLabel("Particle size",
		// SwingConstants.CENTER);
		// JSlider sizeParticlesSlider = new JSlider(SwingConstants.HORIZONTAL,
		// 2, 100, 10);
		// sizeParticlesSlider.setMajorTickSpacing(10);
		// sizeParticlesSlider.setMinorTickSpacing(5);
		// sizeParticlesSlider.setPaintTicks(true);
		// sizeParticlesSlider.setPaintLabels(true);
		// JLabel sizeParticlesValue = new JLabel("10");
		// sizeParticlesSlider.addChangeListener(new ChangeListener() {
		// public void stateChanged(ChangeEvent e) {
		// model.setParticleSize(sizeParticlesSlider.getValue());
		// sizeParticlesValue.setText(Integer.toString(sizeParticlesSlider.getValue()));
		// }
		// });
		// sizeParticles.add(sizeParticlesLabel, BorderLayout.NORTH);
		// sizeParticles.add(sizeParticlesSlider, BorderLayout.CENTER);
		// sizeParticles.add(sizeParticlesValue, BorderLayout.EAST);

		JLabel actEnergyLabel = new JLabel("Activation energy", SwingConstants.CENTER);
		// JSlider actEnergySlider = new JSlider(SwingConstants.HORIZONTAL,
		// (int)model.calculateExpectedMSS(300) / 10,
		// (int)model.calculateExpectedMSS(4000) * 2,
		// (int)model.calculateExpectedMSS(300));
		JSlider actEnergySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 10);
		actEnergySlider.setMajorTickSpacing(20);
		actEnergySlider.setMinorTickSpacing(10);
		actEnergySlider.setPaintTicks(true);
		actEnergySlider.setPaintLabels(true);
		JLabel actEnergyValue = new JLabel("10");
		actEnergySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setActivationEnergy(actEnergySlider.getValue());
				actEnergyValue.setText(Integer.toString(actEnergySlider.getValue()));
			}
		});
		actEnergy.add(actEnergyLabel, BorderLayout.NORTH);
		actEnergy.add(actEnergySlider, BorderLayout.CENTER);
		actEnergy.add(actEnergyValue, BorderLayout.EAST);
		// actEnergySlider.setMaximum(maximum);

		JSlider tempSlider = new JSlider(SwingConstants.HORIZONTAL, 200, 4000, 300);
		JLabel tempValue = new JLabel("300");
		tempSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setT(tempSlider.getValue());
				tempValue.setText(Integer.toString(tempSlider.getValue()));
			}
		});
		JLabel tempLabel = new JLabel("Wall temperature", SwingConstants.CENTER);
		tempSlider.setMajorTickSpacing(950);
		tempSlider.setMinorTickSpacing(190);
		tempSlider.setPaintTicks(true);
		tempSlider.setPaintLabels(true);
		tempComp.add(tempLabel, BorderLayout.NORTH);
		tempComp.add(tempSlider, BorderLayout.CENTER);
		tempComp.add(tempValue, BorderLayout.EAST);

		currT = new JLabel("<html>Average temperature<br>of particles: </html>");
		currP = new JLabel("<html>Average pressure<br>on container: </html>");
		stats.add(currT, BorderLayout.NORTH);
		stats.add(currP, BorderLayout.SOUTH);

		JSlider fpsSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 16, 4);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(2), new JLabel("0.5"));
		labelTable.put(new Integer(4), new JLabel("1"));
		labelTable.put(new Integer(6), new JLabel("1.5"));
		labelTable.put(new Integer(8), new JLabel("2"));
		labelTable.put(new Integer(12), new JLabel("3"));
		labelTable.put(new Integer(16), new JLabel("4"));
		fpsSlider.setLabelTable(labelTable);
		JLabel fpsLabel = new JLabel("Simulation speed multipler", SwingConstants.CENTER);
		JLabel fpsValue = new JLabel("1.00");
		fpsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				comp.setFps(fpsSlider.getValue() * 15);
				fpsValue.setText(String.format("%.2f", fpsSlider.getValue() / 4.0));
			}
		});
		fpsSlider.setMajorTickSpacing(1);
//		fpsSlider.setMinorTickSpacing(15);
		fpsSlider.setPaintTicks(true);
		fpsSlider.setPaintLabels(true);
		fps.add(fpsSlider, BorderLayout.CENTER);
		fps.add(fpsLabel, BorderLayout.NORTH);
		fps.add(fpsValue, BorderLayout.EAST);

		sliderBar.add(stats);
		sliderBar.add(tempComp);
		// sliderBar.add(sizeParticles);
		sliderBar.add(actEnergy);
		sliderBar.add(numParticles);
		sliderBar.add(fps);

		return sliderBar;
	}

	private JPanel buttonBar() {
		JPanel playPauseRestart = new JPanel(new GridLayout(1,2));
		JPanel buttons = new JPanel(new GridLayout(0, 1));

		JButton playPause = new JButton("Pause");
		playPause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (pause) {
					model.resumeSim();
					playPause.setText("Pause");
					pause = false;
				} else {
					model.rollbackBuffer();
					model.pauseSim();
					playPause.setText("Resume");
					pause = true;
					comp.stopWalls();
				}
			}
		});
		
		JButton restart = new JButton("Restart");
		restart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.restartSim();
				comp.stopWalls();
				if (pause) {
					pause = false;
					playPause.setText("Pause");
				}
			}
		});
		
		JButton insulated = new JButton("Insulation: Off");
		insulated.addActionListener(new ActionListener() {
			boolean isInsulated = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isInsulated) {
					insulated.setText("Insulation: Off");
					isInsulated = false;
				} else {
					insulated.setText("Insulation: On");
					isInsulated = true;
				}
				model.setIsInsulated(isInsulated);
			}
		});

		JButton moveWallSlow = new JButton("Move wall in (slowly)");
		JButton moveWallFast = new JButton("Move wall in (quickly)");
		moveWallSlow.addActionListener(new ActionListener() {
			boolean isIn = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isIn) {
					moveWallSlow.setText("Move wall in (slowly)");
					moveWallFast.setText("Move wall in (quickly)");
					isIn = false;
					comp.moveWallOutAuto(2);
				} else {
					moveWallSlow.setText("Move wall out (slowly)");
					moveWallFast.setText("Move wall out (quickly)");
					isIn = true;
					comp.moveWallInAuto(2);
				}
			}
		});

		moveWallFast.addActionListener(new ActionListener() {
			boolean isIn = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isIn) {
					moveWallSlow.setText("Move wall in (slowly)");
					moveWallFast.setText("Move wall in (quickly)");
					isIn = false;
					comp.moveWallOutAuto(10);
				} else {
					moveWallSlow.setText("Move wall in (slowly)");
					moveWallFast.setText("Move wall in (quickly)");
					isIn = true;
					comp.moveWallInAuto(10);
				}
			}
		});
		
		colourParticlesAtActEnergy = new JCheckBox("Colour particles when reaching activation energy");
		colourParticlesAtActEnergy.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					comp.setColouringParticles(false);
				} else {
					comp.setColouringParticles(true);
				}
			}
		});
		
		particlesDisappearAtActEnergy = new JCheckBox("Make particles disappear upon reaching activation energy");
		particlesDisappearAtActEnergy.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					model.setDisappearOnActEnergy(false);
				} else {
					model.setDisappearOnActEnergy(true);
				}
			}
		});
		

		playPauseRestart.add(restart);
		playPauseRestart.add(playPause);
		
		buttons.add(playPauseRestart);
		buttons.add(insulated);
		buttons.add(moveWallSlow);
//		buttons.add(moveWallFast);
		buttons.add(colourParticlesAtActEnergy);
		buttons.add(particlesDisappearAtActEnergy);
		return buttons;
	}
}
