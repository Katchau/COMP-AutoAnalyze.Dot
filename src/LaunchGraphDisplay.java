import org.graphstream.graph.Graph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;

/**
 * Created by syram on 6/5/17.
 */
public class LaunchGraphDisplay implements  Runnable {
    private Graph graph;

    public LaunchGraphDisplay (Graph graph) {
        this.graph = graph;
    }

    public void run() {
        Viewer t = graph.display();
        t.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);
    }
}
