/**********************************************************************
 * This class is used to parse information from configuration file
 * 
 * Revise    Time          Description          Author
 *  v1.0   2018/9/14         Initial              YG
 * 
 *********************************************************************/

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parse {
	
	/************************************************
	 * Func:   extract info from configuration file *
	 * Param:  int    node id                       *
	 *         String file path                     *
	 * Return: none                                 *
	 ************************************************/
	public static void load_file(int nd, String path, int root) {
		
		SysInfo system = SysInfo.getInstance();
		SystemState state;
		
		int network_size = 0;
		List<Node> hosts = new ArrayList<>();
		List<Node> neighbors = new ArrayList<>();
		
		try {
			File file_name = new File(path);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file_name));
			BufferedReader buf = new BufferedReader(reader);
			
			int num_of_valid_lines = 0;
			String line = buf.readLine();
			while (line != null) {
				
				if (line.length() > 0 && line.charAt(0) >= '0' && line.charAt(0) <= '9') {
					// valid line
					num_of_valid_lines++;
					if (num_of_valid_lines == 1) {
						// 1 line is the total number of nodes in the system
						network_size = Integer.parseInt(line);
					} else if (num_of_valid_lines <= network_size + 1) {
						// 2 : n+1 each line is node's socket info
						Node n = new Node();
	
						int i = 0, j = 0;  // i:tail, j:head
						while (i < line.length() && line.charAt(i) != '#') {
			
							while(i < line.length() && line.charAt(i) != ' ')
								i++;
							n.set_id(Integer.parseInt(line.substring(j, i)));
							while (i < line.length() && line.charAt(i) == ' ')
								i++;
							
							j = i;
							while (i < line.length() && line.charAt(i) != ' ')
								i++;
							n.set_host_name(line.substring(j, i));
							while (i < line.length() && line.charAt(i) == ' ')
								i++;
							
							j = i;
							while (i < line.length() && line.charAt(i) != ' ')
								i++;
							n.set_port(Integer.parseInt(line.substring(j, i)));
						}
						
						// Add n line's token info to hosts 
						hosts.add(n);
					} else {
						// n+2 : 2n-1 each line contain neighbors info
						int current_id;
						int i = 0, j = 0;    // i: tail, j: head
						while (i < line.length() && line.charAt(i) != ' ')
							i++;
						// First token is node id
						current_id = Integer.parseInt(line.substring(j, i));
						while (i < line.length() && line.charAt(i) == ' ')
							i++;
						
						if (current_id == nd) {	
							int id;
							j = i;
							while (i < line.length() && line.charAt(i) != '#') {
								while (i < line.length() && line.charAt(i) != ' ')
									i++;
								id = Integer.parseInt(line.substring(j, i));
								Node n = hosts.get(id);
								neighbors.add(n);
								while (i < line.length() && line.charAt(i) == ' ')
									i++;
								j = i;
							}
						}
					}
				}
				line = buf.readLine();
			}
			reader.close();
			
			if (nd == root) {
				state = SystemState.BROADCAST;
			} else {
				state = SystemState.INACTIVE;
			}
			
			system.set_id(nd);
			system.set_network_size(network_size);
			system.set_host(hosts.get(nd));
			system.set_neighbors(neighbors);
			system.set_state(state);
			
//			system.display_sysinfo();
			
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}