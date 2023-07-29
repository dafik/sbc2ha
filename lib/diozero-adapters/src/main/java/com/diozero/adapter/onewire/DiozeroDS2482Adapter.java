package com.diozero.adapter.onewire;

import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Enumeration;


public class DiozeroDS2482Adapter extends com.dalsemi.onewire.adapter.DSPortAdapterAbstract implements DeviceInterface {
    static final byte DS2482_1WireTripletCmd = (byte) 0x78;
    static final byte DS2482_1WireResetCmd = (byte) 0xB4;
    static final byte OWSearchCmd = (byte) 0xF0;
    static final byte OWAlarmSearchCmd = (byte) 0xEC;
    static final byte STATUS_SBR = 0x20;    // Single-Bit Result - logic state of 1-wire line sampled at tMSR of a
    // 1-wire Single Bit command or the 1st bit of a 1-wire Triplet command
    static final byte STATUS_TSB = 0x40;    // Triplet Send Bit - reports state of the active 1-wire line sampled at tMSR of the 2nd bit
    static final byte STATUS_DIRECTION_TAKEN = (byte) 0x80; // Branch Direction taken - search direction chosen by the 3rd bit of the triplet
    static final byte DS2482ResetCmd = (byte) 0xF0;
    static final byte DS2482_1WireWriteByteCmd = (byte) 0xA5;
    static final byte OWMatchROMCmd = 0x55;
    static final byte DS2482_1WireReadByteCmd = (byte) 0x96;
    static final byte DS2482ReadDataRegister = (byte) 0xE1;
    private static final int POLL_LIMIT = 16;   // Number of times totest status
    private static final byte STATUS_1WB = 0x01;    // 1-wire busy
    /**
     * Speed modes for 1-Wire Network, hyperdrive
     */
    private static final byte STATUS_PPD = 0x02;    // Presence Pulse Detected
    private static final byte STATUS_SD = 0x04;    // Short Detected
    private static final byte STATUS_LL = 0x08;    // Logic Level of the selected 1-wire line withour initiating any 1-wire communication
    private static final byte STATUS_RST = 0x10;
    private static final byte DS2482SetReadPointer = (byte) 0xE1;
    final int c1WS = 0x00;
    final int cSPU = 0x00;
    final int cPPM = 0x00;
    final byte CONFIG_APU = 0x01;
    final ByteBuffer command;
    final ByteBuffer byteToRead;
    final byte DS2482_WriteConfigRegCmd = (byte) 0xD2;
    private final DiozeroI2CAdapter i2CAdapter;
    private final byte[] device_serial_no;
    private final byte[] CurrentDevice;
    private final int bufferSize = 1;
    public byte DS2482Config = (byte) (c1WS | cSPU | cPPM | CONFIG_APU);
    int crc8;
    private int LastDiscrepancy;
    private boolean LastDeviceFlag;
    private int LastFamilyDiscrepancy;
    private boolean doAlarmSearch = false;

    public DiozeroDS2482Adapter(DiozeroI2CAdapter i2CAdapter) {

        this.i2CAdapter = i2CAdapter;

        this.byteToRead = ByteBuffer.wrap(new byte[bufferSize]);    // Initialize the ByteBuffers
        this.command = ByteBuffer.wrap(new byte[bufferSize]);

        DS2482WrtCfg(DS2482Config);
        CurrentDevice = new byte[8];
        device_serial_no = new byte[7];

        // Initialize Dallas Semiconductor CRC table
        //byte[] dscrc_table = new byte[256];

    }

    @Override
    public String getAdapterName() {
        return "DS2482";
    }

    @Override
    public String getPortTypeDescription() {
        return null;
    }

    @Override
    public String getClassVersion() {
        return null;
    }

    /////

    @Override
    public Enumeration getPortNames() {
        return null;
    }

    @Override
    public boolean selectPort(String s) {
        return false;
    }

    @Override
    public void freePort() {

    }

    @Override
    public String getPortName() {
        return i2CAdapter.getName();
    }

    @Override
    public boolean adapterDetected() {
        return false;
    }

    @Override
    public boolean findFirstDevice() {
        // reset the search state
        LastDiscrepancy = 0;
        LastDeviceFlag = false;
        LastFamilyDiscrepancy = 0;
        return OWSearch();
    }

