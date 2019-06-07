# Broadcast service in a distributed system

This project is to design and implement a broadcast service in a distributed system. Node in the distributed system are arranged in a certain topology (specified in a configuration file). First build a spanning tree using finite state machine from a root node. When the spanning tree construction completes, each node knows which subset of its neighbors are also its tree neighbors. 

Then use the spanning tree constructed before to implement a broadcast service which allows any node to send a message to all nodes in the system. The braodcast service would also inform the source node of the completion of the broadcast operation.

For the output, each node will print its set of tree neighbors. Each node would also output any broadcast message it sends or receives.
