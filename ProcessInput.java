/**********************************************************************
 * This thread designed for processing message that node received
 * 
 * Revise    Time          Description          Author
 *  v1.0   2018/10/14         Initial              YG
 * 
 *********************************************************************/

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ProcessInput extends Thread{
	
	private SpanningTree spanning_tree = null;
	private BroadcastService broadcast_service = null;
	private SysInfo system = null;
	private Socket sock;
	
	public ProcessInput(Socket s) {
		spanning_tree = SpanningTree.getInstance();
		broadcast_service = BroadcastService.getInstance();
		system = SysInfo.getInstance();
		sock = s;
	}
	
	public void run() {
		try {

			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));
			SystemMsg message = null;	
			try {
				message = (SystemMsg) is.readObject();
	
			} catch (FileNotFoundException e1)
			{		 
				e1.printStackTrace();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			is.close();
		
			if (message.get_msg_type().equals("SPAN")) {
				
				print("Receved spanning tree msg: " + message.get_spanning_tree_msg());
				spanning_tree.process(message.get_spanning_tree_msg());
				
			} else if (message.get_msg_type().equals("BRD")) {
				broadcast_service.process(message.get_broadcast_service_msg());
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void print(String s) {
//		System.out.println(s);
	}
}