    @Override
    public boolean findNextDevice() {
        return OWSearch();
    }

    /**
     * Copies the 'current' iButton address being used by the adapter into
     * the array.  This address is the last iButton or 1-Wire device found
     * in a search (findNextDevice()...).
     * This method copies into a user generated array to allow the
     * reuse of the buffer.  When searching many iButtons on the one
     * wire network, this will reduce the memory burn rate.
     *
     * @param address An array to be filled with the current iButton address.
     * @see com.dalsemi.onewire.utils.Address
     */
    @Override
    public void getAddress(byte[] address) {
        System.arraycopy(CurrentDevice, 0, address, 0, 8);
    }

    @Override
    public void setSearchOnlyAlarmingDevices() {

    }

    @Override
    public void setNoResetSearch() {

    }

    @Override
    public void setSearchAllDevices() {

    }

    @Override
    public boolean beginExclusive(boolean b) {
        return false;
    }

    @Override
    public void endExclusive() {

    }

    @Override
    public void putBit(boolean b) {

    }

    @Override
    public boolean getBit() {
        return false;
    }

    @Override
    public void putByte(int i) {
        OWWriteByte((byte) i);
    }

    @Override
    public int getByte() {
        return OWReadByte();
    }

    @Override
    public byte[] getBlock(int i) {
        return new byte[0];
    }

    @Override
    public void getBlock(byte[] bytes, int i) {

    }

    @Override
    public void getBlock(byte[] bytes, int i, int i1) {

    }

    @Override
    public void setPowerDuration(int timeFactor) {
    }

    @Override
    public boolean startPowerDelivery(int changeCondition) {
        return true;
    }

    @Override
    /**
     * Sends a block of data and returns the data received in the same array.
     * This method is used when sending a block that contains reads and writes.
     * The 'read' portions of the data block need to be pre-loaded with 0xFF's.
     * It starts sending data from the index at offset 'off' for length 'len'.
     *
     * @param dataBlock array of data to transfer to and from the 1-Wire Network.
     * @param off       offset into the array of data to start
     * @param len       length of data to send / receive starting at 'off'
     *                  <p>
     *                  For family 10 device first incoming block looks like:
     *                  <p>
     *                  read scratchpad cmd
     *                  /-- 9 bytes of all 1's --\
     *                  BE FF FF FF FF FF FF FF FF FF
     *                  BE 1F 00 1C 17 FF FF 06 4D 8D  // Real output
     *                  returns:
     *                  BE 01 02 03 04 05 06 07 08 09
     *                  |------ CRC8 ------------|
     *                  <p>
     *                  For family 26 device incoming first block looks like:
     *                  <p>
     *                  Recall memory cmd
     *                  /
     *                  B8 01<--- page number
     *                  <p>
     *                  Second incoming block looks like
     *                  read scratchpad cmd
     *                  /--- 10 bytes of all 1's ---\
     *                  BE FF FF FF FF FF FF FF FF FF FF
     *                  returns:
     *                  BE FF 01 02 03 04 05 06 07 08 09
     *                  |------ CRC8 ------------|
     */

    public void dataBlock(byte[] dataBlock, int off, int len) {
        int t_off, t_len;
        t_off = off;
        t_len = len;
        byte cmd1 = dataBlock[0];
        byte cmd2 = dataBlock[1];
        //msg = "[I2CBridgeAdapter][dataBlock] dataBlock = " + bytesToHexLE(dataBlock) + " ,off = "
        //        + off + " ,len = " + len;
        //printMessage(msg, "dataBlock()", I2C_Device.INFO);

        for (int i = 0; i < t_len; i++) {
            if ((dataBlock[i] & 0xFF) != 0xFF) {
                t_off++;
                OWWriteByte(dataBlock[i]);
            }
        }

        byte[] recv = new byte[t_len];    // allocate space for read from device
        int j = t_off;
        for (int i = 0; j < recv.length; i++, j++) {
            recv[i] = OWReadByte();
        }
        System.arraycopy(recv, 0, dataBlock, t_off, t_len - t_off);
    }

    @Override
    public int reset() {
        return 0;
    }

