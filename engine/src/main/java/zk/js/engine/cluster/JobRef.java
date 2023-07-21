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

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import zk.js.engine.job.JobDefinition;

/**
 * 此类作用是用可以序列化的信息唯一定位到一个Job，在集群环境下每个节点保持一致的副本
 * 不直接使用Job是因为不能保证Job的实现类所有的信息都是可以序列化且便于在分布式环境下进行网络传输的
 * 因此，只维护Job的id以及其关联的JobDefinition
 */
@Getter
@Setter
public class JobRef implements Serializable {

    private String jobId;

    private JobDefinition jobDefinition;

    private String nodeId;

}
