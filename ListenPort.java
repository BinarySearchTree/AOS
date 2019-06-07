import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListenPort extends Thread {
	
	private SpanningTree spanning_tree = null;
	private SysInfo system = null;
	
	private int port;
	private int id;
	
	public ListenPort(int p) {
		spanning_tree = SpanningTree.getInstance();
		system = SysInfo.getInstance();
		
		id = system.get_id();
		port = p;
	}
	
	public void run() {
		
		try {
			ServerSocket serverSock = new ServerSocket(port);
			
			while (true) {
				
				Socket sock = serverSock.accept();
				
				(new ProcessInput(sock)).start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}