    /**
     * OWSearch does a 1-wire search using the DS2482 1-wire Triplet command.
     * <p>
     * This code was taken from the Dallas/Maxim Application note AN3684 "How
     * to Use the DS2482 1-wire Master" and modified for Java ME.  Also see the
     * PDF for the DS2482-100 and DS2482-800 devices.
     * <p>
     * resetSearch - Reset the search (1), or not (0)
     * lastdevice - If the last device has been found (1), or not (0)
     * deviceAddress - the returned serial number
     * This function continues from the previous search state. The search
     * state can be reset by using the 'OWFirst' function.
     * <p>
     * When 'doAlarmSearch' is TRUE (1) the find alarm command
     * 0xEC is sent instead of the normal search command 0xF0.
     * Using the find alarm command 0xEC will limit the search to only
     * 1-Wire devices that are in an 'alarm' state.
     *
     * @return <p>
     * TRUE (1) if 1-wire device was found and its Serial Number
     * placed in the global ROM
     * <p>
     * FALSE (0): when no new device was found.  Either the are no devices
     * on the 1-Wire Net.
     * @version 0.00, 6 June 2015
     * @author Bruce Juntti <bjuntti at unixwizardry.com>
     */
    public boolean OWSearch() {
        int id_bit_number = 1;
        int last_zero = 0, rom_byte_number = 0;
        boolean search_result = false;
        int presence;
        byte id_bit, cmp_id_bit;
        byte rom_byte_mask = 0x01;
        byte search_direction;
        byte status;

        if (LastDeviceFlag) {
            //System.out.println("1-wire search completed");
            LastDiscrepancy = 0;
        }

        // if the last call was not the last one
        if (!LastDeviceFlag) {
            presence = OWReset();
            if (presence != com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_PRESENCE) {
                // Then reset the search
                LastDiscrepancy = 0;
                LastDeviceFlag = false;
                LastFamilyDiscrepancy = 0;
                return false;
            }

            if (!doAlarmSearch)
                OWWriteByte((byte) OWSearchCmd);
            else
                OWWriteByte((byte) OWAlarmSearchCmd);

            // Loop to do the search
            do {
                // if this discrepancy is before the Last Discrepancy
                // on a previous next then pick the same as last time
                if (id_bit_number < LastDiscrepancy) {
                    if ((CurrentDevice[rom_byte_number] & rom_byte_mask) > 0)
                        search_direction = 1;
                    else
                        search_direction = 0;
                } else {
                    // if equal to last pick 1, if not then pick 0
                    if (id_bit_number == LastDiscrepancy)
                        search_direction = 1;
                    else
                        search_direction = 0;
                }

                // Perform a 1-wire triplet operation on the DS2482 which will perform
                // 2 read bits and 1 write bit
                status = DS2482OWTriplet(search_direction);

                id_bit = (byte) (status & STATUS_SBR);
                cmp_id_bit = (byte) (status & STATUS_TSB);
                int IDbit = 0, cmpIDbit = 0;

                if (id_bit > 0)
                    IDbit = 1;
                if (cmp_id_bit > 0)
                    cmpIDbit = 1;

                if (IDbit == 1) {
                    if (cmpIDbit == 1) {
                        break;
                    }
                }

                search_direction = (byte) (((status & STATUS_DIRECTION_TAKEN) == STATUS_DIRECTION_TAKEN) ? 1 : 0);

                if (id_bit == 0x20 && cmp_id_bit == 0x40) {         // If both id_bit and its complement are 1,
                    LastDiscrepancy = LastFamilyDiscrepancy = 0;    // then no 1-wire devices were found
                    LastDeviceFlag = false;
                    break;
                } else {
                    if (id_bit == 0 && cmp_id_bit == 0 && (search_direction == 0)) {
                        last_zero = id_bit_number;
                        // Check for last discrepancy in family
                        if (last_zero < 9)
                            LastFamilyDiscrepancy = last_zero;
                    }
                    // set or clear the bit in the ROM byte rom_byte_number
                    // with mask rom_byte_mask
                    if (search_direction == 1) {
                        CurrentDevice[rom_byte_number] |= rom_byte_mask;
                    } else {
                        CurrentDevice[rom_byte_number] &= ~rom_byte_mask;
                    }


                    // increment the byte counter id_bit_number
                    // and shift the mask rom_byte_mask
                    id_bit_number++;
                    rom_byte_mask <<= 1;

                    // if the mask is 0 then go to new SerialNum byte rom_byte_number
                    // and reset mask
                    if (rom_byte_mask == 0) {
                        rom_byte_number++;
                        rom_byte_mask = 1;
                    }
                }
                if (rom_byte_number < 7)
                    device_serial_no[rom_byte_number] = CurrentDevice[rom_byte_number];

            } while (rom_byte_number < 8);  // Loop through all ROM bytes 0-7

            // if the search was successful then
            if (!((id_bit_number < 65) || (crc8 != 0))) {
                // search successful so set LastDiscrepancy, LastDeviceFlag, search_result
                //System.out.println("Found device at " + bytesToHex(CurrentDevice));
                // Getting the CRC8 value of the device serial needs work - it does the CRC of the FIRST device, then bails
                //crc8 = computeCRC8(device_serial_no);  // Calculate the CRC
                //System.out.println("Calculated CRC of device serial " + bytesToHex(device_serial_no) + " is " + Integer.toHexString(crc8));
                LastDiscrepancy = last_zero;
                // check for last device
                if (LastDiscrepancy == 0) {
                    LastDeviceFlag = true;
                }
                search_result = true;
            }
        }

        // if no device found then reset counters so next
        // 'search' will be like a first
        if (!search_result || (CurrentDevice[0] == 0)) {
            LastDiscrepancy = 0;
            LastDeviceFlag = false;
            LastFamilyDiscrepancy = 0;
            search_result = false;
        }
        return search_result;
    }

