/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
 *
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecutorUtilsTest {

  @Test
  public void stopEmptyExec() {
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
          throw new RuntimeException(e);
        }
      }
    }
  }

}
