/* Generated By:JJTree: Do not edit this line. Expr1.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class Expr1 extends SimpleNode {
  public Expr1(int id) {
    super(id);
  }

  public Expr1(AutoAnalyserParser p, int id) {
    super(p, id);
  }

 public Automata execute() {
   Automata first = null;
   Automata res = null;

    for(int i=0; i < children.length; i++) {
       if (children[i] instanceof Concatenation){
        res = children[i].execute();
      } else if (children[i] instanceof Intersection){
        res = children[i].execute();
      } else if (children[i] instanceof Union){
        res = children[i].execute();
      } else if (children[i] instanceof Difference){
        res = children[i].execute();
      } else if (children[i] instanceof Expr2){
        res = children[i].execute();
      } else {
        System.out.println("Shouldn't go here (Expr1)");
      }
    }
    if (res == null)
      return first;
    return res;
  }
}
/* JavaCC - OriginalChecksum=b245cbe223e1688cfdbdf42df5c0c1ff (do not edit this line) */
