package com.distributed.leader_election;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.zookeeper.KeeperException;

public class OnElectionAction implements OnElectionCallback {

	private final ServiceRegistry serviceRegistry;
	private final int port;

	public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
		this.serviceRegistry = serviceRegistry;
		this.port = port;
	}

	@Override
	public void onElectedToBeLeader() { //called for the leader
		serviceRegistry.unregisterFromCluster();
		serviceRegistry.registerForUpdates();
	}

	@Override
	public void onWorker() { //called for the worker
		try {
			String currentServerAddress = String.format("http://%s:%d",
					InetAddress.getLocalHost().getCanonicalHostName(), port);
			serviceRegistry.registerToCluster(currentServerAddress);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
