package comp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

public class Teste {
	private static int type = 0; // 0 - dfa, 1 - dfa inc, 2 - nfa, 3 - e-nfa
	private static Graph g;
	private static boolean foundType = false;
	private static ArrayList<String> transValues = new ArrayList<String>();
	
	private static void automataTypePrint(){
		switch(type){
			case 0:
				System.out.println("DFA");
				break;
			case 1:
				System.out.println("Incomplete DFA");
				break;
			case 2:
				System.out.println("NFA");
				break;
			case 3:
				System.out.println("E-NFA");
				break;
			default:
				System.out.println("MEGA ULTRA AUTOMATA ERROR");
				break;
		}
	}
	
	public static boolean analyzeEdges(int i,Iterator<Edge> edges){
		ArrayList<String> transactions = new ArrayList<String>();
		while(edges.hasNext()){
			Edge e = edges.next();
			String trans = e.getAttribute("label");
			if(trans == null) {
				System.err.println("Error: No transaction value!");
				return false;
			}
			System.out.println("Edge " + trans + " to " + e.getTargetNode().getId());
			if(!foundType){
				if(transactions.contains(trans))
					type = 2;
				transactions.add(trans);
				if(trans.equals("epsilon") || trans.equals("Epsilon")){
					foundType = true;
					type = 3;
				}
				if(i != 0 && !transValues.contains(trans) && type < 1){
					type = 1;
					transValues.add(trans);
				}
				if(i == 0)
					transValues.add(trans);
			}
		}
		return true;
	}
	
	public static boolean getAutomataType(){
		foundType = false;
		int startStates = 0;
		int endStates = 0;
		for(int i = 0; i < g.getNodeCount(); i++){
			Node n = g.getNode(i);
			String nodeType = n.getId();
			System.out.println("Node "+ i + " : " + nodeType);
			if(nodeType.equals("start") || nodeType.equals("Start")) startStates++;
			if(nodeType.equals("end") || nodeType.equals("End"))endStates++;
			Iterator<Edge> edges = n.getEdgeSet().iterator();
			if(!analyzeEdges(i,edges)) return false;
		}
		if(startStates != 1 && endStates == 0){
			System.err.println("Error: Invalid Graph detected!");
			return false;
		}
		foundType = true;
		return true;
	}
	
	public static void main(String[] args) {
		String file = "test.dot";
		g = new DefaultGraph("g");
		FileSource fs = null;
		try {
			fs = FileSourceFactory.sourceFor(file);
			fs.addSink(g);
			fs.begin(file);
			while(fs.nextEvents()){
				
			}
		} catch (IOException e) {
			System.err.println("Error: No such file" + file);
		}
		
		g.display();
		if(!getAutomataType())
			return;
		automataTypePrint();
		
		//ter isto separado porcausa da biblioteca. ver http://graphstream-project.org/doc/Tutorials/Reading-files-using-FileSource/ 
		try {
			fs.end();
		} catch( IOException e) {
			e.printStackTrace();
		} finally {
			fs.removeSink(g);
		}
	}

}