    /**
     * reset()
     * (AKA OWreset)
     * Does a reset of the 1-wire bus via the DS2482 I2C 1-wire bridge
     *
     * @return status byte from DS2482
     * <p>
     * bit7 bit6 bit5 bit4 bit3 bit2 bit1 bit0
     * ========================================
     * DIR  TSB  SBR  RST   LL   SD  PPD  1WB
     */
    public int OWReset() {
        byte reset_cmd = DS2482_1WireResetCmd;
        int poll_count = 0;
        byte status_reg = 0;
        byte retval;
        //I2CwriteBlock(tmp);
        I2CwriteByte(reset_cmd);
        do {
            //    try {
            //        Thread.sleep(200);
            //    } catch (InterruptedException ex) {
            //       Logger.getLogger(I2CBridgeAdapter.class.getName()).log(Level.SEVERE, null, ex);
            //    }
            //
            status_reg = I2CreadByte();
            poll_count++;
        } while (0x01 == (status_reg & STATUS_1WB) && poll_count < POLL_LIMIT);
        //msg = "status register: " + PrintBits(0, status_reg);
        //printMessage(msg, "OWReset()", I2C_Device.INFO);
        if ((status_reg & STATUS_PPD) == STATUS_PPD) {
            //msg = "returning RESET_PRESENCE";
            //printMessage(msg, "OWReset()", I2C_Device.INFO);
            return com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_PRESENCE;
        } else if ((status_reg & STATUS_SD) == STATUS_SD) {
            //msg = "returning RESET_SHORT";
            //printMessage(msg, "OWReset()", I2C_Device.INFO);
            return com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_SHORT;
        } else {
            //msg = "returning RESET_NOPRESENCE";
            //printMessage(msg, "OWReset()", I2C_Device.INFO);
            return com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_NOPRESENCE;
        }
    }

    //--------------------------------------------------------------------------
    // Use the DS2482 help command '1-Wire triplet' to perform one bit of a
    // 1-Wire search.
    // This command does two read bits and one write bit. The write bit
    // is either the default direction (all device have same bit) or in case of
    // a discrepancy, the 'search_direction' parameter is used.
    //
    // Returns – The DS2482 status byte result from the triplet command
    //
    public byte DS2482OWTriplet(byte search_direction) {
        byte direction;
        int poll_count = 0;
        byte received = 0;
        direction = search_direction > 0 ? (byte) 0xFF : 0x0;
        byte[] temp = {DS2482_1WireTripletCmd, direction};
        I2CwriteBlock(temp);

        do {
            received = I2CreadByte();
        } while (0x01 == STATUS_1WB && poll_count++ < POLL_LIMIT);

        //msg = " status = " + byteToHex(received);
        //printMessage(msg, "DS2482OWTriplet()", I2C_Device.INFO);

        if (poll_count == POLL_LIMIT) {
            DS2482Reset();
            //System.out.println("[DS2482_OWtriplet] Poll count exceeded; DS2482 was reset: result was " + PrintBits(0, received));
            return 0x0;
        }
        return received;
    }

