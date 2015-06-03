package main.java;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JLabel;

public class RbFrame extends JFrame {
	private static final long serialVersionUID = 6360194617243336884L;
	private JTextField txtRB;
	private JTextField txtTrunk;
	private JButton btnTrunkBrowse;
	private JButton btnRBBrowse;
	private JButton btnCompare;
	private JLabel lblEnsureBothLocations;
	public RbFrame() {
		super();
		setSize(430,220);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Compare Rollback Objects to Trunk");
		getContentPane().setLayout(null);
		
		txtRB = new JTextField();
		txtRB.setBounds(13, 36, 296, 20);
		getContentPane().add(txtRB);
		txtRB.setColumns(10);
		
		btnRBBrowse = new JButton("Browse...");
		btnRBBrowse.setBounds(322, 35, 89, 23);
		btnRBBrowse.setActionCommand("RBBrowse");
		getContentPane().add(btnRBBrowse);
		
		txtTrunk = new JTextField();
		txtTrunk.setBounds(13, 93, 296, 20);
		getContentPane().add(txtTrunk);
		txtTrunk.setColumns(10);
		
		btnTrunkBrowse = new JButton("Browse...");
		btnTrunkBrowse.setBounds(322, 92, 89, 23);
		btnTrunkBrowse.setActionCommand("TrunkBrowse");
		getContentPane().add(btnTrunkBrowse);
		
		JLabel lblRBLabel = new JLabel("Rollback Folder Location");
		lblRBLabel.setBounds(13, 11, 180, 14);
		getContentPane().add(lblRBLabel);
		
		JLabel lblTrunkLabel = new JLabel("Trunk location");
		lblTrunkLabel.setBounds(13, 68, 82, 14);
		getContentPane().add(lblTrunkLabel);
		
		btnCompare = new JButton("Compare");
		btnCompare.setBounds(172, 158, 89, 23);
		btnCompare.setActionCommand("Compare");
		getContentPane().add(btnCompare);
		
		lblEnsureBothLocations = new JLabel("Ensure both locations have been updated to head before comparing.");
		lblEnsureBothLocations.setBounds(17, 133, 390, 14);
		getContentPane().add(lblEnsureBothLocations);
	}
	public String getTxtRBText() {
		return txtRB.getText();
	}
	public void setTxtRBText(String txtRB) {
		this.txtRB.setText(txtRB);
	}
	public String getTxtTrunk() {
		return txtTrunk.getText();
	}
	public void setTxtTrunk(String txtTrunk) {
		this.txtTrunk.setText(txtTrunk);
	}
	
	public void addListener(ActionListener a) {
		btnCompare.addActionListener(a);
		btnTrunkBrowse.addActionListener(a);
		btnRBBrowse.addActionListener(a);
	}
	
	public void addKeyListener(KeyListener k) {
		txtRB.addKeyListener(k);
		txtTrunk.addKeyListener(k);
	}
	
	public void toggleCompareEnabled() {
		String path1 = getTxtTrunk();
		String path2 = getTxtRBText();
		if (path1 == null || path1.isEmpty()) {
			btnCompare.setEnabled(false);
			return;
		} else if (path2 == null || path2.isEmpty()) {
			btnCompare.setEnabled(false);
			return;
		} else if (path1.equals(path2)){
			btnCompare.setEnabled(false);
			return;
		} else {
			btnCompare.setEnabled(true);
		}
		
	}
	
}
