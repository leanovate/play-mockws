package mockws

import java.util.concurrent._

import play.core.NamedThreadFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

trait TimeoutProvider {

  /**
   * Times out the future after the specified delay.
   */
  def timeout[T](future: Future[T], delay: FiniteDuration, timeoutMsg: String): Future[T]
}

object SchedulerExecutorServiceTimeoutProvider extends TimeoutProvider {

  /**
   * A scheduler backed by a thread-pool that spins down threads when no longer in use.
   *
   * Unless explicitly set to fork, "sbt test" creates a new one of these
   * thread pools every run and doesn't bother to tear them down.
   *
   * Cleaning up after ourselves is necessary to avoid leaking threads.
   */
  private lazy val scheduler: ScheduledExecutorService = {
    val executor = new ScheduledThreadPoolExecutor(1, new DaemonizingThreadFactory)
    executor.setKeepAliveTime(5, TimeUnit.SECONDS)
    executor.allowCoreThreadTimeOut(true)
    executor
  }

  def timeout[T](future: Future[T], delay: FiniteDuration, msg: String): Future[T] = {
    val p = Promise[T]()

    // happy path
    p completeWith future

    // set the timeout
    val timeoutTask = scheduler.schedule(
      new Runnable {
        override def run(): Unit = p tryFailure new TimeoutException(msg)
      }, delay.length, delay.unit
    )

    // If the future completes before the timeout, unschedule the timeout so the scheduler is free to spin down.
    // This is relevant for application code under test that sets long timeouts which aren't expected to be met.
    future.onComplete(_ => timeoutTask.cancel(false))

    p.future
  }

  /**
   * If for some reason, people use this library in non-test code, then scheduled timeouts
   * shouldn't prevent JVM termination if it would otherwise exit normally.
   */
  class DaemonizingThreadFactory(underlying: ThreadFactory = new NamedThreadFactory(getClass.getSimpleName)) extends ThreadFactory {

    override def newThread(r: Runnable): Thread = {
      val thread = underlying.newThread(r)
      thread.setDaemon(true)
      thread
    }
  }

}
