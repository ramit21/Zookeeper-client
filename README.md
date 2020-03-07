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

The process() method of the watcher interface is what get's invoked when the corresponding zookeeper tree strucutre is modified. This can be used to carry out activities like re-elect leader, update registry with added/removed instance, auto healing (in case of znode deletion due to termination of corresponding instance) etc.

See how every node is wathcing the status of it's previous node. Also see how cluster addresses registered with the leader change when you add/terminate an instance. Also try terminating the leader node, and see the next node become the new leader.

