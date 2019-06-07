import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class SystemMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private SpanningTreeMsg sp_msg;
	private BroadcastMsg bc_msg;
	private String msg_type;        // "SPAN" or "BRD"
	
	public SystemMsg(BroadcastMsg bc) {
		bc_msg = bc;
		msg_type = "BRD";
	}
	
	public SystemMsg(SpanningTreeMsg sp) {
		sp_msg = sp;
		msg_type = "SPAN";
	}
	
	public String get_msg_type() {
		return msg_type;
	}
	
	public SpanningTreeMsg get_spanning_tree_msg() {
		return sp_msg;
	}
	
	public BroadcastMsg get_broadcast_service_msg() {
		return bc_msg;
	}
}