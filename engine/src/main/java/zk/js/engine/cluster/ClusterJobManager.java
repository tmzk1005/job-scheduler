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

import zk.js.engine.job.JobDefinition;

/**
 * 集群环境下，维持每个节点正在运行的任务信息
 */
public interface ClusterJobManager<N extends Node<?>> {

    /**
     * 把给定的JobDefinition分配给某个节点执行
     */
    NodeAndJobId<N> assign(JobDefinition jobDefinition, boolean singleton);

    /**
     * 任务退出了调用此方法，实现信息的更新
     * reAssign决定是否是因为此节点不能执行此Job而要求重新分配其他节点执行
     */
    void jobFinished(String jobId, N node, boolean reAssign);

    N getAssigner(String jobId);

    void stopJobOnNode(String jobId, N node);

}
