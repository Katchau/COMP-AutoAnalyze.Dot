import org.graphstream.graph.Graph;

/**
 * Created by syram on 6/5/17.
 */
public class LaunchGraphDisplay implements  Runnable {
    private Graph graph;

    public LaunchGraphDisplay (Graph graph) {
        this.graph = graph;
    }

    public void run() {
        graph.display();
    }
}
