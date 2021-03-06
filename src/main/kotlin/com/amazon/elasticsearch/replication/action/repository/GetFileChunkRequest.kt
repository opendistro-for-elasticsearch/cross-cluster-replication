/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.elasticsearch.replication.action.repository

import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.action.support.single.shard.SingleShardRequest
import org.elasticsearch.cluster.node.DiscoveryNode
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.index.shard.ShardId
import org.elasticsearch.index.store.StoreFileMetadata
import org.elasticsearch.transport.RemoteClusterAwareRequest

class GetFileChunkRequest : RemoteClusterRepositoryRequest<GetFileChunkRequest> {
    val storeFileMetadata: StoreFileMetadata
    val offset: Long
    val length: Int

    constructor(restoreUUID: String, node: DiscoveryNode, leaderShardId: ShardId, storeFileMetaData: StoreFileMetadata,
                offset: Long, length: Int, followerCluster: String, followerShardId: ShardId):
            super(restoreUUID, node, leaderShardId, followerCluster, followerShardId) {
        this.storeFileMetadata = storeFileMetaData
        this.offset = offset
        this.length = length
    }

    constructor(input : StreamInput): super(input) {
        storeFileMetadata = StoreFileMetadata(input)
        offset = input.readLong()
        length = input.readInt()
    }

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    override fun writeTo(out: StreamOutput) {
        super.writeTo(out)
        storeFileMetadata.writeTo(out)
        out.writeLong(offset)
        out.writeInt(length)
    }
}
