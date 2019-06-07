/************************************************
*                                               *
* Contains enum to indicate the state of system *
* while building spanning tree                  *
*                                               *
*************************************************/
public enum SystemState {
		INACTIVE,    // other node start here, waiting root/parent node to wake up
		BROADCAST,   // node is waken, broadcast its msg to its neighbors
		RECEIVE,     // keep communicate with neighbors until knows whole graph info
		GETALL,      // waiting for neighbors collect spanning tree info
		FINISH       // spanning tree building finish, broadcast service can start
	}