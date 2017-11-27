package ljh590;

import java.awt.BorderLayout;
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
import info.monitorenter.gui.chart.labelformatters.LabelFormatterAutoUnits;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DBijective;
import info.monitorenter.util.Range;

public class GraphView extends JComponent implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SimModel model;
	private Container cont;

	private Chart2D speedDistChart;
	private ITrace2D speedDistTrace;
	private double lowestSpeed;
	private double highestSpeed;
	private double speedDistBarWidth;

	private Chart2D pvChart;
	private ITrace2D pvTrace;

	/**
	 * @param model
	 */
	public GraphView(SimModel model) {
		super();
		setLayout(new GridLayout(2, 0));
		this.model = model;
		this.cont = model.getContainer();
		this.speedDistChart = new Chart2D();
		this.speedDistTrace = new Trace2DBijective();
		speedDistTrace.setTracePainter(
				new info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar(5, speedDistChart));
		speedDistTrace.setName("Speed distribution");
		// Add the speedDistTrace to the chart. This has to be done before
		// adding points (deadlock prevention):
		speedDistChart.addTrace(speedDistTrace);
		speedDistChart.getAxisX().setFormatter(new LabelFormatterAutoUnits());
		speedDistChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("                                       Speed"));
		speedDistChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Percentage of Particles"));

		IRangePolicy rangePolicyX = new RangePolicyFixedViewport(new Range(0, 200));
		speedDistChart.getAxisX().setRangePolicy(rangePolicyX);
		IRangePolicy rangePolicyY = new RangePolicyFixedViewport(new Range(0, 45));
		speedDistChart.getAxisY().setRangePolicy(rangePolicyY);
		speedDistChart.getAxisX().setPaintScale(false);
		speedDistChart.getAxisY().setPaintScale(false);

		lowestSpeed = Math.sqrt(model.calculateExpectedMSS(300)) / 10;
		highestSpeed = Math.sqrt(model.calculateExpectedMSS(4000)) * 2;
		speedDistBarWidth = (highestSpeed - lowestSpeed) / 20;
		// System.out.println(lowestSpeed + "\n" + highestSpeed + "\n" +
		// speedDistBarWidth);

		// IAxis<IAxisScalePolicy> xAxis = (IAxis<IAxisScalePolicy>)
		// speedDistChart.getAxisX();
		// xAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks());
		// xAxis.setMajorTickSpacing(5);
		// xAxis.setMinorTickSpacing(1);
		// xAxis.setStartMajorTick(true);
		// IAxis<IAxisScalePolicy> yAxis =
		// (IAxis<IAxisScalePolicy>)speedDistChart.getAxisY();
		// yAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks());
		// yAxis.setMajorTickSpacing(5);
		// yAxis.setMinorTickSpacing(1);
		// yAxis.setStartMajorTick(true);

		this.pvChart = new Chart2D();
		pvAddTrace();
		// speedDistChart.getAxisX().setFormatter(new
		// LabelFormatterAutoUnits());
		pvChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("Volume"));
		pvChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Pressure"));
		//
		// IRangePolicy rangePolicyX = new RangePolicyFixedViewport(new Range(0,
		// 200));
		// speedDistChart.getAxisX().setRangePolicy(rangePolicyX);
		// IRangePolicy rangePolicyY = new RangePolicyFixedViewport(new Range(0,
		// 35));
		// speedDistChart.getAxisY().setRangePolicy(rangePolicyY);
		// speedDistChart.getAxisX().setPaintScale(false);
		// speedDistChart.getAxisY().setPaintScale(false);

		JPanel pvComponents = new JPanel(new BorderLayout());
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

		add(speedDistChart);
		add(pvComponents);

		Thread speedDistThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					updateSpeedDistChart();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		speedDistThread.start();

		// Thread pvThread = new Thread(new Runnable() {
		// public void run() {
		// while (true) {
		// updatePVChart();
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// });
		// pvThread.start();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		switch (SimModel.Changed.valueOf(arg1.toString())) {
		case NumParticles:
			// System.out.println("NumParticles");
			break;
		case ParticleSize:
			// System.out.println("ParticleSize");
			break;
		case Restart:
			// System.out.println("Restart");
			speedDistTrace.removeAllPoints();
//			pvChart.removeAllTraces();
//			pvAddTrace();
			break;
		case T:
			// System.out.println("T");
			break;
		case WallMoved:
			updatePVChart();
			break;
		default:
			// System.out.println("DEFAULT");
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
			// System.out.println("\nbarTot: " + ((double) barTot /
			// speeds.size()) * 100);
			// System.out.println("i: " + i);
			// speedDistTrace.addPoint((i + 1) * 10, ((double) barTot /
			// speeds.size()) * 100);
			speedDistTrace.addPoint(new TracePoint2D((i + 1) * 10, ((double) barTot / speeds.size()) * 100));
			barTot = 0;
		}
	}

	private void updatePVChart() {
		double pressure = model.getAverageP();
		double volume = cont.getVolume();

		pvTrace.addPoint(volume, pressure);
	}
	
	private void pvAddTrace() {
		pvTrace = new Trace2DBijective();
		pvChart.addTrace(pvTrace);
		pvTrace.setName("");
	}
}
