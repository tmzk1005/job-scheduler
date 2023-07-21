/*
 * Copyright 2023 zoukang, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zk.js.engine.cluster;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import zk.js.engine.AbstractJobEngine;
import zk.js.engine.job.JobConverter;
import zk.js.engine.job.JobDefinition;
import zk.js.engine.job.JobStore;

public abstract class AbstractClusterJobEngine<N extends Node<?>> extends AbstractJobEngine implements ClusterJobManager<N> {

    private final N node;

    protected AbstractClusterJobEngine(JobConverter jobConverter, ExecutorService executorService, JobStore jobStore, N node) {
        super(jobConverter, executorService, jobStore);
        this.node = node;
    }

    @Override
    public String runJob(JobDefinition jobDefinition, boolean singleton) throws Exception {
        NodeAndJobId<N> nodeAndJobId = assign(jobDefinition, singleton);
        if (Objects.isNull(nodeAndJobId)) {
            throw new Exception("Assign job definition failed.");
        }
        if (!isMe(nodeAndJobId.getNode())) {
            // 不是分配给自己，直接返回事先分配的jobId,对应的Job应该在其他节点上启动了
            return nodeAndJobId.getJobId();
        } else {
            return doRunJob(jobDefinition, nodeAndJobId.getJobId());
        }
    }

    @Override
    public void stopJob(String jobId) {
        N assigner = getAssigner(jobId);
        if (isMe(assigner)) {
            super.stopJob(jobId);
            jobFinished(jobId, assigner, false);
        } else {
            stopJobOnNode(jobId, assigner);
        }
    }

    private boolean isMe(N targetNode) {
        Objects.requireNonNull(targetNode);
        if (Objects.isNull(this.node)) {
            throw new IllegalStateException("node not set.");
        }
        return Objects.equals(this.node.getId(), targetNode.getId());
    }

}
