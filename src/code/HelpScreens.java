package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
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

/**
 * Creates the buttons which bring up the help screens, as well as the help
 * screens themselves.
 * 
 * @author Luke
 *
 */
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
	private boolean isModeHelpOpenHE = false;
	private boolean isModeHelpOpenAE = false;
	private JFrame modeFrame;

	public HelpScreens(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;
		createInfoLabels();
	}

	/**
	 * @return A JButton which will open the information window
	 */
	public JButton createInfoButton() {
		// Create the button and format it
		JButton info = new JButton("INFO");
		info.setFont(new Font("Calibri", Font.BOLD, 16));
		info.setToolTipText("Click to see an overview of the entire program");
		info.setMargin(new Insets(0, 5, 0, 5));
		info.setPreferredSize(new Dimension(60, 25));
		info.setMaximumSize(new Dimension(60, 25));

		info.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// If the window is already open, bring it to the front
				if (isHelpOpen) {
					helpFrame.requestFocus();
				} else {
					// Create the window
					helpFrame = new JFrame();
					helpFrame.setTitle("Information");
					helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					// Keep track of whether the window has been opened
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
					// Add the relevant text to the window
					helpTextPanel = new JLabel();
					helpTextPanel.setLayout(new BorderLayout());
					helpTextPanel.add(helpGeneral);
					helpTextPanel.setVerticalAlignment(SwingConstants.TOP);
					helpTextPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
					// Add the contents menu to the window
					JPanel contents = createContents();
					helpContainer.add(contents, BorderLayout.WEST);
					helpContainer.add(helpTextPanel, BorderLayout.CENTER);

					helpFrame.setSize(new Dimension(1250, 725));
					helpFrame.add(helpContainer);
					helpFrame.setLocation(280, 20);
					helpFrame.setVisible(true);
				}
			}
		});

		return info;
	}

	/**
	 * @return The contents menu used in the "INFO" button window
	 */
	private JPanel createContents() {
		JPanel contents = new JPanel(new GridLayout(0, 1));
		// Create the title for the contents
		JLabel contentsTitle = new JLabel("Contents");
		contentsTitle.setFont(new Font("Calibri", Font.BOLD, 20));
		contentsTitle.setHorizontalAlignment(SwingConstants.CENTER);
		// Create an underlined font
		Font font = new Font("Calibri", Font.BOLD, 14);
		Map<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		font = font.deriveFont(map);
		// Create the General Information button
		JButton general = new JButton("General Information");
		general.setEnabled(false);
		general.setFont(font);
		general.setForeground(Color.BLUE);
		general.setBorderPainted(false);
		general.setContentAreaFilled(false);
		general.addMouseListener(createMouseListener(general));
		// Create the Bottom Middle Components button
		JButton bottomMid = new JButton("<html><u>Bottom Middle<br>Components</u></html>");
		bottomMid.setFont(font);
		bottomMid.setForeground(Color.BLUE);
		bottomMid.setBorderPainted(false);
		bottomMid.setContentAreaFilled(false);
		bottomMid.addMouseListener(createMouseListener(bottomMid));
		// Create the Bottom Right Components button
		JButton bottomRight = new JButton("<html><u>Bottom Right<br>Components</u></html>");
		bottomRight.setFont(font);
		bottomRight.setForeground(Color.BLUE);
		bottomRight.setBorderPainted(false);
		bottomRight.setContentAreaFilled(false);
		bottomRight.addMouseListener(createMouseListener(bottomRight));
		// Create the Heat Engines button
		JButton heatEn = new JButton("Heat Engines");
		heatEn.setFont(font);
		heatEn.setForeground(Color.BLUE);
		heatEn.setBorderPainted(false);
		heatEn.setContentAreaFilled(false);
		heatEn.addMouseListener(createMouseListener(heatEn));
		// Create the Activation Energy button
		JButton actEn = new JButton("Activation Energy");
		actEn.setFont(font);
		actEn.setForeground(Color.BLUE);
		actEn.setBorderPainted(false);
		actEn.setContentAreaFilled(false);
		actEn.addMouseListener(createMouseListener(actEn));
		// Update the contents menu and add the relevant text to the window when
		// the General Information button is pressed
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
		// Update the contents menu and add the relevant text to the window when
		// the Bottom Middle Components button is pressed
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
		// Update the contents menu and add the relevant text to the window when
		// the Bottom Right Components button is pressed
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
		// Update the contents menu and add the relevant text to the window when
		// the Heat Engines button is pressed
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
		// Update the contents menu and add the relevant text to the window when
		// the Activation Energy button is pressed
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

		// Place the components into one panel
		contents.add(contentsTitle);
		contents.add(general);
		contents.add(bottomMid);
		contents.add(bottomRight);
		contents.add(heatEn);
		contents.add(actEn);

		return contents;
	}

	/**
	 * Creates a mouse listener which changes the colour of the text of the
	 * button b when the mouse is hovering over it.
	 * 
	 * @param b
	 *            The button to apply the effect to
	 * @return The MouseAdapter to produce the effect
	 */
	private MouseAdapter createMouseListener(JButton b) {
		return new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				b.setForeground(Color.BLUE);
			}

			public void mouseEntered(MouseEvent e) {
				b.setForeground(Color.RED);
			}
		};
	}

	/**
	 * Reads all of the required text files for the information window.
	 */
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

	/**
	 * @param menu
	 *            The menu used to select the current mode
	 * @return A JButton to open a window with information relating to the
	 *         current mode
	 */
	public JButton createMenuHelp(JComboBox<String> menu) {
		// Create the HELP button and format it
		JButton menuHelp = new JButton("HELP");
		menuHelp.setFont(new Font("Calibri", Font.BOLD, 16));
		menuHelp.setToolTipText("Detailed information for the current mode");
		menuHelp.setMargin(new Insets(0, 5, 0, 5));

		menuHelp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String m = (String) menu.getSelectedItem();
				// Only allow one Heat Engines and one Activation Energy window
				// to be open at a time
				if (!((isModeHelpOpenHE && m.equals(menu.getItemAt(0)))
						|| (isModeHelpOpenAE && m.equals(menu.getItemAt(1))))) {
					// Create the window
					modeFrame = new JFrame();
					modeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					// Keep track of whether the windows have been opened
					modeFrame.addWindowListener(new WindowListener() {
						@Override
						public void windowActivated(WindowEvent e) {
						}

						@Override
						public void windowClosed(WindowEvent e) {
							// Heat Engines
							if (m.equals(menu.getItemAt(0))) {
								isModeHelpOpenHE = false;
							}
							// Activation Energy
							if (m.equals(menu.getItemAt(1))) {
								isModeHelpOpenAE = false;
							}
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
							// Heat Engines
							if (m.equals(menu.getItemAt(0))) {
								isModeHelpOpenHE = true;
							}
							// Activation Energy
							if (m.equals(menu.getItemAt(1))) {
								isModeHelpOpenAE = true;
							}
						}
					});

					JLabel container = new JLabel();
					container.setLayout(new BorderLayout());
					container.setVerticalTextPosition(SwingConstants.TOP);

					if (m.equals("Heat Engines")) {
						// Create and format the Heat Engines help window 
						
						modeFrame.setTitle("Heat Engines Help");
						// Read the images
						ImageIcon ccReservoir = controlPanel.createImageIcon("Reservoirs.png",
								"An overview of the model for the Carnot cycle");
						ImageIcon ccPistons = controlPanel.createImageIcon("CarnotCyclePistons.png",
								"An ideal gas-piston model of the Carnot cycle");
						ImageIcon ccPV = controlPanel.createImageIcon("CarnotCyclePV.png",
								"A P-V diagram of the Carnot cycle");
						ImageIcon ccTS = controlPanel.createImageIcon("CarnotCycleTS.png",
								"A T-S diagram of the Carnot cycle");

						// Add labels to the images and format them
						JLabel reservoirImg = new JLabel("Figure 1: An overview of the model for the Carnot cycle",
								ccReservoir, JLabel.CENTER);
						reservoirImg.setVerticalTextPosition(JLabel.BOTTOM);
						reservoirImg.setHorizontalTextPosition(JLabel.CENTER);
						JLabel pistonImg = new JLabel("Figure 2: An ideal gas-piston model of the Carnot cycle",
								ccPistons, JLabel.CENTER);
						pistonImg.setVerticalTextPosition(JLabel.BOTTOM);
						pistonImg.setHorizontalTextPosition(JLabel.CENTER);
						JLabel pvImg = new JLabel("Figure 3: A P-V diagram of the Carnot cycle", ccPV, JLabel.CENTER);
						pvImg.setVerticalTextPosition(JLabel.BOTTOM);
						pvImg.setHorizontalTextPosition(JLabel.CENTER);
						JLabel tsImg = new JLabel("Figure 4: A T-S diagram of the Carnot cycle", ccTS, JLabel.CENTER);
						tsImg.setVerticalTextPosition(JLabel.BOTTOM);
						tsImg.setHorizontalTextPosition(JLabel.CENTER);
						JPanel charts = new JPanel(new GridLayout(1, 0));
						charts.add(pistonImg);
						charts.add(pvImg);
						charts.add(tsImg);

						// Read the text files
						JLabel text1 = new JLabel(controlPanel.readFile("helpscreens/HeatEngines1.txt"));
						text1.setFont(new Font("Calibri", Font.PLAIN, 14));
						JLabel text2 = new JLabel(controlPanel.readFile("helpscreens/HeatEngines2.txt"));
						text2.setFont(new Font("Calibri", Font.PLAIN, 14));
						JLabel text3 = new JLabel(controlPanel.readFile("helpscreens/HeatEngines3.txt"));
						text3.setFont(new Font("Calibri", Font.PLAIN, 14));

						// Format the entire window
						JPanel top = new JPanel(new BorderLayout());
						top.add(text1, BorderLayout.CENTER);
						top.add(reservoirImg, BorderLayout.EAST);
						top.add(text2, BorderLayout.SOUTH);

						JPanel bottom = new JPanel(new BorderLayout());
						bottom.add(charts, BorderLayout.CENTER);
						bottom.add(text3, BorderLayout.SOUTH);

						container.add(top, BorderLayout.CENTER);
						container.add(bottom, BorderLayout.SOUTH);

						modeFrame.setSize(new Dimension(1400, 950));
					} else if (m.equals("Activation Energy")) {
						// Create and format the Activation Energy help window 
						modeFrame.setTitle("Activation Energy Help");

						// Read the images
						ImageIcon btChart = controlPanel.createImageIcon("BoltzmannTemp.png",
								"Boltzmann Factor against Temperature");
						JLabel btImg = new JLabel("Figure 1: Boltzmann Factor against Temperature", btChart,
								JLabel.CENTER);
						btImg.setVerticalTextPosition(JLabel.BOTTOM);
						btImg.setHorizontalTextPosition(JLabel.CENTER);

						// Read the text files and format the entire window
						JLabel text1 = new JLabel(controlPanel.readFile("helpscreens/ActEnergy1.txt"));
						text1.setFont(new Font("Calibri", Font.PLAIN, 14));
						JLabel text2 = new JLabel(controlPanel.readFile("helpscreens/ActEnergy2.txt"), btChart,
								SwingConstants.LEFT);
						text2.setHorizontalTextPosition(SwingConstants.LEFT);
						text2.setFont(new Font("Calibri", Font.PLAIN, 14));
						container.add(text1, BorderLayout.CENTER);
						container.add(text2, BorderLayout.SOUTH);

						modeFrame.setSize(new Dimension(1400, 850));
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
