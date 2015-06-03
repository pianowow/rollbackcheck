package main.java;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	private Logger logger;
	private final String loggerFN = "RollbackCheckError.log";
	
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
		logger = Logger.getLogger(this.getClass().getName());
		Handler fh;
		try {
			fh = new FileHandler(loggerFN, true);  //true means append to existing log file
			Logger.getLogger("").addHandler(fh);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error, could not setup log file", "Log File Setup", JOptionPane.ERROR_MESSAGE);
		}
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
							logger.log(Level.SEVERE, "Rollback Folder open exception", e);
							JOptionPane.showMessageDialog(null, 
				                      "Error, could not open rollback folder." + NL + 
				                      "Please see " + loggerFN + " for details.",
				                      "Log File Setup", JOptionPane.ERROR_MESSAGE);
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
							logger.log(Level.SEVERE, "Trunk Folder open exception", e);
							JOptionPane.showMessageDialog(null, 
				                      "Error, could not open trunk folder." + NL + 
				                      "Please see " + loggerFN + " for details.",
				                      "Log File Setup", JOptionPane.ERROR_MESSAGE);
							
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
							String[] checkSubFiles = checkDir.list( FileFileFilter.FILE);
							boolean printedDirectory = false;
							for ( int i = 0; i < checkSubFiles.length; i++ ) {
								String checkFilePath = FilenameUtils.concat(checkPath, checkSubFiles[i]);
								String againstFilePath = FilenameUtils.concat(againstPath, checkSubFiles[i]);
								File checkFile = new File(checkFilePath);
								File againstFile = new File(againstFilePath);
								try {
									if (!FileUtils.contentEquals(checkFile, againstFile)) {
										if (!printedDirectory) {
											message.append(checkPath + NL);	
											printedDirectory = true;
										}
										message.append("      "+checkSubFiles[i] + NL);
										difference = new ArrayList<File>();
										difference.add(againstFile);
										difference.add(checkDir);
										differences.add(difference);
										diff = true;
									}
								} catch (IOException e) {
									e.printStackTrace();
									System.out.println(checkSubFiles[i] + " exception");
									logger.log(Level.SEVERE, checkSubFiles[i], e);
									JOptionPane.showMessageDialog(null, 
						                      "Error during file comparison." + NL + 
						                      "Please see " + loggerFN + " for details.",
						                      "Log File Setup", JOptionPane.ERROR_MESSAGE);									
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
					    	boolean gotone = false;
					    	boolean error = false;
					        File trunkFile;
					        File fileDest;
					    	for (ArrayList<File> diffi : differences) {
					    		trunkFile = diffi.get(0);
					    		fileDest = diffi.get(1);
					    		System.out.println("Moving " + trunkFile + " to " + fileDest);
					    		try {
					    			FileUtils.copyFileToDirectory(trunkFile, fileDest, true); //preserve file modification date
					    			gotone = true;
								} catch (IOException e) {
									e.printStackTrace();
									System.out.println(trunkFile + " to " + fileDest + " exception");
									logger.log(Level.SEVERE, trunkFile.getAbsolutePath(), e);
									JOptionPane.showMessageDialog(null, 
						                      "Error during file copy." + NL + 
						                      "Please see " + loggerFN + " for details.",
						                      "Log File Setup", JOptionPane.ERROR_MESSAGE);		
									error = true;
								}
					    	}
					    	if (!error && gotone) {
					    		JOptionPane.showMessageDialog(null, "Replaced successfully", "Compare Complete", JOptionPane.INFORMATION_MESSAGE);
					    	} else if (error && gotone) {
					    		JOptionPane.showMessageDialog(null, "Replaced partially", "Compare Complete", JOptionPane.INFORMATION_MESSAGE);
					    	} else {
					    		JOptionPane.showMessageDialog(null, "No files replaced", "Compare Complete", JOptionPane.INFORMATION_MESSAGE);
					    	}
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
