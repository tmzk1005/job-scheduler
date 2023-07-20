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

import zk.js.engine.common.LifecycleService;
import zk.js.engine.job.Job;
import zk.js.engine.job.JobConverter;
import zk.js.engine.job.JobDefinition;

public interface JobEngine extends LifecycleService {

    /**
     * 按照JobDefinition的定义，异步启动一个Job,返回任务的id
     *
     * @param jobDefinition Job定义
     * @param singleton     是否单例，为true表示不能存在相同id的JobDefinition已经正在运行
     * @return 启动的Job的String类型ID
     * @throws Exception 任务启动失败异常
     */
    String runJob(JobDefinition jobDefinition, boolean singleton) throws Exception;

    default String runJob(JobDefinition jobDefinition) throws Exception {
        return runJob(jobDefinition, false);
    }

    /**
     * 同步等待任务执行完成
     */
    void waitJob(String jobId) throws Exception;

    void stopJob(String jobId);

    JobConverter getJobConverter();

    Job getJob(String jobId);

}
