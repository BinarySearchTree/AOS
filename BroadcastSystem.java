import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BroadcastSystem {
	
	private static SpanningTree spanning_tree;
	private static BroadcastService broadcast;
	private static SysInfo system;
	
	private static int id;
	
	public static void main (String[] args) {
		
		// parameter check
		if (args.length != 3)
			System.err.println("Usage: BroadcastSystem <node_id, config_file_path, root_id>");
		
		// parse info with node id, path
		id = Integer.parseInt(args[0]);
		Parse.load_file(id, args[1], Integer.parseInt(args[2]));
		
		// Initial the whole system
		system = SysInfo.getInstance();
		
		spanning_tree = SpanningTree.getInstance();
		spanning_tree.init();
		
		broadcast = BroadcastService.getInstance();
//		broadcast.init();
		
		// open a thread to listen port
		(new ListenPort(system.get_host().get_port())).start();
		
		// root node will start spanning tree process
		if (id == Integer.parseInt(args[2])) {
			spanning_tree.root_activate();
		}
		
//		// Waiting for spanning tree step finish ...
//		while (system.get_state() != SystemState.FINISH) {
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		// Test broadcast service, broadcast 1000 messages total
//		broadcast.broadcast_service();
	}
	
}