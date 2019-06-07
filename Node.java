/**************************************************************************
 * This class is the data structure that record a node's socket info
 * 
 * Revise    Time          Description          Author
 *  v1.0   2018/10/14         Initial              YG
 * 
 *************************************************************************/

public class Node {
	private int id;
	private String host_name;
	private int port;
	
	public Node() {
		id = -1;
		host_name = "";
		port = -1;
	}
	public Node(int n, String host, int p) {
		id = n;
		host_name = host;
		port = p;
	}
	
	public void set_node_info(int n, String host, int p) {
		id = n;
		host_name = host;
		port = p;
	}
	
	public void set_id(int n) {
		id = n;
	}
	public int get_id() {
		return id;
	}
	
	public void set_host_name(String h) {
		host_name = h;
	}
	public String get_host_name() {
		return host_name;
	}
	
	public void set_port(int p) {
		port = p;
	}
	public int get_port() {
		return port;
	}
	
	@Override
	public String toString() {
		return String.format("[id=%d, host=%s, port=%d]", id, host_name, port);
	}
}