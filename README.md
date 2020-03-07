# Zookeeper

Download zookeeper from https://archive.apache.org/dist/zookeeper/zookeeper-3.4.12/zookeeper-3.4.12.tar.gz

Setup /conf/zoo.cfg -> Setup zookeper log directory and take note of clientPort as well. This is the port that zookeeper will listen to.

start zookeeper server from the bin folder. (exe for windows and .sh for unix)

start zookeeper client to send commands to the server:

```
help
ls /   -> list nodes under root znode
create /parent "some parent data"   -> creates parent znode under root znode
create /parent/child "some child data"   -> creates child znode under parent znode
get/parent -> gives details of parent zode
rmr /parent  -> deletes parent znode
```

Run different instances of the application at different ports. 

```
/target> java -jar leader-election-0.0.1-SNAPSHOT-jar-with-dependencies.jar 8080
/target> java -jar leader-election-0.0.1-SNAPSHOT-jar-with-dependencies.jar 8081
/target> java -jar leader-election-0.0.1-SNAPSHOT-jar-with-dependencies.jar 8082
/target> java -jar leader-election-0.0.1-SNAPSHOT-jar-with-dependencies.jar 8083
```

All instances in this POC register themselves with the zookeeper service registry.

As per the leader election algorithm that's implemented in this poc, the node with the lowest no. gets elected as the leader.

The process() method of the watcher interface is what get's invoked when the corresponding zookeeper tree strucutre is modified. This can be used to carry out activities like re-elect leader, update registry with added/removed instance, auto healing (in case of znode deletion due to termination of corresponding instance) etc. Take note that Zookeeper nodes are of 2 types: persistent and ephemeral. The ephemral nodes get removed the moment the instance that registered it goes down. This ephemeral node deletion can then be caught in the wathcer interface implementation of the client application to take further actions.

See how every node is watching the **znode** of its previous node. This is done to avoid herd effect in the event of leader node failure (we don't want all nodes to query zookeeper together in the event of leader failure). Also see how cluster addresses registered with the leader change when you add/terminate an instance. Also try terminating the leader node, and see the next node become the new leader.

**Future work in this POC:**

As a final step, when a request comes in from the client, the leader splits the task into smaller chunks and gives it to the registered workers. Once data is processed and returned by the workers, the leader then merges all the responses and sends them back to the client. The way to implement is :

In the OnElectionAction.java, see how the worker nodes register themselves to service registry while the leader unregisters itself. Now, the leader must register itself with another registry say leaderRegistry, which the client can then query to fetch the address of the leader node. It then sends the request to the leader node, which then using service registry distributes the task among the workers.

## Theory

Q. What is Zookeeper?

Ans. A high performance coordination service designed specifically for distributed systems.

Many companies and projects use Zookeeper internally (eg. Kafka, Hadoop, HBase etc.)

Zookeeper itself can run as a distributed system to provide high availability and reliability. 

Zookeeper cluster should mainly consist of 3 nodes, so that even if one node goes down, the cluster is still up and running.

Zookeeper arranges znodes in a tree like structure called "ensemble".

Zookeeper manages the global order, which can be used to name the znodes sequentially as they are created. This sequential ordering can be put to use for leader election or even for locking on a resource.

znodes can also be used to store small amount of data.

Watches can be set at zookeeper clients, so that any change in zookeeper znodes get notified to the client to take appropriate action.

Writes to zookeeper are liniar, ie all nodes are updated sequentially on every write update. Concurrent writing is not possible, hence zookeeper does not perform well in write heavy systems. Also, this liniar update is the reason the zookeeper nodes are kept at 3 and not more. All in all, Zookeeper is an eventually consistent system.





