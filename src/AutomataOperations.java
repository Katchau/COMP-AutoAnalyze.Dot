import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.graphstream.graph.*;
import org.graphstream.graph.Node;
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
            if(!transi.contains(trans))
                g.getEdge(edge).setAttribute("label",transi + "," + trans);
        }

    }

    private static void addDeathState(Automata a, Graph g){
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

    private static Automata convert2DFA(final Automata a){
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
    public static boolean acceptString(final Automata a, String verify){
        ArrayList<String> transactions = new ArrayList<>(Arrays.asList(verify.split(",")));
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

    public static Automata getDfa(Automata out){
        out.getAutomataType();
        if(out.type > 1){
            out = AutomataOperations.convert2DFA(out);
            out.getAutomataType();
            return out;
        }
        else{
            if(out.type == 1) {
                out.type = 0;
                AutomataOperations.addDeathState(out, out.g);
            }
            out.getAutomataType();
            return out;
        }
    }

    public static Automata getComplement(final Automata in) {
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
            String nodeOppType = n.getId().equals("new-death-node") ?
                    n.getId() : reverseNode(n);
            out.g.addNode(nodeOppType);
        }

        for(int i = 0; i <  g.getEdgeCount(); i++){
            Edge e = g.getEdge(i);
            Node start = e.getNode1();
            Node end = e.getNode0();

            String startNodeType = reverseNode(start);
            String endNodeType = reverseNode(end);

            if(start.getId().equals("new-death-node")) {
                out.g.addEdge(e.getId(), endNodeType, start.getId() , true);
            } else if(end.getId().equals("new-death-node")) {
                out.g.addEdge(e.getId(), start.getId(), startNodeType, true);
            } else {
                out.g.addEdge(e.getId(), startNodeType, endNodeType, true);
            }
            out.g.getEdge(e.getId()).setAttribute("label",e.getAttribute("label").toString());
        }
        return getDfa(out);
    }

    // MORGAN LAW -> L1 ? L2 = not(not(L1) ? not(L2))
    public static Automata getDifference(final Automata in1,final Automata in2) {
        Automata comp = getComplement(in2);
        Automata diff = getIntersection(in1,comp);
        if(diff == null) return null;
        return getDfa(diff);
    }

    private static boolean isFinalInter(String state){
        String test = state + "-";
        String[] test2 = test.split("_end");
        if(test2.length > 2)
            return true;
        return false;
    }

    public static Automata getIntersection(final Automata ini1, final Automata ini2){
        Automata out = new Automata();
        HashMap<String, String> replacements = new HashMap<>();
        boolean valid = false;
        Automata in1 = reverteConditions(ini1);
        Automata in2 = reverteConditions(ini2);

        out.g = new DefaultGraph("intersection");
        String start = in1.start.getId() + "-" + in2.start.getId();
        if(Automata.endState.matcher(start).matches()){
            if(!isFinalInter(start)){
                replacements.put(start.replace("_end","_fakend"),start);
                start = start.replace("_end","_fakend");
            }
            else valid = true;
        }
        out.g.addNode(start);
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
                if(replacements.containsKey(states.get(0)))
                    a = in1.g.getNode(replacements.get(states.get(0)));
                else a = in1.g.getNode(states.get(0));
                if(replacements.containsKey(states.get(1)))
                    b = in2.g.getNode(replacements.get(states.get(1)));
                else b = in2.g.getNode(states.get(1));
            }
            else{
                if(states.size() == 0)break;
                if(curr.startsWith("%")) {
                    if(replacements.containsKey(states.get(0)))
                        b =  in2.g.getNode(replacements.get(states.get(0)).replace("%",""));
                    else b =  in2.g.getNode(states.get(0).replace("%",""));
                }
                else {
                    if(replacements.containsKey(states.get(0)))
                        a = in1.g.getNode(replacements.get(states.get(0)));
                    else a = in1.g.getNode(states.get(0));
                }
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
                        boolean meh = false;
                        if(t1.size() == 0 && t2.size() == 0) continue;
                        if(out.g.getNode(source) == null) source = a.getId() + "%" + b.getId();
                        target = (t1.size() == 0) ? "%" + t2.get(0).getId() : t1.get(0).getId();
                    }
                    boolean addNode = out.g.getNode(target.replace("%","-")) == null;
                    if(addNode){
                        if(Automata.endState.matcher(target).matches()){
                            if(!isFinalInter(target)){
                                replacements.put(target.replace("_end","_fakend"),target);
                                target = target.replace("_end","_fakend");
                            }
                            else valid = true;
                        }
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
                            if(Automata.endState.matcher(target).matches()){
                                if(!isFinalInter(target)){
                                    replacements.put(target.replace("_end","_fakend"),target);
                                    target = target.replace("_end","_fakend");
                                }
                                else valid = true;
                            }
                            transf.put(target,false);
                        }
                        addNodeEdge(out.g,trans,source,target,addNode);
                    }
                }
            }
        }
        if(!valid)return null;
        return getDfa(out);
    }

    private static Automata reverteConditions(final Automata a){
        Automata a2 = new Automata();
        a2.g = new DefaultGraph(a.g.getId());
        for(Node n : a.g.getNodeSet()){
            a2.g.addNode(n.getId().replace("-",""));
        }
        for(Edge e : a.g.getEdgeSet()){
            a2.g.addEdge(e.getId().replace("-",""),
                    e.getNode0().getId().replace("-",""),
                    e.getNode1().getId().replace("-",""), true)
                    .setAttribute("label",e.getAttribute("label").toString());
        }
        a2.getAutomataType();
        return a2;
    }

    public static Automata getUnion (final Automata ini1, final Automata ini2) {
        Automata out = new Automata();
        Automata in1 = reverteConditions(ini1);
        Automata in2 = reverteConditions(ini2);

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
        return getDfa(out);
    }

    public static Automata getConcatenate(final Automata a,final Automata b) {
        Automata out = new Automata();
        out.g = new DefaultGraph("a");
        out.start = a.start;

        ArrayList<String> endsA = new ArrayList<String>();

        int intermediateCount = 0;
        for(int i = 0; i < a.g.getNodeCount(); i++) {
            Node nA = a.g.getNode(i);
            String nAtype = nA.getId();

            //store endpoints to add automata b
            if(Automata.endState.matcher(nAtype).matches()) {
                nAtype = reverseNode(nA);
                endsA.add(nAtype);
            }
            if(nAtype.contains("intermidiate")){
                intermediateCount++;
            }
            out.g.addNode(nAtype);
        }

        //add node and remove endstate
        for(int i = 0; i< a.g.getEdgeCount(); i++) {
            Edge nA = a.g.getEdge(i);
            Node source = nA.getNode0();
            Node dest = nA.getNode1();
            String sourceName = (Automata.endState.matcher(source.getId()).matches()) ? reverseNode(source) : source.getId();
            String destName = (Automata.endState.matcher(dest.getId()).matches()) ? reverseNode(dest) : dest.getId();
            out.g.addEdge(sourceName+destName, sourceName, destName, true);
            out.g.getEdge(sourceName+destName).setAttribute("label",nA.getAttribute("label").toString());
        }

        // add all node non start
        HashMap<String,String> tracker = new HashMap<String,String>();
        for(int i = 0; i < b.g.getNodeCount(); i++) {
            Node nB = b.g.getNode(i);
            String nBtype = nB.getId();

            if(Automata.startState.matcher(nB.getId()).matches()) {
                System.out.println("starter");
                String newName = nBtype.replaceAll("start","") + "intermidiate" + intermediateCount;
                intermediateCount++;
                tracker.put(nBtype,newName);
                out.g.addNode(newName);
                for(String end: endsA){
                    out.g.addEdge(newName+end,end, newName,true);
                    out.g.getEdge(newName+end).setAttribute("label","epsilon");
                }
            } else {
                out.g.addNode(nBtype);
            }
        }

        for(int i = 0; i < b.g.getEdgeCount(); i++) {
            Edge edgeB = b.g.getEdge(i);
            String source = edgeB.getNode0().getId();
            String dest = edgeB.getNode1().getId();

            if(Automata.startState.matcher(source).matches()) {
                source = tracker.get(source);
            }
            if(Automata.startState.matcher(dest).matches()) {
                dest = tracker.get(dest);
            }
            out.g.addEdge(source+dest, source,dest,true);
            out.g.getEdge(source+dest).setAttribute("label", edgeB.getAttribute("label").toString());
        }
        return getDfa(out);
    }

    private static String reverseNode(Node n){
        String nodeType = n.getId();
        nodeType = (Automata.endState.matcher(nodeType).matches()) ?
                nodeType.replaceAll("_end", "") : nodeType + "_end";
        return nodeType;
    }


}
