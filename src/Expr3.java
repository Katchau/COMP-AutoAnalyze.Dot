/* Generated By:JJTree: Do not edit this line. Expr3.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class Expr3 extends SimpleNode {
    public Expr3(int id) {
      super(id);
    }

    public Expr3(AutoAnalyserParser p, int id) {
      super(p, id);
    }

    public Automata execute() {
        Automata res = null;
        for(int i=0; i < children.length; i++) {
            if (children[i] instanceof Identifier){
                res = Start.curAutomatas.get(((Identifier) children[i]).name);
            } else if(children[i] instanceof Expr1) {
                res = children[i].execute();
            } else {
                AutoAnalyser.addToResult("Shouldn't go here (Expr3)");
            }
        }
        return res;
    }
}
/* JavaCC - OriginalChecksum=ff5c9608531dc23ec8680c5c5a120da4 (do not edit this line) */