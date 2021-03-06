package org.corfudb.util.retry;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import java.util.Random;

/**
 * This class implements a basic exponential backoff retry.
 * <p>
 * Created by mwei on 9/1/15.
 */
@Slf4j
public class ExponentialBackoffRetry<E extends Exception, F extends Exception, G extends Exception, H extends Exception, O, A extends IRetry>
        extends AbstractRetry<E, F, G, H, O, ExponentialBackoffRetry> {

    private static final int DEFAULT_BASE = 2;
    private static final float DEFAULT_RANDOM_PORTION = 0f;
    private static final Duration DEFAULT_BACKOFF_DURATION = Duration.ofMinutes(1);
    private long retryCounter = 0;
    private long nextBackoffTime = 0;

    @Getter
    @Setter
    private Duration backoffDuration = DEFAULT_BACKOFF_DURATION;

    /**
     * Base to multiply
     */
    @Getter @Setter
    private int base = DEFAULT_BASE;

    /**
     * Portion of the number to be randomized, between 0 and 1.
     * 0 - no random portion, 1 - randomize everything.
     */
    @Getter @Setter
    private float randomPortion = DEFAULT_RANDOM_PORTION;

    /**
     * Additional fixed retry time in milliseconds for each retry
     * @param runFunction
     */
    @Getter
    @Setter
    private long extraRetry = 0;

    public ExponentialBackoffRetry(IRetryable runFunction) {
        super(runFunction);
    }

    @Override
    public void nextWait() throws InterruptedException {
        if (nextBackoffTime==0) {
            nextBackoffTime = System.currentTimeMillis()+backoffDuration.toMillis();
        }
        retryCounter++;
        long sleepTime = (long) Math.pow(base, retryCounter);
        sleepTime += extraRetry;
        float randomPart = new Random().nextFloat()*randomPortion;
        sleepTime -= sleepTime*randomPart;
        if (System.currentTimeMillis()+sleepTime>nextBackoffTime) {
            nextBackoffTime = 0;
            retryCounter = 1;
            sleepTime = base + extraRetry;
            sleepTime -= sleepTime*randomPart;
        }
        Thread.sleep(sleepTime);
    }

}
