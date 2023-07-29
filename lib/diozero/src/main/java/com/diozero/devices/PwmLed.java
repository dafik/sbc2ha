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

import com.dfi.sbc2ha.EasingOld;
import com.dfi.sbc2ha.helper.Scheduler;
import com.diozero.api.PwmOutputDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.api.RuntimeInterruptedException;
import com.diozero.api.function.FloatConsumer;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
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
@Slf4j
public class PwmLed extends PwmOutputDevice {
    private static final int DEFAULT_PWM_FREQUENCY = 50;
    private final AtomicBoolean runningEasing = new AtomicBoolean();
    private final List<FloatConsumer> changeListeners = new ArrayList<>();

    private Future<?> easingFuture;

    private AtomicInteger cylcle = new AtomicInteger(0);
    private Lock lock = new ReentrantLock();
    private int range;

    /**
     * @param gpio The GPIO to which the LED is attached to.
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmLed(int gpio) throws RuntimeIOException {
        this(DeviceFactoryHelper.getNativeDeviceFactory(), gpio, 0);
    }

    /**
     * @param gpio         The GPIO to which the LED is attached to.
     * @param initialValue Initial PWM output value (range 0..1).
     * @throws RuntimeIOException If an I/O error occurred.
     */
    public PwmLed(int gpio, float initialValue) throws RuntimeIOException {
        this(DeviceFactoryHelper.getNativeDeviceFactory(), gpio, initialValue);
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
        this(deviceFactory, gpio, DEFAULT_PWM_FREQUENCY, initialValue);
    }

    public PwmLed(PwmOutputDeviceFactoryInterface deviceFactory, int gpio, int pwmFrequency, float initialValue)
            throws RuntimeIOException {
        super(deviceFactory, gpio, pwmFrequency, initialValue);
        detectRange(deviceFactory, gpio, pwmFrequency);
    }

    private static float calculateInitialDelta(EasingOld effect, float fromBrightness, float delta) {
        if (fromBrightness == 0) return 0;
        if (fromBrightness == 1) return 1;
        float deltaValue = 0;

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

        return deltaValue;
    }

    private void detectRange(PwmOutputDeviceFactoryInterface deviceFactory, int gpio, int pwmFrequency) {
        if (deviceFactory instanceof PCA9685) {
            range = (int) Math.pow(2, 12);
        } else if (deviceFactory.getClass().getSimpleName().equals("PigpioJDeviceFactory")) {
            try {
                Field delegateField = PwmOutputDevice.class.getDeclaredField("delegate");
                delegateField.setAccessible(true);
                Object delegate = delegateField.get(this);
                delegateField.setAccessible(false);

                Field rangeFiled = delegate.getClass().getDeclaredField("range");
                rangeFiled.setAccessible(true);
                range = (int) rangeFiled.get(delegate);
                rangeFiled.setAccessible(false);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            range = 200;
        }
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

/*    public void effectBackground(Easing effect, int transition, float fromBrightness, float toBrightness) {
        stopEasing();
        final int c = cylcle.incrementAndGet();

        log.trace("c={} new background request", c);
        easingFuture = Scheduler.getInstance().submit(() -> {
            try {
                lock.lock();

                effect(effect, transition, fromBrightness, toBrightness, c);

                log.trace("c={} Background easing finished bright: {}", c, this.getValue());
            } finally {
                lock.unlock();
            }
        });
        log.trace("c={} Background submited", c);
    }*/

    @Override
    public void toggle() throws RuntimeIOException {
        runInBackgroundWithLock(super::toggle);
    }

    public void effectBackgroundDirection(EasingOld effect, int transition, float limit, boolean off) {
        runInBackgroundWithLock(() -> {
            float fromBrightness = getValue();
            float toBrightness = off ? 0 : limit;

            effect(effect, transition, fromBrightness, toBrightness);
        });
    }

    public void runInBackgroundWithLock(Runnable runnable) {
        stopEasing();
        final int c = cylcle.incrementAndGet();

        log.trace("c={} new background request", c);
        easingFuture = Scheduler.getInstance().submit(() -> {
            try {
                lock.lock();

                runnable.run();
                log.trace("c={} Background easing finished bright: {}", c, this.getValue());
            } finally {
                lock.unlock();
            }
        });
        log.trace("c={} Background submited", c);
    }

    private void stopEasing() {
        runningEasing.getAndSet(false);
        if (easingFuture != null) {
            easingFuture.cancel(false);
        }
    }

    public void effect(EasingOld effect, int transition, float fromBrightness, float toBrightness) {
        if (fromBrightness == toBrightness) {
            return;
        }
        boolean increasing = toBrightness > fromBrightness;

        log.debug(" effect: {}, length: {}s, from: {}%, to: {}%", effect.name(), transition, fromBrightness * 100, toBrightness * 100);
        float fadeTime = (float) transition;
        int steps = 200;
        float sleepTime = fadeTime / steps;
        float delta = Math.abs(fromBrightness - toBrightness) / steps;
        log.debug(" fadeTime={}, steps={}, sleep_time={}s, delta={}", fadeTime, steps, sleepTime, delta);
        runningEasing.getAndSet(true);

        float deltaValue = calculateInitialDelta(effect, fromBrightness, delta);
        int step = 0;
        Comparator<Float> floatComparator = fromBrightness > toBrightness ? new Lower() : new Greater();
        try {
            float value = fromBrightness;
            int compare = floatComparator.compare(value, toBrightness);
            while (compare > 0 && runningEasing.get()) {
                step++;
                log.trace(" delta: {}, value: {}, step:{}", deltaValue, value, step);
                setValueInternal(value);
                SleepUtil.sleepSeconds(sleepTime);
                if (increasing) {
                    deltaValue += delta;
                } else {
                    deltaValue -= delta;
                }
                value = effect.getOperator().apply(deltaValue);
                compare = floatComparator.compare(value, toBrightness);
            }
            if (runningEasing.get()) {
                setValueInternal(toBrightness);
            }
            if (compare > 0) {
                log.trace("interuped by runningEasing val:{} ", value);
            }
            //setValueInternal(toBrightness);
            runningEasing.getAndSet(false);
        } catch (RuntimeInterruptedException e) {
            runningEasing.set(false);
            log.trace(" interrupted by thread");
            //log.error(e);
        }
    }


    @Override
    public void close() {
        log.trace("close()");
        stopEasing();
        if (easingFuture != null) {
            log.debug("Interrupting easing thread ");
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
}
