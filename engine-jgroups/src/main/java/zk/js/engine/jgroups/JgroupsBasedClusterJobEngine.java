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
package zk.js.engine.jgroups;

import java.util.concurrent.ExecutorService;

import zk.js.engine.AbstractJobEngine;
import zk.js.engine.job.JobConverter;
import zk.js.engine.job.JobStore;

public class JgroupsBasedClusterJobEngine extends AbstractJobEngine {

    protected JgroupsBasedClusterJobEngine(JobConverter jobConverter, ExecutorService executorService, JobStore jobStore) {
        super(jobConverter, executorService, jobStore);
    }

}
