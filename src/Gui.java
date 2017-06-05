import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class Gui extends JFrame{

	private String runResult = "";
	private JTextArea textArea_1;
    private JComboBox comboBox;
    private AutoAnalyser autoAnalyser;

    public Gui(){
        initUI();
    }

    public Gui(AutoAnalyser t) {
        this.autoAnalyser = t;
        initUI();
    }

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
        scrollPane.setBackground(Color.LIGHT_GRAY);
        getContentPane().add(scrollPane, "cell 1 1,grow");       
       
        
        JTextArea textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        textArea.setBackground(Color.LIGHT_GRAY);
        
        JButton btnRun = new JButton("Run");
        getContentPane().add(btnRun, "flowx,cell 1 2");
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
        
        JButton btnCleanResultConsole = new JButton("Clean Result Console");
        btnCleanResultConsole.setBackground(Color.LIGHT_GRAY);
        getContentPane().add(btnCleanResultConsole, "cell 1 2");
        
        JLabel lblAutomata = new JLabel("Automata:");
        lblAutomata.setForeground(Color.LIGHT_GRAY);
        getContentPane().add(lblAutomata, "cell 1 2");
        
        comboBox = new JComboBox<ComboItem>();
        comboBox.setBackground(Color.LIGHT_GRAY);
        cleanAutomataSelector();
        getContentPane().add(comboBox, "cell 1 2");
        
        JButton btnDisplay = new JButton("Display");
        btnDisplay.setBackground(Color.LIGHT_GRAY);
        getContentPane().add(btnDisplay, "cell 1 2,alignx left");
        
        
        btnRun.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent arg0) {
                String filename = "cache.aa";
                File cache = new File(filename);
                try{
                    FileWriter out = new FileWriter(cache, false);
                    out.write(textArea.getText());
                    System.out.println(textArea.getText());
                    out.close();
                    String result = autoAnalyser.run(cache);
                    textArea_1.setText(result);
                } catch (Exception e) {
                    //TODO
                    System.out.println(e.getMessage());
                    System.out.println(e);
                }


        	}
        });
        
        btnCleanResultConsole.addActionListener(new ActionListener() {	
        	public void actionPerformed(ActionEvent arg0) {
        		 resetRunConsole();
        	}
        });

        btnDisplay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                launchGraphic();
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

    @SuppressWarnings("unchecked")
    public void cleanAutomataSelector() {
        comboBox.removeAllItems();
        comboBox.addItem(new ComboItem("none",-1));
    }

    @SuppressWarnings("unchecked")
    public void addOptionsAutomato(String[] options) {
        for(int i = 0; i < options.length; i++) {
            comboBox.addItem(new ComboItem(options[i],i));
        }
    }

    public void launchGraphic(){
        if(comboBox.getSelectedItem().toString().equals("ignore"))
        return;
        //send to AutoAnalyser to launch graph  
    }

}