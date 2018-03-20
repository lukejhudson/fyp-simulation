package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.TracePoint2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DBijective;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar;
import info.monitorenter.util.Range;

public class GraphView extends JComponent implements Observer {

	public enum Mode {
		HeatEngines, ActivationEnergy
	};

	private static final long serialVersionUID = 1L;
	private SimModel model;
	private Container cont;
	private ControlPanel controlPanel;

	// HEAT ENGINES
	// Speed distribution chart
	private Chart2D speedDistChart;
	private ITrace2D speedDistTrace;
	private double lowestSpeed;
	private double highestSpeed;
	private double speedDistBarWidth;
	// Pressure vs volume chart
	private Chart2D pvChart;
	private ITrace2D pvTrace;
	private JPanel pvComponents;
	private double pvXAxisMax = 6;// 0.000000000000000006;
	private double pvYAxisMax = 30;
	// Entropy vs temperature chart
	private Chart2D tsChart;
	private ITrace2D tsTrace;
	private JPanel tsComponents;
	private double maxEntropy = 0;
	private double minEntropy = 0;
	// Panel containing the auto Carnot buttons
	private JPanel autoCarnotPanel;
	// Button to create Carnot cycle
	private JButton autoCarnot;
	// Button to create continuous Carnot cycles
	private JButton autoCarnotCont;
	// Panel containing the graphs
	private JPanel heatEngineGraphs;
	// Panel containing the heat engine components
	private JPanel heatEnginePanel;

	// ACTIVATION ENERGY
	// Energy distribution chart
	private Chart2D energyDistChart;
	private ArrayList<Trace2DBijective> energyDistTraces;
	private double lowestEnergy;
	private double highestEnergy;
	private double energyDistBarWidth;
	// Panel containing the activation energy components
	private JPanel activationEnergyPanel;

	// Boltzmann factor vs reactions/iteration
	private Chart2D bfrChart;
	private ITrace2D bfrTrace;
	private double prevTemp = 0;
	private JPanel bfrComponents;

	private Chart2D bmFactChart;
	private ITrace2D bmFactTrace;

	// Buttons which need to be visually pressed by the automatic Carnot cycle
	private JButton addTraces;
	private JButton removeTraces;

	// Used to delay the drawing of the PV and ET charts before they have good
	// data
	private int updateIterations = 0;

	// Which mode we are in and thus which information we need to show
	private Mode mode = Mode.HeatEngines;

	public GraphView(SimModel model, JButton autoCarnot, JButton autoCarnotCont, ControlPanel controlPanel) {
		super();
		setLayout(new BorderLayout());
		this.model = model;
		this.cont = model.getContainer();
		this.autoCarnot = autoCarnot;
		this.autoCarnotCont = autoCarnotCont;
		this.controlPanel = controlPanel;

		createHeatEngineComponents();
		createActivationEnergyComponents();

		addHeatEnginePanel();
	}

	private void addHeatEnginePanel() {
		createHeatEnginePanel();
		add(heatEnginePanel);
		heatEnginePanel.updateUI();
	}

	private void removeHeatEnginePanel() {
		remove(heatEnginePanel);
	}

	private void addActivationEnergyPanel() {
		createActivationEnergyPanel();
		add(activationEnergyPanel);
		activationEnergyPanel.updateUI();
	}

	private void removeActivationEnergyPanel() {
		remove(activationEnergyPanel);
	}

	private void createHeatEngineComponents() {
		createSpeedDistChart();
		createTSChart();
		createPVChart();
	}

	private void createHeatEnginePanel() {
		heatEnginePanel = new JPanel(new BorderLayout());

		heatEngineGraphs = new JPanel(new GridLayout(3, 0));
		heatEngineGraphs.add(speedDistChart);
		heatEngineGraphs.add(pvComponents);
		heatEngineGraphs.add(tsComponents);

		autoCarnotPanel = new JPanel(new GridLayout(0, 2));
		autoCarnotPanel.add(autoCarnot);
		autoCarnotPanel.add(autoCarnotCont);

		heatEnginePanel.add(heatEngineGraphs, BorderLayout.CENTER);
		heatEnginePanel.add(autoCarnotPanel, BorderLayout.SOUTH);
	}

