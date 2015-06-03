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
					//String message = "These files differ from trunk:" + NL;
					final StringBuffer message = new StringBuffer();
					message.append("These files differ from trunk:" + NL);
					final ArrayList<ArrayList<File>> differences = new ArrayList<ArrayList<File>>();
					boolean diff = false;
					
					
					class TreeWalker {
						ArrayList<File> difference;
						public boolean walk(String checkPath, String againstPath) {
							boolean diff = false;
							System.out.println(checkPath);
							File checkDir = new File(checkPath);
							String[] checkSubDirs = checkDir.list( DirectoryFileFilter.INSTANCE );
							for ( int i = 0; i < checkSubDirs.length; i++ ) {
								String checkSubDirPath = FilenameUtils.concat(checkPath, checkSubDirs[i]);
								String againstSubDirPath = FilenameUtils.concat(againstPath, checkSubDirs[i]);	
								if (this.walk2(checkSubDirPath, againstSubDirPath)) {
									diff = true;
								}
							}
							return diff;
						}
						
						private boolean walk2(String checkPath, String againstPath) {
							boolean diff = false;
							System.out.println(checkPath);
							File checkDir = new File(checkPath);
							//check files in this directory
							String[] rbSubFiles = checkDir.list( FileFileFilter.FILE);
							boolean printedDirectory = false;
							for ( int i = 0; i < rbSubFiles.length; i++ ) {
								String checkFilePath = FilenameUtils.concat(checkPath, rbSubFiles[i]);
								String againstFilePath = FilenameUtils.concat(againstPath, rbSubFiles[i]);
								File checkFile = new File(checkFilePath);
								File againstFile = new File(againstFilePath);
								try {
									if (!FileUtils.contentEquals(checkFile, againstFile)) {
										if (!printedDirectory) {
											message.append(checkPath + NL);	
											printedDirectory = true;
										}
										message.append("      "+rbSubFiles[i] + NL);
										difference = new ArrayList<File>();
										difference.add(againstFile);
										difference.add(checkDir);
										differences.add(difference);
										diff = true;
									}
								} catch (IOException e) {
									e.printStackTrace();
									System.out.println(rbSubFiles[i] + " exception");
								}
							}
							String[] checkSubDirs = checkDir.list( DirectoryFileFilter.INSTANCE );
							for ( int i = 0; i < checkSubDirs.length; i++ ) {
								String checkSubDirPath = FilenameUtils.concat(checkPath, checkSubDirs[i]);
								String againstSubDirPath = FilenameUtils.concat(againstPath, checkSubDirs[i]);	
								if (this.walk2(checkSubDirPath, againstSubDirPath)) {
									diff = true;
								}
							}
							return diff;
						}
					}
					
					diff = new TreeWalker().walk(form.getTxtRBText(), form.getTxtTrunk());
					
					if (diff) {
						message.append("Replace these rollback files with latest from trunk?" + NL);
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
