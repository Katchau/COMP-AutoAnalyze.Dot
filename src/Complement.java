/* Generated By:JJTree: Do not edit this line. Complement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

// import org.graphstream.graph.*;
// import org.graphstream.graph.implementations.DefaultGraph;
// import org.graphstream.stream.file.FileSinkDOT;

public
class Complement extends SimpleNode {


  public Complement(int id) {
    super(id);
  }

  public Complement(AutoAnalyserParser p, int id) {
    super(p, id);
  }

  public Automata execute(){
    Automata at = null;
    Automata out = null;

    for(int i=0; i < children.length; i++) {
      if (children[i] instanceof Expr3){
        at = children[i].execute();
      } else {
        System.out.println("Shouldn't go here! (Complement)");
      }
    }

    //TODO Automata out = getDifference(aut);

    return out;
  }
}
/* JavaCC - OriginalChecksum=9710604948378a16cf4cc7a9a3c1c1d4 (do not edit this line) */
