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
package zk.js.engine.common;

public interface LifecycleService {

    void build();

    void init();

    void start();

    void stop();

    void suspend();

    void resume();

    void shutdown();

    ServiceState getState();

    boolean isNew();

    boolean isBuild();

    boolean isInit();

    boolean isStarted();

    boolean isStarting();

    boolean isStopping();

    boolean isStopped();

    boolean isSuspending();

    boolean isSuspended();

    boolean isRunAllowed();

    boolean isShutdown();

    boolean isStoppingOrStopped();

    boolean isSuspendingOrSuspended();

    boolean isStartingOrStarted();

    Exception getFailException();

}
