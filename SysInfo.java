/**************************************************************************
 * This class designed for recording system info parsed from config file
 * 
 * Revise    Time          Description          Author
 *  v1.0   2018/10/14         Initial              YG
 * 
 *************************************************************************/

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

public class SysInfo {
	
	private static volatile SysInfo instance = null;
	
	private int id;
	private int network_size;
	
	private Node host;
	private List<Node> neighbors;
	private Map<Integer, Node> tree_neighbors;
	
	private SystemState state;
	
	private SysInfo() {
		neighbors = new ArrayList<>();
		tree_neighbors = new ConcurrentHashMap<>();
	}
	
	public static SysInfo getInstance() {
		// Double check flag for safety
		if(instance == null) {
			synchronized (Parse.class) {
				if(instance == null) {
					instance = new SysInfo();
				}
			}
		}
		return instance;
	}
	
	public void set_id(int i) {
		id = i;
	}
	
	public int get_id() {
		return id;
	}
	
	public void set_network_size(int s) {
		network_size = s;
	}
	
	public int get_network_size() {
		return network_size;
	}
	
	public void set_host(Node h) {
		host = h;
	}
	
	public Node get_host() {
		return host;
	}
	
	public void set_neighbors(List<Node> n) {
		neighbors = n;
	}
	public List<Node> get_neighbors() {
		return neighbors;
	}
	public int get_neighbors_size() {
		return neighbors.size();
	}
	
	public void set_tree_neighbors(Map<Integer, Node> tn) {
		tree_neighbors = tn;
	}
	public Map<Integer, Node> get_tree_neighbors() {
		return tree_neighbors;
	}
	
	public SystemState get_state() {
		return state;
	}
	public void set_state(SystemState s) {
		state = s;
	}
	
	public void display_sysinfo() {
		System.out.println("===============System Info===================");
		System.out.println("node id: " + id);
		System.out.println("network_size: " + network_size);
		System.out.println("running on: " + host);
		System.out.println("State: " + state);		
		System.out.println("neighbors: ");
		for(Node n : neighbors) {
			System.out.println(n);
		}
		System.out.println("==============End of sysinfo=================");
	}
}