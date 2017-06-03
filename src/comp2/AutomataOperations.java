package comp2;


import java.io.IOException;
import java.util.*;
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
		ArrayList<String> oldStates = new ArrayList<>(Arrays.asList(curr.split("%")));
		ArrayList<String> newStates = new ArrayList<>();
		oldStates.remove(0);//o 1º é um espaço
		for(String trans: a.transValues){
			HashSet<String> tmpStates = new HashSet<>();
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
		//System.out.println("Transaction From: " + b4node + " to " + newNode + " when: " + trans);
		String edge = trans + "" + b4node + "" + newNode;
		g.addEdge(edge, b4node, newNode, true);//<3 prof
		g.getEdge(edge).setAttribute("label", trans);
	}

	public static void addDeathState(Automata a, Graph g){
	    boolean hasDeath = false;
        for(int i = 0; i < a.g.getNodeCount(); i++){
            Node n = a.g.getNode(i);
            String startNode = n.getId();
            if(Automata.deathState.matcher(startNode).matches())continue;
            Iterator<Edge> edges = n.getEdgeSet().iterator();
            HashSet<String> states = new HashSet<>();
            while(edges.hasNext()){
                Edge e = edges.next();
                String trans = e.getAttribute("label");
                String endNode = e.getTargetNode().getId();
                if(endNode.equals(startNode) && !endNode.equals(e.getSourceNode().getId()))continue;
                states.add(trans);
            }
            if(states.size() < a.transValues.size()){
                for(String trans : a.transValues){
                    if(!states.contains(trans)){
                        addNodeEdge(g,trans,startNode,"new-death-node",!hasDeath);
                        hasDeath = true;
                    }
                }
            }
        }
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
		addDeathState(a,ret);
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

    public static Automata getComplement(Automata in) {
        Automata out = new Automata();
        out.start = in.start;
        Graph g = in.g;
        out.g = new DefaultGraph("!");

        for(int i = 0; i < in.g.getNodeCount(); i++){
            Node n = in.g.getNode(i);
            String nodeOppType = reverseNode(n);
            out.g.addNode(nodeOppType);
        }

        for(int i = 0; i <  g.getEdgeCount(); i++){
            Edge e = g.getEdge(i);
            Node start = e.getNode0();
            Node end = e.getNode1();

            String startNodeType = reverseNode(start);
            String endNodeType = reverseNode(end);
            out.g.addEdge(e.getId(), startNodeType, endNodeType, true);
            out.g.getEdge(e.getId()).setAttribute("label",e.getAttribute("label"));
        }
        //TODO remove
        exportAutomata(out.g,"lmao2.dot");
        return out;
    }

    // MORGAN LAW -> L1 ? L2 = not(not(L1) ? not(L2))
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
    public static Automata union (Automata in1, Automata in2) {
        Automata out = new Automata();
        out.g = new DefaultGraph("union");

        // create nodes // DFA1 X DFA2
        ArrayList<String[]> pairedNodes = new ArrayList<>();

        for(int i = 0; i < in1.g.getNodeCount(); i++){
            Node n = in1.g.getNode(i);
            String nodeType = n.getId();

            for (int k = 0;k  < in2.g.getNodeCount(); k++) {
                Node nIN = in2.g.getNode(k);
                String newNode = nodeType + " "+nIN.getId();
                pairedNodes.add(new String[]{nodeType,nIN.getId()});
                out.g.addNode(newNode);
            }
        }

        for(int i = 0; i < in1.g.getEdgeCount(); i++) {
            Edge e = in1.g.getEdge(i);
            String sourceA = e.getNode0().getId();
            String destA = e.getNode1().getId();
            String value = e.getAttribute("label");

            for( int k = 0; k < in2.g.getEdgeCount(); k++) {
                Edge b = in2.g.getEdge(k);
                String sourceB = b.getNode0().getId();
                String destB = b.getNode1().getId();
                String valueB = b.getAttribute("label");

                if(value.equals(valueB)) {
                    String source = sourceA + " " + sourceB;
                    String dest = destA + " " + destB;
                    out.g.addEdge(source+dest, source, dest, true );
                }
            }
        }
        return out;
    }

    private static String reverseNode(Node n){
        String nodeType = n.getId();
        nodeType = (Automata.endState.matcher(nodeType).matches()) ?
                nodeType.replaceAll("_end", "") : nodeType + "_end";
        return nodeType;
    }
}
