/*
 * Copyright 2012-2017 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements WHICH ARE COMPATIBLE WITH THE APACHE LICENSE, VERSION 2.0.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.client.async;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.policy.Policy;

public abstract class AsyncSingleCommand extends AsyncCommand {
	protected int receiveSize;
	
	public AsyncSingleCommand(AsyncCluster cluster, Policy policy) {
		super(cluster, policy);
	}
	
	public AsyncSingleCommand(AsyncSingleCommand other) {
		super(other);
	}

	protected final void read() throws AerospikeException, IOException {
		if (inHeader) {
			if (! conn.read(byteBuffer)) {
				return;
			}
			byteBuffer.position(0);
			receiveSize = ((int) (byteBuffer.getLong() & 0xFFFFFFFFFFFFL));
				        
			if (receiveSize <= byteBuffer.capacity()) {
				byteBuffer.clear();
				byteBuffer.limit(receiveSize);
			}
			else {
				byteBuffer = ByteBuffer.allocateDirect(receiveSize);
			}
			inHeader = false;
		}

		if (! conn.read(byteBuffer)) {
			return;
		}
		
		if (inAuthenticate) {
			processAuthenticate();
			return;	
		}
		
		parseResult(byteBuffer);
		finish();
	}
			
	protected abstract void parseResult(ByteBuffer byteBuffer) throws AerospikeException;
}
