package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class HelpScreens {

	private ControlPanel controlPanel;

	// Text used in the info screen
	private JLabel helpBottomMid;
	private JLabel helpBottomRight;
	private JLabel helpGeneral;
	private JLabel helpActEn;
	private JLabel helpHeatEn;
	// Label containing any one of the above
	private JLabel helpTextPanel;
	private JPanel helpContainer;

	// Is the top right help screen open?
	private boolean isHelpOpen = false;
	private JFrame helpFrame;
	// Is the top left help screen open?
	private boolean isModeHelpOpen = false;
	private JFrame modeFrame;

	public HelpScreens(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
		createInfoLabels();
	}

	public JButton createInfoButton() {
		JButton info = new JButton("INFO");
		info.setFont(new Font("Calibri", Font.BOLD, 16));
		info.setToolTipText("Click to see an overview of the entire program");
		info.setMargin(new Insets(0, 5, 0, 5));
		info.setPreferredSize(new Dimension(50, 20));
		info.setMaximumSize(new Dimension(50, 20));

		info.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (isHelpOpen) {
					helpFrame.requestFocus();
				} else {
					helpFrame = new JFrame();
					helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					helpFrame.addWindowListener(new WindowListener() {
						@Override
						public void windowActivated(WindowEvent e) {
						}

						@Override
						public void windowClosed(WindowEvent e) {
							isHelpOpen = false;
						}

						@Override
						public void windowClosing(WindowEvent e) {
						}

						@Override
						public void windowDeactivated(WindowEvent e) {
						}

						@Override
						public void windowDeiconified(WindowEvent e) {
						}

						@Override
						public void windowIconified(WindowEvent e) {
						}

						@Override
						public void windowOpened(WindowEvent e) {
							isHelpOpen = true;
						}
					});

					helpContainer = new JPanel();
					helpContainer.setLayout(new BorderLayout());
					helpContainer.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

					helpTextPanel = new JLabel();
					helpTextPanel.setLayout(new BorderLayout());
					helpTextPanel.add(helpGeneral);
					helpTextPanel.setVerticalAlignment(SwingConstants.TOP);
					helpTextPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

					JPanel contents = createContents();

					helpContainer.add(contents, BorderLayout.WEST);
					helpContainer.add(helpTextPanel, BorderLayout.CENTER);

					helpFrame.setSize(new Dimension(1200, 720));
					helpFrame.add(helpContainer);
					helpFrame.setLocation(325, 20);
					helpFrame.setVisible(true);
				}
			}
		});

		return info;
	}

	/**
	 * @return The panel used in the "INFO" button containing the contents menu.
	 */
	private JPanel createContents() {
		JPanel contents = new JPanel(new GridLayout(0, 1));
		JLabel contentsTitle = new JLabel("Contents");
		contentsTitle.setFont(new Font("Calibri", Font.BOLD, 20));
		contentsTitle.setHorizontalAlignment(SwingConstants.CENTER);

		Font font = new Font("Calibri", Font.BOLD, 14);
		Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		font = font.deriveFont(map);

		JButton general = new JButton("General Information");
		general.setEnabled(false);
		general.setFont(font);
		general.setForeground(Color.BLUE);
		general.setBorderPainted(false);
		general.setContentAreaFilled(false);
		general.addMouseListener(createMouseListener(general));
		JButton bottomMid = new JButton("<html><u>Bottom Middle<br>Components</u></html>");
		bottomMid.setFont(font);
		bottomMid.setForeground(Color.BLUE);
		bottomMid.setBorderPainted(false);
		bottomMid.setContentAreaFilled(false);
		bottomMid.addMouseListener(createMouseListener(bottomMid));
		JButton bottomRight = new JButton("<html><u>Bottom Right<br>Components</u></html>");
		bottomRight.setFont(font);
		bottomRight.setForeground(Color.BLUE);
		bottomRight.setBorderPainted(false);
		bottomRight.setContentAreaFilled(false);
		bottomRight.addMouseListener(createMouseListener(bottomRight));
		JButton heatEn = new JButton("Heat Engines");
		heatEn.setFont(font);
		heatEn.setForeground(Color.BLUE);
		heatEn.setBorderPainted(false);
		heatEn.setContentAreaFilled(false);
		heatEn.addMouseListener(createMouseListener(heatEn));
		JButton actEn = new JButton("Activation Energy");
		actEn.setFont(font);
		actEn.setForeground(Color.BLUE);
		actEn.setBorderPainted(false);
		actEn.setContentAreaFilled(false);
		actEn.addMouseListener(createMouseListener(actEn));

		general.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				general.setEnabled(false);
				bottomMid.setEnabled(true);
				bottomRight.setEnabled(true);
				heatEn.setEnabled(true);
				actEn.setEnabled(true);
				helpTextPanel.removeAll();
				helpTextPanel.add(helpGeneral);
				helpContainer.updateUI();
			}
		});

		bottomMid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				general.setEnabled(true);
				bottomMid.setEnabled(false);
				bottomRight.setEnabled(true);
				heatEn.setEnabled(true);
				actEn.setEnabled(true);
				helpTextPanel.removeAll();
				helpTextPanel.add(helpBottomMid);
				helpContainer.updateUI();
			}
		});

		bottomRight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				general.setEnabled(true);
				bottomMid.setEnabled(true);
				bottomRight.setEnabled(false);
				heatEn.setEnabled(true);
				actEn.setEnabled(true);
				helpTextPanel.removeAll();
				helpTextPanel.add(helpBottomRight);
				helpContainer.updateUI();
			}
		});

		heatEn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				general.setEnabled(true);
				bottomMid.setEnabled(true);
				bottomRight.setEnabled(true);
				heatEn.setEnabled(false);
				actEn.setEnabled(true);
				helpTextPanel.removeAll();
				helpTextPanel.add(helpHeatEn);
				helpContainer.updateUI();
			}
		});

		actEn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				general.setEnabled(true);
				bottomMid.setEnabled(true);
				bottomRight.setEnabled(true);
				heatEn.setEnabled(true);
				actEn.setEnabled(false);
				helpTextPanel.removeAll();
				helpTextPanel.add(helpActEn);
				helpContainer.updateUI();
			}
		});

		contents.add(contentsTitle);
		contents.add(general);
		contents.add(bottomMid);
		contents.add(bottomRight);
		contents.add(heatEn);
		contents.add(actEn);

		return contents;
	}

	private java.awt.event.MouseAdapter createMouseListener(JButton b) {
		return new java.awt.event.MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				b.setForeground(Color.BLUE);
			}

			public void mouseEntered(MouseEvent e) {
				b.setForeground(Color.RED);
			}
		};
	}

	private void createInfoLabels() {
		helpBottomMid = new JLabel(controlPanel.readFile("helpscreens/HelpBottomMid.txt"));
		helpBottomMid.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpBottomMid.setVerticalAlignment(SwingConstants.TOP);
		helpBottomRight = new JLabel(controlPanel.readFile("helpscreens/HelpBottomRight.txt"));
		helpBottomRight.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpBottomRight.setVerticalAlignment(SwingConstants.TOP);
		helpGeneral = new JLabel(controlPanel.readFile("helpscreens/HelpGeneralInfo.txt"));
		helpGeneral.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpGeneral.setVerticalAlignment(SwingConstants.TOP);
		helpActEn = new JLabel(controlPanel.readFile("helpscreens/HelpActEnergy.txt"));
		helpActEn.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpActEn.setVerticalAlignment(SwingConstants.TOP);
		helpHeatEn = new JLabel(controlPanel.readFile("helpscreens/HelpHeatEngines.txt"));
		helpHeatEn.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpHeatEn.setVerticalAlignment(SwingConstants.TOP);
	}

	public JButton createMenuHelp(JComboBox<String> menu) {
		JButton menuHelp = new JButton("HELP");
		menuHelp.setFont(new Font("Calibri", Font.BOLD, 16));
		// menuHelp.setContentAreaFilled(false);
		menuHelp.setToolTipText("Detailed information for the current mode");
		menuHelp.setMargin(new Insets(0, 5, 0, 5));
		menuHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (isModeHelpOpen) {
					modeFrame.requestFocus();
				} else {
					modeFrame = new JFrame();
					modeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					modeFrame.addWindowListener(new WindowListener() {
						@Override
						public void windowActivated(WindowEvent e) {
						}

						@Override
						public void windowClosed(WindowEvent e) {
							isModeHelpOpen = false;
						}

						@Override
						public void windowClosing(WindowEvent e) {
						}

						@Override
						public void windowDeactivated(WindowEvent e) {
						}

						@Override
						public void windowDeiconified(WindowEvent e) {
						}

						@Override
						public void windowIconified(WindowEvent e) {
						}

						@Override
						public void windowOpened(WindowEvent e) {
							isModeHelpOpen = true;
						}
					});

					JLabel container = new JLabel();
					container.setLayout(new BorderLayout());
					container.setVerticalTextPosition(SwingConstants.TOP);

					String m = (String) menu.getSelectedItem();
					if (m.equals("Heat Engines")) {
						modeFrame.setTitle("Heat Engines Help");
						ImageIcon ccPistons = controlPanel.createImageIcon("CarnotCyclePistons.png",
								"An ideal gas-piston model of the Carnot cycle");
						ImageIcon ccPV = controlPanel.createImageIcon("CarnotCyclePV.png",
								"A P-V diagram of the Carnot cycle");
						ImageIcon ccTS = controlPanel.createImageIcon("CarnotCycleTS.png",
								"A T-S diagram of the Carnot cycle");

						JLabel pistonImg = new JLabel("Figure 1: An ideal gas-piston model of the Carnot cycle",
								ccPistons, JLabel.CENTER);
						pistonImg.setVerticalTextPosition(JLabel.BOTTOM);
						pistonImg.setHorizontalTextPosition(JLabel.CENTER);
						JLabel pvImg = new JLabel("Figure 2: A P-V diagram of the Carnot cycle", ccPV, JLabel.CENTER);
						pvImg.setVerticalTextPosition(JLabel.BOTTOM);
						pvImg.setHorizontalTextPosition(JLabel.CENTER);
						JLabel tsImg = new JLabel("Figure 3: A T-S diagram of the Carnot cycle", ccTS, JLabel.CENTER);
						tsImg.setVerticalTextPosition(JLabel.BOTTOM);
						tsImg.setHorizontalTextPosition(JLabel.CENTER);
						JPanel charts = new JPanel(new GridLayout(1, 0));
						charts.add(pvImg);
						charts.add(tsImg);

						JLabel text1 = new JLabel(controlPanel.readFile("helpscreens/HeatEngines1.txt"));
						text1.setFont(new Font("Calibri", Font.PLAIN, 14));
						JLabel text2 = new JLabel(controlPanel.readFile("helpscreens/HeatEngines2.txt"));
						text2.setFont(new Font("Calibri", Font.PLAIN, 14));
						JLabel text3 = new JLabel(controlPanel.readFile("helpscreens/HeatEngines3.txt"));
						text3.setFont(new Font("Calibri", Font.PLAIN, 14));

						JPanel top = new JPanel(new BorderLayout());
						top.add(text1, BorderLayout.CENTER);
						top.add(pistonImg, BorderLayout.EAST);
						top.add(text2, BorderLayout.SOUTH);

						JPanel bottom = new JPanel(new BorderLayout());
						bottom.add(charts, BorderLayout.CENTER);
						bottom.add(text3, BorderLayout.SOUTH);

						container.add(top, BorderLayout.CENTER);
						container.add(bottom, BorderLayout.SOUTH);

						modeFrame.setSize(new Dimension(1300, 900));
					} else if (m.equals("Activation Energy")) {
						modeFrame.setTitle("Activation Energy Help");

						ImageIcon btChart = controlPanel.createImageIcon("BoltzmannTemp - Uncropped, Small.png",
								"Boltzmann Factor against Temperature");
						JLabel btImg = new JLabel("Figure 1: Boltzmann Factor against Temperature", btChart,
								JLabel.CENTER);
						btImg.setVerticalTextPosition(JLabel.BOTTOM);
						btImg.setHorizontalTextPosition(JLabel.CENTER);

						JLabel text1 = new JLabel(controlPanel.readFile("helpscreens/ActEnergy1.txt"));
						text1.setFont(new Font("Calibri", Font.PLAIN, 14));
						JLabel text2 = new JLabel(controlPanel.readFile("helpscreens/ActEnergy2.txt"), btChart,
								SwingConstants.LEFT);
						text2.setHorizontalTextPosition(SwingConstants.LEFT);
						text2.setFont(new Font("Calibri", Font.PLAIN, 14));
						container.add(text1, BorderLayout.CENTER);
						// container.add(btImg, BorderLayout.CENTER);
						container.add(text2, BorderLayout.SOUTH);

						modeFrame.setSize(new Dimension(1300, 800));
					}

					modeFrame.add(container);
					modeFrame.setLocation(20, 20);
					modeFrame.setVisible(true);
				}
			}
		});
		return menuHelp;
	}
}
