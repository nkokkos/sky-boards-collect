/*
 * FirmwareDialog
 *
 * Author  : Eloy Díaz <eldial@gmail.com>
 * Created : 11 jun 2012
 */

package se.sics.contiki.collect.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class FirmwareDialog extends javax.swing.JDialog implements
    java.awt.event.ActionListener {

	private final ArrayList<String> firmList = new ArrayList<String>();
	private final ArrayList<JComboBox> CBlist = new ArrayList<JComboBox>();

	public FirmwareDialog(final JFrame parent, final String title,
	    final String[] moteList, final String[] firmwares) {

		super(parent, title, true);

		JTextField textMote;
		JComboBox comboBoxFw;

		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);

			final int shift = 31;
			int ypos = shift;

			JTextPane infoColPane = new JTextPane();
			infoColPane.setContentType("text/html");
			infoColPane.setEditable(false);
			infoColPane
			    .setText("<html><strong>(Reference, Device, Description)</strong></html>");
			infoColPane.setBounds(31, ypos, 333, 23);
			infoColPane.setBackground(getContentPane().getBackground());
			getContentPane().add(infoColPane);
			ypos = ypos + shift;

			for (final String elem : moteList) {

				textMote = new JTextField();
				getContentPane().add(textMote);
				textMote.setText(elem);
				textMote.setBounds(31, ypos, 333, 23);
				textMote.setEditable(false);

				final ComboBoxModel jComboBox1Model = new DefaultComboBoxModel(
				    firmwares);
				comboBoxFw = new JComboBox();
				getContentPane().add(comboBoxFw);
				comboBoxFw.setModel(jComboBox1Model);
				comboBoxFw.setBounds(395, ypos, 227, 23);

				CBlist.add(comboBoxFw);
				ypos = ypos + shift;
			}

			ypos = ypos + shift;

			JButton ButtonSet = new JButton();
			getContentPane().add(ButtonSet);
			ButtonSet.setText("OK");
			ButtonSet.setBounds(290, ypos + 20, 100, 23);
			ButtonSet.addActionListener(this);

			pack();
			this.setSize(685, ((moteList.length + 1) * shift) + 150);
			this.setLocationRelativeTo(null);
			setVisible(true);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(final ActionEvent e) {
		for (int i = 0; i < CBlist.size(); i++) {
			final JComboBox comboBox = CBlist.get(i);
			final int idx = comboBox.getSelectedIndex();
			firmList.add(comboBox.getItemAt(idx).toString());
		}
		setVisible(false);
	}

	public String[] getChoosenFirmware() {
		return firmList.toArray(new String[firmList.size()]);
	}

}
