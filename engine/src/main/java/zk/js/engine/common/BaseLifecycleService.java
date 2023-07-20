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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseLifecycleService implements LifecycleService {

    protected static final byte NEW = 0;
    protected static final byte BUILT = 1;
    protected static final byte INITIALIZING = 2;
    protected static final byte INITIALIZED = 3;
    protected static final byte STARTING = 4;
    protected static final byte STARTED = 5;
    protected static final byte SUSPENDING = 6;
    protected static final byte SUSPENDED = 7;
    protected static final byte STOPPING = 8;
    protected static final byte STOPPED = 9;
    protected static final byte SHUTTING_DOWN = 10;
    protected static final byte SHUTDOWN = 11;
    protected static final byte FAILED = 12;

    protected final Object lock = new Object();

    protected Exception failException;

    protected volatile byte state = NEW;

    public void build() {
        if (state == NEW) {
            synchronized (lock) {
                if (state == NEW) {
                    log.trace("Building service: {}", this);
                    try (AutoCloseable ignored = doLifecycleChange()) {
                        doBuild();
                    } catch (Exception exception) {
                        doFail(exception);
                    }
                    state = BUILT;
                    log.trace("Built service: {}", this);
                }
            }
        }
    }

    public void init() {
        // allow to initialize again if stopped or failed
        if (state <= BUILT || state >= STOPPED) {
            synchronized (lock) {
                if (state <= BUILT || state >= STOPPED) {
                    build();
                    log.trace("Initializing service: {}", this);
                    try (AutoCloseable ignored = doLifecycleChange()) {
                        state = INITIALIZING;
                        doInit();
                        state = INITIALIZED;
                        log.trace("Initialized service: {}", this);
                    } catch (Exception ex) {
                        log.trace("Error while initializing service: {}", this, ex);
                        fail(ex);
                    }
                }
            }
        }
    }

    public void start() {
        synchronized (lock) {
            if (state == STARTED) {
                log.trace("Service: {} already started", this);
                return;
            }
            if (state == STARTING) {
                log.trace("Service: {} already starting", this);
                return;
            }
            init();
            if (state == FAILED) {
                log.trace("Init failed");
                return;
            }
            try (AutoCloseable ignored = doLifecycleChange()) {
                state = STARTING;
                log.trace("Starting service: {}", this);
                doStart();
                state = STARTED;
                log.trace("Started service: {}", this);
            } catch (Exception e1) {
                // need to stop as some resources may have been started during startup
                try {
                    stop();
                } catch (Exception e2) {
                    // ignore
                    log.trace(
                            "Error while stopping service after it failed to start: {}. This exception is ignored",
                            this, e1
                    );
                }
                log.trace("Error while starting service: {}", this, e1);
                fail(e1);
            }
        }
    }

    public void stop() {
        synchronized (lock) {
            if (state == FAILED) {
                log.trace("Service: {} failed and regarded as already stopped", this);
                return;
            }
            if (state == STOPPED || state == SHUTTING_DOWN || state == SHUTDOWN) {
                log.trace("Service: {} already stopped", this);
                return;
            }
            if (state == STOPPING) {
                log.trace("Service: {} already stopping", this);
                return;
            }
            state = STOPPING;
            log.trace("Stopping service: {}", this);
            try (AutoCloseable ignored = doLifecycleChange()) {
                doStop();
                state = STOPPED;
                log.trace("Stopped: {} service", this);
            } catch (Exception ex) {
                log.trace("Error while stopping service: {}", this, ex);
                fail(ex);
            }
        }
    }

    public void suspend() {
        synchronized (lock) {
            if (state == SUSPENDED) {
                log.trace("Service: {} already suspended", this);
                return;
            }
            if (state == SUSPENDING) {
                log.trace("Service: {} already suspending", this);
                return;
            }
            state = SUSPENDING;
            log.trace("Suspending service: {}", this);
            try (AutoCloseable ignored = doLifecycleChange()) {
                doSuspend();
                state = SUSPENDED;
                log.trace("Suspended service: {}", this);
            } catch (Exception ex) {
                log.trace("Error while suspending service: {}", this, ex);
                fail(ex);
            }
        }
    }

    public void resume() {
        synchronized (lock) {
            if (state != SUSPENDED) {
                log.trace("Service is not suspended: {}", this);
                return;
            }
            state = STARTING;
            log.trace("Resuming service: {}", this);
            try (AutoCloseable ignored = doLifecycleChange()) {
                doResume();
                state = STARTED;
                log.trace("Resumed service: {}", this);
            } catch (Exception ex) {
                log.trace("Error while resuming service: {}", this, ex);
                fail(ex);
            }
        }
    }

    public void shutdown() {
        synchronized (lock) {
            if (state == SHUTDOWN) {
                log.trace("Service: {} already shutdown", this);
                return;
            }
            if (state == SHUTTING_DOWN) {
                log.trace("Service: {} already shutting down", this);
                return;
            }
            stop();
            state = SHUTDOWN;
            log.trace("Shutting down service: {}", this);
            try (AutoCloseable ignored = doLifecycleChange()) {
                doShutdown();
                log.trace("Shutdown service: {}", this);
                state = SHUTDOWN;
            } catch (Exception ex) {
                log.trace("Error shutting down service: {}", this, ex);
                fail(ex);
            }
        }
    }

    public ServiceState getState() {
        return switch (state) {
            case INITIALIZING -> ServiceState.INITIALIZING;
            case INITIALIZED -> ServiceState.INITIALIZED;
            case STARTING -> ServiceState.STARTING;
            case STARTED -> ServiceState.STARTED;
            case SUSPENDING -> ServiceState.SUSPENDING;
            case SUSPENDED -> ServiceState.SUSPENDED;
            case STOPPING -> ServiceState.STOPPING;
            default -> ServiceState.STOPPED;
        };
    }

    public boolean isNew() {
        return state == NEW;
    }

    public boolean isBuild() {
        return state == BUILT;
    }

    public boolean isInit() {
        return state == INITIALIZED;
    }

    public boolean isStarted() {
        return state == STARTED;
    }

    public boolean isStarting() {
        return state == STARTING;
    }

    public boolean isStopping() {
        return state == STOPPING;
    }

    public boolean isStopped() {
        return state < STARTING || state >= STOPPED;
    }

    public boolean isSuspending() {
        return state == SUSPENDING;
    }

    public boolean isSuspended() {
        return state == SUSPENDED;
    }

    public boolean isRunAllowed() {
        return state >= STARTING && state <= SUSPENDED;
    }

    public boolean isShutdown() {
        return state == SHUTDOWN;
    }

    public boolean isStoppingOrStopped() {
        return state < STARTING || state > SUSPENDED;
    }

    public boolean isSuspendingOrSuspended() {
        return state == SUSPENDING || state == SUSPENDED;
    }

    public boolean isStartingOrStarted() {
        return state == STARTING || state == STARTED;
    }

    protected void fail(Exception exception) {
        failException = exception;
        try {
            doFail(exception);
        } finally {
            state = FAILED;
        }
    }

    protected void doBuild() throws Exception {
        // noop
    }

    protected void doInit() throws Exception {
        // noop
    }

    protected void doStart() throws Exception {
        // noop
    }

    protected void doStop() throws Exception {
        // noop
    }

    protected void doSuspend() throws Exception {
        // noop
    }

    protected void doResume() throws Exception {
        // noop
    }

    protected void doShutdown() throws Exception {
        // noop
    }

    protected void doFail(Exception exception) {
        // noop
    }

    protected AutoCloseable doLifecycleChange() {
        return null;
    }

    @Override
    public Exception getFailException() {
        return failException;
    }

}
