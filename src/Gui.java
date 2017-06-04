
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

public class Gui extends JFrame{
	
	public Gui(){
		initUI();
	}
	
	private String runResult = "";
	private JTextArea textArea_1;
	
	private void initUI() {
        
		
        setTitle("AutoAnalyser Compiler");
        setSize(720, 518);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new MigLayout("", "[][grow]", "[][grow][][][grow]"));
        getContentPane().setBackground(Color.DARK_GRAY);
        
        JLabel lblCode = new JLabel("Code");
        getContentPane().add(lblCode, "cell 0 0");
        lblCode.setForeground(Color.LIGHT_GRAY);
        
        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, "cell 1 1,grow");       
       
        
        JTextArea textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        textArea.setBackground(Color.LIGHT_GRAY);
        
        JButton btnRun = new JButton("Run");
        getContentPane().add(btnRun, "cell 1 2");
        btnRun.setBackground(Color.LIGHT_GRAY);
        
        
        JLabel lblNewLabel = new JLabel("Result");
        getContentPane().add(lblNewLabel, "cell 0 3");
        lblNewLabel.setForeground(Color.LIGHT_GRAY);
        
        JScrollPane scrollPane_1 = new JScrollPane();
        getContentPane().add(scrollPane_1, "cell 1 4,grow");
        
        textArea_1 = new JTextArea();
        scrollPane_1.setViewportView(textArea_1);
        textArea_1.setWrapStyleWord(true);
        textArea_1.setLineWrap(true);
        textArea_1.setEditable(false);
        textArea_1.setBackground(Color.LIGHT_GRAY);
        
        
        btnRun.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent arg0) {
        		textArea_1.setText(textArea.getText());//TEMP
        	}
        });
    }

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            Gui ex = new Gui();
            ex.setVisible(true);
            ex.setExtendedState(ex.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        });

	}
	
	public void resetRunConsole() {
		runResult = "";
		textArea_1.setText(runResult);
	}
	
	public void appendString(String append) {
		runResult  = runResult + System.getProperty("line.separator") + append;
		textArea_1.setText(runResult);
	}
}