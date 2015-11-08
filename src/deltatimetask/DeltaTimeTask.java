package deltatimetask;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rodrigo Legendre Lima Rodrigues (AlmightyR)
 */
public abstract class DeltaTimeTask implements Runnable {

  //@TODO: Make methods for this thread-safe!
  private boolean running = false;

  private long currentTime;
  private long lastTime;
  private double deltaTime = 0;

  private long intervalTime;

  //<editor-fold defaultstate="collapsed" desc="CONSTRUCTORS">
  public DeltaTimeTask() {
    setIntervalTime(1D);
  }

  /**
   *
   * @param interval Interval of executions in nanoseconds
   */
  public DeltaTimeTask(long interval) {
    setIntervalTime(interval);
  }

  /**
   *
   * @param rate Rate of executions per second
   */
  public DeltaTimeTask(double rate) {
    setIntervalTime(rate);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="GETTERS & SETTERS">
  //<editor-fold defaultstate="collapsed" desc="GETTERS">
  public double getDeltaTime() {
    return deltaTime;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="SETTERS">
  public final void setIntervalTime(long intervalTime) {
    this.intervalTime = intervalTime;
  }

  /**
   *
   * @param rate Rate of executions per second
   */
  public final void setIntervalTime(double rate) {
    this.intervalTime = (long) (TimeUnit.SECONDS.toNanos(1) / rate);
  }
//</editor-fold>
//</editor-fold>

  synchronized public boolean isRunning() {
    return running;
  }

  synchronized public void stop() {
    this.running = false;
  }

  /**
   * This is where you implement what you want to execute, in other words, what
   * would normaly go in the run() method.
   */
  public abstract void task();

  @Override
  @SuppressWarnings("SleepWhileInLoop")
  public void run() {
    running = true;
    lastTime = currentTime = System.nanoTime();
    while (running) {
      //<editor-fold defaultstate="collapsed" desc="Update delta">
      //Update currentTime
      currentTime = System.nanoTime();
      //Update deltaTime
      deltaTime += (currentTime - lastTime);

      //Update lastTime
      lastTime = currentTime;
//</editor-fold>

      //Check delta for elapsed interval
      if (deltaTime >= intervalTime) {
        //Execute the task
        task();
        //Reset delta
        deltaTime -= intervalTime;

        //Sleep for appropriate time
        try {
          long sleepTimeNanos = (long) Math.floor(intervalTime - deltaTime);
          if (sleepTimeNanos > 0) {
            long millis = TimeUnit.NANOSECONDS.toMillis(sleepTimeNanos);
            Thread.sleep(millis);
          }
        } catch (InterruptedException ex) {
          Logger.getLogger(DeltaTimeTask.class.getName()).log(Level.SEVERE, "Failed to sleep (appropriate time).", ex);
        }
      } else {
        //Prevent "overheating" while keeping as much precision as possible by sleeping for a hundreth of the interval
        try {
          long sleepTimeNanos = (long) Math.floor(intervalTime / 100);
          if (sleepTimeNanos > 0) {
            long millis = TimeUnit.NANOSECONDS.toMillis(sleepTimeNanos);
            Thread.sleep(millis);
          }
        } catch (InterruptedException ex) {
          Logger.getLogger(DeltaTimeTask.class.getName()).log(Level.SEVERE, "Failed to sleep (overheating prevention).", ex);
        }
      }
    }
  }
}
