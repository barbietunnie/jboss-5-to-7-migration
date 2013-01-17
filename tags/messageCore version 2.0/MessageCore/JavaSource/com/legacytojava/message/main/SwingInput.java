package com.legacytojava.message.main;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;


public class SwingInput {
	public SwingInput() {
	}
	
    public static void main(String[] args) {
        //String inputValue = JOptionPane.showInputDialog("Please input a value");
    	SwingInput in = new SwingInput();
    	String password = in.getPasswordFocused();
    	System.out.println("Password: " + password);
		System.exit(0);
    }
    
    String getPasswordUnfocused() {
		JPasswordField pwd = new JPasswordField(16);
		int action = JOptionPane.showConfirmDialog(null, pwd, "Enter Root Password",
				JOptionPane.OK_CANCEL_OPTION);
		if (action < 0) {
			JOptionPane.showMessageDialog(null, "Cancel, X or escape key selected");
			return null;
		}
		else {
			JOptionPane.showMessageDialog(null, "Your password is " + new String(pwd.getPassword()));
			return new String(pwd.getPassword());
		}
    }
    
    private String getPasswordFocused() {
    	final JPasswordField jpf = new JPasswordField();
		JOptionPane jop = new JOptionPane(jpf, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = jop.createDialog(null, "Enter MySql root password:");
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				jpf.requestFocusInWindow();
			}
		});
		dialog.setVisible(true);
		int result = (Integer) jop.getValue();
		dialog.dispose();
		char[] password = null;
		if (result == JOptionPane.OK_OPTION) {
			password = jpf.getPassword();
			return new String(password);
		}
		return null;
    }
}
