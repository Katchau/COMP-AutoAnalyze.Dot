package comp2;

public class Teste {
	
	public static void main(String[] args) {
		Automata at = new Automata("test.dot");
		AutomataOperations.convert2DFA(at);
	}

}
