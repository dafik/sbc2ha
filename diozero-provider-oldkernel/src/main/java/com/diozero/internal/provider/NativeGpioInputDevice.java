package com.diozero.internal.provider;

/*-
 * #%L
 * Organisation: sbc2ha
 * Project:      diozero-provider-oldkernel — Forked diozero providers
 *
 * Fork of diozero's NativeGpioInputDevice.java.
 *
 * Change from upstream: uses local GpioChip.
 * On BBB TI kernel 6.6.x (e.g. 6.6.36-ti-r69), the gpiod ioctl
 * GPIOHANDLE_REQUEST_BIAS_DISABLE returns EINVAL, causing
 * "Error setting line event: Invalid argument" for all GPIO inputs.
 *
 * Local GpioChip omits bias flags entirely (matching diozero 1.4.0-dafik
 * behaviour), so provisionGpioInputDevice works without triggering EINVAL.
 *
 * Original copyright (diozero):
 * Copyright (C) 2016 - 2024 diozero
 * Licensed under the MIT License (see below).
 * #L%
 */

import org.tinylog.Logger;

import com.diozero.api.DigitalInputEvent;
import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.provider.builtin.DefaultDeviceFactory;
import com.diozero.internal.provider.builtin.gpio.GpioChip;
import com.diozero.internal.provider.builtin.gpio.GpioLine;
import com.diozero.internal.provider.builtin.gpio.GpioLineEventListener;
import com.diozero.internal.spi.AbstractInputDevice;
import com.diozero.internal.spi.GpioDigitalInputDeviceInterface;
import com.diozero.internal.spi.MmapGpioInterface;

/**
 * Fork of {@code com.diozero.internal.provider.builtin.NativeGpioInputDevice}
 * that accepts the local {@link GpioChip} instead of diozero's {@code GpioChip}.
 *
 * <p>The local {@code GpioChip} omits bias flags in
 * {@code provisionGpioInputDevice}, avoiding EINVAL on BBB TI kernel 6.6.x.</p>
 */
public class NativeGpioInputDevice extends AbstractInputDevice<DigitalInputEvent>
		implements GpioDigitalInputDeviceInterface, GpioLineEventListener {
	private GpioChip chip;
	private int gpio;
	private GpioLine line;

	public NativeGpioInputDevice(DefaultDeviceFactory deviceFactory, String key, GpioChip chip, PinInfo pinInfo,
	                             GpioPullUpDown pud, GpioEventTrigger trigger, MmapGpioInterface mmapGpio) {
		super(key, deviceFactory);

		gpio = pinInfo.getDeviceNumber();
		int offset = pinInfo.getLineOffset();
		if (offset == PinInfo.NOT_DEFINED) {
			throw new IllegalArgumentException("Line offset not defined for pin " + pinInfo);
		}
		this.chip = chip;

		line = chip.provisionGpioInputDevice(offset, pud, trigger);
		// XXX Remove this once kernel 5.5 is widely adopted - pull-up / pull-down
		// control is not possible with gpiod v1
		if (mmapGpio != null) {
			mmapGpio.setPullUpDown(gpio, pud);
		}
	}

	@Override
	public int getGpio() {
		return gpio;
	}

	@Override
	public boolean getValue() throws RuntimeIOException {
		return line.getValue() == 0 ? false : true;
	}

	@Override
	public void setDebounceTimeMillis(int debounceTime) {
		Logger.warn("Debounce not supported");
	}

	@Override
	protected void enableListener() {
		Logger.trace("enableListener(), {}", Integer.valueOf(gpio));
		chip.register(line.getFd(), this);
	}

	@Override
	protected void disableListener() {
		Logger.trace("disableListener(), {}", Integer.valueOf(gpio));
		chip.deregister(line.getFd());
	}

	@Override
	public void closeDevice() {
		Logger.trace("closeDevice() {}", getKey());
		disableListener();
		line.close();
	}

	@Override
	public void event(int lineFd, int eventDataId, long epochTimeMs, long timestampNanos) {
		accept(new DigitalInputEvent(gpio, epochTimeMs, timestampNanos,
				eventDataId == GpioChip.GPIOEVENT_EVENT_RISING_EDGE));
	}
}
