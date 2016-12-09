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
