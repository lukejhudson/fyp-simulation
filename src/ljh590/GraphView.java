package ljh590;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.JComponent;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterAutoUnits;
import info.monitorenter.gui.chart.traces.Trace2DReplacing;
import info.monitorenter.util.Range;

public class GraphView extends JComponent implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SimModel model;

	/**
	 * @param model
	 */
	public GraphView(SimModel model) {
		super();
		setLayout(new BorderLayout());
		this.model = model;
		Chart2D chart = new Chart2D();
		// Create an ITrace:
		ITrace2D trace = new Trace2DReplacing();
		// change to vertical bar diagram
		trace.setTracePainter(new info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar(chart));
		// Add the trace to the chart. This has to be done before adding points
		// (deadlock prevention):
		chart.addTrace(trace);
		// Add all points, as it is static:
		Random random = new Random();
		for (int i = 10; i >= 0; i--) {
			trace.addPoint(i, random.nextDouble() * 10.0 + i);
		}
//		chart.getAxisX().setMinorTickSpacing(1);
//		chart.getAxisX().setMajorTickSpacing(2);
//		chart.getAxisX().setRange(new Range(0, 10));
		chart.getAxisX().setFormatter(new LabelFormatterAutoUnits());
		add(chart);
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
}
