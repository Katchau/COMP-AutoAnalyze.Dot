import javax.swing.*;
import java.awt.*;
import java.io.*;

public class AutoAnalyser{

      private Gui gui;
      private AutoAnalyserParser analyserParser;

      public static String runResult = new String();
      public static void main(String args[]) throws ParseException, FileNotFoundException {
            new AutoAnalyser();
      }

      public AutoAnalyser() {
          EventQueue.invokeLater(() -> {
              gui = new Gui(this);
              gui.setVisible(true);
              gui.setExtendedState(gui.getExtendedState() | JFrame.MAXIMIZED_BOTH);
          });
        }

        public String run(File cache) {
            runResult = "";
            try {
                FileReader fr = new FileReader(cache);
                BufferedReader br = new BufferedReader(fr);
                if(analyserParser == null)
                    analyserParser = new AutoAnalyserParser(br);
                else {
                    analyserParser.ReInit(fr);
                }
                SimpleNode start = analyserParser.Start();
                start.execute();
                br.close();
                fr.close();
            } catch (Exception e) {
                addToResult(e.getMessage());
            }
            return runResult;
        }

        public static void addToResult(String s) {
          runResult += runResult + "\n" + s;
        }
}
