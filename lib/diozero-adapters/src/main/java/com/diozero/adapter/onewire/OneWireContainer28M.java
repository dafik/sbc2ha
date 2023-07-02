package com.diozero.adapter.onewire;

public class OneWireContainer28M extends com.dalsemi.onewire.container.OneWireContainer28 {

    public OneWireContainer28M() {
    }

    public OneWireContainer28M(com.dalsemi.onewire.adapter.DSPortAdapter sourceAdapter, byte[] newAddress) {
        super(sourceAdapter, newAddress);
    }

    public OneWireContainer28M(com.dalsemi.onewire.adapter.DSPortAdapter sourceAdapter, long newAddress) {
        super(sourceAdapter, newAddress);
    }

    public OneWireContainer28M(com.dalsemi.onewire.adapter.DSPortAdapter sourceAdapter, String newAddress) {
        super(sourceAdapter, newAddress);
    }

    @Override
    public void doTemperatureConvert(byte[] state) throws com.dalsemi.onewire.adapter.OneWireIOException, com.dalsemi.onewire.OneWireException {
        int msDelay = 750;   // in milliseconds

        // select the device
        if (adapter.select(address)) {

            // Setup Power Delivery
            //adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
            //adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
            // send the convert temperature command
            adapter.putByte(com.dalsemi.onewire.container.OneWireContainer28.CONVERT_TEMPERATURE_COMMAND);

            // calculate duration of delay according to resolution desired
            switch (state[4]) {

                case com.dalsemi.onewire.container.OneWireContainer28.RESOLUTION_9_BIT:
                    msDelay = 94;
                    break;
                case com.dalsemi.onewire.container.OneWireContainer28.RESOLUTION_10_BIT:
                    msDelay = 188;
                    break;
                case com.dalsemi.onewire.container.OneWireContainer28.RESOLUTION_11_BIT:
                    msDelay = 375;
                    break;
                case com.dalsemi.onewire.container.OneWireContainer28.RESOLUTION_12_BIT:
                    msDelay = 750;
                    break;
                default:
                    msDelay = 750;
            }   // switch

            // delay for specified amount of time
        /*    try {
                Thread.sleep(msDelay*5);
            } catch (InterruptedException e) {
            }*/
            msDelay = 94;

            // Turn power back to normal.
            adapter.setPowerNormal();
            while (adapter.getByte() != (byte) 0xFF){
                try {
                    Thread.sleep(msDelay);
                } catch (InterruptedException e) {
                }
            }
            // check to see if the temperature conversion is over
            if (adapter.getByte() != (byte) 0xFF)
                throw new com.dalsemi.onewire.adapter.OneWireIOException(
                        "OneWireContainer28-temperature conversion not complete");
        } else {

            // device must not have been present
            throw new com.dalsemi.onewire.adapter.OneWireIOException(
                    "OneWireContainer28-device not present");
        }
    }
}
