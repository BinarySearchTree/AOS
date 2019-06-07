import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;  
import java.io.PrintStream; 


public class BroadcastService {
	
	private static volatile BroadcastService instance = null;
	private static volatile SysInfo system = null;
	
	private PrintStream originalPrintStream = System.out;
	
	private int id;
	private int rcv_count;
	private int snd_count;
	
	private boolean broadcast_en;
	private ReentrantLock broadcast_lock;
	
	private Map<Integer, Node> roots;                     // <root, parent> System knows which root node broadcast a msg and haven't finish yet.
	private Map<Integer, Map<Integer, Node>> children;    // <root, children> System knows for a root node, which children node are still waiting for its finish
	
	private Queue<BroadcastMsg> msg_buffer;
	
	private BroadcastService() {
		system = SysInfo.getInstance();
		id = system.get_id();
		
		rcv_count = 0;
		snd_count = 0;
		
		broadcast_en = true;
		broadcast_lock = new ReentrantLock(true);
		
		roots = new ConcurrentHashMap<>();
		children = new ConcurrentHashMap<>();
		
		msg_buffer = new ArrayDeque<>();
	}
	
	
	public static BroadcastService getInstance() {
		// double check flag for safety
		if (instance == null) {
			synchronized (SpanningTree.class) {
				if (instance == null)
					instance = new BroadcastService();
			}
		}
		return instance;
	}
	
