
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
	public ArrayList<String> transValues = new ArrayList<String>();
	private final Pattern endState = Pattern.compile(".*_end.*");
	private final Pattern startState = Pattern.compile(".*start.*");
	

	public Automata () {
		
	}

	public Automata(String fileName){
		if(!importAutomata(fileName))
			return;
		if(!getAutomataType())
			return;
		automataTypePrint();
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
			String trans = e.getAttribute("label");
			if(trans == null) {
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
		g.display(); //TODO remover dp
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

// needs  test
	public Automata complement() {
		Automata out = new Automata();
		out.start = start;
		out.g = new DefaultGraph("!");

		for(int i = 0; i < g.getNodeCount(); i++){
			Node n = g.getNode(i);
			String nodeOppType = reverseNode(n);
			out.g.addNode(nodeOppType);
		}

		for(int i = 0; i <  g.getEdgeCount(); i++){
			Edge e = g.getEdge(i);
			Node start = e.getNode0();
			Node end = e.getNode1();

			String startNodeType = reverseNode(start);
			String endNodeType = reverseNode(end);
			g.addEdge(e.getId(), startNodeType, endNodeType, true);
		}

		return out;
	}

	// MORGAN LAW -> L1 ∩ L2 = not(not(L1) ∪ not(L2))
	/*public Automata diff(Automata in) {
			
		Automata notL1 = this.complement();
		Automata notL2 = in.complement();
		Automata union = notL1.Union(notL2);
		Automata diff = union.complement();
		return diff;	
	}*/

	//TODO 
	//Todo sameState
	//TODO test
	public Automata union ( Automata in ) {
		Automata out = new Automata();
		out.g = new DefaultGraph("union");

		// create nodes // DFA1 X DFA2
		for(int i = 0; i < g.getNodeCount(); i++){
			Node n = g.getNode(i);
			String nodeType = n.getId();

			for (int k = 0;k  < in.g.getNodeCount(); k++) {
				Node nIN = in.g.getNode(k);
	
				String newNode = nodeType + " "+nIN.getId();
				out.g.addNode(newNode);
			}
		}

		//add edges
		//TODO ter o alfabeto alf
		//Para cada Nó X,Y  -(alf[i])->  Xn,Yn
		// adicionar aresta
		for(int i = 0; i < g.getEdgeCount(); i++) {

		}

		//estados finais dois estados terminais do DFA1 x DFA2
		return out;
	}

	private String reverseNode(Node n){
		String nodeType = n.getId();
			if(endState.matcher(nodeType).matches()){
				nodeType.replaceAll("_end", ""); //removes end tag
			}
			else {
				nodeType = nodeType + "_end"; // add end tag
			}
		return nodeType;
	}
}
