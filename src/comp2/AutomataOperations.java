package comp2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkDOT;

public class AutomataOperations {
	private static String getEClosure(Node n, String currState){
		String states = currState + "%" + n.getId();
		for(Edge e : n.getEachEdge()){
			String trans = e.getAttribute("label");
			if(trans.equals("epsilon") || trans.equals("Epsilon")){
				String endNode = e.getTargetNode().getId();
				if(endNode.equals(n.getId()))continue;
				if(states.contains(endNode))continue;
				states = getEClosure(e.getTargetNode(),states); 
			}
		}
		return states;
	}
	
	private static ArrayList<Node> checkTransition(Node n, String transiction){
		ArrayList<Node> finalState = new ArrayList<Node>();
		for(Edge e : n.getEachEdge()){
			String trans = e.getAttribute("label");
			if(!trans.equals(transiction))continue;
			String endNode = e.getTargetNode().getId();
			if(endNode.equals(n.getId()))continue;
			finalState.add(e.getTargetNode());
		}
		return finalState;
	}
	
	private static String generateString(HashSet<String> hs){
		String ret = "";
		for(String s: hs){
			if(s.equals(""))continue;
			ret += "%" + s;
		}
		return ret;
	}
	
	private static ArrayList<String> getNewStates(Automata a, String curr){
		ArrayList<String> oldStates = new ArrayList<String>(Arrays.asList(curr.split("%")));
		ArrayList<String> newStates = new ArrayList<String>();
		oldStates.remove(0);//o 1º é um espaço
		for(String trans: a.transValues){
			HashSet<String> tmpStates = new HashSet<String>();
			for(String s : oldStates){
				Node n = a.g.getNode(s);
				ArrayList<Node> nf = checkTransition(n,trans);
				if(nf.size() != 0){
					for(Node nn :nf){
						tmpStates.addAll(Arrays.asList(getEClosure(nn,"").split("%")));
					}
				}
			}
			newStates.add(generateString(tmpStates));
		}
		return newStates;
	}
	
	private static void addNodeEdge(Graph g,String trans, String b4node, String state, boolean hasNew){
		String newNode = state.replace("%", "-");
		if(hasNew)g.addNode(newNode);
		System.out.println("Transaction From: " + b4node + " to " + newNode + " when: " + trans);
		String edge = trans + "" + b4node + "" + newNode;
		g.addEdge(edge, b4node, newNode, true);//<3 prof
		g.getEdge(edge).setAttribute("label", trans);
	}
	
	public static void convert2DFA(Automata a){
		if(a.type < 1)return;
		Graph ret = new DefaultGraph(a.g.getId());
		String start = getEClosure(a.start,"");
		HashMap<String, Boolean> states = new HashMap<String, Boolean>();
		ret.addNode(start.replace("%", "-"));
		states.put(start, false);
		while(states.containsValue(false)){
			String curr = "";
			for(String state: states.keySet()){
				curr = (!states.get(state)) ? state : "";
				if(!curr.equals(""))break;
			}
			ArrayList<String> newStates = getNewStates(a,curr);
			String b4node = curr.replace("%", "-");
			for(int i = 0; i < newStates.size(); i++){
				String state = newStates.get(i);
				if(state.equals(""))continue;
				String trans = a.transValues.get(i);
				if(states.containsKey(state)){
					addNodeEdge(ret,trans,b4node,state,false);
					continue;
				}
				addNodeEdge(ret,trans,b4node,state,true);
				states.put(state, false);
			}
			states.put(curr, true);
		}
		ret.display();
	}
	
	//verify tem de ser do genero trans1%trans2%trans3
	//converter para dfa antes :)
	public static boolean acceptString(Automata a, String verify){
		ArrayList<String> transactions = new ArrayList<String>(Arrays.asList(verify.split("%")));
		Node n = a.start;
		for(String trans : transactions){
			boolean hasT = false;
			for(Edge e : n.getEachEdge()){
				Node end = e.getTargetNode();
				if(end.equals(n) && !end.equals(e.getSourceNode()))continue;
				if(trans.equals(e.getAttribute("label"))){
					hasT = true;
					n = end;
					break;
				}
			}
			if(!hasT) return false;
		}
		return Pattern.compile(".*_end.*").matcher(n.getId()).matches();
	}
	
	public static void exportAutomata(Graph g, String fileName){
		FileSinkDOT fsd = new FileSinkDOT();
		fsd.setDirected(true);
		try {
			fsd.writeAll(g, fileName);
		} catch (IOException e) {
			System.err.println("Error: Saving dot file");
		}
	}
}
