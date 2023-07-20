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
package zk.js.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zk.js.engine.job.JobConverter;
import zk.js.engine.job.JobStore;
import zk.js.engine.job.MemoryJobStore;

/**
 * 单机模式的引擎，任务信息都维持在内存里，因此限制总共可以同时执行的任务数，主要用于测试
 */
public class StandaloneJobEngine extends AbstractJobEngine {

    public StandaloneJobEngine(JobConverter jobConverter, ExecutorService executorService, JobStore jobStore) {
        super(jobConverter, executorService, jobStore);
    }

    public StandaloneJobEngine(JobConverter jobConverter) {
        super(jobConverter, Executors.newCachedThreadPool(), new MemoryJobStore());
        setMaxRunningJobCount(1000L);
    }

}
