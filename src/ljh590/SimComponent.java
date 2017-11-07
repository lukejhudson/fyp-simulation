package ljh590;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class SimComponent extends JComponent {

	private Model model;
	private JFrame frame;
	private ModelComponent comp;
	private JLabel currT;
	private JLabel currP;

	public SimComponent(Model m, JFrame frame) {
		super();
		this.model = m;
		this.frame = frame;
		setLayout(new BorderLayout());
		
		JPanel UI = new JPanel(new BorderLayout());
		
		JPanel sliders = sliderBar();
		JPanel buttons = buttonBar();
		comp = new ModelComponent(m, frame, currT, currP);
		
		UI.add(sliders, BorderLayout.CENTER);
		UI.add(buttons, BorderLayout.EAST);
		
		frame.add(comp, BorderLayout.CENTER);
		frame.add(UI, BorderLayout.SOUTH);
	}

	private JPanel sliderBar() {
		JPanel sliderBar = new JPanel(new BorderLayout());
		JPanel tempComp = new JPanel(new BorderLayout());
		JPanel fps = new JPanel(new BorderLayout());
		JPanel stats = new JPanel(new BorderLayout());
		
		JPanel tempAndStats = new JPanel(new BorderLayout());
		
		JPanel particleOptions = new JPanel(new BorderLayout());
		JPanel noParticles = new JPanel(new BorderLayout());
		JPanel sizeParticles = new JPanel(new BorderLayout());
		
		JLabel noParticlesLabel = new JLabel("Number of particles", SwingConstants.CENTER);
		JSlider noParticlesSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 500, 250);
		noParticlesSlider.setMajorTickSpacing(100);
		noParticlesSlider.setMinorTickSpacing(25);
		noParticlesSlider.setPaintTicks(true);
		noParticlesSlider.setPaintLabels(true);
		JLabel noParticlesValue = new JLabel("250");
		noParticlesSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setNumParticles(noParticlesSlider.getValue());
				noParticlesValue.setText(Integer.toString(noParticlesSlider.getValue()));
			}
		});
		noParticles.add(noParticlesLabel, BorderLayout.NORTH);
		noParticles.add(noParticlesSlider, BorderLayout.CENTER);
		noParticles.add(noParticlesValue, BorderLayout.EAST);
		
		JLabel sizeParticlesLabel = new JLabel("Particle size", SwingConstants.CENTER);
		JSlider sizeParticlesSlider = new JSlider(SwingConstants.HORIZONTAL, 2, 100, 10);
		sizeParticlesSlider.setMajorTickSpacing(10);
		sizeParticlesSlider.setMinorTickSpacing(5);
		sizeParticlesSlider.setPaintTicks(true);
		sizeParticlesSlider.setPaintLabels(true);
		JLabel sizeParticlesValue = new JLabel("50");
		sizeParticlesSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setParticleSize(sizeParticlesSlider.getValue());
				sizeParticlesValue.setText(Integer.toString(sizeParticlesSlider.getValue()));
			}
		});
		sizeParticles.add(sizeParticlesLabel, BorderLayout.NORTH);
		sizeParticles.add(sizeParticlesSlider, BorderLayout.CENTER);
		sizeParticles.add(sizeParticlesValue, BorderLayout.EAST);
		
		particleOptions.add(noParticles, BorderLayout.CENTER);
		particleOptions.add(sizeParticles, BorderLayout.EAST);
		
		
		JSlider tempSlider = new JSlider(SwingConstants.HORIZONTAL, 200, 4000, 300);
		JLabel tempValue = new JLabel("300");
		tempSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setT(tempSlider.getValue());
				tempValue.setText(Integer.toString(tempSlider.getValue()));
			}
		});
		JLabel tempLabel = new JLabel("Average particle temperature", SwingConstants.CENTER);
		tempSlider.setMajorTickSpacing(600);
		tempSlider.setMinorTickSpacing(100);
		tempSlider.setPaintTicks(true);
		tempSlider.setPaintLabels(true);
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
		
		tempAndStats.add(stats, BorderLayout.WEST);
		tempAndStats.add(tempComp, BorderLayout.EAST);
		
		sliderBar.add(tempAndStats, BorderLayout.WEST);
		sliderBar.add(particleOptions, BorderLayout.CENTER);
		sliderBar.add(fps, BorderLayout.EAST);
		
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
