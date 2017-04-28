import java.io.*;

public class AutoAnalyser{

      public static void main(String args[]) throws ParseException, FileNotFoundException {
        File f = new File("Input2.aa");
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        AutoAnalyserParser analyser = new AutoAnalyserParser(br);

        SimpleNode start;

          //try {
               start = analyser.start();
              //System.out.println("accepted");
               start.dump("");
          //} catch (Exception t){
              //System.out.println("rejected");
          //} 
              start.execute();
        }
}
