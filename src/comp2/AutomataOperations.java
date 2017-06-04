package comp2;


import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkDOT;

public class AutomataOperations {
	private static String getEClosure(Node n, String currState){
		String states = currState + "%" + n.getId();
		for(Edge e : n.getEachEdge()){
			String[] transs = e.getAttribute("label").toString().split(",");
			for(String trans : transs){
                if(trans.equals("epsilon") || trans.equals("Epsilon")){
                    String endNode = e.getTargetNode().getId();
                    if(endNode.equals(n.getId()))continue;
                    if(states.contains(endNode))continue;
                    states = getEClosure(e.getTargetNode(),states);
                }
            }

		}
		return states;
	}
	
	private static ArrayList<Node> checkTransition(Node n, String transiction){
		ArrayList<Node> finalState = new ArrayList<Node>();
		for(Edge e : n.getEachEdge()){
			String[] transs = e.getAttribute("label").toString().split(",");
			for(String trans : transs){
                if(!trans.equals(transiction))continue;
                String endNode = e.getTargetNode().getId();
                if(endNode.equals(n.getId()) && !endNode.equals(e.getSourceNode().getId()))continue;
                finalState.add(e.getTargetNode());
            }
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
		oldStates.remove(0);//first is a space
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
		String edge = b4node + "" + newNode;
		try{
            g.addEdge(edge, b4node, newNode, true);//<3 prof
            g.getEdge(edge).setAttribute("label", trans);
        }catch(EdgeRejectedException | IdAlreadyInUseException e){
		    String transi = g.getEdge(edge).getAttribute("label");
		    g.getEdge(edge).setAttribute("label",transi + "," + trans);
        }

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
                String[] transs = e.getAttribute("label").toString().split(",");
                String endNode = e.getTargetNode().getId();
                if(endNode.equals(startNode) && !endNode.equals(e.getSourceNode().getId()))continue;
                states.addAll(Arrays.asList(transs));

            }
            if(states.size() < a.transValues.size()){
                for(String trans : a.transValues){
                    if(!states.contains(trans)){
                        addNodeEdge(g,trans,startNode,"new-death-node",!hasDeath);
                        if(!hasDeath){
                            for(String s : a.transValues){
                                addNodeEdge(g,s,"new-death-node","new-death-node",false);
                            }
                            hasDeath = true;
                        }
                    }
                }
            }
        }
    }

	public static Automata convert2DFA(Automata a){
		Automata newA = new Automata();
		Graph ret = new DefaultGraph(a.g.getId());
		String start = getEClosure(a.start,"");
		HashMap<String, Boolean> states = new HashMap<>();
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
		newA.g = ret;
		newA.transValues = a.transValues;
        newA.start = newA.g.getNode(start);
		addDeathState(newA,ret);
        return newA;
	}
	
	//verify tem de ser do genero trans1%trans2%trans3
	//converter para dfa antes :)
	public static boolean acceptString(Automata a, String verify){
		ArrayList<String> transactions = new ArrayList<>(Arrays.asList(verify.split("%")));
		Node n = a.start;
		for(String trans : transactions){
			boolean hasT = false;
			for(Edge e : n.getEachEdge()){
				Node end = e.getTargetNode();
				if(end.equals(n) && !end.equals(e.getSourceNode()))continue;
				for(String s : e.getAttribute("label").toString().split(",")){
                    if(trans.equals(s)) {
                        hasT = true;
                        n = end;
                        break;
                    }
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
        out.transValues = in.transValues;
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
            out.g.getEdge(e.getId()).setAttribute("label",e.getAttribute("label").toString());
        }

        return out;
    }


	public static Automata getReverse(Automata in) {
		Automata out = new Automata();
		out.start = in.start; //isto tem que ser o epsilon TODO
		Graph g = in.g;
		out.g = new DefaultGraph("reversed");

		for(int i = 0; i < in.g.getNodeCount(); i++){
			Node n = in.g.getNode(i);
			String nodeOppType = reverseNode(n);
			out.g.addNode(nodeOppType);
		}

		for(int i = 0; i <  g.getEdgeCount(); i++){
			Edge e = g.getEdge(i);
			Node start = e.getNode1();
			Node end = e.getNode0();

			String startNodeType = reverseNode(start);
			String endNodeType = reverseNode(end);
			out.g.addEdge(e.getId(), startNodeType, endNodeType, true);
			out.g.getEdge(e.getId()).setAttribute("label",e.getAttribute("label").toString());
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

    public static Automata getUnion (final Automata in1, final Automata in2) {
        Automata out = new Automata();
        out.g = new DefaultGraph("union");
        out.g.addNode(in1.start.getId() + "-" + in2.start.getId());
        in1.start = out.g.getNode(0);

        ArrayList<String> transactions = in1.transValues;
        for(String s : in2.transValues){
            if(!transactions.contains(s))
                transactions.add(s);
        }
        out.transValues = transactions;
        HashMap<String,Boolean> transf = new HashMap<>();
        transf.put(out.g.getNode(0).getId().replace("-","%"),false);
        while(transf.containsValue(false)) {
            Node a = null;
            Node b = null;
            String curr = "";
            for(String state: transf.keySet()){
                curr = (!transf.get(state)) ? state : "";
                if(!curr.equals(""))break;
            }
            transf.put(curr,true);
            ArrayList<String> states = new ArrayList<>(Arrays.asList(curr.split("%")));
            if(states.size() == 2){
                a = in1.g.getNode(states.get(0));
                b = in2.g.getNode(states.get(1));
            }
            else{
                if(states.size() == 0)break;
                if(curr.startsWith("%")) b =  in2.g.getNode(states.get(0).replace("%",""));
                else a = in1.g.getNode(states.get(0));
            }
            if(a != null && b != null){
                String source = curr.replace("%","-");
                for(String trans : transactions){
                    ArrayList<Node> t1 = checkTransition(a,trans);
                    ArrayList<Node> t2 = checkTransition(b,trans);
                    String target;
                    if(t1.size() > 0 && t2.size() > 0){
                        String n1 = t1.get(0).getId();
                        String n2 = t2.get(0).getId();
                        target = n1 + "%" + n2;
                    }
                    else{
                        if(t1.size() == 0 && t2.size() == 0) continue;
                        if(out.g.getNode(source) == null) source = a.getId() + "%" + b.getId();
                        target = (t1.size() == 0) ? "%" + t2.get(0).getId() : t1.get(0).getId();
                    }
                    boolean addNode = out.g.getNode(target.replace("%","-")) == null;
                    if(addNode){
                        transf.put(target,false);
                    }
                    addNodeEdge(out.g,trans,source,target,addNode);
                }
            }
            else{
                if( a == null && b == null) continue;
                Node c = (a == null) ? b : a;
                String source = curr.replace("%","-");
                for(String trans: transactions){
                    ArrayList<Node> t = checkTransition(c,trans);
                    if(t.size() > 0){
                        String target = t.get(0).getId();
                        if(a == null) target = "%" + target;
                        boolean addNode = out.g.getNode(target.replace("%","-")) == null;
                        if(addNode){
                            transf.put(target,false);
                        }
                        addNodeEdge(out.g,trans,source,target,addNode);
                    }
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
