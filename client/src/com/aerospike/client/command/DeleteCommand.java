/*
 * Aerospike Client - Java Library
 *
 * Copyright 2013 by Aerospike, Inc. All rights reserved.
 *
 * Availability of this source code to partners and customers includes
 * redistribution rights covered by individual contract. Please check your
 * contract for exact rights and responsibilities.
 */
package com.aerospike.client.command;

import java.io.IOException;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Key;
import com.aerospike.client.ResultCode;
import com.aerospike.client.cluster.Cluster;
import com.aerospike.client.cluster.Connection;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.WritePolicy;

public final class DeleteCommand extends SingleCommand {
	private final WritePolicy policy;
	private boolean existed;

	public DeleteCommand(Cluster cluster, WritePolicy policy, Key key) {
		super(cluster, key);
		this.policy = (policy == null) ? new WritePolicy() : policy;
	}
	
	@Override
	protected Policy getPolicy() {
		return policy;
	}

	@Override
	protected void writeBuffer() throws AerospikeException {
		setDelete(policy, key);
	}

	protected void parseResult(Connection conn) throws AerospikeException, IOException {
		// Read header.		
		conn.readFully(dataBuffer, MSG_TOTAL_HEADER_SIZE);
		int resultCode = dataBuffer[13] & 0xFF;
	
	    if (resultCode != 0 && resultCode != ResultCode.KEY_NOT_FOUND_ERROR) {
	    	throw new AerospikeException(resultCode);        	
	    }        	
		existed = resultCode == 0;
		emptySocket(conn);
	}
	
	public boolean existed() {
		return existed;
	}
}
