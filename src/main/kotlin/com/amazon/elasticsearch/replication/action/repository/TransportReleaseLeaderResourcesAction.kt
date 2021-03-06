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

import com.amazon.elasticsearch.replication.repository.RemoteClusterRestoreLeaderService
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.support.ActionFilters
import org.elasticsearch.action.support.master.AcknowledgedResponse
import org.elasticsearch.action.support.single.shard.TransportSingleShardAction
import org.elasticsearch.cluster.ClusterState
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver
import org.elasticsearch.cluster.routing.ShardsIterator
import org.elasticsearch.cluster.service.ClusterService
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.Writeable
import org.elasticsearch.index.shard.ShardId
import org.elasticsearch.threadpool.ThreadPool
import org.elasticsearch.transport.TransportActionProxy
import org.elasticsearch.transport.TransportService

class TransportReleaseLeaderResourcesAction @Inject constructor(threadPool: ThreadPool, clusterService: ClusterService,
                                                                transportService: TransportService, actionFilters: ActionFilters,
                                                                indexNameExpressionResolver: IndexNameExpressionResolver,
                                                                private val restoreLeaderService: RemoteClusterRestoreLeaderService) :
        TransportSingleShardAction<ReleaseLeaderResourcesRequest, AcknowledgedResponse>(ReleaseLeaderResourcesAction.NAME,
                threadPool, clusterService, transportService, actionFilters,
                indexNameExpressionResolver, ::ReleaseLeaderResourcesRequest, ThreadPool.Names.GET) {
    init {
        TransportActionProxy.registerProxyAction(transportService, ReleaseLeaderResourcesAction.NAME, ::AcknowledgedResponse)
    }

    companion object {
        private val log = LogManager.getLogger(TransportReleaseLeaderResourcesAction::class.java)
    }

    override fun shardOperation(request: ReleaseLeaderResourcesRequest, shardId: ShardId): AcknowledgedResponse {
        log.info("Releasing resources for $shardId with restore-id as ${request.restoreUUID}")
        restoreLeaderService.removeRemoteClusterRestore(request.restoreUUID)
        return AcknowledgedResponse(true)
    }

    override fun resolveIndex(request: ReleaseLeaderResourcesRequest?): Boolean {
        return true
    }

    override fun getResponseReader(): Writeable.Reader<AcknowledgedResponse> {
        return Writeable.Reader { inp: StreamInput -> AcknowledgedResponse(inp) }
    }

    override fun shards(state: ClusterState, request: InternalRequest): ShardsIterator? {
        return state.routingTable().shardRoutingTable(request.request().leaderShardId).primaryShardIt()
    }
}
