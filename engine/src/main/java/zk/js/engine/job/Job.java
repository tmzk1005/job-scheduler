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

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import zk.js.engine.common.LifecycleService;
import zk.js.engine.common.ServiceState;

public interface Job extends LifecycleService, Runnable {

    String getId();

    void setId(String jobId);

    JobDefinition jobDefinition();

    Status getStatus();

    @Getter
    @Setter
    class Status {

        private long startTimestamp;

        private long stopTimestamp;

        private ServiceState serviceState;

        private ExitType exitType;

        private Map<String, Object> extraInfo = new HashMap<>(2);

    }

    enum ExitType {
        /**
         * 正常结束
         */
        FINISHED,

        /**
         * 手动停止
         */
        MANUAL_STOP,

        /**
         * 异常停止
         */
        FAILED
    }

}
