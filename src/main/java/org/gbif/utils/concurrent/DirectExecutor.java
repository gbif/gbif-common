package org.gbif.utils.concurrent;

import java.util.concurrent.Executor;

/**
 * Executor that runs tasks syncroneously and immediately.
 */
public class DirectExecutor implements Executor {

  public void execute(Runnable r) {
    r.run();
  }
}