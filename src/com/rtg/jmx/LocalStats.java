/*
 * Copyright (c) 2014. Real Time Genomics Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rtg.jmx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Set up a separate stats monitoring thread from within the same JVM
 * as SLIM.
 *
 */
public final class LocalStats {

  /** Name of property specifying monitor output destination */
  public static final String MON_DEST = "rtg.jmxmon";
  /** Destination specifier for within output directory */
  public static final String MON_DEST_OUTDIR = "outdir";

  /** Delay in seconds between updates */
  private static final int MON_DELAY = Integer.parseInt(System.getProperty("rtg.jmxmon.delay", "10"));

  /** Comma separated list of hard disks to monitor */
  private static final String MON_DISK = System.getProperty("rtg.jmxmon.disk", "sda");

  /** Comma separated list of network interfaces to monitor */
  private static final String MON_NET = System.getProperty("rtg.jmxmon.net", "eth0");


  private static RecordStats sStats = null;
  private static Thread sThread = null;


  private LocalStats() { }


  // Create a RecordStats object using values defined in system properties
  static RecordStats getLocalStats() {
    return getLocalStats(System.getProperty(MON_DEST), MON_DELAY, MON_DISK, MON_NET);
  }

  /**
   * Make a RecordStats pointing at suitable local object.
   *
   * @param dest destination, either <code>auto</code>, <code>err</code>, or a file name
   * @param delay delay in seconds.
   * @param disk comma separated list of disk names to monitor
   * @param net comma separated list of network interface names to monitor
   * @return a <code>RecordStats</code> value
   */
  static RecordStats getLocalStats(String dest, int delay, String disk, String net) {
    RecordStats rs = null;
    if (dest != null) {
      try {
        final PrintStream monout;
        if (dest.equals("auto")) {
          monout = new PrintStream(new FileOutputStream(File.createTempFile("jmxmon", ".txt", new File(System.getProperty("user.dir")))), true);
        } else if (dest.equals("err")) {
          monout = System.err;
        } else if (dest.equals("outdir")) {
          System.err.println("Output directory has not been set");
          throw new IOException();
        } else {
          monout = new PrintStream(new File(dest));
        }
        rs = new RecordStats(monout, delay * 1000);
        rs.addStats(new MBeanStats());
        for (String d : disk.split(",")) {
          rs.addStats(new DiskStats(d));
        }
        for (String n : net.split(",")) {
          rs.addStats(new NetworkStats(n));
        }
        rs.addStats(new ProgressStats());
      } catch (IOException e) {
        // Do nothing
      }
    }
    return rs;
  }

  /**
   * Starts monitoring in a separate thread. If <code>MON_DEST</code>
   * is not set or is set to <code>MON_DEST_OUTDIR</code>, or if
   * monitoring has already been initiated, this method does nothing.
   */
  public static synchronized void startRecording() {
    if (sThread != null) {
      return;
    }
    final String dest = System.getProperty(MON_DEST);
    if ((dest == null) || dest.equals(MON_DEST_OUTDIR)) {
      return;
    }
    sThread = new Thread(new Runnable() {
        @Override
        public void run() {
          sStats = getLocalStats();
          if (sStats != null) {
            System.err.println("Starting monitoring");
            sStats.run();
          }
        }
      });
    sThread.setDaemon(true);
    sThread.start();
  }

  /**
   * Asynchronously instructs the monitoring thread to stop.
   */
  public static synchronized void stopRecording() {
    if (sStats != null) {
      System.err.println("Shutting down monitoring");
      sStats.terminate();
      sThread.interrupt();
      Thread.yield(); // Give the other one more time to shut down.
    }
  }

  /**
   * Test out the local stats monitoring.
   *
   * @param args ignored
   * @exception IOException if an error occurs.
   * @exception InterruptedException if an error occurs.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final RecordStats rs = getLocalStats("err", 5, "sda", "eth0");
    rs.run();
  }

}
