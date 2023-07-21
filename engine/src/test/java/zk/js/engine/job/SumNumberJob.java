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
package zk.js.engine.job;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

public class SumNumberJob extends AbstractJob {

    private final JobContext jobContext;

    @Getter
    private static Integer result;

    protected SumNumberJob(JobDefinition jobDefinition) {
        super(jobDefinition, UUID.randomUUID().toString());
        this.jobContext = new JobContext();
    }

    @Override
    protected void doInit() throws Exception {
        new ObjectMapper().readerForUpdating(jobContext).readValue(jobDefinition.getJsonConf());
    }

    @Override
    public void run() {
        result = jobContext.numberX + jobContext.numberY;
    }

    @Getter
    @Setter
    public static class JobContext {
        private int numberX;
        private int numberY;
    }

}
