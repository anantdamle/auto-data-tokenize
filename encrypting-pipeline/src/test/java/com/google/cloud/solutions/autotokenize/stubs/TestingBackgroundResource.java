/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.solutions.autotokenize.stubs;

import com.google.api.gax.core.BackgroundResource;
import java.util.concurrent.TimeUnit;

public final class TestingBackgroundResource implements BackgroundResource {

  private boolean closed;
  private boolean shutdown;

  public TestingBackgroundResource() {
    this.closed = false;
    this.shutdown = false;
  }

  @Override
  public final void close() {
    closed = true;
  }

  @Override
  public final void shutdown() {
    close();
    shutdown = true;
  }

  @Override
  public final boolean isShutdown() {
    return shutdown;
  }

  @Override
  public final boolean isTerminated() {
    return (closed && shutdown);
  }

  @Override
  public final void shutdownNow() {
    shutdown = true;
  }

  @Override
  public final boolean awaitTermination(long l, TimeUnit timeUnit) {
    shutdown();
    return shutdown;
  }
}