/***********************************************
 * Message format when broadcasting a message  *
 * among distributed system                    *
 ***********************************************/

import java.io.Serializable;

public class BroadcastMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int root;
	private int src;
	private int dst;
	private String msg;
	private String msg_type;    // MSG: message,  ACK: acknowledge
	
	public BroadcastMsg(int r, int s, String m, String mt) {
		root = r;
		src = s;
		msg = m;
		msg_type = mt;
	}
	
	public BroadcastMsg(int r, int s, int d, String m, String mt) {
		root = r;
		src = s;
		dst = d;
		msg = m;
		msg_type = mt;
	}
	
	public void set_dst(int d) {
		dst = d;
	}
	
	public int get_root_node() {
		return root;
	}
	
	public int get_src_node() {
		return src;
	}
	
	public int get_dst_node() {
		return dst;
	}
	
	public String get_msg() {
		return msg;
	}
	
	public String get_msg_type() {
		return msg_type;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("root: ");
		sb.append(root);
		sb.append(", src: ");
		sb.append(src);
		sb.append(", dst: ");
		sb.append(dst);
		sb.append(", msg: ");
		sb.append(msg);
		sb.append(", msg type: ");
		sb.append(msg_type);
		
		return sb.toString();
	}
}