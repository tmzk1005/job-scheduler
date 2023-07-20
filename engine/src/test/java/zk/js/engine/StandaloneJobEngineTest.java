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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import zk.js.engine.job.DefaultJobConverter;
import zk.js.engine.job.JobConverter;
import zk.js.engine.job.JobDefinition;
import zk.js.engine.job.SumNumberJob;

class StandaloneJobEngineTest {

    @Test
    void test() throws Exception {
        Map<String, String> jobTypeClasses = Map.of("sum", "zk.js.engine.job.SumNumberJob");
        JobConverter jobConverter = new DefaultJobConverter(jobTypeClasses);
        JobEngine jobEngine = new StandaloneJobEngine(jobConverter);
        jobEngine.start();
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setType("sum");

        Map<String, Object> data = new HashMap<>();
        data.put("numberX", 1);
        data.put("numberY", 2);
        jobDefinition.setJsonConf(new ObjectMapper().writeValueAsString(data));

        String jobId = jobEngine.runJob(jobDefinition);
        Assertions.assertNotNull(jobId);

        jobEngine.waitJob(jobId);
        Assertions.assertEquals(3, SumNumberJob.getResult());
        jobEngine.stop();
    }

}