    /**
     * Resets the DS2482 1-wire bridge which does a global reset of the device
     * state-machine logic and terminates any ongoing 1-Wire communication.
     * The command code for the device reset is 0xF0.
     * <p>
     *
     * @return Status byte
     * <p>
     * bit7 bit6 bit5 bit4 bit3 bit2 bit1 bit0
     * ========================================
     * DIR  TSB  SBR  RST   LL   SD  PPD  1WB
     * @version 0.00, 6 June 2015
     * @author Bruce Juntti <bjuntti at unixwizardry.com>
     */
    public boolean DS2482Reset() {
        int poll_count = 0;
        byte status = 0;
        byte cmd = DS2482ResetCmd;

        //try {
        I2CwriteByte(cmd);
        //} catch (OneWireException ex) {
        //    Logger.getLogger(I2CBridgeAdapter.class.getName()).log(Level.SEVERE, null, ex);
        //} catch (IOException ex) {
        //    Logger.getLogger(I2CBridgeAdapter.class.getName()).log(Level.SEVERE, null, ex);
        //} catch (Error ex) {
        //    Logger.getLogger(I2CBridgeAdapter.class.getName()).log(Level.SEVERE, null, ex);
        //}
        do {
            status = I2CreadByte();
            //msg = " status = " + byteToHex(status);
            //printMessage(msg, "DS2482Reset()", I2C_Device.INFO);
            poll_count++;
        } while (0x01 == (status & STATUS_1WB) && poll_count < POLL_LIMIT);


        if ((status & STATUS_SD) == STATUS_SD) {
            System.out.println("[DS2482Reset] Short detected");
            return false;
        }
        //System.out.println("[DS2482Reset] Status: " + PrintBits(0, status));
        return (((status & STATUS_RST) == STATUS_RST) ? true : false);
    }

    public byte DS2482WrtCfg(byte config) {
        int bitmask = 0x00FF;
        byte cfgreg;
        byte cfg = (byte) ((config | ~config << 4) & bitmask);
        byte result;
        byte[] temp = {DS2482_WriteConfigRegCmd, cfg};
        //msg = "Writing config " + byteToHex(cfg);
        //printMessage(msg, "DS2482WrtCfg()", I2C_Device.INFO);

        I2CwriteBlock(temp);

        cfgreg = I2CreadByte();
        //msg = "returned: " + PrintBits(1, cfgreg);
        //printMessage(msg, "DS2482WrtCfg()", I2C_Device.INFO);
        return cfgreg;
    }

    /**
     * I2CwriteBlock() writes a sequence of bytes to the selected DS2482<p>
     *
     * @param buffer is an array of bytes to be written
     */
    public void I2CwriteBlock(byte[] buffer) {

        //msg = "Sending " + Convert.toHexString(buffer);
        //printMessage(msg, "I2CsendBlock()", INFO);
        try {
            i2CAdapter.write(ByteBuffer.wrap(buffer));
        } catch (IOException ex) {
            System.out.println("[I2C_Device][I2CwriteBlock] Error encountered: " + ex.getMessage());
        }
    }

    /**
     * I2CSendByte() tries to write one single byte to the device
     *
     * @param byteToWrite is the single byte to be written
     */

