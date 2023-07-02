package com.diozero.devices;

/*
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     PwmLed.java
 *
 * This file is part of the diozero project. More information about this project
 * can be found at https://www.diozero.com/.
 * %%
 * Copyright (C) 2016 - 2023 diozero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.dfi.sbc2ha.Easing;
import com.dfi.sbc2ha.helper.Scheduler;
import com.diozero.api.PwmOutputDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.api.RuntimeInterruptedException;
import com.diozero.api.function.FloatConsumer;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.util.SleepUtil;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PWM controlled LED. @see com.diozero.sampleapps.PwmLedTest PwmLedTest
 */
public class PwmLed extends PwmOutputDevice {
    private final AtomicBoolean runningEasing = new AtomicBoolean();
    private final List<FloatConsumer> changeListeners = new ArrayList<>();

    private Future<?> easingFuture;

    private AtomicInteger cylcle = new AtomicInteger(0);
    private Lock lock = new ReentrantLock();

    /**
     * @param gpio The GPIO to which the LED is attached to.
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmLed(int gpio) throws RuntimeIOException {
        this(gpio, 0);
    }

    /**
     * @param gpio         The GPIO to which the LED is attached to.
     * @param initialValue Initial PWM output value (range 0..1).
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmLed(int gpio, float initialValue) throws RuntimeIOException {
        super(gpio, initialValue);
    }

    /**
     * @param deviceFactory Device factory to use to provision this device.
     * @param gpio          The GPIO to which the LED is attached to.
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio) throws RuntimeIOException {
        this(deviceFactory, gpio, 0);
    }

    /**
     * @param deviceFactory Device factory to use to provision this device.
     * @param gpio          The GPIO to which the LED is attached to.
     * @param initialValue  Initial PWM output value (range 0..1).
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio, float initialValue)
            throws RuntimeIOException {
        super(deviceFactory, gpio, initialValue);
    }

    public PwmLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio, int pwmFrequency, float initialValue)
            throws RuntimeIOException {
        super(deviceFactory, gpio, pwmFrequency, initialValue);
    }

    @Override
    protected void setValueInternal(float value) throws RuntimeIOException {
        super.setValueInternal(value);
        onValueChange(value);
    }

    public void whenChange(FloatConsumer c) {
        changeListeners.add(c);
    }

    private void onValueChange(float value) {
        for (FloatConsumer c : changeListeners) {
            c.accept(value);
        }
    }

    @Override
    public void setValue(float v) throws RuntimeIOException {
        runInBackgroundWithLock(() -> super.setValue(v));
    }

    @Override
    public void on() throws RuntimeIOException {
        runInBackgroundWithLock(super::on);
    }

    @Override
    public void off() throws RuntimeIOException {
        runInBackgroundWithLock(super::off);
    }

    @Override
    public void toggle() throws RuntimeIOException {
        runInBackgroundWithLock(super::toggle);
    }

/*    public void effectBackground(Easing effect, int transition, float fromBrightness, float toBrightness) {
        stopEasing();
        final int c = cylcle.incrementAndGet();

        Logger.trace("c={} new background request", c);
        easingFuture = Scheduler.getInstance().submit(() -> {
            try {
                lock.lock();

                effect(effect, transition, fromBrightness, toBrightness, c);

                Logger.trace("c={} Background easing finished bright: {}", c, this.getValue());
            } finally {
                lock.unlock();
            }
        });
        Logger.trace("c={} Background submited", c);
    }*/

    public void effectBackgroundDirection(Easing effect, int transition, float limit, boolean off) {
        runInBackgroundWithLock(() -> {
            float fromBrightness = getValue();
            float toBrightness = off ? 0 : limit;

            effect(effect, transition, fromBrightness, toBrightness);
        });
    }

    public void runInBackgroundWithLock(Runnable runnable) {
        stopEasing();
        final int c = cylcle.incrementAndGet();

        Logger.trace("c={} new background request", c);
        easingFuture = Scheduler.getInstance().submit(() -> {
            try {
                lock.lock();

                runnable.run();
                Logger.trace("c={} Background easing finished bright: {}", c, this.getValue());
            } finally {
                lock.unlock();
            }
        });
        Logger.trace("c={} Background submited", c);
    }


    private void stopEasing() {
        runningEasing.getAndSet(false);
        if (easingFuture != null) {
            easingFuture.cancel(false);
        }
    }

    public void effect(Easing effect, int transition, float fromBrightness, float toBrightness) {
        if (fromBrightness == toBrightness) {
            return;
        }
        Logger.debug(" effect: {}, length: {}s, from: {}s, to: {}", effect.name(), transition, fromBrightness, toBrightness);
        //float fadeTime, int steps, int iterations, boolean background
        float fadeTime = (float) transition;
        int steps = 100;
        float sleepTime = fadeTime / steps;
        float delta = 1f / steps;
        Logger.debug(" fadeTime={}, steps={}, sleep_time={}s, delta={}", fadeTime, steps, sleepTime, delta);
        runningEasing.getAndSet(true);
        Comparator<Float> floatComparator = fromBrightness > toBrightness ? new Lower() : new Greater();
        float deltaValue = 0;
        if (fromBrightness > 0 && fromBrightness < 1) {
            if (effect.getRevert() != null) {
                deltaValue = effect.getRevert().apply(fromBrightness);
            } else {
                deltaValue += delta;
                float tmpB = 0;
                while (tmpB < fromBrightness) {
                    deltaValue += delta;
                    tmpB = effect.getOperator().apply(deltaValue);
                }
            }
        }

        try {
            float value = fromBrightness;
            int compare = floatComparator.compare(value, toBrightness);
            while (compare > 0 && runningEasing.get()) {
                Logger.trace(" delta: {}, value: {}, comp:{}", deltaValue, value, compare);
                setValueInternal(value);
                SleepUtil.sleepSeconds(sleepTime);
                deltaValue += delta;
                if (fromBrightness < toBrightness) {
                    value = effect.getOperator().apply(deltaValue);
                } else {
                    value = 1 - effect.getOperator().apply(deltaValue);
                }
                compare = floatComparator.compare(value, toBrightness);
            }
            if (runningEasing.get()) {
                setValueInternal(toBrightness);
            }
            if (compare > 0) {
                Logger.trace("interuped by runningEasing val:{} ", value);
            }
            //setValueInternal(toBrightness);
            runningEasing.getAndSet(false);
        } catch (RuntimeInterruptedException e) {
            runningEasing.set(false);
            Logger.trace(" interrupted by thread");
            //Logger.error(e);
        }


    }

    @Override
    public void close() {
        Logger.trace("close()");
        stopEasing();
        if (easingFuture != null) {
            Logger.debug("Interrupting easing thread ");
            easingFuture.cancel(true);
        }

        super.close();
    }

    class Lower implements Comparator<Float> {

        @Override
        public int compare(Float o1, Float o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1 > o2) {
                return 1;
            }
            return -1;
        }
    }

    class Greater implements Comparator<Float> {

        @Override
        public int compare(Float o1, Float o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1 < o2) {
                return 1;
            }
            return -1;
        }
    }
}
