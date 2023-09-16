package com.dfi.sbc2ha.components.actuator;

import com.dfi.sbc2ha.event.actuator.CoverEvent;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.services.state.State;
import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.GpioDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Cover extends Actuator {

    private final DigitalOutputDevice openDelegate;
    private final DigitalOutputDevice closeDelegate;
    private final Duration openTime;
    private final Duration closeTime;
    private final RelayHelper open;
    private final RelayHelper close;
    private int set_position = -1;
    private long position;
    private CoverState coverState = CoverState.IDLE;

    private ReentrantLock lock = new ReentrantLock();
    private boolean closed;
    private boolean requested_closing;
    private ScheduledFuture timer_handle;


    public Cover(DigitalOutputDevice openDelegate, DigitalOutputDevice closeDelegate, Duration openTime, Duration closeTime, String name, int id, int position) {
        super(name, id);
        this.openDelegate = openDelegate;
        this.closeDelegate = closeDelegate;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.position = position;

        open = new RelayHelper(openDelegate, openTime);
        close = new RelayHelper(closeDelegate, closeTime);
        set_position = -1;
        requested_closing = true;
        if (position == -1) {
            closed = true;
        } else {
            closed = position <= 0;
        }

        handleAny(new CoverEvent((closed ? CoverState.CLOSED : CoverState.OPEN), position));
    }

    @Override
    public String getValue() {
        return String.valueOf(position);
    }


    public void openCover() {
        log.info("open id:{},{}", id, name);
        stop();
        lock.lock();
        coverState = CoverState.OPENING;

        //"""Open cover."""
        if (position == 100) {
            return;
        }
        if (position == -1) {
            closed = false;
            return;
        }
        log.info("Opening cover {}", name);
        requested_closing = false;

        handleAny(new CoverEvent(CoverState.OPENING, position));
        runCover(CoverState.OPENING);

    }


    public void closeCover() {
        log.info("close id:{},{}", id, name);
        stop();
        lock.lock();
        coverState = CoverState.CLOSING;

        //"""Close cover."""
        if (position == 0) {
            return;
        }
        if (position == -1) {
            closed = true;
            return;
        }
        log.info("Closing cover {}", name);

        requested_closing = true;
        handleAny(new CoverEvent(CoverState.CLOSING, position));
        runCover(CoverState.CLOSING);

    }

    public void setPosition(int position) {

    }

    public void stop() {
        openDelegate.off();
        closeDelegate.off();
    }

    public void toggle() {
        log.debug("toggle id:{},{}", id, name);
        if (coverState == CoverState.IDLE) {
            log.debug("cannot determine state for toggle id:{},{}", id, name);
        } else if (coverState == CoverState.OPEN || coverState == CoverState.OPENING) {
            closeCover();
        } else {
            openCover();
        }
    }

    private void runCover(CoverState current_operation) {
        //"""Run cover engine."""
        if (coverState != CoverState.IDLE) {
            stop_cover(false);
        }
        coverState = current_operation;


        DigitalOutputDevice relay, inverted_relay;

        if (coverState == CoverState.OPENING) {
            relay = open.relay;
            inverted_relay = close.relay;
        } else {
            inverted_relay = open.relay;
            relay = close.relay;
        }

        lock.lock();
        if (inverted_relay.isOn()) {
            inverted_relay.off();
        }

        float v = current_operation == CoverState.OPENING ? open.rate : close.rate;
        timer_handle = Scheduler.getInstance()
                .scheduleAtFixedRate(this::listen_cover, 0,
                        (long) v,
                        TimeUnit.MILLISECONDS
                );
        relay.on();
    }

    private void listen_cover() {
        // """Listen for change in cover."""

        if (coverState == CoverState.IDLE) {
            return;
        }

        float step;
        if (requested_closing) {
            step = -close.steps;
        } else {
            step = open.steps;
        }
        position += step;
        int rounded_pos = Math.round(position);

        if (set_position != -1) {
            //Set position is only working for every 10%, so round to nearest 10.
            // Except for start moving time
            if ((requested_closing && rounded_pos < 95) || rounded_pos > 5) {
                rounded_pos = Math.round((float) position / 10) * 10;
            }
        } else {
            if (rounded_pos > 100) {
                rounded_pos = 100;
            } else if (rounded_pos < 0) {
                rounded_pos = 0;
            }
        }
        handleAny(new CoverEvent(coverState, rounded_pos));

        if (rounded_pos == set_position ||
                (set_position == -1 && (rounded_pos >= 100 || rounded_pos <= 0))) {
            position = rounded_pos;
            closed = current_cover_position() <= 0;
            stop_cover(false);
            return;
        }
        closed = current_cover_position() <= 0;
    }

    private int current_cover_position() {
        return Math.round(position);
    }

    private void stop_cover(boolean on_exit) {
        //"""Stop cover."""
        open.relay.off();
        close.relay.off();

        if (timer_handle != null) {
            timer_handle.cancel(true);
            timer_handle = null;
            set_position = -1;
            if (!on_exit) {
                handleAny(new CoverEvent(coverState, position));
            }
        }
        coverState = CoverState.IDLE;
    }


    public void setCoverPosition(long targetPosition) {
        //"""Move cover to a specific position."""

        var set_position = Math.round((float) targetPosition / 10) * 10;

        if (position == targetPosition || set_position == this.set_position) {
            return;
        }
        if (this.set_position != -1) {
            stop_cover(true);
        }
        log.info("Setting cover at position {}.", set_position);
        this.set_position = set_position;
        requested_closing = set_position < position;
        coverState = requested_closing ? CoverState.CLOSING : CoverState.OPENING;
        log.debug("Requested set position {}. Operation {}", set_position, coverState);
        handleAny(new CoverEvent(coverState, position));
        runCover(coverState);
    }

    @Override
    public void close() throws RuntimeIOException {
        openDelegate.close();
        closeDelegate.close();
        super.close();
    }

    public enum CoverAction {
        OPEN,
        CLOSE,
        STOP
    }

    public enum CoverState implements State {
        IDLE,
        OPEN,
        CLOSED,
        OPENING,
        CLOSING


    }

    /**
     * Relay helper for cover either open/close.
     */
    @Getter
    public static class RelayHelper {
        private final DigitalOutputDevice relay;
        private final float steps;
        private final float rate;


        public RelayHelper(DigitalOutputDevice relay, Duration time) {
            this.relay = relay;
            steps = 100f / time.toMillis();
            rate = (float) time.toMillis() / 100;
        }
    }


    @Setter
    public static class Builder extends Actuator.Builder<Builder, DigitalOutputDevice> {
        private PinInfo openPinInfo;
        private PinInfo closePinInfo;
        private int openGpio;
        private int closeGpio;
        private GpioDeviceFactoryInterface openDeviceFactory;
        private GpioDeviceFactoryInterface closeDeviceFactory;
        private Duration closeTime;
        private Duration openTime;
        private DigitalOutputDevice openDelegate;
        private DigitalOutputDevice closeDelegate;

        public Builder(int openGpio, int closeGpio) {
            this.openGpio = openGpio;
            this.closeGpio = closeGpio;
        }

        public Builder(PinInfo openPinInfo, PinInfo closePinInfo) {
            this.openPinInfo = openPinInfo;
            this.closePinInfo = closePinInfo;
        }

        public static Builder builder(int openGpio, int closeGpio) {
            return new Builder(openGpio, closeGpio);
        }

        public static Builder builder(PinInfo openPinInfo, PinInfo closePinInfo) {
            return new Builder(openPinInfo, closePinInfo);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Cover build() {
            if (delegate == null) {
                setupDelegate();
            }
            Cover cover = new Cover(openDelegate, closeDelegate, openTime, closeTime, name, id, getInitialPosition());


            return cover;
        }

        protected Builder setupDelegate() {
            if (openDeviceFactory == null) {
                openDeviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();
            }
            if (closeDeviceFactory == null) {
                closeDeviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();
            }
            if (openPinInfo == null) {
                openPinInfo = deviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(openGpio);
            }
            if (closePinInfo == null) {
                closePinInfo = deviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(closeGpio);
            }

            DigitalOutputDevice.Builder openBuilder = DigitalOutputDevice.Builder
                    .builder(pinInfo)
                    .setDeviceFactory(deviceFactory)
                    .setActiveHigh(activeHigh)
                    .setInitialValue(false);

            DigitalOutputDevice.Builder closeBuilder = DigitalOutputDevice.Builder
                    .builder(pinInfo)
                    .setDeviceFactory(deviceFactory)
                    .setActiveHigh(activeHigh)
                    .setInitialValue(false);

            openDelegate = openBuilder.build();
            closeDelegate = closeBuilder.build();
            return self();
        }

        public Builder setOpenDeviceFactory(GpioDeviceFactoryInterface openDeviceFactory) {
            this.openDeviceFactory = openDeviceFactory;
            return self();
        }

        public Builder setCloseDeviceFactory(GpioDeviceFactoryInterface closeDeviceFactory) {
            this.closeDeviceFactory = closeDeviceFactory;
            return self();
        }

        public Builder setCloseTime(Duration closeTime) {
            this.closeTime = closeTime;
            return self();
        }

        public Builder setOpenTime(Duration openTime) {
            this.openTime = openTime;
            return self();
        }

        private int getInitialPosition() {
            if (initialState == null) {
                return 100;
            }
            return (int) ((CoverEvent) initialState).getPosition();
        }
    }
}
