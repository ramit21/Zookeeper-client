package com.distributed.leader_election;

public interface OnElectionCallback {
	
	void onElectedToBeLeader();
	
	void onWorker();
}
