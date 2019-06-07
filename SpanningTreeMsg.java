
// Message format when building a spanning tree 

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class SpanningTreeMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int src;
	private int dst;
	private SystemState state;
	private Map<Integer, Set<Integer>> graph;
	
	public SpanningTreeMsg(int s, int d, SystemState sta, Map<Integer, Set<Integer>> g) {
		src = s;
		dst = d;
		state = sta;
		graph = g;
	}
	
	public int get_src_node() {
		return src;
	}
	
	public int get_dst_node() {
		return dst;
	}
	
	public SystemState get_msg_state() {
		return state;
	}
	
	public Map<Integer, Set<Integer>> get_msg_graph() {
		return graph;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("src: ");
		sb.append(src);
		sb.append(", dst: ");
		sb.append(dst);
		sb.append(", state: ");
		sb.append(state);
		sb.append(" graph: [");
		
		for (Map.Entry<Integer, Set<Integer>> entry: graph.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" -> ");
			for (Integer i : entry.getValue()) {
				sb.append(i);
				sb.append(" ");
			}
			sb.append(", ");
		}
		sb.append("]");
		
		return sb.toString();
	}
}