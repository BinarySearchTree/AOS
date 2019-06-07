import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpanningTree {
	
	private static volatile SpanningTree instance = null;
	private static volatile SysInfo system = null;
	private static volatile BroadcastService broadcast_service = null;
	
	private int id;
	
	private Map<Integer, Node> active_neighbors;
	
	private Set<Integer> diff_neighbors;
	private Set<Integer> current_vertex;
	private Set<Integer> rcv_vertex;
	
	private Map<Integer, Set<Integer>> graph;
	
	private Queue<SpanningTreeMsg> msg_buffer;
	
	private PrintStream originalPrintStream = System.out;
	
	
	private SpanningTree() {
		broadcast_service = BroadcastService.getInstance();
		system = SysInfo.getInstance();
		id = system.get_id();
		
		active_neighbors = new ConcurrentHashMap<>();
		graph = new ConcurrentHashMap<>();
		
		diff_neighbors = new HashSet<>();
		current_vertex = new HashSet<>();
		rcv_vertex = new HashSet<>();
		
		msg_buffer = new ArrayDeque<>();
	}
	
	public static SpanningTree getInstance() {
		// double check flag for safety
		if (instance == null) {
			synchronized (SpanningTree.class) {
				if (instance == null)
					instance = new SpanningTree();
			}
		}
		return instance;
	}
	
	public void init() {
		graph.put(id, new HashSet<Integer>());
		
		for (Node n : system.get_neighbors()) {
			active_neighbors.put(n.get_id(), n);
		}
		
		diff_neighbors.clear();
		current_vertex.clear();
		rcv_vertex.clear();
	}
	
	// root node will start from BROADCAST directly
	public void root_activate() {
		SystemMsg message = generate_msg(id);
		process(message.get_spanning_tree_msg());
	}
	
	/********************************************************************************************
	 * Process received msg in building tree stage.Using finite state machine. Root node start
	 * from BROADCAST directly, other nodes begin with INACTIVE, waiting for root/parent node
	 * wake up. After waking up, it transfer to BROADCAST, then switch to RECEIVED without 
	 * condition. exit RECEIVED state only when no new vertex is known from current msgs send 
	 * by all neighbors, and switch to GETALL. When all neighbors is ready, it leave GETALL and
	 * enter FINISH state. At this time, broadcast service can be started!
	 *******************************************************************************************/
	public synchronized void process(SpanningTreeMsg message) {
		
		switch (system.get_state()) {
			case INACTIVE:
				system.set_state(SystemState.BROADCAST);
				
			case BROADCAST:
				merge(message.get_msg_graph());
				build_edge(message.get_src_node());
				broadcast();
//				print_graph();
				system.set_state(SystemState.RECEIVE);
				
				print("State change from BROADCAST to RECEIVE, set vertex_diff_set, neighbors_diff_set!");
				current_vertex.addAll(graph.keySet());
				diff_neighbors.addAll(active_neighbors.keySet());
				rcv_vertex.clear();
				
				break;
				
			case RECEIVE:
				merge(message.get_msg_graph());
				if (message.get_msg_state() == SystemState.GETALL) {
					print("BUFFER ! " + message.get_src_node() + " knows all vertex !");
					msg_buffer.offer(message);
				}
				
				rcv_vertex.addAll(message.get_msg_graph().keySet());
				diff_neighbors.remove(message.get_src_node());
				if (diff_neighbors.isEmpty()) {
					print("!! RECEIVE all nodes, vertex: " + rcv_vertex);
					
					Set<Integer> diff_vertex = rcv_vertex;
					diff_vertex.removeAll(current_vertex);
					if (diff_vertex.isEmpty()) {
						system.set_state(SystemState.GETALL);
					}
					
					diff_neighbors.addAll(active_neighbors.keySet());
					rcv_vertex.clear();
					current_vertex.addAll(graph.keySet());

					broadcast();
//					print_graph();
					while (!msg_buffer.isEmpty()) {
						SpanningTreeMsg m = msg_buffer.poll();
						print(m.get_src_node() + " knows all vertex !");
						active_neighbors.remove(m.get_src_node());
					}
				}
				
				break;
				
			case GETALL:
				if (message.get_msg_state() == SystemState.GETALL) {
					print(message.get_src_node() + " knows all vertex !");
					active_neighbors.remove(message.get_src_node());
				}
				break;
				
			case FINISH:
				System.out.println("ERROR! Received graph msg after finish building spanning tree!");
				break;
				
			default:
				break;
		}
		
		// termination detection
		if (active_neighbors.isEmpty()) {
				
			// find out tree neighbors
			Map<Integer, Node> tn = new HashMap<>();
			for (Node n : system.get_neighbors()) {
				if (graph.get(id).contains(n.get_id())) {
					tn.put(n.get_id(), n);
				}
			}
			system.set_tree_neighbors(tn);
			
			print_tree_neighbors();
			//print_graph();
			System.out.println("All Neighbors are ready ! Broadcast can be started! ");
			
			system.set_state(SystemState.FINISH);
			broadcast_service.deliver_buffered_msg();
			broadcast_service.broadcast_service();
		}

	}
	

    // Merge graph's info into a message into node's graph 
	private void merge(Map<Integer, Set<Integer>> msg_graph) {
		
		for (Map.Entry<Integer, Set<Integer>> entry : msg_graph.entrySet()) {
			
			int vertex = entry.getKey();
			Set<Integer> unknown_edges = entry.getValue();
			
			if (!graph.containsKey(vertex)) {
				
				graph.put(vertex, unknown_edges);
			} else {
				
				Set<Integer> new_edges = graph.get(vertex);
				new_edges.addAll(unknown_edges);
				graph.put(vertex, new_edges);
			}
		}
	}
	
	// Build an edge between src and dst when first time
	// the node is waken up
	private void build_edge(int node) {
		if (node != id) {
			Set<Integer> edge = new HashSet<>();
			edge.add(id);
			graph.put(node, edge);
			
			edge = graph.get(id);
			edge.add(node);
			graph.put(id, edge);
		}
	}
	
	public void print_tree_neighbors() {
		
		List<Integer> neighbors = new ArrayList<>();
		for (Integer n : graph.get(id)) {
			neighbors.add(n);
		}
		Collections.sort(neighbors);
		System.out.println(String.format("node %d has tree neighbors: %s", id, neighbors));
		
		try {
			String fname = String.format("node_%d_tree_neighbor.txt", id);
//			File file = new File("receive.txt");
			File file = new File(fname);
            PrintStream print = new PrintStream(new FileOutputStream(file,true),true);
            System.setOut(print);
            System.out.println(String.format("node %d has tree neighbors: %s", id, neighbors));
            System.setOut(originalPrintStream);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public SystemMsg generate_msg(int dst) {
		SpanningTreeMsg span = new SpanningTreeMsg(id, dst, system.get_state(), graph);		
		SystemMsg message = new SystemMsg(span);

		return message;
	}
	private void broadcast() {
		
		Node n;
		int port;
		String host = "";
		
		print("========= Start Broadcasting ... ========");
		
		for (Map.Entry<Integer, Node> entry : active_neighbors.entrySet()) {
			n = entry.getValue();
			port = n.get_port();
			host = n.get_host_name();
			
			SystemMsg m = generate_msg(n.get_id());
			print("Sending 1/2 Done! Message: " + m.get_spanning_tree_msg());
			
			boolean run = true;
			while (run) {
				try {
					Socket client = new Socket(host, port);
					ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
					os.writeObject(m);
					os.flush();
					os.close();
					
					run = false;
					client.close();
					print(String.format("Sending 2/2 Done! Send to %s, port: %d",host, port));
				} catch (IOException e) {
					print("Sending ... Connection failed, reconnecting in 2 sec");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						
					}
				}
			}
		}
		print("========= Finish broadcasting! ===========");
	}
	
	
	// Used for debugging
	public void print_graph() {
		System.out.println(">>>>>> Graph Info <<<<<<<");
		for (Map.Entry<Integer, Set<Integer>> entry : graph.entrySet()) {
			System.out.println(String.format("%d - %s", entry.getKey(), entry.getValue()));
		}
	}
	private void print(String s) {
//		System.out.println(s);
	}
	
}