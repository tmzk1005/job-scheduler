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

import java.io.Serializable;

public enum ServiceState implements Serializable {
    INITIALIZING,
    INITIALIZED,
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    SUSPENDING,
    SUSPENDED;

    public boolean isStartable() {
        return this == INITIALIZED || this == STOPPED || this == SUSPENDED;
    }

    public boolean isStoppable() {
        return this == STARTED || this == SUSPENDED;
    }

    public boolean isSuspendable() {
        return this == STARTED;
    }

    public boolean isInitializing() {
        return this == INITIALIZING;
    }

    public boolean isInitialized() {
        return this == INITIALIZED;
    }

    public boolean isStarting() {
        return this == STARTING;
    }

    public boolean isStarted() {
        return this == STARTED;
    }

    public boolean isStopping() {
        return this == STOPPING;
    }

    public boolean isStopped() {
        return this == STOPPED;
    }

    public boolean isSuspending() {
        return this == SUSPENDING;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

}
