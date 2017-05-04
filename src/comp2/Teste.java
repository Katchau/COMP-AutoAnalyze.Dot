package comp2;

public class Teste {
	
	//atencao! os edges de um determinado no nao sao todos com origem nesse no!
	//e sempre preciso fazer esta verificacao 
	//if(endNode.equals(testNode) && !endNode.equals(startNode))continue;
	public static void main(String[] args) {
		Automata at = new Automata("saved.dot");
		AutomataOperations.convert2DFA(at);
		System.out.println(AutomataOperations.acceptString(at, "a%b%a%b%a%b%b%a%a%b%b"));
	}

}
