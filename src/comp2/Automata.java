package comp2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

public class Automata {
	public int type = 0; // 0 - dfa, 1 - dfa inc, 2 - nfa, 3 - e-nfa
	public Graph g;
	public Node start;
	private FileSource fs = null;
	public ArrayList<String> transValues = new ArrayList<>();
	protected static final Pattern endState = Pattern.compile(".*_end.*");
	protected static final Pattern startState = Pattern.compile(".*start.*");
	protected static final Pattern deathState = Pattern.compile(".*new-death-node.*");

	public Automata(){

    }

	public Automata(String fileName){
		if(!importAutomata(fileName))
			return;
		if(!getAutomataType())
			return;
		automataTypePrint();//TODO remove
		closeAutomata();
	}

	public void automataTypePrint(){
		System.out.print("Automata Type: ");
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
		System.out.println("Existent Transactions: ");
		for(String t: transValues){
			System.out.print(t + ", ");
		}
		System.out.println("");
	}

	public boolean analyzeEdges(int i,Iterator<Edge> edges){
		ArrayList<String> transactions = new ArrayList<String>();
		while(edges.hasNext()){
			Edge e = edges.next();
			String transs = e.getAttribute("label").toString().split(",");
			if(transs == null) {
				System.err.println("Error: No transaction value!");
				return false;
			}
			String endNode = e.getTargetNode().getId();
			if(endNode.equals(g.getNode(i).getId()) && !endNode.equals(e.getSourceNode().getId()))continue;
			if(transactions.contains(trans))
				type = 2;
			transactions.add(trans);
			if(trans.equals("epsilon") || trans.equals("Epsilon")){
				type = 3;
			}
			else if(i != 0 && !transValues.contains(trans)){
				if(type < 1)type = 1;
				transValues.add(trans);
			}
			else if(i == 0)
				transValues.add(trans);
		}
		if(transactions.size() < transValues.size())
		    type = 1;
		return true;
	}

	public boolean getAutomataType(){
		int startStates = 0;
		int endStates = 0;
		for(int i = 0; i < g.getNodeCount(); i++){
			Node n = g.getNode(i);
			String nodeType = n.getId();
			if(startState.matcher(nodeType).matches()){
				 start = n;
				 startStates++;
			}
			if(endState.matcher(nodeType).matches())endStates++;
			Iterator<Edge> edges = n.getEdgeSet().iterator();
			if(!analyzeEdges(i,edges)) return false;
		}
		if(startStates != 1 && endStates == 0){
			System.err.println("Error: Invalid Graph detected!");
			System.err.println("Start Nodes: " + startStates + " endStates " + endStates);
			return false;
		}
		return true;
	}

	public boolean importAutomata(String fileName){
		g = new DefaultGraph(fileName);
		try {
			fs = FileSourceFactory.sourceFor(fileName);
			fs.addSink(g);
			fs.begin(fileName);
			while(fs.nextEvents()){
				//do nothing xD
			}
		} catch (IOException e) {
			System.err.println("Error: No such file " + fileName);
			return false;
		}
		return true;
	}

	public void closeAutomata(){
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
