package org.projectsforge.performance.tp.fourmispeintre;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListener;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Timer;
import javax.swing.event.EventListenerList;

/**
 * The Class StatisticsHandler.
 * 
 * @author Sébastien Aupetit [sebtic@projectsforge.org]
 *
 */
public class StatisticsHandler {

  /**
   * The listener interface for receiving update events. The class that is
   * interested in processing a update event implements this interface, and the
   * object created with that class is registered with a component using the
   * component's <code>addUpdateListener<code> method. When the update event
   * occurs, that object's appropriate method is invoked.
   *
   * @see UpdateEvent
   */
  public interface UpdateListener extends EventListener {

    /**
     * Update.
     *
     * @param handler
     *          the handler
     */
    public void update(StatisticsHandler handler);
  }

  /** The fps timer. */
  private Timer fpsTimer;

  /** Fourmis per second :). */
  private AtomicLong fpsCounter = new AtomicLong();

  /** stocke la valeur du compteur lors du dernier timer. */
  private volatile long lastFps = 0L;

  /** The timestamp. */
  private long timestamp = 0L;

  /** The statistic logger. */
  private PrintStream statisticLogger;

  /** The listener list. */
  protected EventListenerList listenerList = new EventListenerList();

  /**
   * Adds an update listener which will be regularly triggered.
   *
   * @param listener
   *          the listener
   */
  public void addUpdateListener(UpdateListener listener) {
    listenerList.add(UpdateListener.class, listener);
  }

  /**
   * Gets the last FPS.
   *
   * @return the last FPS
   */
  public long getLastFPS() {
    return lastFps;
  }

  /**
   * Increment fps counter.
   */
  public void incrementFpsCounter() {
    fpsCounter.incrementAndGet();
  }

  /**
   * Increment fps counter.
   *
   * @param delta
   *          the delta
   */
  public void incrementFpsCounter(int delta) {
    fpsCounter.addAndGet(delta);
  }

  /**
   * Start the collection of statistics.
   */
  public void start() {
    try {
      statisticLogger = new PrintStream(new BufferedOutputStream(
          new FileOutputStream("stats-" + new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()) + ".dat")));
    } catch (IOException ex) {
      System.err.println(ex);
    }
    long initTimestamp = System.currentTimeMillis();
    fpsTimer = new Timer(1000, e -> {
      updateFPS();
      statisticLogger.println(Long.toString(timestamp - initTimestamp) + " " + lastFps);
      for (UpdateListener listener : listenerList.getListeners(UpdateListener.class)) {
        listener.update(this);
      }
    });
    fpsTimer.setRepeats(true);
    fpsTimer.start();
  }

  /**
   * Stop.
   */
  public void stop() {
    fpsTimer.stop();
    statisticLogger.close();
  }

  /**
   * update Fourmis per second.
   */
  private void updateFPS() {
    long lastTimestamp = System.currentTimeMillis();
    lastFps = (long) (fpsCounter.getAndSet(0) * 1000. / (lastTimestamp - timestamp));
    timestamp = lastTimestamp;
  }

}
