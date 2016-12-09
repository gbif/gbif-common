package org.gbif.utils.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Throwables;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ExecutorUtilsTest {

  @Test
  public void stopEmptyExec() throws Exception {
    ExecutorService exec = Executors.newFixedThreadPool(2, new NamedThreadFactory("tata"));

    ExecutorUtils.stop(exec, 1, TimeUnit.SECONDS);
    assertTrue(exec.isTerminated());
  }

  @Test
  public void stopExecWithJobs() throws Exception {
    ExecutorService exec = Executors.newFixedThreadPool(2, new NamedThreadFactory("tata"));
    exec.submit(new DontStop());
    exec.submit(new DontStop());
    exec.submit(new DontStop());

    ExecutorUtils.stop(exec, 1, TimeUnit.SECONDS);
    Thread.sleep(1000);
    assertTrue(exec.isTerminated());
  }

  class DontStop implements Runnable {

    @Override
    public void run() {
      long x = 0;
      while (true) {
        if (x == Long.MAX_VALUE) {
          x = Long.MIN_VALUE;
        }
        x++;
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          Throwables.propagate(e);
        }
      }
    }
  }

}