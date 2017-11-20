package ljh590;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterAutoUnits;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DReplacing;
import info.monitorenter.util.Range;

public class GraphView extends JComponent implements Observer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SimModel model;
	private Chart2D speedDistChart;
	private ITrace2D speedDistTrace;

	/**
	 * @param model
	 */
	public GraphView(SimModel model) {
		super();
		setLayout(new BorderLayout());
		this.model = model;
		this.speedDistChart = new Chart2D();
		this.speedDistTrace = new Trace2DReplacing();
		speedDistTrace.setTracePainter(new info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar(10, speedDistChart));
		speedDistTrace.setName("Speed distribution");
		// Add the speedDistTrace to the chart. This has to be done before adding points
		// (deadlock prevention):
		speedDistChart.addTrace(speedDistTrace);
		// Add all points, as it is static:
		// chart.getAxisX().setMinorTickSpacing(1);
		// chart.getAxisX().setMajorTickSpacing(2);
		// chart.getAxisX().setRange(new Range(0, 10));
		speedDistChart.getAxisX().setFormatter(new LabelFormatterAutoUnits());
		// chart.getAxisY().setPaintScale(false);
		speedDistChart.getAxisX()
				.setAxisTitle(new IAxis.AxisTitle("                                       Speed"));
		speedDistChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("Percentage of Particles"));
		
		IRangePolicy rangePolicyX = new RangePolicyFixedViewport(new Range(0, 100));
		speedDistChart.getAxisX().setRangePolicy(rangePolicyX);
		IRangePolicy rangePolicyY = new RangePolicyFixedViewport(new Range(0, 100));
		speedDistChart.getAxisY().setRangePolicy(rangePolicyY);
		
//		IAxis<IAxisScalePolicy> xAxis = (IAxis<IAxisScalePolicy>)speedDistChart.getAxisX();
//	    xAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks()); 
//	    xAxis.setMajorTickSpacing(5);
//	    xAxis.setMinorTickSpacing(1);
//	    xAxis.setStartMajorTick(true);
//	    IAxis<IAxisScalePolicy> yAxis = (IAxis<IAxisScalePolicy>)speedDistChart.getAxisY();
//	    yAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks()); 
//	    yAxis.setMajorTickSpacing(5);
//	    yAxis.setMinorTickSpacing(1);
//	    yAxis.setStartMajorTick(true);
 		add(speedDistChart);
		
		
		Thread speedDistThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					updateSpeedDist();
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		speedDistThread.start();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		switch (SimModel.Changed.valueOf(arg1.toString())) {
		case NumParticles:
			System.out.println("NumParticles");
			break;
		case ParticleSize:
			System.out.println("ParticleSize");
			break;
		case Restart:
			System.out.println("Restart");
			break;
		case T:
			System.out.println("T");
			break;
		default:
			System.out.println("DEFAULT");
			break;
		}
	}
	
	private void updateSpeedDist() {
		speedDistChart.removeAllTraces();
		speedDistTrace = new Trace2DReplacing();
		speedDistTrace.setTracePainter(new info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar(10, speedDistChart));
		speedDistTrace.setName("Speed distribution");
		speedDistChart.addTrace(speedDistTrace);
		
		
		ArrayList<Double> speeds = model.getSpeeds();
		speeds.sort((a, b) -> (a > b) ? 1 : (a < b) ? -1 : 0);
		int barTot = 0;
		double lowest = speeds.get(0);
		double highest = speeds.get(speeds.size() - 1);
		double diff = (highest - lowest) / 10;
		
		for (int i = 1; i <= 10; i++) {
			for (double speed : speeds) {
				if (speed >= (lowest + i * diff) && speed < (lowest + (i + 1) * diff)) {
					barTot++;
				}
			}
//			System.out.println("\nbarTot: " + barTot);
//			System.out.println("i: " + i);
			speedDistTrace.addPoint(i * 10, barTot);
			barTot = 0;
		}
	}
}