	private void createActivationEnergyComponents() {
		createEnergyDistChart();
		createBMFRsChart();
	}

	private void createActivationEnergyPanel() {
		activationEnergyPanel = new JPanel(new GridLayout(3, 0));
		activationEnergyPanel.add(speedDistChart);
		activationEnergyPanel.add(energyDistChart);
		activationEnergyPanel.add(bfrComponents);
	}

	private void createSpeedDistChart() {
		speedDistChart = new Chart2D();
		speedDistChart.setToolTipText(controlPanel.readFile("tooltips/SpeedDist.txt"));
		speedDistTrace = new Trace2DBijective();
		speedDistTrace.setTracePainter(new TracePainterVerticalBar(5, speedDistChart));
		speedDistTrace.setName("");
		// Add the speedDistTrace to the chart. This has to be done before
		// adding points (deadlock prevention):
		speedDistChart.addTrace(speedDistTrace);
		// speedDistChart.getAxisX().setFormatter(new
		// LabelFormatterAutoUnits());
		speedDistChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Percentage of Particles vs Speed"));
		speedDistChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));

		IRangePolicy speedDistRangePolicyX = new RangePolicyFixedViewport(new Range(0, 200));
		speedDistChart.getAxisX().setRangePolicy(speedDistRangePolicyX);
		IRangePolicy speedDistRangePolicyY = new RangePolicyFixedViewport(new Range(0, 70));
		speedDistChart.getAxisY().setRangePolicy(speedDistRangePolicyY);
		speedDistChart.getAxisX().setPaintScale(false);
		speedDistChart.getAxisY().setPaintScale(false);
		// Stops y-axis flickering
		speedDistChart.setMinPaintLatency(5000);

		lowestSpeed = Math.sqrt(model.calculateExpectedMSS(300)) / 10;
		highestSpeed = Math.sqrt(model.calculateExpectedMSS(4000)) * 5;
		speedDistBarWidth = (highestSpeed - lowestSpeed) / 20;

		Thread speedDistThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					// if (mode == Mode.HeatEngines) {
					updateSpeedDistChart();
					// }
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		speedDistThread.start();
	}

	private void createPVChart() {
		pvChart = new Chart2D();
		pvChart.setToolTipText(controlPanel.readFile("tooltips/PVChart.txt"));
		pvAddTrace();
		pvChart.getAxisX().getAxisTitle().setTitle("Pressure (Pa) vs Volume (m^2 x10^-18)");
		pvChart.getAxisY().getAxisTitle().setTitle("");
		// pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
		IRangePolicy pvRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
		pvChart.getAxisX().setRangePolicy(pvRangePolicyX);
		IRangePolicy pvRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
		pvChart.getAxisY().setRangePolicy(pvRangePolicyY);
		pvChart.setUseAntialiasing(true);
		pvComponents = new JPanel(new BorderLayout());
		pvComponents.add(pvChart, BorderLayout.CENTER);

		Thread pvThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (mode == Mode.HeatEngines) {// &&
													// model.getContainer().getWidthChange()
													// != 0) {
						// if (updateIterations > -2) {
						updatePVChart();
						updateTSChart();
						// }
					}
					try {
						updateIterations++;
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		pvThread.start();
	}

	private void createTSChart() {
		// pvChart = new Chart2D();
		tsChart = new Chart2D();
		tsChart.setToolTipText(controlPanel.readFile("tooltips/TSChart.txt"));
		tsAddTrace();
		tsChart.getAxisX().getAxisTitle().setTitle("Temperature (K) vs Entropy");
		tsChart.getAxisY().getAxisTitle().setTitle("");
		// tsChart.getAxisX().setPaintScale(false);
		// tsChart.getAxisY().setPaintScale(false);
		// tsChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// tsChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
		// IRangePolicy etRangePolicyX = new RangePolicyFixedViewport(new
		// Range(-0.001, 0.001));
		// tsChart.getAxisX().setRangePolicy(etRangePolicyX);
		IRangePolicy etRangePolicyY = new RangePolicyFixedViewport(new Range(0, 5000));
		tsChart.getAxisY().setRangePolicy(etRangePolicyY);
		tsChart.setUseAntialiasing(true);

		tsComponents = new JPanel(new BorderLayout());
		JPanel tsButtons = new JPanel(new GridLayout(1, 0));
		addTraces = new JButton("Add Traces");
		addTraces.setFont(new Font(null, Font.BOLD, 10));
		addTraces.setToolTipText(controlPanel.readFile("tooltips/AddTracesButton.txt"));
		addTraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pvAddTrace();
				tsAddTrace();
			}
		});
		tsButtons.add(addTraces);
		removeTraces = new JButton("Remove Traces");
		removeTraces.setFont(new Font(null, Font.BOLD, 10));
		removeTraces.setToolTipText(controlPanel.readFile("tooltips/RemoveTracesButton.txt"));
		removeTraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pvRemoveTraces();
				tsRemoveTraces();
			}
		});
		tsButtons.add(removeTraces);
		tsComponents.add(tsChart, BorderLayout.CENTER);
		tsComponents.add(tsButtons, BorderLayout.SOUTH);
	}

	private void createEnergyDistChart() {
		energyDistChart = new Chart2D();
		energyDistChart.setToolTipText(controlPanel.readFile("tooltips/EnergyDist.txt"));

		energyDistTraces = new ArrayList<Trace2DBijective>();
		Trace2DBijective t;
		for (int i = 0; i < 20; i++) {
			t = new Trace2DBijective();
			t.setTracePainter(new TracePainterVerticalBar(5, energyDistChart));
			if (i == 0) {
				t.setName("");
			} else {
				t.setName("");
			}
			energyDistTraces.add(t);
			energyDistChart.addTrace(t);
		}
		// Add the energyDistTrace to the chart. This has to be done before
		// adding points (deadlock prevention):
		energyDistChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Percentage of Particles vs Energy"));
		energyDistChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));

		IRangePolicy energyDistRangePolicyX = new RangePolicyFixedViewport(new Range(0, 200));
		energyDistChart.getAxisX().setRangePolicy(energyDistRangePolicyX);
		IRangePolicy energyDistRangePolicyY = new RangePolicyFixedViewport(new Range(0, 80));
		energyDistChart.getAxisY().setRangePolicy(energyDistRangePolicyY);
		energyDistChart.getAxisX().setPaintScale(false);
		energyDistChart.getAxisY().setPaintScale(false);
		// Stops y-axis flickering
		energyDistChart.setMinPaintLatency(5000);

		lowestEnergy = model.calculateExpectedMSS(300) / 10;
		highestEnergy = model.calculateExpectedMSS(4000) * 2;
		energyDistBarWidth = (highestEnergy - lowestEnergy) / 20;

		Thread energyDistThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (mode == Mode.ActivationEnergy) {
						updateEnergyDistChart();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		energyDistThread.start();
	}

	private void createBMFRsChart() {
		bfrChart = new Chart2D();
		bfrChart.setToolTipText(controlPanel.readFile("tooltips/BFRChart.txt"));
		bfrTrace = new Trace2DSimple();
		bfrTrace.setName("");
		bfrChart.addTrace(bfrTrace);
		bfrTrace.setTracePainter(new TracePainterDisc());
		bfrChart.getAxisX()
				.setAxisTitle(new IAxis.AxisTitle("                   Boltzmann Factor vs Reactions/iteration"));
		bfrChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));

		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					updateBMFRsChart();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();

		JButton bfrClear = new JButton("Clear Graph");
		bfrClear.addActionListener(e -> bfrClearChart());
		bfrClear.setToolTipText(controlPanel.readFile("tooltips/BFRClearChart.txt"));

		bfrComponents = new JPanel(new BorderLayout());
		bfrComponents.add(bfrChart, BorderLayout.CENTER);
		bfrComponents.add(bfrClear, BorderLayout.SOUTH);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		switch (SimModel.Changed.valueOf(arg1.toString())) {
		case NumParticles:
			break;
		case ParticleSize:
			break;
		case Restart:
			speedDistTrace.removeAllPoints();
			IRangePolicy pvRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
			pvChart.getAxisX().setRangePolicy(pvRangePolicyX);
			IRangePolicy pvRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
			pvChart.getAxisY().setRangePolicy(pvRangePolicyY);
			// pvChart.removeAllTraces();
			// pvAddTrace();
			updateIterations = 0;
			break;
		case T:
			// updateBMFactChart();
			break;
		case WallMoved:
			updatePVChart();
			updateTSChart();
			break;
		case HeatEngines:
			if (mode != Mode.HeatEngines) {
				mode = Mode.HeatEngines;
				removeActivationEnergyPanel();
				addHeatEnginePanel();
				pvChart.removeAllTraces();
				pvAddTrace();
			}
			break;
		case ActivationEnergy:
			if (mode != Mode.ActivationEnergy) {
				mode = Mode.ActivationEnergy;
				removeHeatEnginePanel();
				addActivationEnergyPanel();
			}
			break;
		default:
			System.out.println("DEFAULT");
			break;
		}
	}

	private void updateSpeedDistChart() {
		if (speedDistChart.getTraces() == null) {
			return;
		}
		ArrayList<Double> speeds = model.getSpeeds();
		speeds.sort((a, b) -> (a > b) ? 1 : (a < b) ? -1 : 0);
		int barTot = 0;

		for (int i = 0; i < 20; i++) {
			for (double speed : speeds) {
				if (speed >= (lowestSpeed + i * speedDistBarWidth)
						&& speed < (lowestSpeed + (i + 1) * speedDistBarWidth)) {
					barTot++;
				}
			}
			try {
				speedDistTrace.addPoint(new TracePoint2D((i + 1) * 10, ((double) barTot / speeds.size()) * 100));
			} catch (Exception e) {
				System.err.println("Unable to plot speed distriubtion bar");
			}
			barTot = 0;
		}
	}

	private void updatePVChart() {
		if (pvChart.getTraces() == null) {
			return;
		}
		if (mode == Mode.HeatEngines) {
			double pressure = model.getAverageP();
			double volume = cont.getActualVolume();

			if (model.getIsInsulated()) {
				pvTrace.setColor(Color.RED);
			} else {
				pvTrace.setColor(Color.BLUE);
			}
			try {
				pvTrace.addPoint(volume * 1E18, pressure);
			} catch (Exception e) {
				System.err.println("Unable to plot PV point");
			}
		}
	}

	private void updateTSChart() {
		if (tsChart.getTraces() == null) {
			return;
		}
		if (mode == Mode.HeatEngines) {
			double temperature = model.getAverageT();
			double entropy = model.getEntropy();

			if (entropy > maxEntropy) {
				maxEntropy = entropy;
			} else if (entropy < minEntropy) {
				minEntropy = entropy;
			}

			if (model.getIsInsulated()) {
				// if (model.isAutoCarnot()) {
				// if (model.isAutoCarnotCompress()) {
				// entropy = minEntropy;
				// } else {
				// entropy = maxEntropy;
				// }
				// }
				tsTrace.setColor(Color.RED);
			} else {
				tsTrace.setColor(Color.BLUE);
			}
			// System.out.println(model.isAutoCarnot() + ", " +
			// model.isAutoCarnotCompress() + ", " + entropy);
			// if (entropy > 0.0008) {
			// entropy = 0.0008;
			// } else if (entropy < -0.0008) {
			// entropy = -0.0008;
			// }
			try {
				tsTrace.addPoint(entropy, temperature);
			} catch (Exception e) {
				System.err.println("Unable to plot TS point");
			}
		}
	}

	public void pvAddTrace() {
		// pvTrace = new Trace2DBijective();
		pvTrace = new Trace2DSimple();
		IRangePolicy pvRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
		pvChart.getAxisX().setRangePolicy(pvRangePolicyX);
		IRangePolicy pvRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
		pvChart.getAxisY().setRangePolicy(pvRangePolicyY);
		if (pvChart.getTraces().size() >= 30) {
			pvChart.removeTrace(pvChart.getTraces().first());
		}
		pvChart.addTrace(pvTrace);
		pvTrace.setName("");
	}

	public void pvRemoveTraces() {
		pvChart.removeAllTraces();
		pvAddTrace();
	}

	public void tsAddTrace() {
		// tsTrace = new Trace2DBijective();
		tsTrace = new Trace2DSimple();
		// IRangePolicy etRangePolicyX = new RangePolicyFixedViewport(new
		// Range(0, pvXAxisMax));
		// tsChart.getAxisX().setRangePolicy(etRangePolicyX);
		// IRangePolicy etRangePolicyY = new RangePolicyFixedViewport(new
		// Range(0, pvYAxisMax));
		// tsChart.getAxisY().setRangePolicy(etRangePolicyY);
		if (tsChart.getTraces().size() >= 30) {
			tsChart.removeTrace(tsChart.getTraces().first());
		}
		tsChart.addTrace(tsTrace);
		tsTrace.setName("");
	}

	public void tsRemoveTraces() {
		maxEntropy = 0;
		minEntropy = 0;
		tsChart.removeAllTraces();
		tsAddTrace();
	}

	private void updateEnergyDistChart() {
		if (energyDistChart.getTraces() == null) {
			return;
		}
		ArrayList<Double> energies = model.getEnergies();
		energies.sort((a, b) -> (a > b) ? 1 : (a < b) ? -1 : 0);
		int barTot = 0;

		for (int i = 0; i < 20; i++) {
			for (double e : energies) {
				if (e >= (lowestEnergy + i * energyDistBarWidth) && e < (lowestEnergy + (i + 1) * energyDistBarWidth)) {
					barTot++;
				}
			}
			if ((lowestEnergy + i * energyDistBarWidth) > model.getActivationEnergy()) {
				energyDistTraces.get(i).setColor(Color.RED);
			} else {
				energyDistTraces.get(i).setColor(Color.BLACK);
			}
			try {
				energyDistTraces.get(i)
						.addPoint(new TracePoint2D((i + 1) * 10, ((double) barTot / energies.size()) * 100));
			} catch (Exception e) {
				System.err.println("Unable to plot energy distribution bar");
			}
			barTot = 0;
		}
	}

	private void updateBMFRsChart() {
		if (bfrChart.getTraces() == null) {
			return;
		}
		if (mode == Mode.ActivationEnergy) {
			double temp = model.getAverageT();
			double factor = -(model.getActualActivationEnergy() / (1.38E-23 * temp));
			double bmf = Math.exp(factor);

			double noReactions = model.getAverageNoReactions();

			try {
				if (temp != prevTemp) {
					bfrTrace.addPoint(noReactions, bmf);
				}
				prevTemp = temp;
			} catch (Exception e) {
				System.err.println("Unable to plot Boltzmann-Reactions/Iteration point");
			}
		}
	}

	public void bfrClearChart() {
		bfrChart.removeAllTraces();
		bfrTrace = new Trace2DSimple();
		bfrChart.addTrace(bfrTrace);
		bfrTrace.setTracePainter(new TracePainterDisc());
	}

	/**
	 * Used by the automatic Carnot cycle to visually press the "Add Traces"
	 * button.
	 */
	public void pressAddTraces() {
		addTraces.doClick(50);
	}

	/**
	 * Used by the automatic Carnot cycle to visually press the "Remove Traces"
	 * button.
	 */
	public void pressRemoveTraces() {
		removeTraces.doClick(50);
	}
}
