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

	private ControlPanel cont;

	// Text used in the info screen
	private JLabel helpBottomMid;
	private JLabel helpBottomRight;
	private JLabel helpGeneral;
	private JLabel helpActEn;
	private JLabel helpHeatEn;
	// Label containing any one of the above
	private JLabel helpTextPanel;
	
	private JPanel helpContainer;

	public HelpScreens(ControlPanel controlPanel) {
		cont = controlPanel;
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
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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

				frame.setSize(new Dimension(1200, 720));
				frame.add(helpContainer);
				frame.setLocation(325, 20);
				frame.setVisible(true);
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
		helpBottomMid = new JLabel(ControlPanel.readFile("helpscreens/HelpBottomMid.txt"));
		helpBottomMid.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpBottomMid.setVerticalAlignment(SwingConstants.TOP);
		helpBottomRight = new JLabel(ControlPanel.readFile("helpscreens/HelpBottomRight.txt"));
		helpBottomRight.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpBottomRight.setVerticalAlignment(SwingConstants.TOP);
		helpGeneral = new JLabel(ControlPanel.readFile("helpscreens/HelpGeneralInfo.txt"));
		helpGeneral.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpGeneral.setVerticalAlignment(SwingConstants.TOP);
		helpActEn = new JLabel(ControlPanel.readFile("helpscreens/HelpActEnergy.txt"));
		helpActEn.setFont(new Font("Calibri", Font.PLAIN, 14));
		helpActEn.setVerticalAlignment(SwingConstants.TOP);
		helpHeatEn = new JLabel(ControlPanel.readFile("helpscreens/HelpHeatEngines.txt"));
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
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

				JLabel container = new JLabel();
				container.setLayout(new BorderLayout());
				container.setVerticalTextPosition(SwingConstants.TOP);

				String m = (String) menu.getSelectedItem();
				if (m.equals("Heat Engines")) {
					frame.setTitle("Heat Engines Help");
					ImageIcon ccPistons = cont.createImageIcon("CarnotCyclePistons.png",
							"An ideal gas-piston model of the Carnot cycle");
					ImageIcon ccPV = cont.createImageIcon("CarnotCyclePV.png", "A P-V diagram of the Carnot cycle");
					ImageIcon ccTS = cont.createImageIcon("CarnotCycleTS.png", "A T-S diagram of the Carnot cycle");

					JLabel pistonImg = new JLabel("Figure 1: An ideal gas-piston model of the Carnot cycle", ccPistons,
							JLabel.CENTER);
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

					JLabel text1 = new JLabel(ControlPanel.readFile("helpscreens/HeatEngines1.txt"));
					text1.setFont(new Font("Calibri", Font.PLAIN, 14));
					JLabel text2 = new JLabel(ControlPanel.readFile("helpscreens/HeatEngines2.txt"));
					text2.setFont(new Font("Calibri", Font.PLAIN, 14));
					JLabel text3 = new JLabel(ControlPanel.readFile("helpscreens/HeatEngines3.txt"));
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

					frame.setSize(new Dimension(1300, 900));
				} else if (m.equals("Activation Energy")) {
					frame.setTitle("Activation Energy Help");

					ImageIcon btChart = cont.createImageIcon("BoltzmannTemp - Uncropped, Small.png",
							"Boltzmann Factor against Temperature");
					JLabel btImg = new JLabel("Figure 1: Boltzmann Factor against Temperature", btChart, JLabel.CENTER);
					btImg.setVerticalTextPosition(JLabel.BOTTOM);
					btImg.setHorizontalTextPosition(JLabel.CENTER);

					JLabel text1 = new JLabel(ControlPanel.readFile("helpscreens/ActEnergy1.txt"));
					text1.setFont(new Font("Calibri", Font.PLAIN, 14));
					JLabel text2 = new JLabel(ControlPanel.readFile("helpscreens/ActEnergy2.txt"), btChart, SwingConstants.LEFT);
					text2.setHorizontalTextPosition(SwingConstants.LEFT);
					text2.setFont(new Font("Calibri", Font.PLAIN, 14));
					container.add(text1, BorderLayout.CENTER);
//					container.add(btImg, BorderLayout.CENTER);
					container.add(text2, BorderLayout.SOUTH);

					frame.setSize(new Dimension(1300, 800));
				}

				frame.add(container);
				frame.setLocation(20, 20);
				frame.setVisible(true);
			}
		});
		return menuHelp;
	}
}
