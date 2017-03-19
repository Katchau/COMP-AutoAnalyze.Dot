package comp2;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;

public class Teste {

	public static void main(String[] args) {
		String file = "test.dot";
		Graph g = new DefaultGraph("g");
		FileSource fs = null;
		try {
			fs = FileSourceFactory.sourceFor(file);
			fs.addSink(g);
			
			fs.begin(file);
			while(fs.nextEvents()){
				g.display();
			}
			
		} catch (IOException e) {
			System.err.println("Error: No such file" + file);
		}
		
		//ter isto separado porcausa da biblioteca. ver http://graphstream-project.org/doc/Tutorials/Reading-files-using-FileSource/ 
		try {
			fs.end();
		} catch( IOException e) {
			e.printStackTrace();
		} finally {
			fs.removeSink(g);
		}
	}

}
