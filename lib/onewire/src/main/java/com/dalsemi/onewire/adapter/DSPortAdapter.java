package com.dalsemi.onewire.adapter;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.Family;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.utils.Address;

import java.util.Enumeration;

public interface DSPortAdapter {
    /**
     * Retrieves the name of the port adapter as a string.  The 'Adapter'
     * is a device that connects to a 'port' that allows one to
     * communicate with an iButton or other 1-Wire device.  As example
     * of this is 'DS9097U'.
     *
     * @return <code>String</code> representation of the port adapter.
     */
    String getAdapterName();

    /**
     * Retrieves a description of the port required by this port adapter.
     * An example of a 'Port' would 'serial communication port'.
     *
     * @return <code>String</code> description of the port type required.
     */
    String getPortTypeDescription();

    /**
     * Retrieves a version string for this class.
     *
     * @return version string
     */
    String getClassVersion();

    /**
     * Retrieves a list of the platform appropriate port names for this
     * adapter.  A port must be selected with the method 'selectPort'
     * before any other communication methods can be used.  Using
     * a communcation method before 'selectPort' will result in
     * a <code>OneWireException</code> exception.
     *
     * @return <code>Enumeration</code> of type <code>String</code> that contains the port
     * names
     */
    Enumeration getPortNames();

    void registerOneWireContainerClass(int family, Class OneWireContainerClass) throws OneWireException;

    /**
     * Specifies a platform appropriate port name for this adapter.  Note that
     * even though the port has been selected, it's ownership may be relinquished
     * if it is not currently held in a 'exclusive' block.  This class will then
     * try to re-aquire the port when needed.  If the port cannot be re-aquired
     * ehen the exception <code>PortInUseException</code> will be thrown.
     *
     * @param portName name of the target port, retrieved from
     *                 getPortNames()
     * @return <code>true</code> if the port was aquired, <code>false</code>
     * if the port is not available.
     * @throws OneWireIOException If port does not exist, or unable to communicate with port.
     * @throws OneWireException   If port does not exist
     */
    boolean selectPort(String portName) throws OneWireIOException, OneWireException;

    /**
     * Frees ownership of the selected port, if it is currently owned, back
     * to the system.  This should only be called if the recently
     * selected port does not have an adapter, or at the end of
     * your application's use of the port.
     *
     * @throws OneWireException If port does not exist
     */
    void freePort() throws OneWireException;

    /**
     * Retrieves the name of the selected port as a <code>String</code>.
     *
     * @return <code>String</code> of selected port
     * @throws OneWireException if valid port not yet selected
     */
    String getPortName() throws OneWireException;

    /**
     * Detects adapter presence on the selected port.
     *
     * @return <code>true</code> if the adapter is confirmed to be connected to
     * the selected port, <code>false</code> if the adapter is not connected.
     * @throws OneWireIOException
     * @throws OneWireException
     */
    boolean adapterDetected() throws OneWireIOException, OneWireException;

    String getAdapterVersion() throws OneWireIOException, OneWireException;

    String getAdapterAddress() throws OneWireIOException, OneWireException;

    boolean canOverdrive() throws OneWireIOException, OneWireException;

    boolean canHyperdrive() throws OneWireIOException, OneWireException;

    boolean canFlex() throws OneWireIOException, OneWireException;

    boolean canProgram() throws OneWireIOException, OneWireException;

    boolean canDeliverPower() throws OneWireIOException, OneWireException;

    boolean canDeliverSmartPower() throws OneWireIOException, OneWireException;

    boolean canBreak() throws OneWireIOException, OneWireException;

    Enumeration getAllDeviceContainers() throws OneWireIOException, OneWireException;

    OneWireContainer getFirstDeviceContainer() throws OneWireIOException, OneWireException;

    OneWireContainer getNextDeviceContainer() throws OneWireIOException, OneWireException;

    /**
     * Returns <code>true</code> if the first iButton or 1-Wire device
     * is found on the 1-Wire Network.
     * If no devices are found, then <code>false</code> will be returned.
     *
     * @return <code>true</code> if an iButton or 1-Wire device is found.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    boolean findFirstDevice() throws OneWireIOException, OneWireException;

    /**
     * Returns <code>true</code> if the next iButton or 1-Wire device
     * is found. The previous 1-Wire device found is used
     * as a starting point in the search.  If no more devices are found
     * then <code>false</code> will be returned.
     *
     * @return <code>true</code> if an iButton or 1-Wire device is found.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    boolean findNextDevice() throws OneWireIOException, OneWireException;

    /**
     * Copies the 'current' 1-Wire device address being used by the adapter into
     * the array.  This address is the last iButton or 1-Wire device found
     * in a search (findNextDevice()...).
     * This method copies into a user generated array to allow the
     * reuse of the buffer.  When searching many iButtons on the one
     * wire network, this will reduce the memory burn rate.
     *
     * @param address An array to be filled with the current iButton address.
     * @see com.dalsemi.onewire.utils.Address
     */
    void getAddress(byte[] address);