	// Test broadcast service. New broadcast service can be
	// started only when received all ACK from its tree neighbors
	public void broadcast_service() {
		
		for (int i = 0; i < 200; i++) {
			
			try {
				while (!broadcast_en) {
					print("Waiting for last msg broadcast finish ... ");
					synchronized(broadcast_lock) {
						broadcast_lock.wait();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Node root = system.get_host();
			Map<Integer, Node> kids = new ConcurrentHashMap<>();
			kids.putAll(system.get_tree_neighbors());
			
			try {
				String fname = String.format("node_%d_snd.txt", id);
				//File file = new File("send.txt");
				File file = new File(fname);
	            PrintStream print1 = new PrintStream(new FileOutputStream(file,true),true);
	            System.setOut(print1);
	            System.out.println("======== Start Broadcasting Service ... ========");
				
			System.out.println("");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			print("======== Start Broadcasting Service ... ========");
			SystemMsg msg = generate_msg(id, id, String.format("%d", i), "MSG");
			broadcast(kids, msg);
			snd_count++;
			
			roots.put(id, root);
			children.put(id, kids);
			broadcast_en = false;
		}
		
		System.setOut(originalPrintStream);
		System.out.println("Finish broadcast the last round!");
	}
	
	
	// Process rcvd msgs. If the package is the message, if no child nodes, 
	// return ACK to parent, otherwise, continue broadcast. For ACK package,
	// remove the node from child set and return ACK to node's parent if
	// child set is empty.
	public synchronized void process(BroadcastMsg msg)   {


		
		if (system.get_state() != SystemState.FINISH) {
			print("@@@@@ Waiting for all neighbors ready, buffer msg first !");
			msg_buffer.offer(msg);
			return;
		}
		
		print("***** Received: " + msg + " ******");
		try {
			String fname = String.format("node_%d_rcv.txt", id);
//			File file = new File("receive.txt");
			File file = new File(fname);
            PrintStream print = new PrintStream(new FileOutputStream(file,true),true);
            System.setOut(print);
            System.out.println("***** Received: " + msg + " ******");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (msg.get_msg_type().equals("MSG")) {
			
			rcv_count++;
			
			System.setOut(originalPrintStream);
			System.out.println("Total messages received: " + rcv_count);
		
			try {
				String fname = String.format("node_%d_rcv.txt", id);
//				File file = new File("receive.txt");
				File file = new File(fname);
	            PrintStream print = new PrintStream(new FileOutputStream(file,true),true);
	            System.setOut(print);
	            System.out.println(String.format("It's the msg \"%s\" from parent %d, root: %d", msg.get_msg(), msg.get_src_node(), msg.get_root_node()));
	            System.out.println("Total messages received: " + rcv_count);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Node parent = system.get_tree_neighbors().get(msg.get_src_node());
			Map<Integer, Node> kids = new ConcurrentHashMap<>();
			kids.putAll(system.get_tree_neighbors());
			kids.remove(msg.get_src_node());
			
			if(kids.isEmpty()) {
				return_ack_to_parent(parent, msg);
			} else {
				SystemMsg snd_msg = generate_msg(msg.get_root_node(), id, msg.get_msg(), "MSG");
				broadcast(kids, snd_msg);
				
				roots.put(msg.get_root_node(), parent);
				children.put(msg.get_root_node(), kids);
			}
		} else if (msg.get_msg_type().equals("ACK")) {
			
			Map<Integer, Node> kids = children.get(msg.get_root_node());
			kids.remove(msg.get_src_node());
			
			if (kids.isEmpty()) {
				if (msg.get_root_node() != msg.get_dst_node()) {
					return_ack_to_parent(roots.get(msg.get_root_node()), msg);
				} else {
					print("======== Broadcast round " + snd_count + " finished! ========");
					try {
						String fname = String.format("node_%d_snd.txt", id);
						//File file = new File("send.txt");
						File file = new File(fname);
			            PrintStream print1 = new PrintStream(new FileOutputStream(file,true),true);
			            System.setOut(print1);
			            System.out.println("======== Broadcast round " + snd_count + " finished! ========");
						
					System.out.println("");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					
					broadcast_en = true;
					synchronized(broadcast_lock) {
						broadcast_lock.notifyAll();
					}
				}
				children.remove(msg.get_root_node());
				roots.remove(msg.get_root_node());
			}
		} else {
			System.out.print("ERROR! WRONG MESSAGE TYPE!!!!");
		}
	}
	
	
	public synchronized void deliver_buffered_msg() {
		while (!msg_buffer.isEmpty()) {
			print("@@@@@ Process Buffer Message !");
			process(msg_buffer.poll());
		}
	}
	
	private void return_ack_to_parent(Node parent, BroadcastMsg msg) {
		Map<Integer, Node> retn = new ConcurrentHashMap<>();
		retn.put(parent.get_id(), parent);
		SystemMsg ack = generate_msg(msg.get_root_node(), msg.get_dst_node(), msg.get_msg(), "ACK");
		broadcast(retn, ack);
	}
	
	private SystemMsg generate_msg(int root, int src, String msg, String msg_type) {
		BroadcastMsg brd = new BroadcastMsg(root, src, msg, msg_type);
		SystemMsg message = new SystemMsg(brd);
		return message;
	}
	private synchronized void broadcast(Map<Integer, Node> children, SystemMsg msg) {
		
		Node n;
		int port;
		String host = "";
		
		print("###### Broadcasting Service, Start Broadcasting ... ######");
		
		for (Map.Entry<Integer, Node> entry : children.entrySet()) {
			n = entry.getValue();
			port = n.get_port();
			host = n.get_host_name();
			
			msg.get_broadcast_service_msg().set_dst(n.get_id());
			print("Sending Message: " + msg.get_broadcast_service_msg());
			try {
				String fname = String.format("node_%d_snd.txt", id);
				//File file = new File("send.txt");
				File file = new File(fname);
	            PrintStream print1 = new PrintStream(new FileOutputStream(file,true),true);
	            System.setOut(print1);
	            System.out.println("Sending Message: " + msg.get_broadcast_service_msg());
//				System.out.println(String.format("Sending: "+ msg.get_broadcast_service_msg().get_msg()));
				
			System.out.println("");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			
			boolean run = true;
			while (run) {
				try {
					Socket client = new Socket(host, port);
					ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
					os.writeObject(msg);
					os.flush();
					os.close();
					
					run = false;
					client.close();
					
					try {
						String fname = String.format("node_%d_snd.txt", id);
						//File file = new File("send.txt");
						File file = new File(fname);
			            PrintStream print1 = new PrintStream(new FileOutputStream(file,true),true);
			            System.setOut(print1);
			            System.out.println("**** Send Finished! ****");
//						System.out.println(String.format("Sending: "+ msg.get_broadcast_service_msg().get_msg()));
						
					System.out.println("");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}					
				} catch (IOException e) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						
					}
				}
			}
		}
		print("###### Finish broadcasting! ######");
	}

	private void print(String s) {
//		System.out.println(s);
	}
}