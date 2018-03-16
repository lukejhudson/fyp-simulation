package code;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

import code.GraphView.Mode;

@SuppressWarnings("serial")
public class ControlPanel extends JComponent {

	private Simulation sim;
	private SimModel model;
	private GraphView view;
	private JFrame frame;
	private SimComponent comp;
	private JLabel currT;
	private JLabel currP;

	// COMPONENTS NEEDED FOR AUTOCARNOT
	private JSlider tempSlider;
	private JButton restart;
	private JButton playPause;
	private JButton moveWallIn;
	private JCheckBox colourParticlesAtActEnergy;
	private JCheckBox insulated;
	private JCheckBox particlesPushWall;

	private JCheckBox particlesDisappearAtActEnergy;
	private JSlider actEnergySlider;
	// The button to automatically create a Carnot cycle graph
	private JButton autoCarnot;
	// Continuous Carnot cycles
	private JButton autoCarnotCont;
	// Are we running an auto Carnot cycle?
	private boolean running = false;
	// Is the autoCarnot function requesting the restart?
	private boolean carnotRestart = false;
	// Is the simulation paused?
	private boolean pause = false;

	public ControlPanel(Simulation sim, JFrame frame) {
		super();
		this.sim = sim;
		this.frame = frame;

		this.model = new SimModel(sim);
		createAutoCarnot();
		this.view = new GraphView(model, autoCarnot, autoCarnotCont);
		model.addObserver(view);
		HelpScreens help = new HelpScreens(this);

		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		JPanel menuBar = new JPanel(new BorderLayout());
		JPanel UI = new JPanel(new BorderLayout());
		JPanel graphView = new JPanel(new GridBagLayout());

		String[] modes = { "Heat Engines", "Activation Energy" };
		JComboBox<String> menu = new JComboBox<String>(modes);
		menu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String m = (String) menu.getSelectedItem();
				if (m.equals("Heat Engines")) {
					model.changeMode(Mode.HeatEngines);
					colourParticlesAtActEnergy.setSelected(false);
					colourParticlesAtActEnergy.setEnabled(false);
					particlesDisappearAtActEnergy.setEnabled(false);
					particlesDisappearAtActEnergy.setSelected(false);
					actEnergySlider.setEnabled(false);
				} else if (m.equals("Activation Energy")) {
					model.changeMode(Mode.ActivationEnergy);
					colourParticlesAtActEnergy.setSelected(true);
					colourParticlesAtActEnergy.setEnabled(true);
					particlesDisappearAtActEnergy.setEnabled(true);
					actEnergySlider.setEnabled(true);
				}
			}
		});
		menu.setMinimumSize(new Dimension(160, 40));

		JButton menuHelp = help.createMenuHelp(menu);

		menuBar.add(menu, BorderLayout.CENTER);
		menuBar.add(menuHelp, BorderLayout.EAST);

		JPanel sliders = sliderBar();
		JPanel buttons = buttonBar();
		comp = new SimComponent(model, frame, currT, currP, this);

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

		graphView.setMinimumSize(new Dimension(277, frame.getHeight()));
		graphView.setPreferredSize(new Dimension(277, frame.getHeight()));

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		graphView.add(menuBar, c);

		c.fill = GridBagConstraints.BOTH;
		c.gridy = 1;
		graphView.add(view, c);

		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;

		// graphView.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		frame.add(graphView, c); // Graphs

		comp.setMinimumSize(new Dimension((int) model.getContainer().getWidth() + 305,
				(int) model.getContainer().getHeight() + 10));
		comp.setPreferredSize(new Dimension((int) model.getContainer().getWidth() + 305,
				(int) model.getContainer().getHeight() + 10));
		// c.fill = GridBagConstraints.BOTH;
		// c.ipadx = 0;
		// c.ipady = 0;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		JButton info = help.createInfoButton();
		JPanel compAndInfo = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.FIRST_LINE_START;
		c2.gridx = 0;
		c2.gridy = 0;
		c2.fill = GridBagConstraints.BOTH;
		compAndInfo.add(comp, c2);
		c2.anchor = GridBagConstraints.FIRST_LINE_END;
		c2.gridx = 1;
		c2.fill = GridBagConstraints.HORIZONTAL;
		compAndInfo.add(info, c2);

		frame.add(compAndInfo, c); // Simulation

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_END; // Stick to bottom
		c.ipadx = 20;
		c.ipady = 0;
		c.gridx = 1;
		c.gridy = 1;
		UI.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		frame.add(UI, c); // Bottom bar

		// c.anchor = GridBagConstraints.FIRST_LINE_START;
		// c.gridx = 2;
		// c.gridy = 0;
		// frame.add(info, c);
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
		// ActivationEnergy slider, label and value
		JPanel actEnergy = new JPanel(new BorderLayout());

		JLabel numParticlesLabel = new JLabel("Number of particles", SwingConstants.CENTER);
		JSlider numParticlesSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 500, 250);
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
		numParticles.setToolTipText(readFile("NumParticlesSliderTooltipHover.txt"));
		numParticlesSlider.setToolTipText(readFile("NumParticlesSliderTooltipHover.txt"));

		numParticles.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		Thread numParticlesUpdater = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					int num = model.getNumParticles();
					if (!numParticlesSlider.getValueIsAdjusting()) {
						numParticlesSlider.setValue(num);
						numParticlesValue.setText(Integer.toString(num));
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		numParticlesUpdater.start();

		JLabel actEnergyLabel = new JLabel("Activation energy", SwingConstants.CENTER);
		// JSlider actEnergySlider = new JSlider(SwingConstants.HORIZONTAL,
		// (int)model.calculateExpectedMSS(300) / 10,
		// (int)model.calculateExpectedMSS(4000) * 2,
		// (int)model.calculateExpectedMSS(300));
		actEnergySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 10);
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
		actEnergySlider.setEnabled(false);
		actEnergy.add(actEnergyLabel, BorderLayout.NORTH);
		actEnergy.add(actEnergySlider, BorderLayout.CENTER);
		actEnergy.add(actEnergyValue, BorderLayout.EAST);
		// actEnergySlider.setMaximum(maximum);
		actEnergy.setToolTipText(readFile("ActEnergySliderTooltipHover.txt"));
		actEnergySlider.setToolTipText(readFile("ActEnergySliderTooltipHover.txt"));

		actEnergy.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		tempSlider = new JSlider(SwingConstants.HORIZONTAL, 200, 4000, 300);
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

		tempComp.setToolTipText(readFile("WallTempSliderTooltipHover.txt"));
		tempSlider.setToolTipText(readFile("WallTempSliderTooltipHover.txt"));

		tempComp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		currT = new JLabel("<html>Average temperature<br>of particles: </html>");
		currT.setToolTipText(readFile("AvgTempLabelTooltipHover.txt"));
		currP = new JLabel("<html>Average pressure<br>on container: </html>");
		currP.setToolTipText(readFile("AvgPressureLabelTooltipHover.txt"));
		stats.add(currT, BorderLayout.NORTH);
		stats.add(currP, BorderLayout.SOUTH);

		JSlider fpsSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 16, 4);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(Integer.valueOf(2), new JLabel("0.5"));
		labelTable.put(Integer.valueOf(4), new JLabel("1"));
		labelTable.put(Integer.valueOf(6), new JLabel("1.5"));
		labelTable.put(Integer.valueOf(8), new JLabel("2"));
		labelTable.put(Integer.valueOf(12), new JLabel("3"));
		labelTable.put(Integer.valueOf(16), new JLabel("4"));
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
		// fpsSlider.setMinorTickSpacing(15);
		fpsSlider.setPaintTicks(true);
		fpsSlider.setPaintLabels(true);
		fps.add(fpsSlider, BorderLayout.CENTER);
		fps.add(fpsLabel, BorderLayout.NORTH);
		fps.add(fpsValue, BorderLayout.EAST);

		fps.setToolTipText(readFile("SimSpeedSliderTooltipHover.txt"));
		fpsSlider.setToolTipText(readFile("SimSpeedSliderTooltipHover.txt"));

		fps.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		sliderBar.add(stats);
		sliderBar.add(tempComp);
		// sliderBar.add(sizeParticles);
		sliderBar.add(actEnergy);
		sliderBar.add(numParticles);
		sliderBar.add(fps);

		return sliderBar;
	}

	private JPanel buttonBar() {
		JPanel playPauseRestart = new JPanel(new GridLayout(1, 2));
		JPanel buttons = new JPanel(new GridLayout(0, 1));

		playPause = new JButton("Pause");
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
		playPause.setToolTipText(readFile("PauseResumeButtonTooltipHover.txt"));

		restart = new JButton("Restart");
		restart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.restartSim();
				comp.stopWalls();
				if (pause) {
					pause = false;
					playPause.setText("Pause");
				}
				if (!carnotRestart && running) {
					autoCarnot.doClick();
				}
				carnotRestart = false;
			}
		});
		restart.setToolTipText(readFile("RestartButtonTooltipHover.txt"));

		JPanel moveWall = new JPanel(new GridLayout(1, 1));

		moveWallIn = new JButton("Move wall in");
		moveWallIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Don't do anything if the sim is paused
				if (!pause) {
					comp.moveWallInAuto(2, moveWallIn);
				}
			}
		});
		moveWallIn.setToolTipText(readFile("MoveWallInButtonTooltipHover.txt"));

		JButton moveWallOut = new JButton("Move wall out");
		moveWallOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Don't do anything if the sim is paused
				if (!pause) {
					comp.moveWallOutAuto(2, moveWallOut);
				}
			}
		});
		moveWallOut.setToolTipText(readFile("MoveWallOutButtonTooltipHover.txt"));

		moveWall.add(moveWallIn);
		moveWall.add(moveWallOut);

		insulated = new JCheckBox("Insulate the walls");
		insulated.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					model.setIsInsulated(false);
				} else {
					model.setIsInsulated(true);
				}
			}
		});
		insulated.setToolTipText(readFile("InsulateWallsCheckboxTooltipHover.txt"));

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
		colourParticlesAtActEnergy.setToolTipText(readFile("ColourParticlesCheckboxTooltipHover.txt"));
		colourParticlesAtActEnergy.setEnabled(false);

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
		particlesDisappearAtActEnergy.setToolTipText(readFile("ParticlesDisappearCheckboxTooltipHover.txt"));
		particlesDisappearAtActEnergy.setEnabled(false);

		particlesPushWall = new JCheckBox("Allow particles to push the right wall");
		particlesPushWall.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					model.setParticlesPushWall(false);
					model.getContainer().setWidthChange(0);
				} else {
					model.setParticlesPushWall(true);
				}
			}
		});
		particlesPushWall.setToolTipText(readFile("ParticlesPushCheckboxTooltipHover.txt"));

		playPauseRestart.add(restart);
		playPauseRestart.add(playPause);

		buttons.add(playPauseRestart);
		buttons.add(moveWall);
		buttons.add(insulated);
		buttons.add(colourParticlesAtActEnergy);
		buttons.add(particlesDisappearAtActEnergy);
		buttons.add(particlesPushWall);
		return buttons;
	}

	private void createAutoCarnot() {
		autoCarnot = new JButton("Single Carnot");
		autoCarnot.setToolTipText(readFile("AutoCarnotButtonTooltipHover.txt"));
		autoCarnot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autoCarnot(false);
			}
		});

		autoCarnotCont = new JButton("Const. Carnot");
		autoCarnotCont.setToolTipText(readFile("AutoCarnotConstButtonTooltipHover.txt"));
		autoCarnotCont.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autoCarnot(true);
			}
		});
	}

	private void autoCarnot(boolean continuous) {
		Container cont = model.getContainer();
		Thread t = new Thread(new Runnable() {
			public void run() {
				model.setAutoCarnot(true);
				running = true;
				autoCarnot.setText("Stop Carnot");
				autoCarnotCont.setText("Stop Carnot");
				// Number of times we've been through the loop
				int i = 0;
				while (running && ((i == 0 && !continuous) || continuous)) {
					// Insulation off, allow gas to move wall out, wall
					// starts in until halfway, high wall temp --> high
					// wall temp
					if (i == 0) {
						playPause.doClick(100);
					}
					comp.stopWalls();
					cont.setWidth(cont.getMinWidth());
					tempSlider.setValue(3000);
					if (i == 0) {
						carnotRestart = true;
						restart.doClick(100);
						view.pvResetTraces();
						view.etResetTraces();
					} else {
						view.pvAddTrace();
						view.etAddTrace();
					}
					insulated.setSelected(false);
					particlesPushWall.setSelected(true);

					double contTwoThirds = cont.getMinWidth() + 2 * (cont.getMaxWidth() - cont.getMinWidth()) / 3;
					while (cont.getWidth() < contTwoThirds && running) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (!running) {
						return;
					}

					// Insulation on, allow gas to move wall out, wall
					// starts halfway until out, high wall temp --> low
					// wall temp
					view.pvAddTrace();
					view.etAddTrace();
					insulated.setSelected(true);

					while (cont.getWidth() < cont.getMaxWidth() && running) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (!running) {
						return;
					}

					// Insulation off, compress gas, wall starts out
					// until halfway, low wall temp --> low wall temp
					comp.stopWalls();
					view.pvAddTrace();
					view.etAddTrace();
					insulated.setSelected(false);
					particlesPushWall.setSelected(false);
					cont.setWidth(cont.getMaxWidth());
					tempSlider.setValue(1000);
					moveWallIn.doClick(100);
					model.setAutoCarnotCompress(true);

					double contOneThird = cont.getMinWidth() + (cont.getMaxWidth() - cont.getMinWidth()) / 3;
					while (cont.getWidth() > contOneThird && running) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (!running) {
						return;
					}

					// Insulation on, compress gas, wall starts halfway
					// until in, low wall temp --> high wall temp
					view.pvAddTrace();
					view.etAddTrace();
					insulated.setSelected(true);

					while (cont.getWidth() > cont.getMinWidth() && running) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (!running) {
						return;
					}
					i++;
				}
				playPause.doClick(100);
				running = false;
				model.setAutoCarnot(false);
				model.setAutoCarnotCompress(false);
				autoCarnot.setText("Single Carnot");
				autoCarnotCont.setText("Const. Carnot");
			}
		});
		if (running) {
			running = false;
			model.setAutoCarnot(false);
			model.setAutoCarnotCompress(false);
			particlesPushWall.setSelected(false);
			autoCarnot.setText("Single Carnot");
			autoCarnotCont.setText("Const. Carnot");
		} else {
			t.start();
		}
	}

	public boolean isPaused() {
		return pause;
	}

	public static String readFile(String name) {
		String s = "";
		try {
			s = new String(Files.readAllBytes(Paths.get("./src/resources/" + name).toAbsolutePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	public ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = getClass().getResource("../resources/images/" + path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: ../resources/images/" + path);
			return null;
		}
	}
}
