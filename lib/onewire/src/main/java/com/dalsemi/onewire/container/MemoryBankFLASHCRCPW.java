/*---------------------------------------------------------------------------
 * Copyright (C) 2002 Maxim Integrated Products, All Rights Reserved.
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
 * Memory bank class for the OTP memory with built-in CRC generation and Password
 * protected memory read/write iButtons and 1-Wire Devices.  An example of
 * such a devices is the DS1925 Thermochron or password protected
 * log memory.
 *
 * @author DS
 * @version 1.00, 6 April 2015
 */
public class MemoryBankFLASHCRCPW
        extends MemoryBankNVCRC {
    /**
     * External Power (XPC) Command
     */
    public static final byte XPC_COMMAND = (byte) 0x66;

    /**
     * XPC Read Memory (with CRC and Password) Command (sub command of XPC command)
     */
    public static final byte XPC_READ_MEMORY_CRC_PW_COMMAND = (byte) 0x44;

    /**
     * Delay when doing a read memory command
     */
    public static final int DELAY_XPC_READ_MEMORY = 5;

    /**
     * Scratchpad with Password.  Used as container for password.
     */
    protected MemoryBankScratchFLASHCRCPW scratchpadPW = null;

    /**
     * Password Container to access the passwords for the memory bank.
     */
    protected PasswordContainer ibPass = null;

    /**
     * Memory bank constructor.  Requires reference to the OneWireContainer
     * this memory bank resides on.
     */
    public MemoryBankFLASHCRCPW(PasswordContainer ibutton, MemoryBankScratchFLASHCRCPW scratch) {
        super((OneWireContainer) ibutton, scratch);

        ibPass = ibutton;

        // initialize attributes of this memory bank
        pageAutoCRC = true;
        readContinuePossible = true;
        numVerifyBytes = 0;

        scratchpadPW = scratch;
    }

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
     * device with extra information.  Not supported by all devices.
     * See the method 'hasPageAutoCRC()'.
     * See the method 'hasExtraInfo()' for a description of the optional
     * extra information.
     *
     * @param page         page number to read
     * @param readContinue if 'true' then device read is continued without
     *                     re-selecting.  This can only be used if the new
     *                     readPagePacket() continuous where the last one
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
        readPageCRC(page, readContinue, readBuf, offset, extraInfo,
                extraInfoLength);
    }

    /**
     * Read a complete memory page with CRC verification provided by the
     * device with extra information.  Not supported by all devices.
     * If not extra information available then just call with extraLength=0.
     *
     * @param page         page number to read
     * @param readContinue if 'true' then device read is continued without
     *                     re-selecting.  This can only be used if the new
     *                     readPagePacket() continuous where the last one
     *                     stopped and it is inside a
     *                     'beginExclusive/endExclusive' block.
     * @param readBuf      byte array to put data read. Must have at least
     *                     'getMaxPacketDataLength()' elements.
     * @param offset       offset into readBuf to place data
     * @param extraInfo    byte array to put extra info read into
     * @param extraLength  length of extra information
     * @throws OneWireIOException
     * @throws OneWireException
     */
    protected void readPageCRC(int page, boolean readContinue,
                               byte[] readBuf, int offset, byte[] extraInfo,
                               int extraLength)
            throws OneWireIOException, OneWireException {
        int last_crc = 0;
        byte[] raw_buf;
        byte temp;

        // only needs to be implemented if supported by hardware
        if (!pageAutoCRC)
            throw new OneWireException(
                    "Read page with CRC not supported in this memory bank");

        // attempt to put device at max desired speed
        if (!readContinue)
            sp.checkSpeed();

        // check if read exceeds memory
        if (page > numberPages)
            throw new OneWireException("Read exceeds memory bank end");

        // see if need to access the device
        if (!readContinue || !readContinuePossible) {
            // select the device
            if (!ib.adapter.select(ib.address)) {
                sp.forceVerify();
                throw new OneWireIOException("Device select failed");
            }

            // build start reading memory block
            raw_buf = new byte[16];
            raw_buf[0] = XPC_COMMAND;
            raw_buf[1] = 0x0B;  // length byte
            raw_buf[2] = XPC_READ_MEMORY_CRC_PW_COMMAND;

            int addr = page * pageLength + startPhysicalAddress;

            if (addr < 576) {
                // use byte addressing
                raw_buf[3] = (byte) (addr & 0xFF);
                raw_buf[4] = (byte) (((addr & 0xFFFF) >>> 8) & 0xFF);
            } else {
                // use page addressing
                int physical_page = page + startPhysicalAddress / pageLength;

                if (pageLength == 64) {
                    // use standard page number to start
                    physical_page *= 2;
                    // TA1
                    raw_buf[3] = (byte) (physical_page & 0xFF);
                    // TA2
                    raw_buf[4] = (byte) ((((physical_page & 0xFFFF) >>> 8) & 0xFF) | 0xC0);
                } else {
                    // TA1
                    raw_buf[3] = (byte) (physical_page & 0xFF);
                    // TA2
                    raw_buf[4] = (byte) ((((physical_page & 0xFFFF) >>> 8) & 0xFF) | 0x80);
                }
            }

            if (ibPass.isContainerReadWritePasswordSet())
                ibPass.getContainerReadWritePassword(raw_buf, 5);
            else
                ibPass.getContainerReadOnlyPassword(raw_buf, 5);

            // set CRC ff bytes
            System.arraycopy(ffBlock, 0, raw_buf, 13, 2);

            // do the first block for xpc command, length, sub command, TA1, TA2, and password
            ib.adapter.dataBlock(raw_buf, 0, 15);

            // check CRC
            if (CRC16.compute(raw_buf, 0, 15, 0) != 0x0000B001) {
                sp.forceVerify();
                throw new OneWireIOException("Invalid CRC16 read from device, block " + Convert.toHexString(raw_buf));
            }
        }

        // get strong pull-up ready
        ib.adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
        ib.adapter.getByte();

        // delay to allow
        msWait(DELAY_XPC_READ_MEMORY);

        ib.adapter.setPowerNormal();

        // pre-fill with 0xFF (FF byte, page data, crc16)
        raw_buf = new byte[pageLength + extraLength + 3 + numVerifyBytes];
        System.arraycopy(ffBlock, 0, raw_buf, 0, raw_buf.length);

        // send block to read data + crc
        ib.adapter.dataBlock(raw_buf, 0, raw_buf.length);

        // check the CRC
        if (CRC16.compute(raw_buf, 1, raw_buf.length - numVerifyBytes - 1, 0) != 0x0000B001) {
            sp.forceVerify();
            throw new OneWireIOException("Invalid CRC16 read from device.  Raw: " + Convert.toHexString(raw_buf));
        }

        // extract the page data
        System.arraycopy(raw_buf, 1, readBuf, offset, pageLength);

        // optional extract the extra info
        if (extraInfo != null)
            System.arraycopy(raw_buf, pageLength, extraInfo, 0, extraLength);
    }

    /**
     * Read  memory in the current bank with no CRC checking (device or
     * data). The resulting data from this API may or may not be what is on
     * the 1-Wire device.  It is recommends that the data contain some kind
     * of checking (CRC) like in the readPagePacket() method or have
     * the 1-Wire device provide the CRC as in readPageCRC().  readPageCRC()
     * however is not supported on all memory types, see 'hasPageAutoCRC()'.
     * If neither is an option then this method could be called more
     * then once to at least verify that the same thing is read consistently.
     *
     * @param startAddr    starting physical address
     * @param readContinue if 'true' then device read is continued without
     *                     re-selecting.  This can only be used if the new
     *                     read() continuous where the last one led off
     *                     and it is inside a 'beginExclusive/endExclusive'
     *                     block.
     * @param readBuf      byte array to place read data into
     * @param offset       offset into readBuf to place data
     * @param len          length in bytes to read
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void read(int startAddr, boolean readContinue, byte[] readBuf,
                     int offset, int len)
            throws OneWireIOException, OneWireException {
        int i;
        byte[] raw_buf = new byte[64];

        // attempt to put device at max desired speed
        if (!readContinue) {
            sp.checkSpeed();
        }

        // check if read exceeds memory
        if ((startAddr + len) > size)
            throw new OneWireException("Read exceeds memory bank end");

        // pre-fill readBuf with 0xFF
        int startOffset = startAddr % pageLength;

        // First, count how many bytes are leftover from the
        // whole page reads.
        int bytesLeftover = (len % pageLength);

        // Second, account for the whole number of pages
        int pgs = (len / pageLength);

        // Third, if read starts in middle, go ahead and count
        // that whole page as 1 more page.
        if (startOffset > 0) {
            pgs += 1;
            // subtract the bytes from this extra page from our
            // leftover bytes, since we just accounted for each
            // byte to the end of this first page.
            bytesLeftover -= (pageLength - startOffset);
        }

        // Finally, account for 1 more page read if there are
        // any more left over bytes
        if (bytesLeftover > 0)
            pgs += 1;

        int startPage = startAddr / pageLength;

        for (i = 0; i < pgs; i++) {
            readPageCRC(startPage + i, (i != 0), raw_buf, 0, null, 0);

            if (i == 0) {
                System.arraycopy(raw_buf, startOffset, readBuf, offset, (pageLength - startOffset));
            } else if ((i == (pgs - 1)) && (bytesLeftover != 0)) {
                System.arraycopy(raw_buf, 0, readBuf, (offset + (pageLength - startOffset) + ((i - 1) * pageLength)),
                        bytesLeftover);
            } else {
                System.arraycopy(raw_buf, 0, readBuf, (offset + (pageLength - startOffset) + ((i - 1) * pageLength)),
                        pageLength);
            }
        }
    }

    /**
     * Write  memory in the current bank.  It is recommended that
     * when writing  data that some structure in the data is created
     * to provide error free reading back with read().  Or the
     * method 'writePagePacket()' could be used which automatically
     * wraps the data in a length and CRC.
     * <p>
     * When using on Write-Once devices care must be taken to write into
     * into empty space.  If write() is used to write over an unlocked
     * page on a Write-Once device it will fail.  If write verification
     * is turned off with the method 'setWriteVerification(false)' then
     * the result will be an 'AND' of the existing data and the new data.
     *
     * @param startAddr starting address
     * @param writeBuf  byte array containing data to write
     * @param offset    offset into writeBuf to get data
     * @param len       length in bytes to write
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public void write(int startAddr, byte[] writeBuf, int offset, int len)
            throws OneWireIOException, OneWireException {
        // find the last (non-inclusive) address for this write
        int endingOffset = (startAddr + len);
        if ((endingOffset & 0x1F) > 0) {
            // find the number of bytes left until the end of the page
            int numBytes = pageLength - (endingOffset & 0x1F);
            if ( // endingOffset == 0x250 ???? why??
                    (
                            ibPass.hasReadWritePassword() &&
                                    (0xFFE0 & endingOffset) == (0xFFE0 & ibPass.getReadWritePasswordAddress()) &&
                                    endingOffset < (ibPass.getReadWritePasswordAddress() + ibPass.getReadWritePasswordLength())
                    ) || (
                            ibPass.hasReadOnlyPassword() &&
                                    (0xFFE0 & endingOffset) == (0xFFE0 & ibPass.getReadOnlyPasswordAddress()) &&
                                    endingOffset < (ibPass.getReadOnlyPasswordAddress() + ibPass.getReadOnlyPasswordLength())
                    ) || (
                            ibPass.hasWriteOnlyPassword() &&
                                    (0xFFE0 & endingOffset) == (0xFFE0 & ibPass.getWriteOnlyPasswordAddress()) &&
                                    endingOffset < (ibPass.getWriteOnlyPasswordAddress() + ibPass.getWriteOnlyPasswordLength())
                    )
            ) {

                // password block would be written to with potentially bad data
                throw new OneWireException(
                        "Executing write would overwrite password control registers with "
                                + "potentially invalid data.  Please ensure write does not occur over"
                                + "password control register page, or the password control data is "
                                + "specified exactly in the write buffer.");
            }

            byte[] tempBuf = new byte[len + numBytes];
            System.arraycopy(writeBuf, offset, tempBuf, 0, len);
            read(endingOffset, false, tempBuf, len, numBytes);

            super.write(startAddr, tempBuf, 0, tempBuf.length);
        } else {
            // write does extend to end of page
            super.write(startAddr, writeBuf, offset, len);
        }

    }
}
