
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Maxim Integrated Products, All Rights Reserved.
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

import com.dalsemi.onewire.adapter.DSPortAdapter;

import java.util.Enumeration;
import java.util.Vector;

/**
 * <P> 1-Wire container for 512 byte memory iButton, DS1993.  This container
 * encapsulates the functionality of the iButton family
 * type <B>06</B> (hex)</P>
 *
 * <P> This iButton is primarily used as a read/write portable memory device. </P>
 *
 * <H3> Features </H3>
 * <UL>
 * <LI> 4096 bits (512 bytes) of read/write nonvolatile memory
 * <LI> 256-bit (32-byte) scratchpad ensures integrity of data
 * transfer
 * <LI> Memory partitioned into 256-bit (32-byte) pages for
 * packetizing data
 * <LI> Data integrity assured with strict read/write
 * protocols
 * <LI> Operating temperature range from -40@htmlonly &#176C @endhtmlonly to
 * +70@htmlonly &#176C @endhtmlonly
 * <LI> Over 10 years of data retention
 * </UL>
 *
 * <H3> Memory </H3>
 *
 * <P> The memory can be accessed through the objects that are returned
 * from the {@link #getMemoryBanks() getMemoryBanks} method. </P>
 * <p>
 * The following is a list of the MemoryBank instances that are returned:
 *
 * <UL>
 * <LI> <B> Scratchpad </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 32 starting at physical address 0
 * <LI> <I> Features</I> Read/Write not-general-purpose volatile
 * <LI> <I> Pages</I> 1 pages of length 32 bytes
 * <LI> <I> Extra information for each page</I>  Target address, offset, length 3
 * </UL>
 * <LI> <B> Main Memory </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 512 starting at physical address 0
 * <LI> <I> Features</I> Read/Write general-purpose non-volatile
 * <LI> <I> Pages</I> 16 pages of length 32 bytes giving 29 bytes Packet data payload
 * </UL>
 * </UL>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> See the usage example in
 * {@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer}
 * to enumerate the MemoryBanks.
 * <DD> See the usage examples in
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank} and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * for bank specific operations.
 * </DL>
 *
 * <H3> DataSheet </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1992-DS1994.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1992-DS1994.pdf</A>
 * </DL>
 *
 * @author DS
 * @version 0.00, 28 Aug 2000
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 */
public class OneWireContainer06
        extends OneWireContainer {

    //--------
    //-------- Constructors
    //--------

    /**
     * Create an empty container that is not complete until after a call
     * to <code>setupContainer</code>. <p>
     * <p>
     * This is one of the methods to construct a container.  The others are
     * through creating a OneWireContainer with parameters.
     *
     * @see #setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter, byte[]) super.setupContainer()
     */
    public OneWireContainer06() {
        super();
    }

    /**
     * Create a container with the provided adapter instance
     * and the address of the iButton or 1-Wire device.<p>
     * <p>
     * This is one of the methods to construct a container.  The other is
     * through creating a OneWireContainer with NO parameters.
     *
     * @param sourceAdapter adapter instance used to communicate with
     *                      this iButton
     * @param newAddress    {@link com.dalsemi.onewire.utils.Address Address}
     *                      of this 1-Wire device
     * @see #OneWireContainer06() OneWireContainer06
     * @see com.dalsemi.onewire.utils.Address utils.Address
     */
    public OneWireContainer06(DSPortAdapter sourceAdapter, byte[] newAddress) {
        super(sourceAdapter, newAddress);
    }

    /**
     * Create a container with the provided adapter instance
     * and the address of the iButton or 1-Wire device.<p>
     * <p>
     * This is one of the methods to construct a container.  The other is
     * through creating a OneWireContainer with NO parameters.
     *
     * @param sourceAdapter adapter instance used to communicate with
     *                      this 1-Wire device
     * @param newAddress    {@link com.dalsemi.onewire.utils.Address Address}
     *                      of this 1-Wire device
     * @see #OneWireContainer06() OneWireContainer06
     * @see com.dalsemi.onewire.utils.Address utils.Address
     */
    public OneWireContainer06(DSPortAdapter sourceAdapter, long newAddress) {
        super(sourceAdapter, newAddress);
    }

    /**
     * Create a container with the provided adapter instance
     * and the address of the iButton or 1-Wire device.<p>
     * <p>
     * This is one of the methods to construct a container.  The other is
     * through creating a OneWireContainer with NO parameters.
     *
     * @param sourceAdapter adapter instance used to communicate with
     *                      this 1-Wire device
     * @param newAddress    {@link com.dalsemi.onewire.utils.Address Address}
     *                      of this 1-Wire device
     * @see #OneWireContainer06() OneWireContainer06
     * @see com.dalsemi.onewire.utils.Address utils.Address
     */
    public OneWireContainer06(DSPortAdapter sourceAdapter, String newAddress) {
        super(sourceAdapter, newAddress);
    }

    //--------
    //-------- Methods
    //--------

    /**
     * Get the Maxim Integrated Products part number of the iButton
     * or 1-Wire Device as a string.  For example 'DS1992'.
     *
     * @return iButton or 1-Wire device name
     */
    public String getName() {
        return "DS1993";
    }

    /**
     * Get a short description of the function of this iButton
     * or 1-Wire Device type.
     *
     * @return device description
     */
    public String getDescription() {
        return "4096 bit read/write nonvolatile memory partitioned "
                + "into sixteen pages of 256 bits each. ";
    }

    /**
     * Get an enumeration of memory bank instances that implement one or more
     * of the following interfaces:
     * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
     * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank},
     * and {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}.
     *
     * @return <CODE>Enumeration</CODE> of memory banks
     */
    public Enumeration getMemoryBanks() {
        Vector bank_vector = new Vector(2);

        // scratchpad
        MemoryBankScratch scratch = new MemoryBankScratch(this);

        bank_vector.addElement(scratch);

        // NVRAM
        bank_vector.addElement(new MemoryBankNV(this, scratch));

        return bank_vector.elements();
    }
}