    long getAddressAsLong();

    String getAddressAsString();

    boolean isPresent(byte[] address) throws OneWireIOException, OneWireException;

    boolean isPresent(long address) throws OneWireIOException, OneWireException;

    boolean isPresent(String address) throws OneWireIOException, OneWireException;

    boolean isAlarming(byte[] address) throws OneWireIOException, OneWireException;

    boolean isAlarming(long address) throws OneWireIOException, OneWireException;

    boolean isAlarming(String address) throws OneWireIOException, OneWireException;

    boolean select(byte[] address) throws OneWireIOException, OneWireException;

    boolean select(long address) throws OneWireIOException, OneWireException;

    boolean select(String address) throws OneWireIOException, OneWireException;

    void assertSelect(byte[] address) throws OneWireIOException, OneWireException;

    void assertSelect(long address) throws OneWireIOException, OneWireException;

    void assertSelect(String address) throws OneWireIOException, OneWireException;

    /**
     * Sets the 1-Wire Network search to find only iButtons and 1-Wire
     * devices that are in an 'Alarm' state that signals a need for
     * attention.  Not all iButton types
     * have this feature.  Some that do: DS1994, DS1920, DS2407.
     * This selective searching can be canceled with the
     * 'setSearchAllDevices()' method.
     *
     * @see #setNoResetSearch
     */
    void setSearchOnlyAlarmingDevices();

    /**
     * Sets the 1-Wire Network search to not perform a 1-Wire
     * reset before a search.  This feature is chiefly used with
     * the DS2409 1-Wire coupler.
     * The normal reset before each search can be restored with the
     * 'setSearchAllDevices()' method.
     */
    void setNoResetSearch();

    /**
     * Sets the 1-Wire Network search to find all iButtons and 1-Wire
     * devices whether they are in an 'Alarm' state or not and
     * restores the default setting of providing a 1-Wire reset
     * command before each search. (see setNoResetSearch() method).
     *
     * @see #setNoResetSearch
     */
    void setSearchAllDevices();

    void targetAllFamilies();

    void targetFamily(int family);

    void targetFamily(byte[] family);

    void excludeFamily(int family);

    void excludeFamily(byte[] family);

    /**
     * Gets exclusive use of the 1-Wire to communicate with an iButton or
     * 1-Wire Device.
     * This method should be used for critical sections of code where a
     * sequence of commands must not be interrupted by communication of
     * threads with other iButtons, and it is permissible to sustain
     * a delay in the special case that another thread has already been
     * granted exclusive access and this access has not yet been
     * relinquished. <p>
     * <p>
     * It can be called through the OneWireContainer
     * class by the end application if they want to ensure exclusive
     * use.  If it is not called around several methods then it
     * will be called inside each method.
     *
     * @param blocking <code>true</code> if want to block waiting
     *                 for an excluse access to the adapter
     * @return <code>true</code> if blocking was false and a
     * exclusive session with the adapter was aquired
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    boolean beginExclusive(boolean blocking) throws OneWireException;

    /**
     * Relinquishes exclusive control of the 1-Wire Network.
     * This command dynamically marks the end of a critical section and
     * should be used when exclusive control is no longer needed.
     */
    void endExclusive();

