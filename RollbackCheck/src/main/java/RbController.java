package main.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

public class RbController {
	
	private RbFrame form;
	
	Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
	private final String NL = System.getProperty("line.separator");
	
	public static void main(String[] args) {
		new RbController();
	}
	
	public RbController() {
		form = new RbFrame();
		form.addListener(new Listener());
		form.addKeyListener(new keyListener());
		form.setTxtRBText(prefs.get("rbpath", ""));  
		form.setTxtTrunk(prefs.get("trunkpath", ""));
		form.toggleCompareEnabled();
		form.setVisible(true);
	}
	
	private class Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String cmd = arg0.getActionCommand();
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal;
			String path = "";
			switch (cmd) {
				case "RBBrowse":
					fc.setCurrentDirectory(new File(prefs.get("rbpath", "")));
					returnVal = fc.showOpenDialog(form);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							path = fc.getSelectedFile().getCanonicalPath();
							prefs.put("rbpath", path);
							form.setTxtRBText(path);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
				case "TrunkBrowse":
					fc.setCurrentDirectory(new File(prefs.get("trunkpath", "")));
					returnVal = fc.showOpenDialog(form);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							path = fc.getSelectedFile().getCanonicalPath();
							prefs.put("trunkpath", path);
							form.setTxtTrunk(path);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
				case "Compare":
					String message = "These files differ from trunk:" + NL;
					File rbDir = new File(form.getTxtRBText());
					String[] rbSubDirs = rbDir.list( DirectoryFileFilter.INSTANCE );
					ArrayList<ArrayList<File>> differences = new ArrayList<ArrayList<File>>();
					boolean diff = false;
					ArrayList<File> difference;
					for ( int i = 0; i < rbSubDirs.length; i++ ) {
						String rbSubDirPath = FilenameUtils.concat(form.getTxtRBText(), rbSubDirs[i]);
						String trunkSubDirPath = FilenameUtils.concat(form.getTxtTrunk(), rbSubDirs[i]);
						File rbSubDir = new File(rbSubDirPath);
						String[] rbSubFiles = rbSubDir.list( FileFileFilter.FILE);
						boolean printedDirectory = false;
						for ( int j = 0; j < rbSubFiles.length; j++ ) {
							String rbFilePath = FilenameUtils.concat(rbSubDirPath, rbSubFiles[j]);
							String trunkFilePath = FilenameUtils.concat(trunkSubDirPath, rbSubFiles[j]);
							File rbFile = new File(rbFilePath);
							File trunkFile = new File(trunkFilePath);
							try {
								if (!FileUtils.contentEquals(rbFile, trunkFile)) {
									if (!printedDirectory) {
										message += rbSubDirs[i] + NL;	
										printedDirectory = true;
									}
									message += "      "+rbSubFiles[j] + NL;
									difference = new ArrayList<File>();
									difference.add(trunkFile);
									difference.add(rbSubDir);
									differences.add(difference);
									diff = true;
								}
							} catch (IOException e) {
								e.printStackTrace();
								System.out.println(rbSubFiles[j] + " exception");
							}
						}
					}
					if (diff) {
						message += "Replace these rollback files with latest from trunk?" + NL;
						System.out.println(message);
					    int answer = JOptionPane.showConfirmDialog(form, message, "Replace rollback files?", JOptionPane.YES_NO_OPTION);
					    if (answer == JOptionPane.YES_OPTION) {
					        File trunkFile;
					        File fileDest;
					    	for (ArrayList<File> diffi : differences) {
					    		trunkFile = diffi.get(0);
					    		fileDest = diffi.get(1);
					    		System.out.println("Moving " + trunkFile + " to " + fileDest);
					    		try {
					    			FileUtils.copyFileToDirectory(trunkFile, fileDest, true); //preserve file modification date
								} catch (IOException e) {
									e.printStackTrace();
									System.out.println(trunkFile + " to " + fileDest + " exception");
								}
					    	}
					    	JOptionPane.showMessageDialog(null, "Replaced Successfully", "Compare Complete", JOptionPane.INFORMATION_MESSAGE);
					    } else if (answer == JOptionPane.NO_OPTION) {
					    	System.out.println("you clicked no");
					    }
					} else {
						JOptionPane.showMessageDialog(null, "No differences found", "Compare Complete", JOptionPane.INFORMATION_MESSAGE);
						System.out.println("no differences found");
					}
					break;
			}
			form.toggleCompareEnabled();
		}
	}
	
	private class keyListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent arg0) {}
		@Override
		public void keyReleased(KeyEvent arg0) { form.toggleCompareEnabled(); }
		@Override
		public void keyTyped(KeyEvent arg0) {}
	}
}
