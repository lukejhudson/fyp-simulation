package ljh590;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.TracePoint2D;
import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterAutoUnits;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DBijective;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
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

	// ACTIVATION ENERGY
	// Energy distribution chart
	private Chart2D energyDistChart;
	private ArrayList<Trace2DBijective> energyDistTraces;
	private double lowestEnergy;
	private double highestEnergy;
	private double energyDistBarWidth;

	private Chart2D bmFactChart;
	private ITrace2D bmFactTrace;

	// Which mode we are in and thus which information we need to show
	private Mode mode = Mode.HeatEngines;

	/**
	 * @param model
	 */
	public GraphView(SimModel model) {
		super();
		setLayout(new GridLayout(3, 0));
		this.model = model;
		this.cont = model.getContainer();

		createSpeedDistChart();
		createPVChart();
		createETChart();
		createEnergyDistChart();

		add(speedDistChart);
		add(pvComponents);
		add(etComponents);
	}

	private void createSpeedDistChart() {
		speedDistChart = new Chart2D();
		speedDistTrace = new Trace2DBijective();
		speedDistTrace.setTracePainter(
				new info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar(5, speedDistChart));
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
		pvAddTrace();
		pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Volume (m^2 x10^-18)"));
		pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Pressure (Pa)"));
		// pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
		IRangePolicy pvRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
		pvChart.getAxisX().setRangePolicy(pvRangePolicyX);
		IRangePolicy pvRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
		pvChart.getAxisY().setRangePolicy(pvRangePolicyY);

		pvComponents = new JPanel(new BorderLayout());
		JPanel pvButtons = new JPanel(new GridLayout(1, 0));
		JButton pvAddTrace = new JButton("Add trace");
		pvAddTrace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pvAddTrace();
			}
		});
		pvButtons.add(pvAddTrace);
		JButton pvRemoveTraces = new JButton("Remove traces");
		pvRemoveTraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pvChart.removeAllTraces();
				pvAddTrace();
			}
		});
		pvButtons.add(pvRemoveTraces);
		pvComponents.add(pvChart, BorderLayout.CENTER);
		pvComponents.add(pvButtons, BorderLayout.SOUTH);
	}
	
	private void createETChart() {
		// pvChart = new Chart2D();
		etChart = new Chart2D();
		etAddTrace();
		etChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Temperature (K)"));
		etChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Entropy (Heat transfer / temperature)"));
		// etChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// etChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
//		IRangePolicy etRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
//		etChart.getAxisX().setRangePolicy(etRangePolicyX);
//		IRangePolicy etRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
//		etChart.getAxisY().setRangePolicy(etRangePolicyY);

		etComponents = new JPanel(new BorderLayout());
		JPanel etButtons = new JPanel(new GridLayout(1, 0));
		JButton etAddTrace = new JButton("Add trace");
		etAddTrace.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				etAddTrace();
			}
		});
		etButtons.add(etAddTrace);
		JButton etRemoveTraces = new JButton("Remove traces");
		etRemoveTraces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				etChart.removeAllTraces();
				etAddTrace();
			}
		});
		etButtons.add(etRemoveTraces);
		etComponents.add(etChart, BorderLayout.CENTER);
		etComponents.add(etButtons, BorderLayout.SOUTH);
	}

	private void createEnergyDistChart() {
		energyDistChart = new Chart2D();

		energyDistTraces = new ArrayList<Trace2DBijective>();
		Trace2DBijective t;
		for (int i = 0; i < 20; i++) {
			t = new Trace2DBijective();
			t.setTracePainter(
					new info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar(5, energyDistChart));
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

	private void createBMFactChart() {
		bmFactChart = new Chart2D();
		bmFactTrace = new Trace2DSimple();
		// speedDistChart.getAxisX().setFormatter(new
		// LabelFormatterAutoUnits());
		bmFactChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Temperature"));
		bmFactChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Boltzmann Factor"));
		// pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle(""));
		// pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle(""));
		// IRangePolicy bmFactRangePolicyX = new RangePolicyFixedViewport(new
		// Range(0, pvXAxisMax));
		// bmFactChart.getAxisX().setRangePolicy(bmFactRangePolicyX);
		// IRangePolicy bmFactRangePolicyY = new RangePolicyFixedViewport(new
		// Range(0, pvYAxisMax));
		// bmFactChart.getAxisY().setRangePolicy(bmFactRangePolicyY);
		bmFactChart.addTrace(bmFactTrace);
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
				remove(energyDistChart);
				add(speedDistChart);
				add(pvComponents);
				add(etComponents);
				pvChart.removeAllTraces();
				pvAddTrace();
			}
			break;
		case ActivationEnergy:
			if (mode != Mode.ActivationEnergy) {
				mode = Mode.ActivationEnergy;
				// remove(speedDistChart);
				remove(pvComponents);
				remove(etComponents);
				add(energyDistChart);
				// add(bmFactChart);
			}
			break;
		default:
			System.out.println("DEFAULT");
			break;
		}
	}

	private void updateSpeedDistChart() {
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
			speedDistTrace.addPoint(new TracePoint2D((i + 1) * 10, ((double) barTot / speeds.size()) * 100));
			barTot = 0;
		}
	}

	private void updatePVChart() {
		if (mode == Mode.HeatEngines) {
			double pressure = model.getAverageP();
			double volume = cont.getActualVolume();

			if (model.getIsInsulated()) {
				pvTrace.setColor(Color.RED);
			} else {
				pvTrace.setColor(Color.BLUE);
			}
			pvTrace.addPoint(volume * 1E18, pressure);
		}
	}
	
	private void updateETChart() {
		if (mode == Mode.HeatEngines) {
			double tChange = model.getAverageTChange();
			double temperature = model.getAverageT();
			
			double entropy = tChange / temperature;

			if (model.getIsInsulated()) {
				etTrace.setColor(Color.RED);
			} else {
				etTrace.setColor(Color.BLUE);
			}
			etTrace.addPoint(temperature, entropy);
		}
	}
	
	private void pvAddTrace() {
		// pvTrace = new Trace2DBijective();
		pvTrace = new Trace2DSimple();
		IRangePolicy pvRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
		pvChart.getAxisX().setRangePolicy(pvRangePolicyX);
		IRangePolicy pvRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
		pvChart.getAxisY().setRangePolicy(pvRangePolicyY);
		pvChart.addTrace(pvTrace);
		pvTrace.setName("");
	}
	
	private void etAddTrace() {
		// etTrace = new Trace2DBijective();
		etTrace = new Trace2DSimple();
//		IRangePolicy etRangePolicyX = new RangePolicyFixedViewport(new Range(0, pvXAxisMax));
//		etChart.getAxisX().setRangePolicy(etRangePolicyX);
//		IRangePolicy etRangePolicyY = new RangePolicyFixedViewport(new Range(0, pvYAxisMax));
//		etChart.getAxisY().setRangePolicy(etRangePolicyY);
		etChart.addTrace(etTrace);
		etTrace.setName("");
	}

	private void updateEnergyDistChart() {
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
			energyDistTraces.get(i).addPoint(new TracePoint2D((i + 1) * 10, ((double) barTot / energies.size()) * 100));
			barTot = 0;
		}
	}

	private void updateBMFactChart() {
		if (mode == Mode.ActivationEnergy) {
			double temp = model.getAverageT();
			double factor = -(model.getActualActivationEnergy() / (1.38E-23 * temp));
			double bmf = Math.exp(factor);
			bmFactTrace.setColor(Color.BLUE);
			bmFactTrace.addPoint(temp, bmf);
		}
	}
}
