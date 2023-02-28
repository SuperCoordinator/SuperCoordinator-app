package utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class RunnableWrap {
    public void runNTimes(Runnable task, AtomicBoolean stop, long period, TimeUnit unit, ScheduledExecutorService executor) {
        new FixedExecutionRunnable(task, stop).runNTimes(executor, period, unit);
    }

    static class FixedExecutionRunnable implements Runnable {
        private final AtomicBoolean stop;
        private final Runnable delegate;
        private volatile ScheduledFuture<?> self;

        public FixedExecutionRunnable(Runnable delegate, AtomicBoolean stop) {
            this.delegate = delegate;
            this.stop = stop;
        }

        @Override
        public void run() {
            delegate.run();
            if (stop.get()) {
                System.out.println(Thread.currentThread().getName() + " " + stop.get());
                boolean interrupted = false;
                try {
                    while (self == null) {
                        System.out.println("self is null");
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                    self.cancel(true);
                } finally {
                    if (interrupted) {
                        System.out.println("Interrupting " + Thread.currentThread().getName());
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void runNTimes(ScheduledExecutorService executor, long period, TimeUnit unit) {
            self = executor.scheduleAtFixedRate(this, 0, period, unit);
        }
    }
}
