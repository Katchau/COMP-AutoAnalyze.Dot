package comp2;

public class Teste {
	
	//atencao! os edges de um determinado no nao sao todos com origem nesse no!
	//e sempre preciso fazer esta verificacao 
	//if(endNode.equals(testNode) && !endNode.equals(startNode))continue;
	public static void main(String[] args) {
		Automata at1 = new Automata("teste2.dot");
		Automata at2 = new Automata("teste.dot");
        if(at1.type == 1){
            at1.type = 0;
            AutomataOperations.addDeathState(at1,at1.g);
        }
        if(at2.type == 1){
            at2.type = 0;
            AutomataOperations.addDeathState(at2,at2.g);
        }

        Automata at = AutomataOperations.getDifference(at2,at1);
        if(at == null) System.out.println("LMAO");
        else AutomataOperations.exportAutomata(at.g,"lmao3.dot");
	}

}
