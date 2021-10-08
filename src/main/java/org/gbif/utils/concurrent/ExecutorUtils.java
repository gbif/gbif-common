/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.utils.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ExecutorUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ExecutorUtils.class);

  /**
   * Shuts down an executor and waits up to 1 hour for the already submitted jobs to finish.
   * @param exec
   */
  public static void stop(ExecutorService exec) {
    stop(exec, 1, TimeUnit.HOURS);
  }

  /**
   * Shuts down an executor and waits for the job to finish until the given timeout is reached
   * before forcing an immediate shutdown.
   * @param exec
   * @param timeout
   * @param unit
   */
  public static void stop(ExecutorService exec, int timeout, TimeUnit unit) {
    LOG.debug("Shutting down executor service {}", exec);
    exec.shutdown(); // Disable new tasks from being submitted
    try {
      if (!exec.awaitTermination(timeout, unit)) {
        LOG.warn("Forcing shut down of executor service, pending tasks will be lost! {}", exec);
        exec.shutdownNow();
      }
    } catch (InterruptedException ie) {
      LOG.warn("Forcing shut down of executor service, pending tasks will be lost! {}", exec);
      exec.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
