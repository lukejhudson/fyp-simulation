package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.TracePoint2D;
import info.monitorenter.gui.chart.ZoomableChart;
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

	// HEAT ENGINES
	// Speed distribution chart
	private Chart2D speedDistChart;
	private ITrace2D speedDistTrace;
	private double lowestSpeed;
	private double highestSpeed;
	private double speedDistBarWidth;
	// Pressure vs volume chart
	private ZoomableChart pvChart;
	private ITrace2D pvTrace;
	private JPanel pvComponents;
	private double pvXAxisMax = 6;// 0.000000000000000006;
	private double pvYAxisMax = 30;
	// Entropy vs temperature chart
	private Chart2D etChart;
	private ITrace2D etTrace;
	private JPanel etComponents;
	private double maxEntropy = 0;
	private double minEntropy = 0;
	// Button to create Carnot cycle
	private JButton autoCarnot;
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

	// Boltzmann factor vs reactions/sec
	private Chart2D bmfRsChart;
	private ITrace2D bmfRsTrace;

	private Chart2D bmFactChart;
	private ITrace2D bmFactTrace;

	// Used to delay the drawing of the PV and ET charts before they have good
	// data
	private int updateIterations = 0;

	// Which mode we are in and thus which information we need to show
	private Mode mode = Mode.HeatEngines;

	/**
	 * @param model
	 * @param autoCarnot
	 */
	public GraphView(SimModel model, JButton autoCarnot) {
		super();
		setLayout(new BorderLayout());
		this.model = model;
		this.cont = model.getContainer();
		this.autoCarnot = autoCarnot;

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
		createETChart();
		createPVChart();
	}

	private void createHeatEnginePanel() {
		heatEnginePanel = new JPanel(new BorderLayout());

		heatEngineGraphs = new JPanel(new GridLayout(3, 0));
		heatEngineGraphs.add(speedDistChart);
		heatEngineGraphs.add(pvComponents);
		heatEngineGraphs.add(etComponents);

		heatEnginePanel.add(heatEngineGraphs, BorderLayout.CENTER);
		heatEnginePanel.add(autoCarnot, BorderLayout.SOUTH);
	}

	private void createActivationEnergyComponents() {
		createEnergyDistChart();
		createBMFRsChart();
	}

	private void createActivationEnergyPanel() {
		activationEnergyPanel = new JPanel(new GridLayout(3, 0));
		activationEnergyPanel.add(speedDistChart);
		activationEnergyPanel.add(energyDistChart);
		activationEnergyPanel.add(bmfRsChart);
	}

	private void createSpeedDistChart() {
		speedDistChart = new Chart2D();
		speedDistChart.setToolTipText(ControlPanel.readFile("/todo/SpeedDistTooltipHover.txt"));
		speedDistTrace = new Trace2DBijective();
		speedDistTrace.setTracePainter(new TracePainterVerticalBar(5, speedDistChart));
		speedDistTrace.setName("Speed distribution");
		// Add the speedDistTrace to the chart. This has to be done before
		// adding points (deadlock prevention):
		speedDistChart.addTrace(speedDistTrace);
		// speedDistChart.getAxisX().setFormatter(new
		// LabelFormatterAutoUnits());
		speedDistChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("                                       Speed"));
		speedDistChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Percentage of Particles"));

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
		// pvChart = new Chart2D();
		pvChart = new ZoomableChart();
		pvChart.setToolTipText(ControlPanel.readFile("/todo/PVChartTooltipHover.txt"));
		pvAddTrace();
		pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Volume (m^2 x10^-18)"));
		pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Pressure (Pa)"));
		// pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
		IRangePolicy pvRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
		pvChart.getAxisX().setRangePolicy(pvRangePolicyX);
		IRangePolicy pvRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
		pvChart.getAxisY().setRangePolicy(pvRangePolicyY);
		// pvChart.setUseAntialiasing(true);
		pvComponents = new JPanel(new BorderLayout());
		JPanel pvButtons = new JPanel(new GridLayout(1, 0));
		JButton pvAddTrace = new JButton("Add trace");
		pvAddTrace.setToolTipText(ControlPanel.readFile("AddTraceButtonTooltipHover.txt"));
		pvAddTrace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pvAddTrace();
			}
		});
		pvButtons.add(pvAddTrace);
		JButton pvRemoveTraces = new JButton("Remove traces");
		pvRemoveTraces.setToolTipText(ControlPanel.readFile("RemoveTracesButtonTooltipHover.txt"));
		pvRemoveTraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pvResetTraces();
			}
		});
		pvButtons.add(pvRemoveTraces);
		pvComponents.add(pvChart, BorderLayout.CENTER);
		pvComponents.add(pvButtons, BorderLayout.SOUTH);

		Thread pvThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (mode == Mode.HeatEngines) {// &&
													// model.getContainer().getWidthChange()
													// != 0) {
//						if (updateIterations > -2) {
							updatePVChart();
							updateETChart();
//						}
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

	private void createETChart() {
		// pvChart = new Chart2D();
		etChart = new Chart2D();
		etChart.setToolTipText(ControlPanel.readFile("/todo/ETChartTooltipHover.txt"));
		etAddTrace();
		etChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Entropy (Heat transfer / temperature)"));
		etChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Temperature (K)"));
		// etChart.getAxisX().setPaintScale(false);
		// etChart.getAxisY().setPaintScale(false);
		// etChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// etChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
//		IRangePolicy etRangePolicyX = new RangePolicyFixedViewport(new Range(-0.001, 0.001));
//		etChart.getAxisX().setRangePolicy(etRangePolicyX);
		IRangePolicy etRangePolicyY = new RangePolicyFixedViewport(new Range(0, 5000));
		etChart.getAxisY().setRangePolicy(etRangePolicyY);

		etComponents = new JPanel(new BorderLayout());
		JPanel etButtons = new JPanel(new GridLayout(1, 0));
		JButton etAddTrace = new JButton("Add trace");
		etAddTrace.setToolTipText(ControlPanel.readFile("AddTraceButtonTooltipHover.txt"));
		etAddTrace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				etAddTrace();
			}
		});
		etButtons.add(etAddTrace);
		JButton etRemoveTraces = new JButton("Remove traces");
		etRemoveTraces.setToolTipText(ControlPanel.readFile("RemoveTracesButtonTooltipHover.txt"));
		etRemoveTraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				etResetTraces();
			}
		});
		etButtons.add(etRemoveTraces);
		etComponents.add(etChart, BorderLayout.CENTER);
		etComponents.add(etButtons, BorderLayout.SOUTH);
	}

	private void createEnergyDistChart() {
		energyDistChart = new Chart2D();
		energyDistChart.setToolTipText(ControlPanel.readFile("/todo/EnergyDistTooltipHover.txt"));

		energyDistTraces = new ArrayList<Trace2DBijective>();
		Trace2DBijective t;
		for (int i = 0; i < 20; i++) {
			t = new Trace2DBijective();
			t.setTracePainter(new TracePainterVerticalBar(5, energyDistChart));
			if (i == 0) {
				t.setName("Maxwell-Boltzmann Distribution");
			} else {
				t.setName("");
			}
			energyDistTraces.add(t);
			energyDistChart.addTrace(t);
		}
		// Add the energyDistTrace to the chart. This has to be done before
		// adding points (deadlock prevention):
		energyDistChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("                                       Energy"));
		energyDistChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Percentage of Particles"));

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
		bmfRsChart = new Chart2D();
		bmfRsChart.setToolTipText(ControlPanel.readFile("/todo/BMFRsChartTooltipHover.txt"));
		bmfRsTrace = new Trace2DSimple();
		bmfRsChart.addTrace(bmfRsTrace);
		bmfRsTrace.setTracePainter(new TracePainterDisc());
		bmfRsChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Reactions/iteration"));
		bmfRsChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Boltzmann Factor"));

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
			pvChart.zoomAll();
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
			updateETChart();
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

	private void updateETChart() {
		if (etChart.getTraces() == null) {
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
//				if (model.isAutoCarnot()) {
//					if (model.isAutoCarnotCompress()) {
//						entropy = minEntropy;
//					} else {
//						entropy = maxEntropy;
//					}
//				}
				etTrace.setColor(Color.RED);
			} else {
				etTrace.setColor(Color.BLUE);
			}
//			System.out.println(model.isAutoCarnot() + ", " + model.isAutoCarnotCompress() + ", " + entropy);
//			if (entropy > 0.0008) {
//				entropy = 0.0008;
//			} else if (entropy < -0.0008) {
//				entropy = -0.0008;
//			}
			try {
				etTrace.addPoint(entropy, temperature);
			} catch (Exception e) {
				System.err.println("Unable to plot ST point");
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
		pvChart.addTrace(pvTrace);
		pvTrace.setName("");
	}

	public void pvResetTraces() {
		pvChart.removeAllTraces();
		pvAddTrace();
	}

	public void etAddTrace() {
		// etTrace = new Trace2DBijective();
		etTrace = new Trace2DSimple();
		// IRangePolicy etRangePolicyX = new RangePolicyFixedViewport(new
		// Range(0, pvXAxisMax));
		// etChart.getAxisX().setRangePolicy(etRangePolicyX);
		// IRangePolicy etRangePolicyY = new RangePolicyFixedViewport(new
		// Range(0, pvYAxisMax));
		// etChart.getAxisY().setRangePolicy(etRangePolicyY);
		etChart.addTrace(etTrace);
		etTrace.setName("");
	}

	public void etResetTraces() {
		maxEntropy = 0;
		minEntropy = 0;
		etChart.removeAllTraces();
		etAddTrace();
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
		if (bmfRsChart.getTraces() == null) {
			return;
		}
		if (mode == Mode.ActivationEnergy) {
			double temp = model.getAverageT();
			double factor = -(model.getActualActivationEnergy() / (1.38E-23 * temp));
			double bmf = Math.exp(factor);

			double noReactions = model.getAverageNoReactions();

			try {
				bmfRsTrace.addPoint(noReactions, bmf);
			} catch (Exception e) {
				System.err.println("Unable to plot Boltzmann-Reactions/sec point");
			}
		}
	}
}
