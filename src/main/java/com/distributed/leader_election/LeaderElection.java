package com.distributed.leader_election;

import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class LeaderElection implements Watcher {
	//Create this /election root node via cli
	private static final String ELECTION_NAMESPACE = "/election"; 
	private String currentZnodeName;
	private ZooKeeper zooKeeper;
	private final OnElectionCallback onElectionCallback;
	//private static final String TARGET_ZNODE = "/target_znode";
	
	public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
		this.zooKeeper = zooKeeper;
		this.onElectionCallback = onElectionCallback;
	}

	public void volunteerForLeadership() throws KeeperException, InterruptedException {
		String znodePrefix = ELECTION_NAMESPACE + "/c_";
		String znodeFullPath = zooKeeper.create(znodePrefix, new byte[] {},
				ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//empty byte array represents node without data
		System.out.println("znode name = "+znodeFullPath);
		//extract znode name without the full path
		this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
	}
	
	//Fault tolerant leader election
	//Selecting the node with lowest id as the leader
	public void relectLeader() throws KeeperException, InterruptedException {
		Stat predecessorStat = null;
		String predecessorZnodeName = "";
		while(predecessorStat==null) {
			List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
			Collections.sort(children);
			String smallestChild = children.get(0);
			if(smallestChild.equals(currentZnodeName)) {
				System.out.println("I am the leader");
				onElectionCallback.onElectedToBeLeader();
				return;
			}else {
				//Every non-leader node will watch its predecessor node.
				System.out.println("I am not the leader");
				int predecessorIndex = Collections.binarySearch(children, currentZnodeName) -1;
				predecessorZnodeName = children.get(predecessorIndex);
				predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnodeName, this);
			}
		}
		onElectionCallback.onWorker();
		
		System.out.println("Watching znode "+predecessorZnodeName);
		System.out.println();
	}
	
	@Override
	//Zookeeper client API is Async, hence we need watcher events to react to async messages like 
	//connect or disconnect. Hence we implement Watcher interface and override this process method.
	//Common events to check for: None, NodeDeleted, NodeCreated, NodeDataChanged, NodeChildrenChanged
	public void process(WatchedEvent event) {
		switch(event.getType()) {
			case NodeDeleted:
				try {
					relectLeader();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				break;
		}
		//watchTargetZnodes();
	}
	
	/*
	private void watchTargetZnodes() throws KeeperException, InterruptedException {
		Stat stat = zooKeeper.exists(TARGET_ZNODE, this);
		if(stat == null) {
			return;
		}
		byte[] data = zooKeeper.getData(TARGET_ZNODE, this, stat);
		List<String> children = zooKeeper.getChildren(TARGET_ZNODE, this);
		System.out.println("Data: " + new String(data) + ", children : " + children);
	}
	*/
}
