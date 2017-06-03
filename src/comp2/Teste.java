package comp2;

public class Teste {
	
	//atencao! os edges de um determinado no nao sao todos com origem nesse no!
	//e sempre preciso fazer esta verificacao 
	//if(endNode.equals(testNode) && !endNode.equals(startNode))continue;
	public static void main(String[] args) {
		Automata at = new Automata("saved2.dot");
		if(at.type > 1)AutomataOperations.convert2DFA(at);
		if(at.type == 1){
		    at.type = 0;
		    AutomataOperations.addDeathState(at,at.g);
        }


	}

}