    public void I2CwriteByte(byte byteToWrite) {
        try {
            command.clear();
            command.put(byteToWrite);
            command.rewind();
            //msg = "Sending " + Convert.byteToHex(byteToWrite);
            //printMessage(msg, "I2CsendByte()", INFO);
            i2CAdapter.write(command);
        } catch (IOException ex) {
            System.out.println("[I2C_Device][I2CwriteByte] Error encountered: " + ex.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    // Send 8 bits of read communication to the 1-Wire Net and return the
    // result 8 bits read from the 1-Wire Net.
    //
    // Returns:  8 bits read from 1-Wire Net
    //
    // NOTE: To read the data byte received from the 1-Wire line, issue the Set
    //       Read Pointer command and select the Read Data register. Then access
    //       the DS2482 in read mode.
    //

    /**
     * This method reads one byte from the I2C device. The method
     * checks that the byte is actually read, otherwise it'll show some messages
     * in the output
     *
     * @return Byte read from the register
     */
    public byte I2CreadByte() {
        byteToRead.clear();
        int result;
        try {
            result = i2CAdapter.read(byteToRead);
            if (result < 1) {
                System.out.println("[I2C_Device][I2CreadByte] source could not be read");
            } else {
                byteToRead.rewind();
                return byteToRead.get();
            }
            //return (byte) result;

        } catch (IOException ex) {
            System.out.println("[I2C_Device][I2CwriteBytes] Error encountered: " + ex.getMessage());
        }
        return 2;
    }

    /**
     * Send 8 bits of communication to the 1-Wire Net
     * <p>
     * The parameter 'sendbyte' least significant 8 bits are used.
     *
     * @param sendbyte - 8 bits to send (least significant byte)
     */
    public void OWWriteByte(byte sendbyte) {
        byte byteToSend = (byte) (sendbyte & 0xFF);
        byte received_status;
        int poll_count = 0;
        byte[] tempp = {DS2482_1WireWriteByteCmd, byteToSend};
        I2CwriteBlock(tempp);
        do {
            received_status = I2CreadByte();
        } while (0x01 == (received_status & STATUS_1WB) && poll_count++ < POLL_LIMIT);
        if (poll_count == POLL_LIMIT) {
            DS2482Reset();
            //System.out.println("[OWWriteByte] Poll count exceeded; DS2482 was reset: result was " + PrintBits(0, received_status));
        }
    }

    /**
     * Selects the specified iButton or 1-Wire device by broadcasting its
     * address.  This operation is referred to a 'MATCH ROM' operation
     * in the iButton and 1-Wire device data sheets.  This does not
     * affect the 'current' device state information used in searches
     * (findNextDevice...).
     * <p>
     * Warning, this does not verify that the device is currently present
     * on the 1-Wire Network (See isPresent).
     *
     * @param address address of iButton or 1-Wire device to select
     * @return <code>true</code> if device address was sent, <code>false</code>
     * otherwise.
     * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
     * @see com.dalsemi.onewire.utils.Address
     */
    @Override
    public boolean select(byte[] address) {
        // send 1-Wire Reset
        int rslt = OWReset();
        if (rslt != com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_PRESENCE) {
            System.out.println("No presence pulse, rslt = " + rslt);
            return false;
        }

        // broadcast the MATCH ROM command and address
        byte[] send_packet = new byte[9];

        send_packet[0] = OWMatchROMCmd;   // MATCH ROM command
        System.arraycopy(address, 0, send_packet, 1, 8);
        /*
       try {
           sleep(500);
       } catch (InterruptedException ex) {
           Logger.getLogger(I2CBridgeAdapter.class.getName()).log(Level.SEVERE, null, ex);
       }
        */

        for (int i = 0; i < 9; i++) {
            OWWriteByte(send_packet[i]);
        }

        return ((rslt == com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_PRESENCE) || (rslt == com.dalsemi.onewire.adapter.DSPortAdapterAbstract.RESET_ALARM));
    }

    /**
     * @return
     */
    public byte OWReadByte() {
        byte received;
        int poll_count = 0;
        byte temp = DS2482_1WireReadByteCmd;
        I2CwriteByte(temp);

        // loop checking 1WB bit for completion of 1-Wire operation
        // abort if poll limit reached
        do {
            received = I2CreadByte();
        } while (0x01 == (received & STATUS_1WB) && poll_count++ < POLL_LIMIT);
        if (poll_count == POLL_LIMIT) {
            DS2482Reset();
            //System.out.println("[OWReadByte] Poll count exceeded; DS2482 was reset: result was " + PrintBits(0, received));
        }
        byte[] toSend = {DS2482SetReadPointer, DS2482ReadDataRegister};
        I2CwriteBlock(toSend);

        do {
            received = I2CreadByte();
        } while (0x01 == (received & STATUS_1WB) && poll_count++ < POLL_LIMIT);
        if (poll_count == POLL_LIMIT) {
            DS2482Reset();
            //System.out.println("[OWReadByte] Poll count exceeded; DS2482 was reset: result was " + PrintBits(0, received));
        }
        return received;
    }

    @Override
    public void close() throws RuntimeIOException {
        try {
            i2CAdapter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // bit7 bit6 bit5 bit4 bit3 bit2 bit1 bit0
    //========================================
    //  DIR  TSB  SBR  RST   LL   SD  PPD  1WB
    //
}
