/* Generated By:JJTree: Do not edit this line. Accept.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class Accept extends SimpleNode {
  public Accept(int id) {
    super(id);
  }

  public Accept(AutoAnalyserParser p, int id) {
    super(p, id);
  }

  public Automata execute() {
    String id = "";
    String in = "";

    for(int i=0; i < children.length; i++) {
      if (children[i] instanceof Identifier){
        id = ((Identifier) children[i]).name;
        System.out.println(id);
      } else if (children[i] instanceof Input){
        in = ((Input) children[i]).name;
        System.out.println(in);
      } else {
        System.out.println("Shouldn't go here! (Complement)");
      }
    }


    return null;
  }
}
/* JavaCC - OriginalChecksum=092406c825d9494b4ca9040bf5245eb7 (do not edit this line) */
