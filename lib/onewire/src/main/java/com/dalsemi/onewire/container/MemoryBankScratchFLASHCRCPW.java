/*---------------------------------------------------------------------------
 * Copyright (C) 2002-2016 Maxim Integrated Products, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL MAXIM INTEGRATED PRODUCTS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Maxim Integrated Products
 * shall not be used except as stated in the Maxim Integrated Products
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.dalsemi.onewire.container;

// imports

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapterAbstract;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.utils.Convert;


/**
 * Memory bank class for the Scratchpad section of NVRAM iButtons and
 * 1-Wire devices with password protected memory pages.
 *
 * @author DS
 * @version 1.00, 6 April 2015
 */
public class MemoryBankScratchFLASHCRCPW
        extends MemoryBankScratchEx {

    /**
     * Delay when doing a XPC standard delay
     */
    public static final int DELAY_XPC_STANDARD = 5;
    /**
     * Repeat byte, success
     */
    public static final byte REPEAT_TOGGLE_SUCCESS = (byte) 0xAA;
    /**
     * Repeat byte, success
     */
    public static final byte REPEAT_TOGGLE_SUCCESS_SHIFT = (byte) 0x55;
    /**
     * 1-Wire command for XPC
     */
    public static final byte XPC_COMMAND = (byte) 0x66;
    /**
     * 1-Wire command for XPC Copy Scratchpad
     */
    public static final byte XPC_COPY_SCRATCHPAD_COMMAND = (byte) 0x99;
    /**
     * The Password container to access the 8 byte passwords
     */
    protected PasswordContainer ibPass = null;

    //--------
    //-------- Constructor
    //--------

    /**
     * Memory bank constructor.  Requires reference to the OneWireContainer
     * this memory bank resides on.
     */
    public MemoryBankScratchFLASHCRCPW(PasswordContainer ibutton) {
        super((OneWireContainer) ibutton);

        ibPass = ibutton;

        // initialize attributes of this memory bank - DEFAULT: DS1963L scratchapd
        bankDescription = "Flash Scratchpad with CRC and Password";
        pageAutoCRC = true;
    }

    //--------
    //-------- PagedMemoryBank I/O methods
    //--------

    /**
     * helper method to pause for specified milliseconds
     */
    private static final void msWait(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
        }
    }

    /**
     * Read a complete memory page with CRC verification provided by the
     * device.  Not supported by all devices.  See the method
     * 'hasPageAutoCRC()'.
     *
     * @param page         page number to read
     * @param readContinue if 'true' then device read is continued without
     *                     re-selecting.  This can only be used if the new
     *                     readPagePacket() continues where the last one
     *                     stopped and it is inside a
     *                     'beginExclusive/endExclusive' block.
     * @param readBuf      byte array to put data read. Must have at least
     *                     'getMaxPacketDataLength()' elements.
     * @param offset       offset into readBuf to place data
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void readPageCRC(int page, boolean readContinue, byte[] readBuf,
                            int offset)
            throws OneWireIOException, OneWireException {
        byte[] extraInfo = new byte[extraInfoLength];

        readPageCRC(page, readContinue, readBuf, offset, extraInfo);
    }

    //--------
    //-------- ScratchPad methods
    //--------

    /**
     * Read a complete memory page with CRC verification provided by the
     * device with extra information.  Not supported by all devices.
     * See the method 'hasPageAutoCRC()'.
     * See the method 'hasExtraInfo()' for a description of the optional
     * extra information.
     *
     * @param page         page number to read
     * @param readContinue if 'true' then device read is continued without
     *                     re-selecting.  This can only be used if the new
     *                     readPagePacket() continues where the last one
     *                     stopped and it is inside a
     *                     'beginExclusive/endExclusive' block.
     * @param readBuf      byte array to put data read. Must have at least
     *                     'getMaxPacketDataLength()' elements.
     * @param offset       offset into readBuf to place data
     * @param extraInfo    byte array to put extra info read into
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void readPageCRC(int page, boolean readContinue, byte[] readBuf,
                            int offset, byte[] extraInfo)
            throws OneWireIOException, OneWireException {

        // only needs to be implemented if supported by hardware
        if (!pageAutoCRC)
            throw new OneWireException(
                    "Read page with CRC not supported in this memory bank");

        // attempt to put device at max desired speed
        if (!readContinue)
            checkSpeed();

        // check if read exceeds memory
        if (page > numberPages)
            throw new OneWireException("Read exceeds memory bank end");

        // read the scratchpad
        readScratchpad(readBuf, offset, pageLength, extraInfo);
    }

    /**
     * Read the scratchpad page of memory from a NVRAM device
     * This method reads and returns the entire scratchpad after the byte
     * offset regardless of the actual ending offset
     *
     * @param readBuf   byte array to place read data into
     *                  length of array is always pageLength.
     * @param offset    offset into readBuf to put data
     * @param len       length in bytes to read
     * @param extraInfo byte array to put extra info read into
     *                  (TA1, TA2, e/s byte)
     *                  length of array is always extraInfoLength.
     *                  Can be 'null' if extra info is not needed.
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void readScratchpad(byte[] readBuf, int offset, int len,
                               byte[] extraInfo)
            throws OneWireIOException, OneWireException {
        int num_crc = 0;

        // select the device
        if (!ib.adapter.select(ib.address)) {
            forceVerify();

            throw new OneWireIOException("Device select failed");
        }

        // build block
        byte[] raw_buf = new byte[extraInfoLength + pageLength + 3];
        raw_buf[0] = READ_SCRATCHPAD_COMMAND;
        System.arraycopy(ffBlock, 0, raw_buf, 1, raw_buf.length - 1);

        // send command and read extra info
        ib.adapter.dataBlock(raw_buf, 0, extraInfoLength + 1);

        // get the starting offset to see when the crc will show up
        int addr = raw_buf[1];
        addr = (addr | ((raw_buf[2] << 8) & 0xFF00)) & 0xFFFF;
        num_crc = pageLength + 3 - (addr & 0x001F) + extraInfoLength;

        // read the rest of the data
        ib.adapter.dataBlock(raw_buf, extraInfoLength + 1, num_crc - (extraInfoLength + 1));

        // check crc of entire block
        if (len == pageLength) {
            if (CRC16.compute(raw_buf, 0, num_crc, 0) != 0x0000B001) {
                forceVerify();
                throw new OneWireIOException("Invalid CRC16 read from device, block " + Convert.toHexString(raw_buf) + " num_crc " + Convert.toHexString((byte) num_crc));
            }
        }

        // optionally extract the extra info
        if (extraInfo != null)
            System.arraycopy(raw_buf, 1, extraInfo, 0, extraInfoLength);

        // extract the page data
        System.arraycopy(raw_buf, extraInfoLength + 1, readBuf, offset, len);
    }

    /**
     * Copy the scratchpad page to memory.
     *
     * @param startAddr starting address
     * @param len       length in bytes that was written already
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void copyScratchpad(int startAddr, int len)
            throws OneWireIOException, OneWireException {
        // select the device
        if (!ib.adapter.select(ib.address)) {
            forceVerify();
            throw new OneWireIOException("Device select failed");
        }

        byte[] buffer = new byte[20];
        buffer[0] = XPC_COMMAND;
        buffer[1] = 0x0C;  // length byte
        buffer[2] = XPC_COPY_SCRATCHPAD_COMMAND;
        buffer[3] = (byte) (startAddr & 0xFF);
        buffer[4] = (byte) (((startAddr & 0xFFFF) >>> 8) & 0xFF);
        buffer[5] = (byte) ((startAddr + len - 1) & 0x1F);

        ibPass.getContainerReadWritePassword(buffer, 6);
        buffer[14] = (byte) 0xFF;
        buffer[15] = (byte) 0xFF;

        // do the block
        ib.adapter.dataBlock(buffer, 0, 16);

        // Compute CRC and verify it is correct
        if (CRC16.compute(buffer, 0, 16, 0) != 0x0000B001)
            throw new OneWireIOException("Invalid CRC16 read from device.");

        ib.adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
        ib.adapter.getByte();

        // delay to allow memory clear
        msWait(DELAY_XPC_STANDARD);

        ib.adapter.setPowerNormal();

        // Read result byte (make several attempts if not done)
        int cnt = 0;
        byte result;
        do {
            result = (byte) ib.adapter.getByte();
        }
        while ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT) && (cnt++ < 50));

        if ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT)) {
            throw new OneWireException(
                    "OneWireContainer53-XPC Copy Scratchpad failed. Return Code " + Convert.toHexString(result));
        }
    }

    /**
     * Write to the scratchpad page of memory a NVRAM device.
     *
     * @param startAddr starting address
     * @param writeBuf  byte array containing data to write
     * @param offset    offset into readBuf to place data
     * @param len       length in bytes to write
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void writeScratchpad(int startAddr, byte[] writeBuf, int offset,
                                int len)
            throws OneWireIOException, OneWireException {
        boolean calcCRC = false;

        if (len > pageLength)
            throw new OneWireException("Write exceeds memory bank end");

        // select the device
        if (!ib.adapter.select(ib.address)) {
            forceVerify();

            throw new OneWireIOException("Device select failed");
        }

        // build block to send
        byte[] raw_buf = new byte[pageLength + 5];

        raw_buf[0] = WRITE_SCRATCHPAD_COMMAND;
        raw_buf[1] = (byte) (startAddr & 0xFF);
        raw_buf[2] = (byte) (((startAddr & 0xFFFF) >>> 8) & 0xFF);

        System.arraycopy(writeBuf, offset, raw_buf, 3, len);

        // check if full page (can utilize CRC)
        if (((startAddr + len) % pageLength) == 0) {
            System.arraycopy(ffBlock, 0, raw_buf, len + 3, 2);
            calcCRC = true;
        }

        // send block, return result
        if (calcCRC)
            ib.adapter.dataBlock(raw_buf, 0, len + 5);
        else
            ib.adapter.dataBlock(raw_buf, 0, len + 3);

        // check crc
        if (calcCRC) {
            if (CRC16.compute(raw_buf, 0, len + 5, 0) != 0x0000B001) {
                forceVerify();

                throw new OneWireIOException("Invalid CRC16 read from device, block: " + Convert.toHexString(raw_buf));
            }
        }
    }
}
