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

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import zk.js.engine.common.BaseLifecycleService;
import zk.js.engine.job.Job;
import zk.js.engine.job.JobConverter;
import zk.js.engine.job.JobDefinition;
import zk.js.engine.job.JobStore;
import zk.js.engine.job.JobStoreException;

@Slf4j
public abstract class AbstractJobEngine extends BaseLifecycleService implements JobEngine {

    protected final JobConverter jobConverter;

    protected final ExecutorService executorService;

    protected final JobStore jobStore;

    @Getter
    protected long maxRunningJobCount;

    protected final Map<String, JobAndFuture> runningJobs = new ConcurrentHashMap<>(4);

    protected AbstractJobEngine(JobConverter jobConverter, ExecutorService executorService, JobStore jobStore) {
        this.jobConverter = jobConverter;
        this.executorService = executorService;
        this.jobStore = jobStore;
        setMaxRunningJobCount(Long.MAX_VALUE);
    }

    public void setMaxRunningJobCount(long number) {
        if (number <= 0L) {
            number = Long.MAX_VALUE;
        }
        this.maxRunningJobCount = number;
    }

    @Override
    public String runJob(JobDefinition jobDefinition, boolean singleton) throws Exception {
        if (singleton && existJobRunningWithDefinitionId(jobDefinition.getId())) {
            log.warn(
                    "Can not run job with definition id = {}, a job with same job definition id is running now, " +
                            "and only one instance is allowed for this job definition.",
                    jobDefinition.getId()
            );
            throw new IllegalStateException("A job with same job definition id = " + jobDefinition.getId() + " is already running.");
        }
        return doRunJob(jobDefinition, UUID.randomUUID().toString());
    }

    protected String doRunJob(JobDefinition jobDefinition, String jobId) throws Exception {
        if (runningJobs.size() >= maxRunningJobCount) {
            log.warn("Can not run job with job definition id = {}, max job count reached.", jobDefinition.getId());
            throw new IllegalStateException("Can not run job with job definition id = " + jobDefinition.getId() + ", max job count reached.");
        }
        if (isStarted()) {
            synchronized (lock) {
                if (isStarted()) {
                    Job job = getJobConverter().convertJobDefinition(jobDefinition);
                    job.setId(jobId);
                    job.init();
                    if (!job.getState().isInitialized()) {
                        throw new IllegalStateException("Job with id = {} init failed", job.getFailException());
                    }
                    JobAndFuture jobAndFuture = new JobAndFuture(job);
                    runningJobs.put(job.getId(), jobAndFuture);

                    try {
                        jobStore.saveJob(job);
                    } catch (JobStoreException exception) {
                        log.error("Failed to save job status after started it, job id = {}", job.getId());
                        runningJobs.remove(job.getId());
                        throw exception;
                    }

                    job.getStatus().setStartTimestamp(System.currentTimeMillis());
                    submitJob(jobAndFuture);
                    return job.getId();
                }
            }
        }
        log.warn("Can not run job with definition id = {} because current job engine state is {}", jobDefinition.getId(), getState());
        throw new IllegalStateException("Can not run job because current job engine state is " + getState());
    }

    private void submitJob(JobAndFuture jobAndFuture) {
        Job job = jobAndFuture.job;
        jobAndFuture.future = this.executorService.submit(job::start);
        this.executorService.execute(() -> {
            try {
                jobAndFuture.future.get();
                if (Objects.nonNull(job.getStatus().getExitType())) {
                    // 任务自己结束的时候没有根据其内部的具体情况来设置退出状态，则这里统一设置为Job.ExitType.FINISHED
                    job.getStatus().setExitType(Job.ExitType.FINISHED);
                }
            } catch (ExecutionException exception) {
                log.error("job with id = {} to exception failed.", job.getId(), exception);
                job.getStatus().setExitType(Job.ExitType.FAILED);
            } catch (InterruptedException exception) {
                log.error("InterruptedException waiting job with id = {} to finish.", job.getId(), exception);
                job.getStatus().setExitType(Job.ExitType.FAILED);
                Thread.currentThread().interrupt();
            } finally {
                if (job.getState().isStoppable()) {
                    job.stop();
                }
            }
        });
    }

    @Override
    public void stopJob(String jobId) {
        if (!isStarted()) {
            return;
        }
        synchronized (lock) {
            if (!isStarted()) {
                return;
            }
            JobAndFuture jobAndFuture = runningJobs.get(jobId);
            if (Objects.isNull(jobAndFuture)) {
                log.warn("Can not stop job: no job with id = {} exists.", jobId);
                return;
            }
            Job job = jobAndFuture.job;
            if (job.getState().isStoppable()) {
                if (Objects.isNull(job.getStatus().getExitType())) {
                    job.getStatus().setExitType(Job.ExitType.MANUAL_STOP);
                }
                job.stop();

                if (job.getStatus().getStopTimestamp() == 0L) {
                    job.getStatus().setStopTimestamp(System.currentTimeMillis());
                }
                runningJobs.remove(jobId);
                try {
                    jobStore.saveJob(job);
                } catch (JobStoreException exception) {
                    log.error("Failed to save job status after stop job, job id = {}", job.getId());
                }
            } else {
                log.error("Can not stop job with id = {} because of it's current state {}", jobId, job.getState());
            }
        }
    }

    @Override
    public void waitJob(String jobId) throws Exception {
        JobAndFuture jobAndFuture = runningJobs.get(jobId);
        if (Objects.isNull(jobAndFuture)) {
            throw new IllegalStateException("No job with id = " + jobId + " is running now.");
        }
        if (Objects.nonNull(jobAndFuture.future)) {
            Thread.sleep(50);
        }
        if (Objects.isNull(jobAndFuture.future)) {
            Thread.sleep(1000);
        }
        if (Objects.isNull(jobAndFuture.future)) {
            throw new IllegalStateException("Try later");
        }
        jobAndFuture.future.get();
    }

    protected boolean existJobRunningWithDefinitionId(String jobDefinitionId) {
        return runningJobs.containsKey(jobDefinitionId);
    }

    @Override
    public JobConverter getJobConverter() {
        return jobConverter;
    }

    @Override
    public Job getJob(String jobId) {
        JobAndFuture jobAndFuture = runningJobs.get(jobId);
        if (Objects.nonNull(jobAndFuture)) {
            return jobAndFuture.job;
        }
        try {
            return jobStore.getJob(jobId);
        } catch (JobStoreException exception) {
            log.error("Failed to get job by id = {}", jobId, exception);
            return null;
        }
    }

    @Override
    protected void doStop() throws Exception {
        log.info("JobEngine is going to stop, stop all running jobs.");
        for (JobAndFuture jobAndFuture : runningJobs.values()) {
            jobAndFuture.job.stop();
        }
    }

    @Getter
    public static class JobAndFuture {
        private final Job job;

        @Setter
        private Future<?> future;

        public JobAndFuture(Job job) {
            this.job = job;
        }
    }

}
