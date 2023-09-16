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
import com.dfi.sbc2ha.EasingType;
import com.dfi.sbc2ha.EasingVariant;
import com.dfi.sbc2ha.helper.Scheduler;
import com.diozero.api.PwmOutputDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.api.RuntimeInterruptedException;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.util.SleepUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * PWM controlled LED. @see com.diozero.sampleapps.PwmLedTest PwmLedTest
 */
@Slf4j
public class PwmFadingLed extends PwmOutputDevice {
    private final AtomicBoolean runningEasing = new AtomicBoolean();
    private final List<Consumer<PwmEvent>> changeListeners = new ArrayList<>();

    private final int gpioNr;

    private Future<?> easingFuture;

    private AtomicInteger cylcle = new AtomicInteger(0);
    private Lock lock = new ReentrantLock();

    /**
     * @param gpio The GPIO to which the LED is attached to.
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmFadingLed(int gpio) throws RuntimeIOException {
        this(gpio, 0);
    }

    /**
     * @param gpio         The GPIO to which the LED is attached to.
     * @param initialValue Initial PWM output value (range 0..1).
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmFadingLed(int gpio, float initialValue) throws RuntimeIOException {
        super(gpio, initialValue);
        this.gpioNr = gpio;
    }

    /**
     * @param deviceFactory Device factory to use to provision this device.
     * @param gpio          The GPIO to which the LED is attached to.
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmFadingLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio) throws RuntimeIOException {
        this(deviceFactory, gpio, 0);
    }

    /**
     * @param deviceFactory Device factory to use to provision this device.
     * @param gpio          The GPIO to which the LED is attached to.
     * @param initialValue  Initial PWM output value (range 0..1).
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmFadingLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio, float initialValue)
            throws RuntimeIOException {
        super(deviceFactory, gpio, initialValue);
        this.gpioNr = gpio;
    }

    public PwmFadingLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio, int pwmFrequency, float initialValue)
            throws RuntimeIOException {
        super(deviceFactory, gpio, pwmFrequency, initialValue);
        this.gpioNr = gpio;
    }

    @Override
    protected void setValueInternal(float v) throws RuntimeIOException {
        setValueInternal(v, v, null, null);
    }

    protected void setValueInternal(float value, float step, EasingType easingType, EasingVariant easingVariant) throws RuntimeIOException {
        super.setValueInternal(value);
        onValueChange(value, step, easingType, easingVariant);
    }

    public void whenChange(Consumer<PwmEvent> c) {
        changeListeners.add(c);
    }

    private void onValueChange(float value, float step, EasingType easingType, EasingVariant easingVariant) {
        for (Consumer<PwmEvent> c : changeListeners) {
            c.accept(new PwmEvent(value, step, easingType, easingVariant));
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

    public void effectBackgroundDirection(EasingType easingType, EasingVariant easingVariant, float step, float duration, float maxBrightness, boolean increasing) {
        runInBackgroundWithLock(() -> effect(easingType, easingVariant, step, duration, maxBrightness, increasing));
    }

    public void runInBackgroundWithLock(Runnable runnable) {
        stopEasing();
        final int c = cylcle.incrementAndGet();


        log.trace("gpio:{}  c={} new background request", gpioNr, c);
        easingFuture = Scheduler.getInstance().submit(() -> {
            try {
                lock.lock();
                runnable.run();
                log.trace("gpio:{} c={} Background easing finished bright: {}", gpioNr, c, this.getValue());
            } finally {
                lock.unlock();
            }
        });
        log.trace("gpio:{} c={} Background submited", gpioNr, c);
    }

    private void stopEasing() {
        runningEasing.getAndSet(false);
        if (easingFuture != null) {
            easingFuture.cancel(false);
        }
    }

    public void effect(EasingType easingType, EasingVariant easingVariant, float step, float duration, float maxBrightness, boolean increasing) {

        log.debug("gpio:{} effect: {}:{}, step: {}, duration: {}, max: {}, direction:{}", gpioNr, easingType, easingVariant, step, duration, maxBrightness, increasing ? "On" : "Off");
        int steps = 100;
        float sleepTime = duration / steps;
        runningEasing.getAndSet(true);

        Comparator<Float> floatComparator = increasing ? new Greater() : new Lower();
        try {
            float value = getValue();
            int compare = floatComparator.compare(value, increasing ? maxBrightness : 0);
            while (compare > 0 && runningEasing.get()) {
                log.trace("gpio:{}  value: {}, step:{}", gpioNr, value, step);
                setValueInternal(value, step, easingType, easingVariant);
                SleepUtil.sleepSeconds(sleepTime);
                if (increasing) {
                    step += sleepTime;
                } else {
                    step -= sleepTime;
                }
                value = Easing.ease(step, 0, maxBrightness, duration, easingType, easingVariant);
                compare = floatComparator.compare(value, increasing ? maxBrightness : 0);
            }
            if (runningEasing.get()) {
                log.trace("gpio:{} value: {}, step:{}", gpioNr, maxBrightness, duration);
                setValueInternal(increasing ? maxBrightness : 0, duration, easingType, easingVariant);
            }
            if (compare > 0) {
                log.trace("gpio:{} interuped by runningEasing val:{} ", gpioNr, value);
            }
            //setValueInternal(toBrightness);
            runningEasing.getAndSet(false);
        } catch (RuntimeInterruptedException e) {
            runningEasing.set(false);
            log.trace("gpio:{}  interrupted by thread", gpioNr);
            //log.error(e);
        }


    }

    @Override
    public void close() {
        log.trace("gpio:{} close()", gpioNr);
        stopEasing();
        if (easingFuture != null) {
            log.debug("gpio:{} Interrupting easing thread", gpioNr);
            easingFuture.cancel(true);
        }

        super.close();
    }

    static class Lower implements Comparator<Float> {

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

    static class Greater implements Comparator<Float> {

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

    @Data
    public static class PwmEvent {
        final float value;
        final float step;
        final EasingType easingType;
        final EasingVariant easingVariant;
    }
}
