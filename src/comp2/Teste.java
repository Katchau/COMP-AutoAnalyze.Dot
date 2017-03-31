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
	
	private static void automataType(){
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
				System.out.println("MEGA ULTRA AUTOMATA");
				break;
		}
	}
	
	public static void main(String[] args) {
		String file = "test.dot";
		Graph g = new DefaultGraph("g");
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
		
		ArrayList<String> transValues = new ArrayList<String>();
		
		boolean foundType = false;
		for(int i = 0; i < g.getNodeCount(); i++){
			Node n = g.getNode(i);
			System.out.println("Node "+ i + " : " + g.getNode(i).getId());
			Iterator<Edge> edges = n.getEdgeSet().iterator();
			ArrayList<String> transactions = new ArrayList<String>();
			while(edges.hasNext()){
				Edge e = edges.next();
				String trans = e.getAttribute("label");
				System.out.println("Edge " + trans);
				if(!foundType){
					if(transactions.contains(trans))
						type = 2;
					transactions.add(trans);
					if(trans.equals("epsilon")){
						foundType = true;
						type = 3;
					}
					if(i != 0 && !transValues.contains(trans) && type < 1)
						type = 1;
					if(i == 0)
						transValues.add(trans);
				}
			}
		}
		
		automataType();
		
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