    /**
     * Sends a bit to the 1-Wire Network.
     *
     * @param bitValue the bit value to send to the 1-Wire Network.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    void putBit(boolean bitValue) throws OneWireIOException, OneWireException;

    /**
     * Gets a bit from the 1-Wire Network.
     *
     * @return the bit value recieved from the the 1-Wire Network.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    boolean getBit() throws OneWireIOException, OneWireException;

    /**
     * Sends a byte to the 1-Wire Network.
     *
     * @param byteValue the byte value to send to the 1-Wire Network.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    void putByte(int byteValue) throws OneWireIOException, OneWireException;

    /**
     * Gets a byte from the 1-Wire Network.
     *
     * @return the byte value received from the the 1-Wire Network.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    int getByte() throws OneWireIOException, OneWireException;

    /**
     * Gets a block of data from the 1-Wire Network.
     *
     * @param len length of data bytes to receive
     * @return the data received from the 1-Wire Network.
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    byte[] getBlock(int len) throws OneWireIOException, OneWireException;

    /**
     * Gets a block of data from the 1-Wire Network and write it into
     * the provided array.
     *
     * @param arr array in which to write the received bytes
     * @param len length of data bytes to receive
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    void getBlock(byte[] arr, int len) throws OneWireIOException, OneWireException;

    /**
     * Gets a block of data from the 1-Wire Network and write it into
     * the provided array.
     *
     * @param arr array in which to write the received bytes
     * @param off offset into the array to start
     * @param len length of data bytes to receive
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    void getBlock(byte[] arr, int off, int len) throws OneWireIOException, OneWireException;

    /**
     * Sends a block of data and returns the data received in the same array.
     * This method is used when sending a block that contains reads and writes.
     * The 'read' portions of the data block need to be pre-loaded with 0xFF's.
     * It starts sending data from the index at offset 'off' for length 'len'.
     *
     * @param dataBlock array of data to transfer to and from the 1-Wire Network.
     * @param off       offset into the array of data to start
     * @param len       length of data to send / receive starting at 'off'
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    void dataBlock(byte[] dataBlock, int off, int len) throws OneWireIOException, OneWireException;

    /**
     * Sends a Reset to the 1-Wire Network.
     *
     * @return the result of the reset. Potential results are:
     * <ul>
     * <li> 0 (RESET_NOPRESENCE) no devices present on the 1-Wire Network.
     * <li> 1 (RESET_PRESENCE) normal presence pulse detected on the 1-Wire
     *        Network indicating there is a device present.
     * <li> 2 (RESET_ALARM) alarming presence pulse detected on the 1-Wire
     *        Network indicating there is a device present and it is in the
     *        alarm condition.  This is only provided by the DS1994/DS2404
     *        devices.
     * <li> 3 (RESET_SHORT) indicates 1-Wire appears shorted.  This can be
     *        transient conditions in a 1-Wire Network.  Not all adapter types
     *        can detect this condition.
     * </ul>
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException   on a setup error with the 1-Wire adapter
     */
    int reset() throws OneWireIOException, OneWireException;

    void setPowerDuration(int timeFactor) throws OneWireIOException, OneWireException;

    boolean startPowerDelivery(int changeCondition) throws OneWireIOException, OneWireException;

    void setProgramPulseDuration(int timeFactor) throws OneWireIOException, OneWireException;

    boolean startProgramPulse(int changeCondition) throws OneWireIOException, OneWireException;

    void startBreak() throws OneWireIOException, OneWireException;

    void setPowerNormal() throws OneWireIOException, OneWireException;

    int getSpeed();

    void setSpeed(int speed) throws OneWireIOException, OneWireException;

    /**
     * Constructs a <code>OneWireContainer</code> object with a user supplied 1-Wire network address.
     *
     * @param address device address with which to create a new container
     * @return The <code>OneWireContainer</code> object
     * @see com.dalsemi.onewire.utils.Address
     */
    default OneWireContainer getDeviceContainer(byte[] address) {
        int family_code = address[0] & 0x7F;
        Family family = Family.ofValue((byte) family_code);
        Class<? extends OneWireContainer> containerClass = family.getContainerClass();

        // try to load the ibutton container class
        try {

            // create the iButton container with a reference to this adapter
            OneWireContainer container = containerClass.getDeclaredConstructor().newInstance();
            container.setupContainer(this, address);
            return container;

        } catch (Exception e) {
            System.out.println("EXCEPTION: Unable to instantiate OneWireContainer " + containerClass + ": " + e);
            e.printStackTrace();

            return null;
        }

    }

    /**
     * Constructs a <code>OneWireContainer</code> object with a user supplied 1-Wire network address.
     *
     * @param address device address with which to create a new container
     * @return The <code>OneWireContainer</code> object
     * @see com.dalsemi.onewire.utils.Address
     */
    default OneWireContainer getDeviceContainer(long address) {
        return getDeviceContainer(Address.toByteArray(address));
    }

    /**
     * Constructs a <code>OneWireContainer</code> object with a user supplied 1-Wire network address.
     *
     * @param address device address with which to create a new container
     * @return The <code>OneWireContainer</code> object
     * @see Address
     */
    default OneWireContainer getDeviceContainer(String address) {
        return getDeviceContainer(Address.toByteArray(address));
    }

    /**
     * Constructs a <code>OneWireContainer</code> object using the current 1-Wire network address.
     * The internal state of the port adapter keeps track of the last
     * address found and is able to create container objects from this
     * state.
     *
     * @return the <code>OneWireContainer</code> object
     */
    default OneWireContainer getDeviceContainer() {

        // Mask off the upper bit.
        byte[] address = new byte[8];

        getAddress(address);

        return getDeviceContainer(address);
    }
}
