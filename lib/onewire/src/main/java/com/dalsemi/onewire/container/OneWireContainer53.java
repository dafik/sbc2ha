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
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.DSPortAdapterAbstract;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.utils.Convert;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;


/**
 * <P> 1-Wire&reg; container for a Temperature Logging iButton, DS1925.
 * This container encapsulates the functionality of the 1-Wire family type <B>53</B> (hex).
 * </P>
 *
 * <H3> Features </H3>
 * <UL>
 * <LI> Logs up to 125,440 consecutive temperature measurements in
 * nonvolatile, read-only memory
 * <LI> Real-Time clock
 * <LI> Programmable high and low temperature alarms
 * <LI> Automatically 'wakes up' and logs temperature at user-programmable intervals
 * <LI> 4096 bits of general-purpose nonvolatile memory (write-once and clear)
 * <LI> 256-bit scratchpad ensures integrity of data transfer
 * <LI> On-chip 16-bit CRC generator to verify read operations
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
 * <LI> <B> Scratchpad with CRC and Password support </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 32 starting at physical address 0
 * <LI> <I> Features</I> Read/Write not-general-purpose volatile
 * <LI> <I> Pages</I> 1 page of length 32 bytes
 * <LI> <I> Page Features </I> page-device-CRC
 * <li> <i> Extra information for each page</i>  Target address, offset, length 3
 * <LI> <i> Supports XPC Copy Scratchpad With Password command </I>
 * </UL>
 * <LI> <B> Main Memory </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 512 starting at physical address 0
 * <LI> <I> Features</I> Read/Write-once general-purpose non-volatile
 * <LI> <I> Pages</I> 16 pages of length 32 bytes giving 29 bytes Packet data payload
 * <LI> <I> Page Features </I> page-device-CRC
 * <LI> <I> Read-Only and Read/Write password </I> if enabled, passwords are required for
 * reading from and writing to the device.
 * <LI> <I> Clear with ClearMemory() method
 * </UL>
 * <LI> <B> Register control </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 64 starting at physical address 512
 * <LI> <I> Features</I> Read/Write not-general-purpose non-volatile
 * <LI> <I> Pages</I> 2 pages of length 32 bytes
 * <LI> <I> Page Features </I> page-device-CRC
 * <LI> <I> Read-Only and Read/Write password </I> if enabled, passwords are required for
 * reading from and writing to the device.
 * </UL>
 * <LI> <B> Backup Register control </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 64 starting at physical address 608
 * <LI> <I> Features</I> Read-only not-general-purpose non-volatile
 * <LI> <I> Pages</I> 2 pages of length 32 bytes
 * <LI> <I> Page Features </I> page-device-CRC
 * <LI> <I> Read-Only and Read/Write password </I> if enabled, passwords are required for
 * reading from the device.
 * </UL>
 * <LI> <B> Temperature log </B>
 * <UL>
 * <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * <LI> <I> Size </I> 125,440 starting at physical address 4096
 * <LI> <I> Features</I> Read-only not-general-purpose non-volatile
 * <LI> <I> Pages</I> 3920 pages of length 32 bytes
 * <LI> <I> Page Features </I> page-device-CRC
 * <LI> <I> Read-Only and Read/Write password </I> if enabled, passwords are required for
 * reading from the device.
 * <LI> <I> Can be read 2-pages (64 bytes) at a time
 * </UL>
 * </UL>
 *
 * <H3> Usage </H3>
 *
 * <p>The code below starts a mission with the following characteristics:
 * <ul>
 *     <li>Sets temperature log low resolution</li>
 *     <li>High temperature alarm of 28.0@htmlonly &#176C @endhtmlonly and a low temperature alarm of 23.0@htmlonly &#176C @endhtmlonly.</li>
 *     <li>Sets the Real-Time Clock to the host system's clock.</li>
 *     <li>The mission will start in 2 minutes.</li>
 *     <li>A sample rate of 5 minutes.</li>
 * </ul></p>
 * <pre><code>
 *       // "ID" is a byte array of size 8 with an address of a part we
 *       // have already found with family code 22 hex
 *       // "access" is a DSPortAdapter
 *       OneWireContainer53 DS1925 = (OneWireContainer53)access.getDeviceContainer(ID);
 *       DS1925.setupContainer(access,ID);
 *       //  stop the currently running mission, if there is one
 *       DS1925.stopMission();
 *       //  clear the previous mission results
 *       DS1925.clearMemory(true);
 *       //  set the high temperature alarm to 28 C
 *       DS1925.setMissionAlarm(DS1925.TEMPERATURE_CHANNEL, DS1925.ALARM_HIGH, 28);
 *       DS1925.setMissionAlarmEnable(DS1925.TEMPERATURE_CHANNEL,
 *          DS1925.ALARM_HIGH, true);
 *       //  set the low temperature alarm to 23 C
 *       DS1925.setMissionAlarm(DS1925.TEMPERATURE_CHANNEL, DS1925.ALARM_LOW, 23);
 *       DS1925.setMissionAlarmEnable(DS1925.TEMPERATURE_CHANNEL,
 *          DS1925.ALARM_LOW, true);
 *       // set temperature to low resolution.
 *       DS1925.setMissionResolution(DS1925.TEMPERATURE_CHANNEL,
 *          DS1925.getMissionResolutions()[0]);
 *       // enable temperature
 *       boolean[] enableChannel = new boolean[DS1925.getNumberMissionChannels()];
 *       enableChannel[DS1925.TEMPERATURE_CHANNEL] = true;
 *       //  now start the mission with a sample rate of 5 minutes
 *       DS1925.startNewMission(300, 2, false, true, enableChannel);
 * </code></pre>
 * <p>The following code processes the mission log:</p>
 * <code><pre>
 *       DS1925.loadMissionResults();
 *       System.out.println("Temperature Readings");
 *       if(DS1925.getMissionChannelEnable(owc.TEMPERATURE_CHANNEL))
 *       {
 *          int dataCount =
 *             DS1925.getMissionSampleCount(DS1925.TEMPERATURE_CHANNEL);
 *          System.out.println("SampleCount = " + dataCount);
 *          for(int i=0; i&lt;dataCount; i++)
 *          {
 *             System.out.println(
 *                DS1925.getMissionSample(DS1925.TEMPERATURE_CHANNEL, i));
 *          }
 *       }
 * </pre></code>
 *
 * <p>Also see the usage examples in the {@link com.dalsemi.onewire.container.TemperatureContainer TemperatureContainer}
 * and {@link com.dalsemi.onewire.container.ClockContainer ClockContainer}
 * and {@link com.dalsemi.onewire.container.ADContainer ADContainer}
 * interfaces.</p>
 * <p>
 * For examples regarding memory operations,
 * <uL>
 * <li> See the usage example in
 * {@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer}
 * to enumerate the MemoryBanks.
 * <li> See the usage examples in
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank} and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * for bank specific operations.
 * </uL>
 *
 * <H3> DataSheet </H3>
 * <P>DataSheet link is unavailable at time of publication.  Please visit the website
 * and search for DS1925 to find the current datasheet.
 * <DL>
 * <DD><A HREF="http://www.maxim-ic.com/">Maxim Website</A>
 * </DL>
 *
 * @author DS
 * @version 2.00, 7 March 2016
 * @see com.dalsemi.onewire.container.OneWireSensor
 * @see com.dalsemi.onewire.container.TemperatureContainer
 * @see com.dalsemi.onewire.container.MissionContainer
 * @see com.dalsemi.onewire.container.PasswordContainer
 */
public class OneWireContainer53 extends OneWireContainer implements PasswordContainer, MissionContainer, ClockContainer, TemperatureContainer, ADContainer {
    /**
     * Refers to the Temperature Channel for this device
     */
    public static final int TEMPERATURE_CHANNEL = 0;
    /**
     * 1-Wire command for XPC
     */
    public static final byte XPC_COMMAND = (byte) 0x66;
    /**
     * 1-Wire command for XPC Read Memory CRC With Password
     */
    public static final byte XPC_READ_MEMORY_CRC_PW_COMMAND = (byte) 0x44;
    /**
     * 1-Wire command for XPC Sub-command Clear Memory With Password
     */
    public static final byte XPC_CLEAR_MEMORY_PW_COMMAND = (byte) 0x96;
    /**
     * 1-Wire command for XPC Sub-command Read Battery Voltage
     */
    public static final byte XPC_READ_BATTERY_COMMAND = (byte) 0x33;
    /**
     * 1-Wire command for XPC Forced Conversion
     */
    public static final byte XPC_FORCED_CONVERSION_COMMAND = (byte) 0x4B;
    /**
     * 1-Wire command for XPC Start Mission With Password
     */
    public static final byte XPC_START_MISSION_COMMAND = (byte) 0xDD;
    /**
     * 1-Wire command for XPC Stop Mission With Password
     */
    public static final byte XPC_STOP_MISSION_COMMAND = (byte) 0xBB;
    /**
     * Size of pages when reading log
     */
    public static final int LOG_PAGE_SIZE = 64;
    /**
     * Delay when doing a XPC standard delay
     */
    public static final int DELAY_XPC_STANDARD = 6;
    /**
     * Delay when doing a XPC long standard delay
     */
    public static final int DELAY_XPC_LONG_STANDARD = 15;
    /**
     * Delay when doing a forced conversion command
     */
    public static final int DELAY_FORCED_CONVERSION = 500;
    /**
     * Delay when doing a XPC clear memory (log) command
     */
    public static final int DELAY_XPC_CLEAR_LOG = 1500;
    /**
     * Repeat byte, not done
     */
    public static final byte REPEAT_NOT_DONE = (byte) 0xFF;
    /**
     * Repeat byte, success
     */
    public static final byte REPEAT_TOGGLE_SUCCESS = (byte) 0xAA;
    /**
     * Repeat byte, success (shifted 1 bit)
     */
    public static final byte REPEAT_TOGGLE_SUCCESS_SHIFT = (byte) 0x55;
    /**
     * Repeat byte, mission in progress
     */
    public static final byte REPEAT_MISSION_IN_PROGRESS = (byte) 0x22;
    /**
     * Repeat byte, error during write
     */
    public static final byte REPEAT_ERROR_WRITE = (byte) 0x44;
    /**
     * Repeat byte, invalid parameter
     */
    public static final byte REPEAT_INVALID_PARAMETER = (byte) 0x77;
    /**
     * Repeat byte, invalid authorization
     */
    public static final byte REPEAT_INVALID_AUTH = (byte) 0x33;
    /**
     * Repeat byte, invalid password
     */
    public static final byte REPEAT_INVALID_PW = (byte) 0x11;
    /**
     * Repeat byte, device in start state
     */
    public static final byte REPEAT_DEVICE_START_STATE = (byte) 0x00;
    /**
     * Address of the Real-time Clock Time value
     */
    public static final int RTC_TIME = 0x200;
    /**
     * Address of the Sample Rate Register
     */
    public static final int SAMPLE_RATE = 0x206;// 2 bytes, LSB first, MSB no greater than 0x3F
    /**
     * Address of the Temperature Low Alarm Register
     */
    public static final int TEMPERATURE_LOW_ALARM_THRESHOLD = 0x208;
    /**
     * Address of the Temperature High Alarm Register
     */
    public static final int TEMPERATURE_HIGH_ALARM_THRESHOLD = 0x209;
    /**
     * Address of the last temperature conversion's LSB
     */
    public static final int LAST_TEMPERATURE_CONVERSION_LSB = 0x20C;
    /**
     * Address of the last temperature conversion's MSB
     */
    public static final int LAST_TEMPERATURE_CONVERSION_MSB = 0x20D;
    /**
     * Address of the last data conversion's LSB
     */
    public static final int LAST_DATA_CONVERSION_LSB = 0x20E;
    /**
     * Address of the last data conversion's MSB
     */
    public static final int LAST_DATA_CONVERSION_MSB = 0x20F;
    /**
     * Address of Temperature Control Register
     */
    public static final int TEMPERATURE_CONTROL_REGISTER = 0x210;
    /**
     * Temperature Control Register Bit: Enable Data Low Alarm
     */
    public static final byte TCR_BIT_ENABLE_TEMPERATURE_LOW_ALARM = (byte) 0x01;
    /**
     * Temperature Control Register Bit: Enable Data Low Alarm
     */
    public static final byte TCR_BIT_ENABLE_TEMPERATURE_HIGH_ALARM = (byte) 0x02;
    /**
     * Address of Data Control Register
     */
    public static final int DATA_CONTROL_REGISTER = 0x211;
    /**
     * Data Control Register Bit: Enable Data Low Alarm
     */
    public static final byte DCR_BIT_ENABLE_DATA_LOW_ALARM = (byte) 0x01;
    /**
     * Data Control Register Bit: Enable Data High Alarm
     */
    public static final byte DCR_BIT_ENABLE_DATA_HIGH_ALARM = (byte) 0x02;
    /**
     * Address of Real-Time Clock Control Register
     */
    public static final int RTC_CONTROL_REGISTER = 0x212;
    /**
     * Real-Time Clock Control Register Bit: Enable Oscillator
     */
    public static final byte RCR_BIT_ENABLE_OSCILLATOR = (byte) 0x01;
    /**
     * Real-Time Clock Control Register Bit: Enable High Speed Sample
     */
    public static final byte RCR_BIT_ENABLE_HIGH_SPEED_SAMPLE = (byte) 0x02;
    /**
     * Address of Mission Control Register
     */
    public static final int MISSION_CONTROL_REGISTER = (byte) 0x213;
    /**
     * Mission Control Register Bit: Enable Temperature Logging
     */
    public static final byte MCR_BIT_ENABLE_TEMPERATURE_LOGGING = (byte) 0x01;
    /**
     * Mission Control Register Bit: Enable Data Logging
     */
    public static final byte MCR_BIT_ENABLE_DATA_LOGGING = (byte) 0x02;
    /**
     * Mission Control Register Bit: Set Temperature Resolution
     */
    public static final byte MCR_BIT_TEMPERATURE_RESOLUTION = (byte) 0x04;

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// 1-Wire Commands
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************
    /**
     * Mission Control Register Bit: Set Data Resolution
     */
    public static final byte MCR_BIT_DATA_RESOLUTION = (byte) 0x08;
    /**
     * Mission Control Register Bit: Enable Rollover
     */
    public static final byte MCR_BIT_ENABLE_ROLLOVER = (byte) 0x10;
    /**
     * Mission Control Register Bit: Start Mission on Temperature Alarm
     */
    public static final byte MCR_BIT_START_MISSION_ON_TEMPERATURE_ALARM = (byte) 0x20;
    /**
     * Address of Alarm Status Register
     */
    public static final int ALARM_STATUS_REGISTER = 0x214;
    /**
     * Alarm Status Register Bit: Temperature Low Alarm
     */
    public static final byte ASR_BIT_TEMPERATURE_LOW_ALARM = (byte) 0x01;
    /**
     * Alarm Status Register Bit: Temperature High Alarm
     */
    public static final byte ASR_BIT_TEMPERATURE_HIGH_ALARM = (byte) 0x02;
    /**
     * Alarm Status Register Bit: Data Low Alarm
     */
    public static final byte ASR_BIT_DATA_LOW_ALARM = (byte) 0x04;
    /**
     * Alarm Status Register Bit: Data High Alarm
     */
    public static final byte ASR_BIT_DATA_HIGH_ALARM = (byte) 0x08;
    /**
     * Alarm Status Register Bit: Battery On Reset
     */
    public static final byte ASR_BIT_BATTERY_ON_RESET = (byte) 0x80;

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Delays for operations
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************
    /**
     * Address of General Status Register
     */
    public static final int GENERAL_STATUS_REGISTER = 0x215;
    /**
     * General Status Register Bit: Sample In Progress
     */
    public static final byte GSR_BIT_SAMPLE_IN_PROGRESS = (byte) 0x01;
    /**
     * General Status Register Bit: Mission In Progress
     */
    public static final byte GSR_BIT_MISSION_IN_PROGRESS = (byte) 0x02;
    /**
     * General Status Register Bit: Conversion In Progress
     */
    public static final byte GSR_BIT_CONVERSION_IN_PROGRESS = (byte) 0x04;

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Repeat Bytes
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************
    /**
     * General Status Register Bit: Memory Cleared
     */
    public static final byte GSR_BIT_MEMORY_CLEARED = (byte) 0x08;
    /**
     * General Status Register Bit: Waiting for Temperature Alarm
     */
    public static final byte GSR_BIT_WAITING_FOR_TEMPERATURE_ALARM = (byte) 0x10;
    /**
     * Address of the Mission Start Delay
     */
    public static final int MISSION_START_DELAY = 0x216; // 3 bytes, LSB first
    /**
     * Address of the Mission Timestamp Time value
     */
    public static final int MISSION_TIMESTAMP_TIME = 0x219;
    /**
     * Address of Device Configuration Register
     */
    public static final int DEVICE_CONFIGURATION_BYTE = 0x226;
    /**
     * Value of Device Configuration Register for DS1925
     */
    public static final byte DCB_DS1925 = 0x00;
    /**
     * Address of the Password Control Register.
     */
    public static final int PASSWORD_CONTROL_REGISTER = 0x227;
    /**
     * Address of Read Access Password.
     */
    public static final int READ_ACCESS_PASSWORD = 0x228;
    /**
     * Address of the Read Write Access Password.
     */
    public static final int READ_WRITE_ACCESS_PASSWORD = 0x230;


// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Register addresses and control bits
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************
    /**
     * Address of the Mission Sample Count
     */
    public static final int MISSION_SAMPLE_COUNT = 0x220;
    /**
     * maximum size of the mission log
     */
    public static final int MISSION_LOG_SIZE = 125440;
    /**
     * Address of the Device Sample Count
     */
    public static final int DEVICE_SAMPLE_COUNT = 0x223;
    // enables/disables debugging
    private static final boolean DEBUG = false;
    // when reading a page, the memory bank may throw a crc exception if the device
    // is sampling or starts sampling during the read.  This value sets how many
    // times the device retries before passing the exception on to the application.
    private static final int MAX_READ_RETRY_CNT = 10;
    // Polling for completion maximum
    private static final int MAX_COMPUTE_POLL = 50;
    // the length of the Read-Only and Read/Write password registers
    private static final int PASSWORD_LENGTH = 8;
    // temperature is 8-bit or 11-bit
    private static final double[] temperatureResolutions = new double[]{.5d, .0625d};
    // first year that calendar starts counting years from
    private static final int FIRST_YEAR_EVER = 2000;
    // used to 'enable' passwords
    private static final byte ENABLE_BYTE = (byte) 0xAA;
    // used to 'disable' passwords
    private static final byte DISABLE_BYTE = 0x00;
    // Minimum sample rate in seconds
    private static final int SAMPLE_SECONDS_MIN = 180;
    private static final String PART_NUMBER_DS1925 = "DS1925";
    private static final String DESCRIPTION_DS1925 = "The DS1925 Temperature Logger iButtons are rugged, " + "self-sufficient systems that measure temperature and record the " + "result in a protected memory section. The recording is done at a " + "user-defined rate. A total of 122K 8-bit readings or 61K 16-bit " + "readings taken at equidistant intervals ranging from 5m to 273hrs " + "can be stored. In addition to this, there are 512 bytes of EEPROM for " + "storing application-specific information." + "A mission to collect data can be programmed to begin " + "immediately, or after a user-defined delay or after a temperature " + "alarm. Access to the memory and control functions can be password " + "protected.";
    /**
     * The current password for readingfrom this device.
     */
    private final byte[] readPassword = new byte[8];
    /**
     * The current password for reading/writing from/to this device.
     */
    private final byte[] readWritePassword = new byte[8];
    // state buffer for A/D state (kept separate from normal sensor state buffer)
    private final byte[] ad_state = new byte[20];
    // Temperature resolution in degrees Celsius
    private final double temperatureResolution = 0.5;
    /**
     * Number of bytes used to store data values (0, 1, or 2)
     */
    private final int dataBytes = 0;
    /**
     * indicates whether or not the log has rolled over
     */
    private final boolean rolledOver = false;
    // indicates whether or not the device configuration has been read
    // and all the ranges for the part have been set.
    private boolean isContainerVariablesSet = false;
    // memory bank for scratchpad
    private MemoryBankScratchFLASHCRCPW scratch = null;
    // memory bank for general-purpose user data
    private MemoryBankFLASHCRCPW userDataMemory = null;
    // memory bank for control register
    private MemoryBankFLASHCRCPW register = null;
    // memory bank for control register backup
    private MemoryBankFLASHCRCPW registerbackup = null;
    // memory bank for mission log
    private MemoryBankFLASHCRCPW log = null;
    // Maxim/Maxim Integrated Products Part number
    private String partNumber = null;
    // Device Configuration Byte
    private byte deviceConfigByte = (byte) 0xFF;
    // Temperature range low temperature in degrees Celsius
    private double temperatureRangeLow = -40.0;
    // Temperature range width in degrees Celsius
    private double temperatureRangeWidth = 125.0;
    // should we update the Real time clock?
    private boolean updatertc = false;
    // should we check the speed
    private boolean doSpeedEnable = true;
    private boolean readPasswordSet = false;
    private boolean readOnlyPasswordEnabled = false;
    private boolean readWritePasswordSet = false;
    private boolean readWritePasswordEnabled = false;
    /**
     * indicates whether or not the results of a mission are successfully loaded
     */
    private boolean isMissionLoaded = false;
    /**
     * holds the missionRegister, which details the status of the current mission
     */
    private byte[] missionRegister = null;
    /**
     * The mission logs
     */
    private byte[] temperatureLog = null;
    /**
     * Number of bytes used to store temperature values (0, 1, or 2)
     */
    private int temperatureBytes = 0;

    // 1 byte, alternating ones and zeroes indicates passwords are enabled
    /**
     * start time offset for the first sample, if start delay
     */
    private long timeOffset = 0;

    // 8 bytes, write only, for setting the Read Access Password
    /**
     * the time (unix time) when mission started
     */
    private long missionTimeStamp = -1;

    // 8 bytes, write only, for setting the Read Access Password
    /**
     * The rate at which samples are taken, and the number of samples
     */
    private int sampleRate = -1;

    // 3 bytes, LSB first
    /**
     * total number of samples
     */
    private int sampleCountTotal;
    /**
     * count of log mission pages count to download loadMissionResults
     */
    private int logMissionPagesCount;

    // 3 bytes, LSB first
    /**
     * next page to downloaded in loadMissionResults
     */
    private int logMissionPagesPointer;
    /**
     * flag to indicate the backup mission status values are used
     */
    private boolean useBackupMissionFlag = false;
    private String descriptionString = "DS1925";


// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Constructors and Initializers
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Creates a new <code>OneWireContainer</code> for communication with a
     * DS1925.
     * Note that the method <code>setupContainer(DSPortAdapter,byte[])</code>
     * must be called to set the correct <code>DSPortAdapter</code> device address.
     *
     * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter, byte[]) setupContainer(DSPortAdapter,byte[])
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, byte[]) OneWireContainer53(DSPortAdapter,byte[])
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, long)   OneWireContainer53(DSPortAdapter,long)
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, java.lang.String) OneWireContainer53(DSPortAdapter,String)
     */
    public OneWireContainer53() {
        super();
        // initialize the memory banks
        initMem();
        setContainerVariables(null);
    }

    /**
     * Creates a new <code>OneWireContainer</code> for communication with a
     * DS1925.
     *
     * @param sourceAdapter adapter object required to communicate with
     *                      this iButton
     * @param newAddress    address of this DS1925
     * @see #OneWireContainer53()
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, long)   OneWireContainer53(DSPortAdapter,long)
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, java.lang.String) OneWireContainer53(DSPortAdapter,String)
     */
    public OneWireContainer53(DSPortAdapter sourceAdapter, byte[] newAddress) {
        super(sourceAdapter, newAddress);

        // initialize the memory banks
        initMem();
        setContainerVariables(null);
    }

    /**
     * Creates a new <code>OneWireContainer</code> for communication with a
     * DS1925.
     *
     * @param sourceAdapter adapter object required to communicate with
     *                      this iButton
     * @param newAddress    address of this DS1925
     * @see #OneWireContainer53()
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, byte[]) OneWireContainer53(DSPortAdapter,byte[])
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, java.lang.String) OneWireContainer53(DSPortAdapter,String)
     */
    public OneWireContainer53(DSPortAdapter sourceAdapter, long newAddress) {
        super(sourceAdapter, newAddress);

        // initialize the memory banks
        initMem();
        setContainerVariables(null);
    }

    /**
     * Creates a new <code>OneWireContainer</code> for communication with a
     * DS1925.
     *
     * @param sourceAdapter adapter object required to communicate with
     *                      this iButton
     * @param newAddress    address of this DS1925
     * @see #OneWireContainer53()
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, long) OneWireContainer53(DSPortAdapter,long)
     * @see #OneWireContainer53(com.dalsemi.onewire.adapter.DSPortAdapter, java.lang.String) OneWireContainer53(DSPortAdapter,String)
     */
    public OneWireContainer53(DSPortAdapter sourceAdapter, String newAddress) {
        super(sourceAdapter, newAddress);

        // initialize the memory banks
        initMem();
        setContainerVariables(null);
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
     * Provides this container with the adapter object used to access this device and
     * the address of the iButton or 1-Wire device.
     *
     * @param sourceAdapter adapter object required to communicate with
     *                      this iButton
     * @param newAddress    address of this 1-Wire device
     * @see com.dalsemi.onewire.utils.Address
     */
    public void setupContainer(DSPortAdapter sourceAdapter, byte[] newAddress) {
        super.setupContainer(sourceAdapter, newAddress);

        // initialize the memory banks
        initMem();
        setContainerVariables(null);
    }

    /**
     * Provides this container with the adapter object used to access this device and
     * the address of the iButton or 1-Wire device.
     *
     * @param sourceAdapter adapter object required to communicate with
     *                      this iButton
     * @param newAddress    address of this 1-Wire device
     * @see com.dalsemi.onewire.utils.Address
     */
    public void setupContainer(DSPortAdapter sourceAdapter, long newAddress) {
        super.setupContainer(sourceAdapter, newAddress);

        // initialize the memory banks
        initMem();
        setContainerVariables(null);
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Sensor read/write
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Provides this container with the adapter object used to access this device and
     * the address of the iButton or 1-Wire device.
     *
     * @param sourceAdapter adapter object required to communicate with
     *                      this iButton
     * @param newAddress    address of this 1-Wire device
     * @see com.dalsemi.onewire.utils.Address
     */
    public void setupContainer(DSPortAdapter sourceAdapter, String newAddress) {
        super.setupContainer(sourceAdapter, newAddress);

        // initialize the memory banks
        initMem();
        setContainerVariables(null);
    }

    /**
     * Retrieves the 1-Wire device sensor state.  This state is
     * returned as a byte array.  Pass this byte array to the 'get'
     * and 'set' methods.  If the device state needs to be changed then call
     * the 'writeDevice' to finalize the changes.
     * Uses global 'useBackupMissionFlag' flag to read either the normal register (0)
     * memory back or the backup memory bank (1).
     *
     * @return 1-Wire device sensor state
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            reading an incorrect CRC from a 1-Wire device.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException   on a communication or setup error with the 1-Wire
     *                            adapter
     */
    public byte[] readDevice() throws OneWireIOException, OneWireException {
        byte[] buffer = new byte[64];
        MemoryBankFLASHCRCPW reg;

        // use the backup memory back if we are in back up mode
        if (getUseBackupMissionFlag()) reg = registerbackup;
        else reg = register;

        int retryCnt = MAX_READ_RETRY_CNT;
        int page = 0;
        do {
            try {
                switch (page) {
                    default:
                        break;
                    case 0:
                        reg.readPageCRC(0, false, buffer, 0);
                        page++;
                    case 1:
                        // read with optional continue
                        reg.readPageCRC(1, retryCnt == MAX_READ_RETRY_CNT, buffer, 32);
                        page++;
                }
                retryCnt = MAX_READ_RETRY_CNT;
            } catch (OneWireIOException owioe) {
                //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                if(DEBUG) {
                 //   Debug.debug("readDevice exc, retryCnt=" + retryCnt, owioe);
                }
                //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                if (--retryCnt == 0) throw owioe;
            } catch (OneWireException owe) {
                //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                if (DEBUG) {
                    //Debug.debug("readDevice exc, retryCnt=" + retryCnt, owe);
                }
                //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                if (--retryCnt == 0) throw owe;
            }
        } while (page < 2);

        if (!isContainerVariablesSet) setContainerVariables(buffer);

        return buffer;
    }

    /**
     * Writes the 1-Wire device sensor state that
     * have been changed by 'set' methods.  Only the state registers that
     * changed are updated.  This is done by referencing a field information
     * appended to the state data.
     *
     * @param state 1-Wire device sensor state
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            reading an incorrect CRC from a 1-Wire device.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException   on a communication or setup error with the 1-Wire
     *                            adapter
     */
    public void writeDevice(byte[] state) throws OneWireIOException, OneWireException {
        int start = updatertc ? 0 : 6;

        register.write(start, state, start, 32 - start);

        synchronized (this) {
            updatertc = false;
        }
    }

    /**
     * Reads a single byte from the DS1925.  Note that the preferred manner
     * of reading from the DS1925 Thermocron is through the <code>readDevice()</code>
     * method or through the <code>MemoryBank</code> objects returned in the
     * <code>getMemoryBanks()</code> method.
     *
     * @param memAddr the address to read from  (in the range of 0x200-0x21F)
     * @return the data byte read
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            reading an incorrect CRC from a 1-Wire device.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException   on a communication or setup error with the 1-Wire
     *                            adapter
     * @see #readDevice()
     * @see #getMemoryBanks()
     */
    public byte readByte(int memAddr) throws OneWireIOException, OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        return missionRegister[memAddr & 0x3F];
    }

    /**
     * <p>Gets the status of the specified flag from the specified register.
     * This method actually communicates with the DS1925.  To improve
     * performance if you intend to make multiple calls to this method,
     * first call <code>readDevice()</code> and use the
     * <code>getFlag(int, byte, byte[])</code> method instead.</p>
     *
     * <p>The DS1925 has several sets of flags.</p>
     * <ul>
     *    <LI>Register: <CODE> TEMPERATURE_CONTROL_REGISTER </CODE><BR>
     *       Flags:
     *       <UL>
     *          <li><code> TCR_BIT_ENABLE_TEMPERATURE_LOW_ALARM  </code></li>
     *          <li><code> TCR_BIT_ENABLE_TEMPERATURE_HIGH_ALARM </code></li>
     *       </UL>
     *    </LI>
     *    <LI>Register: <CODE> DATA_CONTROL_REGISTER </CODE><BR>
     *       Flags:
     *       <UL>
     *          <li><code> DCR_BIT_ENABLE_DATA_LOW_ALARM  </code></li>
     *          <li><code> DCR_BIT_ENABLE_DATA_HIGH_ALARM </code></li>
     *       </UL>
     *    </LI>
     *    <LI>Register: <CODE> RTC_CONTROL_REGISTER </CODE><BR>
     *       Flags:
     *       <UL>
     *          <li><code> RCR_BIT_ENABLE_OSCILLATOR        </code></li>
     *          <li><code> RCR_BIT_ENABLE_HIGH_SPEED_SAMPLE </code></li>
     *       </UL>
     *    </LI>
     *    <LI>Register: <CODE> MISSION_CONTROL_REGISTER </CODE><BR>
     *       Flags:
     *       <UL>
     *          <li><code> MCR_BIT_ENABLE_TEMPERATURE_LOGGING           </code></li>
     *          <li><code> MCR_BIT_ENABLE_DATA_LOGGING                  </code></li>
     *          <li><code> MCR_BIT_TEMPERATURE_RESOLUTION               </code></li>
     *          <li><code> MCR_BIT_DATA_RESOLUTION                      </code></li>
     *          <li><code> MCR_BIT_ENABLE_ROLLOVER                      </code></li>
     *          <li><code> MCR_BIT_START_MISSION_UPON_TEMPERATURE_ALARM </code></li>
     *       </UL>
     *    </LI>
     *    <LI>Register: <CODE> ALARM_STATUS_REGISTER </CODE><BR>
     *       Flags:
     *       <UL>
     *          <li><code> ASR_BIT_TEMPERATURE_LOW_ALARM  </code></li>
     *          <li><code> ASR_BIT_TEMPERATURE_HIGH_ALARM </code></li>
     *          <li><code> ASR_BIT_DATA_LOW_ALARM         </code></li>
     *          <li><code> ASR_BIT_DATA_HIGH_ALARM        </code></li>
     *          <li><code> ASR_BIT_BATTERY_ON_RESET       </code></li>
     *       </UL>
     *    </LI>
     *    <LI>Register: <CODE> GENERAL_STATUS_REGISTER </CODE><BR>
     *       Flags:
     *       <UL>
     *          <li><code> GSR_BIT_SAMPLE_IN_PROGRESS            </code></li>
     *          <li><code> GSR_BIT_MISSION_IN_PROGRESS           </code></li>
     *          <li><code> GSR_BIT_MEMORY_CLEARED                </code></li>
     *          <li><code> GSR_BIT_WAITING_FOR_TEMPERATURE_ALARM </code></li>
     *       </UL>
     *    </LI>
     * </ul>
     *
     * @param register address of register containing the flag (see above for available options)
     * @param bitMask  the flag to read (see above for available options)
     * @return the status of the flag, where <code>true</code>
     * signifies a "1" and <code>false</code> signifies a "0"
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            reading an incorrect CRC from a 1-Wire device.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     * @throws OneWireException   on a communication or setup error with the 1-Wire
     *                            adapter
     * @see #getFlag(int, byte, byte[])
     * @see #readDevice()
     * @see #setFlag(int, byte, boolean)
     */
    public boolean getFlag(int register, byte bitMask) throws OneWireIOException, OneWireException {
        missionRegister = readDevice();

        return ((missionRegister[register & 0x3F] & bitMask) != 0);
    }

    /**
     * <p>Gets the status of the specified flag from the specified register.
     * This method is the preferred manner of reading the control and
     * status flags.</p>
     *
     * <p>For more information on valid values for the <code>bitMask</code>
     * parameter, see the {@link #getFlag(int, byte) getFlag(int,byte)} method.</p>
     *
     * @param register address of register containing the flag (see
     *                 {@link #getFlag(int, byte) getFlag(int,byte)} for available options)
     * @param bitMask  the flag to read (see {@link #getFlag(int, byte) getFlag(int,byte)}
     *                 for available options)
     * @param state    current state of the device returned from <code>readDevice()</code>
     * @return the status of the flag, where <code>true</code>
     * signifies a "1" and <code>false</code> signifies a "0"
     * @see #getFlag(int, byte)
     * @see #readDevice()
     * @see #setFlag(int, byte, boolean, byte[])
     */
    public boolean getFlag(int register, byte bitMask, byte[] state) {
        return ((state[register & 0x3F] & bitMask) != 0);
    }

    /**
     * <p>Sets the status of the specified flag in the specified register.
     * If a mission is in progress a <code>OneWireIOException</code> will be thrown
     * (one cannot write to the registers while a mission is commencing).  This method
     * actually communicates with the DS1925.  To improve
     * performance if you intend to make multiple calls to this method,
     * first call <code>readDevice()</code> and use the
     * <code>setFlag(int,byte,boolean,byte[])</code> method instead.</p>
     *
     * <p>For more information on valid values for the <code>bitMask</code>
     * parameter, see the {@link #getFlag(int, byte) getFlag(int,byte)} method.</p>
     *
     * @param register  address of register containing the flag (see
     *                  {@link #getFlag(int, byte) getFlag(int,byte)} for available options)
     * @param bitMask   the flag to read (see {@link #getFlag(int, byte) getFlag(int,byte)}
     *                  for available options)
     * @param flagValue new value for the flag (<code>true</code> is logic "1")
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            reading an incorrect CRC from a 1-Wire device.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     *                            In the case of the DS1925, this could also be due to a
     *                            currently running mission.
     * @throws OneWireException   on a communication or setup error with the 1-Wire
     *                            adapter
     * @see #getFlag(int, byte)
     * @see #getFlag(int, byte, byte[])
     * @see #setFlag(int, byte, boolean, byte[])
     * @see #readDevice()
     */
    public void setFlag(int register, byte bitMask, boolean flagValue) throws OneWireIOException, OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        setFlag(register, bitMask, flagValue, missionRegister);

        writeDevice(missionRegister);
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Container Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * <p>Sets the status of the specified flag in the specified register.
     * If a mission is in progress a <code>OneWireIOException</code> will be thrown
     * (one cannot write to the registers while a mission is commencing).  This method
     * is the preferred manner of setting the DS1925 status and control flags.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.</p>
     *
     * <p>For more information on valid values for the <code>bitMask</code>
     * parameter, see the {@link #getFlag(int, byte) getFlag(int,byte)} method.</p>
     *
     * @param register  address of register containing the flag (see
     *                  {@link #getFlag(int, byte) getFlag(int,byte)} for available options)
     * @param bitMask   the flag to read (see {@link #getFlag(int, byte) getFlag(int,byte)}
     *                  for available options)
     * @param flagValue new value for the flag (<code>true</code> is logic "1")
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @see #getFlag(int, byte)
     * @see #getFlag(int, byte, byte[])
     * @see #setFlag(int, byte, boolean)
     * @see #readDevice()
     * @see #writeDevice(byte[])
     */
    public void setFlag(int register, byte bitMask, boolean flagValue, byte[] state) {
        register = register & 0x3F;

        byte flags = state[register];

        if (flagValue) flags = (byte) (flags | bitMask);
        else flags = (byte) (flags & ~(bitMask));

        // write the regs back
        state[register] = flags;
    }

    /**
     * Gets an enumeration of memory bank instances that implement one or more
     * of the following interfaces:
     * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
     * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank},
     * and {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}.
     *
     * @return <CODE>Enumeration</CODE> of memory banks
     */
    public Enumeration getMemoryBanks() {
        Vector v = new Vector(4);

        v.addElement(scratch);
        v.addElement(userDataMemory);
        v.addElement(register);
        v.addElement(registerbackup);
        v.addElement(log);

        return v.elements();
    }

    /**
     * Returns instance of the memory bank representing this device's
     * scratchpad.
     *
     * @return scratchpad memory bank
     */
    public MemoryBankScratchFLASHCRCPW getScratchpadMemoryBank() {
        return this.scratch;
    }

    /**
     * Returns instance of the memory bank representing this device's
     * general-purpose user data memory.
     *
     * @return user data memory bank
     */
    public MemoryBankFLASHCRCPW getUserDataMemoryBank() {
        return this.userDataMemory;
    }

    /**
     * Returns instance of the memory bank representing this device's
     * data log.
     *
     * @return data log memory bank
     */
    public MemoryBankFLASHCRCPW getDataLogMemoryBank() {
        return this.log;
    }

    /**
     * Returns instance of the memory bank representing this device's
     * special function registers.
     *
     * @return register memory bank
     */
    public MemoryBankFLASHCRCPW getRegisterMemoryBank() {
        return this.register;
    }

    /**
     * Returns the maximum speed this iButton device can
     * communicate at.
     *
     * @return maximum speed
     * @see DSPortAdapterAbstract#setSpeed
     */
    public int getMaxSpeed() {
        return DSPortAdapterAbstract.SPEED_OVERDRIVE;
    }

    /**
     * Gets the Maxim Integrated Products part number of the iButton
     * or 1-Wire Device as a <code>java.lang.String</code>.
     * For example "DS1992".
     *
     * @return iButton or 1-Wire device name
     */
    public String getName() {
        return partNumber;
    }

    /**
     * Retrieves the alternate Maxim Integrated Products part numbers or names.
     * A 'family' of MicroLAN devices may have more than one part number
     * depending on packaging.  There can also be nicknames such as
     * "Crypto iButton".
     *
     * @return the alternate names for this iButton or 1-Wire device
     */
    public String getAlternateNames() {
        return "Thermochron";
    }

    /**
     * Gets a short description of the function of this iButton
     * or 1-Wire Device type.
     *
     * @return device description
     */
    public String getDescription() {
        return descriptionString;
    }

    /**
     * Returns the Device Configuration Byte, which specifies whether or
     * not this device is a DS1925.
     *
     * @return the Device Configuration Byte
     * @throws OneWireIOException
     * @throws OneWireException
     */
    public byte getDeviceConfigByte() throws OneWireIOException, OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        deviceConfigByte = missionRegister[DEVICE_CONFIGURATION_BYTE & 0x3F];

        if (deviceConfigByte == (byte) 0xFF) {
            // not sure why we need an extra attempt
            missionRegister = readDevice();
            if (deviceConfigByte == (byte) 0xFF) deviceConfigByte = missionRegister[DEVICE_CONFIGURATION_BYTE & 0x3F];
        }
        return deviceConfigByte;
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// DS1925 Device Specific Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Directs the container to avoid the calls to doSpeed() in methods that communicate
     * with the DS1925. To ensure that all parts can talk to the 1-Wire bus
     * at their desired speed, each method contains a call
     * to <code>doSpeed()</code>.  However, this is an expensive operation.
     * If a user manages the bus speed in an
     * application,  call this method with <code>doSpeedCheck</code>
     * as <code>false</code>.  The default behavior is
     * to call <code>doSpeed()</code>.
     *
     * @param doSpeedCheck <code>true</code> for <code>doSpeed()</code> to be called before every
     *                     1-Wire bus access, <code>false</code> to skip this expensive call
     * @see OneWireContainer#doSpeed()
     */
    public synchronized void setSpeedCheck(boolean doSpeedCheck) {
        doSpeedEnable = doSpeedCheck;
    }

    /**
     * Stops the currently running mission.
     */
    public void stopMission() throws OneWireException {
        // check to see if we are in mission backup mode
        if (getUseBackupMissionFlag())
            throw new OneWireException("OneWireContainer53-In Mission Backup mode, possible battery expired.");

        // read a user specified amount of memory and verify its validity
        if (doSpeedEnable) doSpeed();

        if (!adapter.select(address)) throw new OneWireException("OneWireContainer53-Device not present.");

        byte[] buffer = new byte[20];
        buffer[0] = XPC_COMMAND;
        buffer[1] = 0x09;  // length byte
        buffer[2] = XPC_STOP_MISSION_COMMAND;
        getContainerReadWritePassword(buffer, 3);
        buffer[11] = (byte) 0xFF;
        buffer[12] = (byte) 0xFF;

        // do the block
        adapter.dataBlock(buffer, 0, 13);

        // Compute CRC and verify it is correct
        if (CRC16.compute(buffer, 0, 13, 0) != 0x0000B001)
            throw new OneWireIOException("Invalid CRC16 read from device.");

        adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
        adapter.getByte();

        // delay to allow memory clear
        msWait(DELAY_XPC_STANDARD);

        adapter.setPowerNormal();

        // Read result byte (make several attempts if not done)
        int cnt = 0;
        byte result;
        do {
            result = (byte) adapter.getByte();
        } while ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT) && (cnt++ < 50));

        if ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT)) {
            throw new OneWireException("OneWireContainer53-XPC Stop Mission failed. Return Code " + Convert.toHexString(result));
        }

        if (getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MISSION_IN_PROGRESS))
            throw new OneWireException("OneWireContainer53-Stop mission failed.  Check read/write password.");
    }

    /**
     * Starts a new mission.  Assumes all parameters have been set by either
     * writing directly to the device registers, or by calling other setup
     * methods.
     */
    public void startMission() throws OneWireException {
        missionRegister = readDevice();

        // check to see if we are in backup mission mode
        if (getUseBackupMissionFlag())
            throw new OneWireException("OneWireContainer53-In Mission Backup mode, possible battery expired");

        if (getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MISSION_IN_PROGRESS, missionRegister))
            throw new OneWireException("OneWireContainer53-Cannot start a mission while a mission is in progress.");

        if (!getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MEMORY_CLEARED, missionRegister))
            throw new OneWireException("OneWireContainer53-Must clear memory before calling start mission.");

        if (doSpeedEnable) doSpeed();

        if (!adapter.select(address)) throw new OneWireException("OneWireContainer53-Device not present.");

        byte[] buffer = new byte[20];
        buffer[0] = XPC_COMMAND;
        buffer[1] = 0x09;  // length byte
        buffer[2] = XPC_START_MISSION_COMMAND;
        getContainerReadWritePassword(buffer, 3);
        buffer[11] = (byte) 0xFF;
        buffer[12] = (byte) 0xFF;

        // do the block
        adapter.dataBlock(buffer, 0, 13);

        // Compute CRC and verify it is correct
        if (CRC16.compute(buffer, 0, 13, 0) != 0x0000B001)
            throw new OneWireIOException("Invalid CRC16 read from device.");

        adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
        adapter.getByte();

        // delay to allow memory clear
        msWait(DELAY_XPC_STANDARD);

        adapter.setPowerNormal();

        // Read result byte (make several attempts if not done)
        int cnt = 0;
        byte result;
        do {
            result = (byte) adapter.getByte();
        } while ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT) && (cnt++ < 50));

        if ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT)) {
            throw new OneWireException("OneWireContainer53-XPC Start Mission failed. Return Code " + Convert.toHexString(result));
        }

        // refresh mission register
        missionRegister = readDevice();
    }

    /**
     * Erases the log memory
     */
    public void clearMemory() throws OneWireException {
        clearMemory(true); // Clears the log
    }


// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Read/Write Password Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Erases the log or user memory
     */
    public void clearMemory(boolean log_memory) throws OneWireException {
        // check to see if we are in battery expired mode
        if (getUseBackupMissionFlag())
            throw new OneWireException("OneWireContainer53-In Mission Backup mode, possible battery expired");

        // verify that when clearing log memory that there is no mission in progress
        if (log_memory) {
            if (isMissionRunning())
                throw new OneWireException("OneWireContainer53-Cannot clear memory while mission is in progress.");
        }

        if (doSpeedEnable) doSpeed();

        if (!adapter.select(address)) throw new OneWireException("OneWireContainer53-Device not present.");

        byte[] buffer = new byte[20];
        buffer[0] = XPC_COMMAND;
        buffer[1] = 0x0A;  // length byte
        buffer[2] = XPC_CLEAR_MEMORY_PW_COMMAND;
        buffer[3] = (byte) ((log_memory) ? 0x01 : 0x00);
        getContainerReadWritePassword(buffer, 4);
        buffer[12] = (byte) 0xFF;
        buffer[13] = (byte) 0xFF;

        // do the block
        adapter.dataBlock(buffer, 0, 14);

        // Compute CRC and verify it is correct
        if (CRC16.compute(buffer, 0, 14, 0) != 0x0000B001) {
            throw new OneWireIOException("Invalid CRC16 read from device.");
        }

        adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
        adapter.getByte();

        // delay to allow memory clear
        msWait((log_memory) ? DELAY_XPC_CLEAR_LOG : DELAY_XPC_LONG_STANDARD);

        adapter.setPowerNormal();

        // Read result byte (make several attempts if not done)
        int cnt = 0;
        byte result;
        do {
            result = (byte) adapter.getByte();
        } while ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT) && (cnt++ < 50));

        if ((result != REPEAT_TOGGLE_SUCCESS) && (result != REPEAT_TOGGLE_SUCCESS_SHIFT)) {
            throw new OneWireException("OneWireContainer53-Clear Memory failed. Return Code " + Convert.toHexString(result));
        }

        if (log_memory) {
            if (!getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MEMORY_CLEARED))
                throw new OneWireException("OneWireContainer53-Clear Memory failed.  Check read/write password.");

            // Turn on RTC First time
            byte[] state = readDevice();
            doTemperatureConvert(state);
        }
    }

    /**
     * Returns the length in bytes of the Read-Only password.
     *
     * @return the length in bytes of the Read-Only password.
     */
    public int getReadOnlyPasswordLength() throws OneWireException {
        return PASSWORD_LENGTH;
    }

    /**
     * Returns the length in bytes of the Read/Write password.
     *
     * @return the length in bytes of the Read/Write password.
     */
    public int getReadWritePasswordLength() throws OneWireException {
        return PASSWORD_LENGTH;
    }

    /**
     * Returns the length in bytes of the Write-Only password.
     *
     * @return the length in bytes of the Write-Only password.
     */
    public int getWriteOnlyPasswordLength() throws OneWireException {
        throw new OneWireException("The DS1925 does not have a write only password.");
    }

    /**
     * Returns the absolute address of the memory location where
     * the Read-Only password is written.
     *
     * @return the absolute address of the memory location where
     * the Read-Only password is written.
     */
    public int getReadOnlyPasswordAddress() throws OneWireException {
        return READ_ACCESS_PASSWORD;
    }

    /**
     * Returns the absolute address of the memory location where
     * the Read/Write password is written.
     *
     * @return the absolute address of the memory location where
     * the Read/Write password is written.
     */
    public int getReadWritePasswordAddress() throws OneWireException {
        return READ_WRITE_ACCESS_PASSWORD;
    }

    /**
     * Returns the absolute address of the memory location where
     * the Write-Only password is written.
     *
     * @return the absolute address of the memory location where
     * the Write-Only password is written.
     */
    public int getWriteOnlyPasswordAddress() throws OneWireException {
        throw new OneWireException("The DS1925 does not have a write password.");
    }

    /**
     * Returns true if this device has a Read-Only password.
     * If false, all other functions dealing with the Read-Only
     * password will throw an exception if called.
     *
     * @return <code>true</code> always, since DS1925 has Read-Only password.
     */
    public boolean hasReadOnlyPassword() {
        return true;
    }

    /**
     * Returns true if this device has a Read/Write password.
     * If false, all other functions dealing with the Read/Write
     * password will throw an exception if called.
     *
     * @return <code>true</code> always, since DS1925 has Read/Write password.
     */
    public boolean hasReadWritePassword() {
        return true;
    }

    /**
     * Returns true if this device has a Write-Only password.
     * If false, all other functions dealing with the Write-Only
     * password will throw an exception if called.
     *
     * @return <code>false</code> always, since DS1925 has no Write-Only password.
     */
    public boolean hasWriteOnlyPassword() {
        return false;
    }

    /**
     * Returns true if the device's Read-Only password has been enabled.
     *
     * @return <code>true</code> if the device's Read-Only password has been enabled.
     */
    public boolean getDeviceReadOnlyPasswordEnable() throws OneWireException {
        return readOnlyPasswordEnabled;
    }

    /**
     * Returns true if the device's Read/Write password has been enabled.
     *
     * @return <code>true</code> if the device's Read/Write password has been enabled.
     */
    public boolean getDeviceReadWritePasswordEnable() throws OneWireException {
        return readWritePasswordEnabled;
    }

    /**
     * Returns true if the device's Write-Only password has been enabled.
     *
     * @return <code>true</code> if the device's Write-Only password has been enabled.
     */
    public boolean getDeviceWriteOnlyPasswordEnable() throws OneWireException {
        throw new OneWireException("The DS1925 does not have a Write Only Password.");
    }

    /**
     * Returns true if this device has the capability to enable one type of password
     * while leaving another type disabled.  i.e. if the device has Read-Only password
     * protection and Write-Only password protection, this method indicates whether or
     * not you can enable Read-Only protection while leaving the Write-Only protection
     * disabled.
     *
     * @return <code>true</code> if the device has the capability to enable one type
     * of password while leaving another type disabled.
     */
    public boolean hasSinglePasswordEnable() {
        return false;
    }

    /**
     * <p>Enables/Disables passwords for this Device.  This method allows you to
     * individually enable the different types of passwords for a particular
     * device.  If <code>hasSinglePasswordEnable()</code> returns true,
     * you can selectively enable particular types of passwords.  Otherwise,
     * this method will throw an exception if all supported types are not
     * enabled.</p>
     *
     * <p>For this to be successful, either write-protect passwords must be disabled,
     * or the write-protect password(s) for this container must be set and must match
     * the value of the write-protect password(s) in the device's register.</p>
     *
     * <P><B>
     * WARNING: Enabling passwords requires that both the read password and the
     * read/write password be re-written to the part.  Before calling this method,
     * you should set the container read password and read/write password values.
     * This will ensure that the correct value is written into the part.
     * </B></P>
     *
     * @param enableReadOnly  if <code>true</code> Read-Only passwords will be enabled.
     * @param enableReadWrite if <code>true</code> Read/Write passwords will be enabled.
     * @param enableWriteOnly if <code>true</code> Write-Only passwords will be enabled.
     */
    public void setDevicePasswordEnable(boolean enableReadOnly, boolean enableReadWrite, boolean enableWriteOnly) throws OneWireException {
        if (enableWriteOnly) throw new OneWireException("The DS1925 does not have a write only password.");
        if (enableReadOnly != enableReadWrite)
            throw new OneWireException("Both read-only and read/write will be set with enable.");
        if (!isContainerReadOnlyPasswordSet()) throw new OneWireException("Container Read Password is not set");
        if (!isContainerReadWritePasswordSet()) throw new OneWireException("Container Read/Write Password is not set");

        // must write both passwords for this to work
        byte[] bothPasswordsEnable = new byte[17];
        bothPasswordsEnable[0] = (enableReadOnly ? ENABLE_BYTE : DISABLE_BYTE);
        getContainerReadOnlyPassword(bothPasswordsEnable, 1);
        getContainerReadWritePassword(bothPasswordsEnable, 9);

        register.write(PASSWORD_CONTROL_REGISTER & 0x3F, bothPasswordsEnable, 0, 17);

        if (enableReadOnly) {
            readOnlyPasswordEnabled = true;
            readWritePasswordEnabled = true;
        } else {
            readOnlyPasswordEnabled = false;
            readWritePasswordEnabled = false;
        }
    }

    /**
     * <p>Enables/Disables passwords for this device.  If the part has more than one
     * type of password (Read-Only, Write-Only, or Read/Write), all passwords
     * will be enabled.  This function is equivalent to the following:
     * <code> owc53.setDevicePasswordEnable(
     * owc53.hasReadOnlyPassword(),
     * owc53.hasReadWritePassword(),
     * owc53.hasWriteOnlyPassword() ); </code></p>
     *
     * <p>For this to be successful, either write-protect passwords must be disabled,
     * or the write-protect password(s) for this container must be set and must match
     * the value of the write-protect password(s) in the device's register.</P>
     *
     * <P><B>
     * WARNING: Enabling passwords requires that both the read password and the
     * read/write password be re-written to the part.  Before calling this method,
     * you should set the container read password and read/write password values.
     * This will ensure that the correct value is written into the part.
     * </B></P>
     *
     * @param enableAll if <code>true</code>, all passwords are enabled.  Otherwise,
     *                  all passwords are disabled.
     */
    public void setDevicePasswordEnableAll(boolean enableAll) throws OneWireException {
        setDevicePasswordEnable(enableAll, enableAll, false);
    }

    /**
     * <p>Writes the given password to the device's Read-Only password register.  Note
     * that this function does not enable the password, just writes the value to
     * the appropriate memory location.</p>
     *
     * <p>For this to be successful, either write-protect passwords must be disabled,
     * or the write-protect password(s) for this container must be set and must match
     * the value of the write-protect password(s) in the device's register.</p>
     *
     * <P><B>
     * WARNING: Setting the read password requires that both the read password
     * and the read/write password be written to the part.  Before calling this
     * method, you should set the container read/write password value.
     * This will ensure that the correct value is written into the part.
     * </B></P>
     *
     * @param password the new password to be written to the device's Read-Only
     *                 password register.  Length must be
     *                 <code>(offset + getReadOnlyPasswordLength)</code>
     * @param offset   the starting point for copying from the given password array
     */
    public void setDeviceReadOnlyPassword(byte[] password, int offset) throws OneWireException {
        if (getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MISSION_IN_PROGRESS))
            throw new OneWireIOException("OneWireContainer53-Cannot change password while mission is in progress.");

        if (!isContainerReadWritePasswordSet()) throw new OneWireException("Container Read/Write Password is not set");

        // must write both passwords for this to work
        byte[] bothPasswords = new byte[16];
        System.arraycopy(password, offset, bothPasswords, 0, 8);
        getContainerReadWritePassword(bothPasswords, 8);

        register.write(READ_ACCESS_PASSWORD & 0x3F, bothPasswords, 0, 16);
        setContainerReadOnlyPassword(password, offset);
    }

    /**
     * <p>Writes the given password to the device's Read/Write password register.  Note
     * that this function does not enable the password, just writes the value to
     * the appropriate memory location.</p>
     *
     * <p>For this to be successful, either write-protect passwords must be disabled,
     * or the write-protect password(s) for this container must be set and must match
     * the value of the write-protect password(s) in the device's register.</p>
     *
     * @param password the new password to be written to the device's Read-Write
     *                 password register.  Length must be
     *                 <code>(offset + getReadWritePasswordLength)</code>
     * @param offset   the starting point for copying from the given password array
     */
    public void setDeviceReadWritePassword(byte[] password, int offset) throws OneWireException {
        if (getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MISSION_IN_PROGRESS))
            throw new OneWireIOException("OneWireContainer53-Cannot change password while mission is in progress.");

        register.write(READ_WRITE_ACCESS_PASSWORD & 0x3F, password, offset, 8);
        setContainerReadWritePassword(password, offset);
    }

    /**
     * <p>Writes the given password to the device's Write-Only password register.  Note
     * that this function does not enable the password, just writes the value to
     * the appropriate memory location.</p>
     *
     * <p>For this to be successful, either write-protect passwords must be disabled,
     * or the write-protect password(s) for this container must be set and must match
     * the value of the write-protect password(s) in the device's register.</p>
     *
     * @param password the new password to be written to the device's Write-Only
     *                 password register.  Length must be
     *                 <code>(offset + getWriteOnlyPasswordLength)</code>
     * @param offset   the starting point for copying from the given password array
     */
    public void setDeviceWriteOnlyPassword(byte[] password, int offset) throws OneWireException {
        throw new OneWireException("The DS1925 does not have a write only password.");
    }

    /**
     * Sets the Read-Only password used by the API when reading from the
     * device's memory.  This password is not written to the device's
     * Read-Only password register.  It is the password used by the
     * software for interacting with the device only.
     *
     * @param password the new password to be used by the API when
     *                 reading from the device's memory.  Length must be
     *                 <code>(offset + getReadOnlyPasswordLength)</code>
     * @param offset   the starting point for copying from the given password array
     */
    public void setContainerReadOnlyPassword(byte[] password, int offset) throws OneWireException {
        System.arraycopy(password, offset, readPassword, 0, PASSWORD_LENGTH);
        readPasswordSet = true;
    }

    /**
     * Sets the Read/Write password used by the API when reading from  or
     * writing to the device's memory.  This password is not written to
     * the device's Read/Write password register.  It is the password used
     * by the software for interacting with the device only.
     *
     * @param password the new password to be used by the API when
     *                 reading from or writing to the device's memory.  Length must be
     *                 <code>(offset + getReadWritePasswordLength)</code>
     * @param offset   the starting point for copying from the given password array
     */
    public void setContainerReadWritePassword(byte[] password, int offset) throws OneWireException {
        System.arraycopy(password, offset, readWritePassword, 0, 8);
        readWritePasswordSet = true;
    }

    /**
     * Sets the Write-Only password used by the API when writing to the
     * device's memory.  This password is not written to the device's
     * Write-Only password register.  It is the password used by the
     * software for interacting with the device only.
     *
     * @param password the new password to be used by the API when
     *                 writing to the device's memory.  Length must be
     *                 <code>(offset + getWriteOnlyPasswordLength)</code>
     * @param offset   the starting point for copying from the given password array
     */
    public void setContainerWriteOnlyPassword(byte[] password, int offset) throws OneWireException {
        throw new OneWireException("The DS1925 does not have a write only password.");
    }

    /**
     * Returns true if the password used by the API for reading from the
     * device's memory has been set.  The return value is not affected by
     * whether or not the read password of the container actually matches
     * the value in the device's password register
     *
     * @return <code>true</code> if the password used by the API for
     * reading from the device's memory has been set.
     */
    public boolean isContainerReadOnlyPasswordSet() throws OneWireException {
        return readPasswordSet;
    }

    /**
     * Returns true if the password used by the API for reading from or
     * writing to the device's memory has been set.  The return value is
     * not affected by whether or not the read/write password of the
     * container actually matches the value in the device's password
     * register.
     *
     * @return <code>true</code> if the password used by the API for
     * reading from or writing to the device's memory has been set.
     */
    public boolean isContainerReadWritePasswordSet() throws OneWireException {
        return readWritePasswordSet;
    }

    /**
     * Returns true if the password used by the API for writing to the
     * device's memory has been set.  The return value is not affected by
     * whether or not the write password of the container actually matches
     * the value in the device's password register.
     *
     * @return <code>true</code> if the password used by the API for
     * writing to the device's memory has been set.
     */
    public boolean isContainerWriteOnlyPasswordSet() throws OneWireException {
        throw new OneWireException("The DS1925 does not have a write only password");
    }

    /**
     * Gets the Read-Only password used by the API when reading from the
     * device's memory.  This password is not read from the device's
     * Read-Only password register.  It is the password used by the
     * software for interacting with the device only and must have been
     * set using the <code>setContainerReadOnlyPassword</code> method.
     *
     * @param password array for holding the password that is used by the
     *                 API when reading from the device's memory.  Length must be
     *                 <code>(offset + getWriteOnlyPasswordLength)</code>
     * @param offset   the starting point for copying into the given password array
     */
    public void getContainerReadOnlyPassword(byte[] password, int offset) throws OneWireException {
        System.arraycopy(readPassword, 0, password, offset, PASSWORD_LENGTH);
    }

    /**
     * Gets the Read/Write password used by the API when reading from or
     * writing to the device's memory.  This password is not read from
     * the device's Read/Write password register.  It is the password used
     * by the software for interacting with the device only and must have
     * been set using the <code>setContainerReadWritePassword</code> method.
     *
     * @param password array for holding the password that is used by the
     *                 API when reading from or writing to the device's memory.  Length must be
     *                 <code>(offset + getReadWritePasswordLength)</code>
     * @param offset   the starting point for copying into the given password array
     */
    public void getContainerReadWritePassword(byte[] password, int offset) throws OneWireException {
        System.arraycopy(readWritePassword, 0, password, offset, PASSWORD_LENGTH);
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Mission Interface Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Gets the Write-Only password used by the API when writing to the
     * device's memory.  This password is not read from the device's
     * Write-Only password register.  It is the password used by the
     * software for interacting with the device only and must have been
     * set using the <code>setContainerWriteOnlyPassword</code> method.
     *
     * @param password array for holding the password that is used by the
     *                 API when writing to the device's memory.  Length must be
     *                 <code>(offset + getWriteOnlyPasswordLength)</code>
     * @param offset   the starting point for copying into the given password array
     */
    public void getContainerWriteOnlyPassword(byte[] password, int offset) throws OneWireException {
        throw new OneWireException("The DS1925 does not have a write only password");
    }

    /**
     * Returns the status of the mission backup flag. If 'true' then
     * the mission download and counters is based on the backup values.
     *
     * @return true if in backup mission mode
     */
    public boolean getUseBackupMissionFlag() throws OneWireException {
        return useBackupMissionFlag;
    }

    /**
     * Sets the flag to indicate if we need to use the backup values
     * to read the mission
     *
     * @param useBackup sets/clears the flag to indicate to use backup mission values
     */
    public void setUseBackupMissionFlag(boolean useBackup) throws OneWireException {
        useBackupMissionFlag = useBackup;

        isMissionLoaded = false;
        missionRegister = null;
    }

    /**
     * Checks the status of the backup to see if the last mission stopped
     * due to battery failure.  If the mission did fail due to battery failure
     * then automatically set into 'useBackupMissionFlag' mode.
     *
     * @param forceIgnoreBackup - flag to force ignore the backup mission
     * @return true if in mission failed from battery failure
     */
    public boolean checkMissionBackup(boolean forceIgnoreBackup) throws OneWireException {
        if (forceIgnoreBackup) {
            setUseBackupMissionFlag(false);
            //useBackupMissionFlag = false;
            return getUseBackupMissionFlag();
        }

        // read both the registers and the backup registers
        useBackupMissionFlag = false;
        missionRegister = readDevice();
        useBackupMissionFlag = true;
        byte[] backupRegister = readDevice();

        // check to see if mission stopped prematurely
        //
        // if ( (RTC_backup == FF’s) &&
        //       (!MIP) &&
        //     (MissionTimeStamp_backup != FF’s) )
        setUseBackupMissionFlag(((backupRegister[RTC_TIME & 0x3F] == (byte) 0xFF)
                && (backupRegister[RTC_TIME & 0x3F + 1] == (byte) 0xFF)
                && (backupRegister[RTC_TIME & 0x3F + 2] == (byte) 0xFF)
                && (backupRegister[RTC_TIME & 0x3F + 3] == (byte) 0xFF))
                && ((missionRegister[GENERAL_STATUS_REGISTER & 0x3F] & GSR_BIT_MISSION_IN_PROGRESS) == 0)
                && (
                        (backupRegister[MISSION_TIMESTAMP_TIME & 0x3F] != (byte) 0xFF)
                                || (backupRegister[MISSION_TIMESTAMP_TIME & 0x3F] != (byte) 0xFF)
                                || (backupRegister[MISSION_TIMESTAMP_TIME & 0x3F] != (byte) 0xFF)
                                || (backupRegister[MISSION_TIMESTAMP_TIME & 0x3F] != (byte) 0xFF)));

        return getUseBackupMissionFlag();
    }

    /**
     * Returns a default friendly label for each channel supported by this
     * Missioning device.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return friendly label for the specified channel
     */
    public String getMissionLabel(int channel) throws OneWireException {
        if (channel == TEMPERATURE_CHANNEL) {
            return "Temperature";
        } else throw new OneWireException("Invalid Channel");
    }

    /**
     * Sets the SUTA (Start Upon Temperature Alarm) bit in the Mission Control
     * register.  This method will communicate with the device directly.
     *
     * @param enable sets/clears the SUTA bit in the Mission Control register.
     */
    public void setStartUponTemperatureAlarmEnable(boolean enable) throws OneWireException {
        setFlag(MISSION_CONTROL_REGISTER, MCR_BIT_START_MISSION_ON_TEMPERATURE_ALARM, enable);
    }

    /**
     * Sets the SUTA (Start Upon Temperature Alarm) bit in the Mission Control
     * register.  This method will set the bit in the provided 'state' array,
     * which should be acquired through a call to <code>readDevice()</code>.
     * After updating the 'state', the method <code>writeDevice(byte[])</code>
     * should be called to commit your changes.
     *
     * @param enable sets/clears the SUTA bit in the Mission Control register.
     * @param state  current state of the device returned from <code>readDevice()</code>
     */
    public void setStartUponTemperatureAlarmEnable(boolean enable, byte[] state) throws OneWireException {
        setFlag(MISSION_CONTROL_REGISTER, MCR_BIT_START_MISSION_ON_TEMPERATURE_ALARM, enable, state);
    }

    /**
     * Returns true if the SUTA (Start Upon Temperature Alarm) bit in the
     * Mission Control register is set.  This method will communicate with
     * the device to read the status of the SUTA bit.
     *
     * @return <code>true</code> if the SUTA bit in the Mission Control register is set.
     */
    public boolean isStartUponTemperatureAlarmEnabled() throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        return getFlag(MISSION_CONTROL_REGISTER, MCR_BIT_START_MISSION_ON_TEMPERATURE_ALARM, missionRegister);
    }

    /**
     * Returns true if the SUTA (Start Upon Temperature Alarm) bit in the
     * Mission Control register is set.  This method will check for  the bit
     * in the provided 'state' array, which should be acquired through a call
     * to <code>readDevice()</code>.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return <code>true</code> if the SUTA bit in the Mission Control register is set.
     */
    public boolean isStartUponTemperatureAlarmEnabled(byte[] state) throws OneWireException {
        return getFlag(MISSION_CONTROL_REGISTER, MCR_BIT_START_MISSION_ON_TEMPERATURE_ALARM, state);
    }

    /**
     * Returns true if the currently loaded mission results indicate
     * that this mission has the SUTA bit enabled.
     *
     * @return <code>true</code> if the currently loaded mission
     * results indicate that this mission has the SUTA bit
     * enabled.
     */
    public boolean isMissionSUTA() throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        return getFlag(MISSION_CONTROL_REGISTER, MCR_BIT_START_MISSION_ON_TEMPERATURE_ALARM, missionRegister);
    }

    /**
     * Returns true if the currently loaded mission results indicate
     * that this mission has the SUTA bit enabled and is still
     * Waiting For Temperature Alarm (WFTA).
     *
     * @return <code>true</code> if the currently loaded mission
     * results indicate that this mission has the SUTA bit
     * enabled and is still Waiting For Temperature Alarm (WFTA).
     */
    public boolean isMissionWFTA() throws OneWireException {
        // check for MIP=1 and SUTA=1 before returning value of WFTA.
        // if MIP=0 or SUTA=0, WFTA could be in invalid state if previous
        // mission did not get a temperature alarm.  Clear Memory should
        // clear this bit, so this is the workaround.
        if (isMissionRunning() && isMissionSUTA()) {
            if (missionRegister == null) missionRegister = readDevice();

            return getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_WAITING_FOR_TEMPERATURE_ALARM, missionRegister);
        }
        return false;
    }

    /**
     * Begins a new mission on this missioning device.
     *
     * @param sampleRate        indicates the sampling rate, in seconds, that
     *                          this missioning device should log samples.
     * @param missionStartDelay indicates the amount of time, in minutes,
     *                          that should pass before the mission begins.
     * @param rolloverEnabled   if <code>false</code>, this device will stop
     *                          recording new samples after the data log is full.  Otherwise,
     *                          it will replace samples starting at the beginning.
     * @param syncClock         if <code>true</code>, the real-time clock of this
     *                          missioning device will be synchronized with the current time
     *                          according to this <code>java.util.Date</code>.
     */
    public void startNewMission(int sampleRate, int missionStartDelay, boolean rolloverEnabled, boolean syncClock, boolean[] channelEnabled) throws OneWireException {
        missionRegister = readDevice();

        // get SUTA flag if set
        boolean suta_enabled = isStartUponTemperatureAlarmEnabled(missionRegister);
        double resolution = getMissionResolution(0);

        // Clear memory does not preserve Mission Control register (0x0213)
        clearMemory(true);

        // restore SUTA flag (in Mission Control)
        if (suta_enabled) setStartUponTemperatureAlarmEnable(suta_enabled);

        // resistor resolution (in Mission Control)
        setMissionResolution(0, resolution);

        // always force RTC on (even if we don't sync clock)
        setClockRunEnable(true, missionRegister);
        writeDevice(missionRegister);

        // Set channels (for this device only temperature)
        for (int i = 0; i < getNumberMissionChannels(); i++)
            setMissionChannelEnable(i, channelEnabled[i], missionRegister);

        if (sampleRate % 60 == 0 || sampleRate > 0x03FFF) {
            //convert to minutes
            sampleRate = (sampleRate / 60) & 0x03FFF;
            setFlag(RTC_CONTROL_REGISTER, RCR_BIT_ENABLE_HIGH_SPEED_SAMPLE, false, missionRegister);
        } else {
            setFlag(RTC_CONTROL_REGISTER, RCR_BIT_ENABLE_HIGH_SPEED_SAMPLE, true, missionRegister);

            // check for sample rate too fast
            //????if (sampleRate < SAMPLE_SECONDS_MIN)
            //????   throw new OneWireException("Sample rate too short");
        }

        Convert.toByteArray(sampleRate, missionRegister, SAMPLE_RATE & 0x3F, 2);

        Convert.toByteArray(missionStartDelay, missionRegister, MISSION_START_DELAY & 0x3F, 3);

        if (rolloverEnabled) throw new OneWireException("Roll over not a feature of this logger");

        if (syncClock) {
            setClock(new Date().getTime() + 2000L, missionRegister);
        }

        writeDevice(missionRegister);
        startMission();
    }

    /**
     * Loads the results of the last mission.  Must be called
     * repeatedly until complete.
     * Uses class global values: sampleCountTotal, sampleRate,
     * missionTimeStamp,temperatureBytes, temperatureLog
     *
     * @return Number of remaining pages. The load is complete when it returns '0'.
     */
    public synchronized int loadMissionResultsPartial(boolean resetLoad) throws OneWireException {
        boolean usingBackupMission;

        // read the state of the battery
        usingBackupMission = getUseBackupMissionFlag();

        if (resetLoad) {
            // read the register contents
            missionRegister = readDevice();

            // get the number of samples
            sampleCountTotal = Convert.toInt(missionRegister, MISSION_SAMPLE_COUNT & 0x3F, 3);

            // Use sampleCountTotal to count samples (should already be 0)
            if (usingBackupMission) sampleCountTotal = 0;

            // sample rate, in seconds
            sampleRate = Convert.toInt(missionRegister, SAMPLE_RATE & 0x3F, 2);
            if (!getFlag(RTC_CONTROL_REGISTER, RCR_BIT_ENABLE_HIGH_SPEED_SAMPLE, missionRegister))
                // if sample rate is in minutes, convert to seconds
                sampleRate *= 60;

            missionTimeStamp = Convert.toLong(missionRegister, MISSION_TIMESTAMP_TIME - register.startPhysicalAddress, 4) * 1000L;

            // figure out how many bytes for each temperature sample
            temperatureBytes = 1;
            // if it's 16-bit resolution, add another 1 to the size
            if (getFlag(MISSION_CONTROL_REGISTER, MCR_BIT_TEMPERATURE_RESOLUTION, missionRegister))
                temperatureBytes += 1;

            // default size of the log, could be different if using an odd
            // figure max number of samples
            int maxSamples = log.size / temperatureBytes;

            // calculate first log entry time offset in seconds
            timeOffset = Convert.toLong(missionRegister, MISSION_START_DELAY - register.startPhysicalAddress, 3) * 60L;

            // temperature log
            if (usingBackupMission) temperatureLog = new byte[maxSamples * temperatureBytes];
            else temperatureLog = new byte[sampleCountTotal * temperatureBytes];

            // check for non-zero download
            if (temperatureLog.length > 0) {
                // read the data log for temperature
                logMissionPagesCount = (temperatureLog.length / LOG_PAGE_SIZE) + ((temperatureLog.length % LOG_PAGE_SIZE) > 0 ? 1 : 0);
            } else {
                logMissionPagesCount = 0;
                isMissionLoaded = true;
                return 0; // done with mission download
            }

            // next page to read
            logMissionPagesPointer = 0;
        }

        // cache for page of log
        byte[] pagebuffer = new byte[LOG_PAGE_SIZE];

        // read the data log for temperature
        int retryCnt = MAX_READ_RETRY_CNT;

        // Read just one page, loop only for retry
        while (true) {
            try {
                log.readPageCRC(logMissionPagesPointer, (logMissionPagesPointer > 0) && (retryCnt == MAX_READ_RETRY_CNT), pagebuffer, 0);

                // copy to the temperature log buffer
                System.arraycopy(pagebuffer, 0, temperatureLog, (logMissionPagesPointer * LOG_PAGE_SIZE), Math.min(LOG_PAGE_SIZE, temperatureLog.length - (logMissionPagesPointer * LOG_PAGE_SIZE)));

                // reset counter
                retryCnt = MAX_READ_RETRY_CNT;

                // if battery expired then count the samples
                if (usingBackupMission) {
                    for (int j = 0; j < LOG_PAGE_SIZE; j += temperatureBytes) {
                        boolean sample_ffs = true;
                        for (int k = 0; k < temperatureBytes; k++) {
                            if (pagebuffer[j + k] != (byte) 0xFF) {
                                sample_ffs = false;
                                break;
                            }
                        }

                        if (sample_ffs) {
                            // done with battery expired read
                            logMissionPagesPointer = logMissionPagesCount;
                            isMissionLoaded = true;
                            return 0; // done
                        } else sampleCountTotal++; // count this sample because it is not FF's
                    }
                }

                logMissionPagesPointer++;
                break;
            } catch (OneWireIOException owioe) {
                if (--retryCnt == 0) throw owioe;
                msWait(10);  // Give time for mission sample to complete
            } catch (OneWireException owe) {
                if (--retryCnt == 0) throw owe;
                msWait(10);  // Give time for mission sample to complete
            }
        }

        // check for end of download
        if (logMissionPagesPointer == logMissionPagesCount) {
            isMissionLoaded = true;
            return 0;
        } else return (logMissionPagesCount - logMissionPagesPointer);
    }

    /**
     * Loads the results of the currently running mission.  Must be called
     * before all mission result/status methods.
     */
    public synchronized void loadMissionResults() throws OneWireException {
        int pagesToGo = 0;

        // loop to read the entire mission log result
        do {
            pagesToGo = loadMissionResultsPartial(pagesToGo == 0);
        } while (pagesToGo != 0);
    }

    /**
     * Returns true if the mission results have been loaded from the device.
     *
     * @return <code>true</code> if the mission results have been loaded.
     */
    public boolean isMissionLoaded() {
        return isMissionLoaded;
    }

    /**
     * Gets the number of channels supported by this Missioning device.
     * Channel specific methods will use a channel number specified
     * by an integer from [0 to (<code>getNumberOfMissionChannels()</code> - 1)].
     *
     * @return the number of channels
     */
    public int getNumberMissionChannels() {
        return 1; // temperature only
    }

    /**
     * Enables/disables the specified mission channel, indicating whether or
     * not the channel's readings will be recorded in the mission log.
     *
     * @param channel the channel to enable/disable
     * @param enable  if true, the channel is enabled
     */
    public void setMissionChannelEnable(int channel, boolean enable) throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        setMissionChannelEnable(channel, enable, missionRegister);
        writeDevice(missionRegister);
    }

    /**
     * Enables/disables the specified mission channel, indicating whether or
     * not the channel's readings will be recorded in the mission log.
     *
     * @param channel the channel to enable/disable
     * @param enable  if true, the channel is enabled
     * @param state   the state as returned from readDevice, for cached writes
     */
    public void setMissionChannelEnable(int channel, boolean enable, byte[] state) throws OneWireException {
        if (channel == TEMPERATURE_CHANNEL) {
            setFlag(MISSION_CONTROL_REGISTER, MCR_BIT_ENABLE_TEMPERATURE_LOGGING, enable, state);
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }
    }

    /**
     * Returns true if the specified mission channel is enabled, indicating
     * that the channel's readings will be recorded in the mission log.
     *
     * @param channel the channel to enable/disable
     *
     */
    public boolean getMissionChannelEnable(int channel) throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        return getMissionChannelEnable(channel, missionRegister);
    }


    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - Mission Results
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Returns true if the specified mission channel is enabled, indicating
     * that the channel's readings will be recorded in the mission log.
     *
     * @param channel the channel to enable/disable
     * @param state  if true, the channel is enabled
     */
    public boolean getMissionChannelEnable(int channel, byte[] state) throws OneWireException {
        if (channel == TEMPERATURE_CHANNEL) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }
    }

    /**
     * Returns the amount of time, in seconds, between samples taken
     * by this missioning device.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return time, in seconds, between sampling
     */
    public int getMissionSampleRate(int channel) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        return sampleRate;
    }

    /**
     * Returns the number of samples available for the specified channel
     * during the current mission.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return number of samples available for the specified channel
     */
    public int getMissionSampleCount(int channel) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        return sampleCountTotal;
    }

    /**
     * Reads the device and returns the total number of samples logged
     * since the first power-on of this device.
     *
     * @return the total number of samples logged since the first power-on
     * of this device.
     */
    public int getDeviceSampleCount() throws OneWireException {
        return getDeviceSampleCount(readDevice());
    }

    /**
     * Returns the total number of samples logged since the first power-on
     * of this device.
     *
     * @param state The current state of the device as return from <code>readDevice()</code>
     * @return the total number of samples logged since the first power-on
     * of this device.
     */
    public int getDeviceSampleCount(byte[] state) throws OneWireException {
        return Convert.toInt(state, DEVICE_SAMPLE_COUNT & 0x3F, 3);
    }

    /**
     * Returns the total number of samples taken for the specified channel
     * during the current mission.  This number can be more than the actual
     * sample count if rollover is enabled and the log has been filled.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return number of samples taken for the specified channel
     */
    public int getMissionSampleCountTotal(int channel) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        return sampleCountTotal;
    }

    /**
     * Returns the sample as degrees celsius if temperature channel
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param sampleNum the sample number to return, between <code>0</code> and
     *                  <code>(getMissionSampleCount(channel)-1)</code>
     * @return the sample's value in degrees Celsius or percent RH.
     */
    public double getMissionSample(int channel, int sampleNum) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        if (sampleNum >= sampleCountTotal || sampleNum < 0) throw new IllegalArgumentException("Invalid sample number");

        double val = 0;
        if (channel == TEMPERATURE_CHANNEL) {
            val = decodeTemperature(temperatureLog, sampleNum * temperatureBytes, temperatureBytes, true);
        } else throw new IllegalArgumentException("Invalid Channel");

        return val;
    }

    /**
     * Returns the sample as an integer value.  This value is not converted to
     * degrees Celsius for temperature.  It is
     * simply the 8 or 16 bits of digital data written in the mission log for
     * this sample entry.  It is up to the user to mask off the unused bits
     * and convert this value to it's proper units.
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param sampleNum the sample number to return, between <code>0</code> and
     *                  <code>(getMissionSampleCount(channel)-1)</code>
     * @return the sample as a whole integer
     */
    public int getMissionSampleAsInteger(int channel, int sampleNum) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        if (sampleNum >= sampleCountTotal || sampleNum < 0) throw new IllegalArgumentException("Invalid sample number");

        int i = 0;
        if (channel == TEMPERATURE_CHANNEL) {
            if (temperatureBytes == 2) {
                i = ((0x0FF & temperatureLog[sampleNum * temperatureBytes]) << 8) | ((0x0FF & temperatureLog[sampleNum * temperatureBytes + 1]));
            } else {
                i = (0x0FF & temperatureLog[sampleNum * temperatureBytes]);
            }
        } else throw new IllegalArgumentException("Invalid Channel");

        return i;
    }

    /**
     * Returns the time, in milliseconds, that each sample was taken by the
     * current mission.
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param sampleNum the sample number to return, between <code>0</code> and
     *                  <code>(getMissionSampleCount(channel)-1)</code>
     * @return the sample's timestamp, in milliseconds
     */
    public long getMissionSampleTimeStamp(int channel, int sampleNum) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        long delta = (long) sampleNum * (long) sampleRate + timeOffset;
        return delta * 1000L + missionTimeStamp;
    }

    /**
     * Returns <code>true</code> if a mission is currently running.
     *
     * @return <code>true</code> if a mission is currently running.
     */
    public boolean isMissionRunning() throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        byte gsr = missionRegister[GENERAL_STATUS_REGISTER & 0x3F];

        return (((gsr & GSR_BIT_MISSION_IN_PROGRESS) != 0) && (!getUseBackupMissionFlag()));
    }

    /**
     * Returns <code>true</code> if a rollover is enabled.
     *
     * @return <code>true</code> if a rollover is enabled.
     */
    public boolean isMissionRolloverEnabled() throws OneWireException {
        return false;  // Device does not have rollover
    }

    /**
     * Returns <code>true</code> if a mission has rolled over.
     *
     * @return <code>true</code> if a mission has rolled over.
     */
    public boolean hasMissionRolloverOccurred() throws OneWireException {
        return false;  // Device does not have rollover
    }

    /**
     * Clears the mission results and erases the log memory from this
     * missioning device.
     */
    public void clearMissionResults() throws OneWireException {
        clearMemory(true);
        isMissionLoaded = false;
    }

    /**
     * Returns the time, in milliseconds, that the mission began.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return time, in milliseconds, that the mission began
     */
    public long getMissionTimeStamp(int channel) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        return this.missionTimeStamp;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - Mission Resolutions
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Returns the amount of time, in milliseconds, before the first sample
     * occurred.  If rollover disabled, or datalog didn't fill up, this
     * will be 0.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return time, in milliseconds, before first sample occurred
     */
    public long getFirstSampleOffset(int channel) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        return 0L;
    }

    /**
     * Returns all available resolutions for the specified mission channel.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return all available resolutions for the specified mission channel.
     */
    public double[] getMissionResolutions(int channel) throws OneWireException {
        if (channel == TEMPERATURE_CHANNEL) {
            return new double[]{temperatureResolutions[0], temperatureResolutions[1]};
        } else throw new IllegalArgumentException("Invalid Channel");
    }

    /**
     * Returns the currently selected resolution for the specified
     * channel.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return the currently selected resolution for the specified channel.
     */
    public double getMissionResolution(int channel) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        double resolution = 0;
        if (channel == TEMPERATURE_CHANNEL) {
            if (getFlag(MISSION_CONTROL_REGISTER, MCR_BIT_TEMPERATURE_RESOLUTION, missionRegister))
                resolution = temperatureResolutions[1];
            else resolution = temperatureResolutions[0];
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }
        return resolution;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - Mission Alarms
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Sets the selected resolution for the specified channel.
     *
     * @param channel    the mission channel, between <code>0</code> and
     *                   <code>(getNumberOfMissionChannels()-1)</code>
     * @param resolution the new resolution for the specified channel.
     */
    public void setMissionResolution(int channel, double resolution) throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        if (channel == TEMPERATURE_CHANNEL) {
            setFlag(MISSION_CONTROL_REGISTER, MCR_BIT_TEMPERATURE_RESOLUTION, resolution == temperatureResolutions[1], missionRegister);
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }

        writeDevice(missionRegister);
    }

    /**
     * Indicates whether or not the specified channel of this missioning device
     * has mission alarm capabilities.
     *
     * @param channel the mission channel, between <code>0</code> and
     *                <code>(getNumberOfMissionChannels()-1)</code>
     * @return true if the device has mission alarms for the specified channel.
     */
    public boolean hasMissionAlarms(int channel) {
        return true;
    }

    /**
     * Returns true if the specified channel's alarm value of the specified
     * type has been triggered during the mission.
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param alarmType valid value: <code>ALARM_HIGH</code> or
     *                  <code>ALARM_LOW</code>
     * @return true if the alarm was triggered.
     */
    public boolean hasMissionAlarmed(int channel, int alarmType) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        if (channel == TEMPERATURE_CHANNEL) {
            if (alarmType == MissionContainer.ALARM_HIGH) {
                return getFlag(ALARM_STATUS_REGISTER, ASR_BIT_TEMPERATURE_HIGH_ALARM, missionRegister);
            } else {
                return getFlag(ALARM_STATUS_REGISTER, ASR_BIT_TEMPERATURE_LOW_ALARM, missionRegister);
            }
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }
    }

    /**
     * Returns true if the alarm of the specified type has been enabled for
     * the specified channel.
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param alarmType valid value: <code>ALARM_HIGH</code> or
     *                  <code>ALARM_LOW</code>
     * @return true if the alarm of the specified type has been enabled for
     * the specified channel.
     */
    public boolean getMissionAlarmEnable(int channel, int alarmType) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        if (channel == TEMPERATURE_CHANNEL) {
            if (alarmType == MissionContainer.ALARM_HIGH) {
                return getFlag(TEMPERATURE_CONTROL_REGISTER, TCR_BIT_ENABLE_TEMPERATURE_HIGH_ALARM, missionRegister);
            } else {
                return getFlag(TEMPERATURE_CONTROL_REGISTER, TCR_BIT_ENABLE_TEMPERATURE_LOW_ALARM, missionRegister);
            }
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }
    }

    /**
     * Enables/disables the alarm of the specified type for the specified channel
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param alarmType valid value: <code>ALARM_HIGH</code> or
     *                  <code>ALARM_LOW</code>
     * @param enable    if true, alarm is enabled.
     */
    public void setMissionAlarmEnable(int channel, int alarmType, boolean enable) throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        if (channel == TEMPERATURE_CHANNEL) {
            if (alarmType == MissionContainer.ALARM_HIGH) {
                setFlag(TEMPERATURE_CONTROL_REGISTER, TCR_BIT_ENABLE_TEMPERATURE_HIGH_ALARM, enable, missionRegister);
            } else {
                setFlag(TEMPERATURE_CONTROL_REGISTER, TCR_BIT_ENABLE_TEMPERATURE_LOW_ALARM, enable, missionRegister);
            }
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }

        writeDevice(missionRegister);
    }

    /**
     * Returns the threshold value which will trigger the alarm of the
     * specified type on the specified channel.
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param alarmType valid value: <code>ALARM_HIGH</code> or
     *                  <code>ALARM_LOW</code>
     * @return the threshold value which will trigger the alarm
     */
    public double getMissionAlarm(int channel, int alarmType) throws OneWireException {
        if (!isMissionLoaded) throw new OneWireException("Must load mission results first.");

        double th = 0;
        if (channel == TEMPERATURE_CHANNEL) {
            if (alarmType == MissionContainer.ALARM_HIGH) {
                th = decodeTemperature(missionRegister, TEMPERATURE_HIGH_ALARM_THRESHOLD & 0x3F, 1, false);
            } else {
                th = decodeTemperature(missionRegister, TEMPERATURE_LOW_ALARM_THRESHOLD & 0x3F, 1, false);
            }
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }
        return th;
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Temperature Interface Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Sets the threshold value which will trigger the alarm of the
     * specified type on the specified channel.
     *
     * @param channel   the mission channel, between <code>0</code> and
     *                  <code>(getNumberOfMissionChannels()-1)</code>
     * @param alarmType valid value: <code>ALARM_HIGH</code> or
     *                  <code>ALARM_LOW</code>
     * @param threshold the threshold value which will trigger the alarm
     */
    public void setMissionAlarm(int channel, int alarmType, double threshold) throws OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        if (channel == TEMPERATURE_CHANNEL) {
            if (alarmType == MissionContainer.ALARM_HIGH) {
                encodeTemperature(threshold, missionRegister, TEMPERATURE_HIGH_ALARM_THRESHOLD & 0x3F, 1, false);
            } else {
                encodeTemperature(threshold, missionRegister, TEMPERATURE_LOW_ALARM_THRESHOLD & 0x3F, 1, false);
            }
        } else {
            throw new IllegalArgumentException("Invalid Channel");
        }

        writeDevice(missionRegister);
    }

    /**
     * Checks to see if this temperature measuring device has high/low
     * trip alarms.
     *
     * @return <code>true</code> if this <code>TemperatureContainer</code>
     * has high/low trip alarms
     * @see #getTemperatureAlarm
     * @see #setTemperatureAlarm
     */
    public boolean hasTemperatureAlarms() {
        return true;
    }

    /**
     * Checks to see if this device has selectable temperature resolution.
     *
     * @return <code>true</code> if this <code>TemperatureContainer</code>
     * has selectable temperature resolution
     * @see #getTemperatureResolution
     * @see #getTemperatureResolutions
     * @see #setTemperatureResolution
     */
    public boolean hasSelectableTemperatureResolution() {
        return false;
    }

    /**
     * Get an array of available temperature resolutions in Celsius.
     *
     * @return byte array of available temperature resolutions in Celsius with
     * minimum resolution as the first element and maximum resolution
     * as the last element
     * @see #hasSelectableTemperatureResolution
     * @see #getTemperatureResolution
     * @see #setTemperatureResolution
     */
    public double[] getTemperatureResolutions() {
        double[] d = new double[1];

        d[0] = temperatureResolutions[1];

        return d;
    }

    /**
     * Gets the temperature alarm resolution in Celsius.
     *
     * @return temperature alarm resolution in Celsius for this 1-wire device
     * @see #hasTemperatureAlarms
     * @see #getTemperatureAlarm
     * @see #setTemperatureAlarm
     */
    public double getTemperatureAlarmResolution() {
        return temperatureResolutions[0];
    }

    /**
     * Gets the maximum temperature in Celsius.
     *
     * @return maximum temperature in Celsius for this 1-wire device
     * @see #getMinTemperature()
     */
    public double getMaxTemperature() {
        return temperatureRangeLow + temperatureRangeWidth;
    }

    /**
     * Gets the minimum temperature in Celsius.
     *
     * @return minimum temperature in Celsius for this 1-wire device
     * @see #getMaxTemperature()
     */
    public double getMinTemperature() {
        return temperatureRangeLow;
    }

    /**
     * Performs a temperature conversion.  Use the <code>state</code>
     * information to calculate the conversion time.
     *
     * @param state byte array with device state information
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            reading an incorrect CRC from a 1-Wire device.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     *                            In the case of the DS1925 Thermocron, this could also be due to a
     *                            currently running mission.
     * @throws OneWireException   on a communication or setup error with the 1-Wire
     *                            adapter
     */
    public void doTemperatureConvert(byte[] state) throws OneWireIOException, OneWireException {
        /* check for mission in progress */
        if (getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MISSION_IN_PROGRESS, state))
            throw new OneWireIOException("OneWireContainer53-Cant force " + "temperature read during a mission.");

        /* get the temperature*/
        if (doSpeedEnable) doSpeed();   //we aren't worried about how long this takes...we're sleeping for 750 ms!

        adapter.reset();  // not sure why this is here

        if (adapter.select(address)) {
            // XPC Forced Conversion
            byte[] buffer = new byte[20];
            buffer[0] = XPC_COMMAND;        // XPC main command
            buffer[1] = 0x01;               // length byte
            buffer[2] = XPC_FORCED_CONVERSION_COMMAND;   // Sub-command XPC Forced Conversion
            buffer[3] = (byte) 0xFF;         // CRC
            buffer[4] = (byte) 0xFF;         // CRC

            // do the block
            adapter.dataBlock(buffer, 0, 5);

            // Compute CRC and verify it is correct
            if (CRC16.compute(buffer, 0, 5, 0) != 0x0000B001) {
                throw new OneWireIOException("Invalid CRC16 read from device. (first)");
            }

            adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
            adapter.getByte();

            // delay to allow battery voltage conversion
            msWait(DELAY_FORCED_CONVERSION);

            adapter.setPowerNormal();

            // Read the conversion result and put in buffer
            buffer[0] = (byte) 0xFF;  // Dummy
            buffer[1] = (byte) 0xFF;  // temp LS
            buffer[2] = (byte) 0xFF;  // temp MS
            buffer[3] = (byte) 0xFF;  // CRC
            buffer[4] = (byte) 0xFF;  // CRC

            // do the block
            adapter.dataBlock(buffer, 0, 5);

            // Compute CRC and verify it is correct
            if (CRC16.compute(buffer, 1, 4, 0) != 0x0000B001) {
                throw new OneWireIOException("Invalid CRC16 read from device. (second) " + Convert.toHexString(buffer));
            }

            // we have the temperature here, however the sensor class does a 'readDevice' to get the value so do nothing
            // Poke the value into the state anyway
            state[LAST_TEMPERATURE_CONVERSION_LSB & 0x3F] = buffer[2];
            state[LAST_TEMPERATURE_CONVERSION_MSB & 0x3F] = buffer[3];
        } else throw new OneWireException("OneWireContainer53-Device not found!");
    }

    /**
     * Gets the temperature value in Celsius from the <code>state</code>
     * data retrieved from the <code>readDevice()</code> method.
     *
     * @param state byte array with device state information
     * @return temperature in Celsius from the last
     * <code>doTemperatureConvert()</code>
     */
    public double getTemperature(byte[] state) {
        double val = decodeTemperature(state, LAST_TEMPERATURE_CONVERSION_LSB & 0x3F, 2, false);
        return val;
    }

    /**
     * Gets the specified temperature alarm value in Celsius from the
     * <code>state</code> data retrieved from the
     * <code>readDevice()</code> method.
     *
     * @param alarmType valid value: <code>ALARM_HIGH</code> or
     *                  <code>ALARM_LOW</code>
     * @param state     byte array with device state information
     * @return temperature alarm trip values in Celsius for this 1-wire device
     * @see #hasTemperatureAlarms
     * @see #setTemperatureAlarm
     */
    public double getTemperatureAlarm(int alarmType, byte[] state) {
        double th = 0;
        if (alarmType == TemperatureContainer.ALARM_HIGH)
            th = decodeTemperature(state, TEMPERATURE_HIGH_ALARM_THRESHOLD & 0x3F, 1, false);
        else th = decodeTemperature(state, TEMPERATURE_LOW_ALARM_THRESHOLD & 0x3F, 1, false);
        return th;
    }

    /**
     * Gets the current temperature resolution in Celsius from the
     * <code>state</code> data retrieved from the <code>readDevice()</code>
     * method.
     *
     * @param state byte array with device state information
     * @return temperature resolution in Celsius for this 1-wire device
     * @see #hasSelectableTemperatureResolution
     * @see #getTemperatureResolutions
     * @see #setTemperatureResolution
     */
    public double getTemperatureResolution(byte[] state) {
        return temperatureResolutions[1];
    }

    /**
     * Sets the temperature alarm value in Celsius in the provided
     * <code>state</code> data.
     * Use the method <code>writeDevice()</code> with
     * this data to finalize the change to the device.
     *
     * @param alarmType  valid value: <code>ALARM_HIGH</code> or
     *                   <code>ALARM_LOW</code>
     * @param alarmValue alarm trip value in Celsius
     * @param state      byte array with device state information
     * @see #hasTemperatureAlarms
     * @see #getTemperatureAlarm
     */
    public void setTemperatureAlarm(int alarmType, double alarmValue, byte[] state) {
        if (alarmType == TemperatureContainer.ALARM_HIGH) {
            encodeTemperature(alarmValue, state, TEMPERATURE_HIGH_ALARM_THRESHOLD & 0x3F, 1, false);
        } else {
            encodeTemperature(alarmValue, state, TEMPERATURE_LOW_ALARM_THRESHOLD & 0x3F, 1, false);
        }
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Clock Interface Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Sets the current temperature resolution in Celsius in the provided
     * <code>state</code> data.   Use the method <code>writeDevice()</code>
     * with this data to finalize the change to the device.
     *
     * @param resolution temperature resolution in Celsius
     * @param state      byte array with device state information
     * @throws OneWireException if the device does not support
     *                          selectable temperature resolution
     * @see #hasSelectableTemperatureResolution
     * @see #getTemperatureResolution
     * @see #getTemperatureResolutions
     */
    public void setTemperatureResolution(double resolution, byte[] state) throws OneWireException {
        throw new OneWireException("Selectable Temperature Resolution Not Supported");
    }

    /**
     * Checks to see if the clock has an alarm feature.
     *
     * @return false, since this device does not have clock alarms
     * @see #getClockAlarm(byte[])
     * @see #isClockAlarmEnabled(byte[])
     * @see #isClockAlarming(byte[])
     * @see #setClockAlarm(long, byte[])
     * @see #setClockAlarmEnable(boolean, byte[])
     */
    public boolean hasClockAlarm() {
        return false;
    }

    /**
     * Checks to see if the clock can be disabled.
     *
     * @return true if the clock can be enabled and disabled
     * @see #isClockRunning(byte[])
     * @see #setClockRunEnable(boolean, byte[])
     */
    public boolean canDisableClock() {
        return true;
    }

    //--------
    //-------- Clock 'get' Methods
    //--------

    /**
     * Gets the clock resolution in milliseconds
     *
     * @return the clock resolution in milliseconds
     */
    public long getClockResolution() {
        return 1000;
    }

    /**
     * Extracts the Real-Time clock value in milliseconds.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return the time represented in this clock in milliseconds since 1970
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #setClock(long, byte[])
     */
    public long getClock(byte[] state) {
        return Convert.toLong(state, RTC_TIME - register.startPhysicalAddress, 4) * 1000;
    }

    /**
     * Extracts the clock alarm value for the Real-Time clock.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return milliseconds since 1970 that the clock alarm is set to
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #hasClockAlarm()
     * @see #isClockAlarmEnabled(byte[])
     * @see #isClockAlarming(byte[])
     * @see #setClockAlarm(long, byte[])
     * @see #setClockAlarmEnable(boolean, byte[])
     */
    public long getClockAlarm(byte[] state) throws OneWireException {
        throw new OneWireException("Device does not support clock alarms");
    }

    /**
     * Checks if the clock alarm flag has been set.
     * This will occur when the value of the Real-Time
     * clock equals the value of the clock alarm.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return true if the Real-Time clock is alarming
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #hasClockAlarm()
     * @see #isClockAlarmEnabled(byte[])
     * @see #getClockAlarm(byte[])
     * @see #setClockAlarm(long, byte[])
     * @see #setClockAlarmEnable(boolean, byte[])
     */
    public boolean isClockAlarming(byte[] state) {
        return false;
    }

    /**
     * Checks if the clock alarm is enabled.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return true if clock alarm is enabled
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #hasClockAlarm()
     * @see #isClockAlarming(byte[])
     * @see #getClockAlarm(byte[])
     * @see #setClockAlarm(long, byte[])
     * @see #setClockAlarmEnable(boolean, byte[])
     */
    public boolean isClockAlarmEnabled(byte[] state) {
        return false;
    }

    //--------
    //-------- Clock 'set' Methods
    //--------

    /**
     * Checks if the device's oscillator is enabled.  The clock
     * will not increment if the clock oscillator is not enabled.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return true if the clock is running
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #canDisableClock()
     * @see #setClockRunEnable(boolean, byte[])
     */
    public boolean isClockRunning(byte[] state) {
        return getFlag(RTC_CONTROL_REGISTER, RCR_BIT_ENABLE_OSCILLATOR, state);
    }

    /**
     * Sets the Real-Time clock.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.
     *
     * @param time  new value for the Real-Time clock, in milliseconds
     *              since January 1, 1970
     * @param state current state of the device returned from <code>readDevice()</code>
     * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
     * @see #getClock(byte[])
     */
    public void setClock(long time, byte[] state) {
        updatertc = true;
        Convert.toByteArray(time / 1000L, state, RTC_TIME - register.startPhysicalAddress, 4);
    }

    /**
     * Sets the clock alarm.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all clock devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasClockAlarm()</code> method.
     *
     * @param time  - new value for the Real-Time clock alarm, in milliseconds
     *              since January 1, 1970
     * @param state current state of the device returned from <code>readDevice()</code>
     * @throws OneWireException if this device does not have clock alarms
     * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
     * @see #hasClockAlarm()
     * @see #isClockAlarmEnabled(byte[])
     * @see #getClockAlarm(byte[])
     * @see #isClockAlarming(byte[])
     * @see #setClockAlarmEnable(boolean, byte[])
     */
    public void setClockAlarm(long time, byte[] state) throws OneWireException {
        throw new OneWireException("Device does not support clock alarms");
    }

    /**
     * Enables or disables the oscillator, turning the clock 'on' and 'off'.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all clock devices can disable their oscillators.  Check to see if this device can
     * disable its oscillator first by calling the <code>canDisableClock()</code> method.
     *
     * @param runEnable true to enable the clock oscillator
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
     * @see #canDisableClock()
     * @see #isClockRunning(byte[])
     */
    public void setClockRunEnable(boolean runEnable, byte[] state) {
        setFlag(RTC_CONTROL_REGISTER, RCR_BIT_ENABLE_OSCILLATOR, runEnable, state);
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// A/D Interface Functions (for read of battery voltage)
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    //--------
    //-------- A/D Feature methods
    //--------

    /**
     * Enables or disables the clock alarm.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all clock devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasClockAlarm()</code> method.
     *
     * @param alarmEnable true to enable the clock alarm
     * @param state       current state of the device returned from <code>readDevice()</code>
     * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
     * @see #hasClockAlarm()
     * @see #isClockAlarmEnabled(byte[])
     * @see #getClockAlarm(byte[])
     * @see #setClockAlarm(long, byte[])
     * @see #isClockAlarming(byte[])
     */
    public void setClockAlarmEnable(boolean alarmEnable, byte[] state) throws OneWireException {
        throw new OneWireException("Device does not support clock alarms");
    }

    /**
     * Gets the number of channels supported by this A/D.
     * Channel specific methods will use a channel number specified
     * by an integer from [0 to (<code>getNumberADChannels()</code> - 1)].
     *
     * @return the number of channels
     */
    public int getNumberADChannels() {
        return 1;
    }

    /**
     * Checks to see if this A/D measuring device has high/low
     * alarms.
     *
     * @return true if this device has high/low trip alarms
     */
    public boolean hasADAlarms() {
        return false;
    }

    /**
     * Gets an array of available ranges for the specified
     * A/D channel.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @return array indicating the available ranges starting
     * from the largest range to the smallest range
     * @see #getNumberADChannels()
     */
    public double[] getADRanges(int channel) {
        double[] ranges = new double[1];

        ranges[0] = 3.5;

        return ranges;
    }

    /**
     * Gets an array of available resolutions based
     * on the specified range on the specified A/D channel.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param range   A/D range setting from the <code>getADRanges(int)</code> method
     * @return array indicating the available resolutions on this
     * <code>channel</code> for this <code>range</code>
     * @see #getNumberADChannels()
     * @see #getADRanges(int)
     */
    public double[] getADResolutions(int channel, double range) {
        double[] res = new double[1];

        res[0] = 3.5 / 65536;

        return res;
    }

    //--------
    //-------- A/D IO Methods
    //--------

    /**
     * Checks to see if this A/D supports doing multiple voltage
     * conversions at the same time.
     *
     * @return true if the device can do multi-channel voltage reads
     * @see #doADConvert(boolean[], byte[])
     */
    public boolean canADMultiChannelRead() {
        return false;
    }

    /**
     * Performs a voltage conversion on one specified channel.
     * Use the method <code>getADVoltage(int,byte[])</code> to read
     * the result of this conversion, using the same <code>channel</code>
     * argument as this method uses.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param state   current state of the device returned from <code>readDevice()</code>
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            no 1-Wire device present.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     *                            This is usually a recoverable error.
     * @throws OneWireException   on a communication or setup error with the
     *                            1-Wire adapter.  This is usually a non-recoverable error.
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #getADVoltage(int, byte[])
     */
    public void doADConvert(int channel, byte[] state) throws OneWireIOException, OneWireException {
        if (missionRegister == null) missionRegister = readDevice();

        if (getFlag(GENERAL_STATUS_REGISTER, GSR_BIT_MISSION_IN_PROGRESS, missionRegister))
            throw new OneWireException("OneWireContainer53-Cannot read battery voltage while a mission is in progress.");

        if (!adapter.select(address)) throw new OneWireException("OneWireContainer53-Device not present.");

        byte[] buffer = new byte[20];
        buffer[0] = XPC_COMMAND;        // XPC main command
        buffer[1] = 0x01;               // length byte
        buffer[2] = XPC_READ_BATTERY_COMMAND;   // Sub-command Read Battery Voltage
        buffer[3] = (byte) 0xFF;         // CRC
        buffer[4] = (byte) 0xFF;         // CRC

        // do the block
        adapter.dataBlock(buffer, 0, 5);

        // Compute CRC and verify it is correct
        if (CRC16.compute(buffer, 0, 5, 0) != 0x0000B001) {
            throw new OneWireIOException("Invalid CRC16 read from device. (first)");
        }

        adapter.startPowerDelivery(DSPortAdapterAbstract.CONDITION_AFTER_BYTE);
        adapter.getByte();

        // delay to allow battery voltage conversion
        msWait(DELAY_XPC_LONG_STANDARD);

        adapter.setPowerNormal();

        // Read the conversion result and put in state
        ad_state[0] = (byte) 0xFF;  // Dummy
        ad_state[1] = (byte) 0xFF;  // bat voltage LS
        ad_state[2] = (byte) 0xFF;  // bat voltage MS
        ad_state[3] = (byte) 0xFF;  // CRC
        ad_state[4] = (byte) 0xFF;  // CRC

        // do the block
        adapter.dataBlock(ad_state, 0, 5);

        // Compute CRC and verify it is correct
        if (CRC16.compute(ad_state, 1, 4, 0) != 0x0000B001) {
            throw new OneWireIOException("Invalid CRC16 read from device. (second) " + Convert.toHexString(ad_state));
        }
    }

    /**
     * Performs voltage conversion on one or more specified
     * channels.  The method <code>getADVoltage(byte[])</code> can be used to read the result
     * of the conversion(s). This A/D must support multi-channel read,
     * reported by <code>canADMultiChannelRead()</code>, if more then 1 channel is specified.
     *
     * @param doConvert array of size <code>getNumberADChannels()</code> representing
     *                  which channels should perform conversions
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            no 1-Wire device present.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     *                            This is usually a recoverable error.
     * @throws OneWireException   on a communication or setup error with the
     *                            1-Wire adapter.  This is usually a non-recoverable error.
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #getADVoltage(byte[])
     * @see #canADMultiChannelRead()
     */
    public void doADConvert(boolean[] doConvert, byte[] state) throws OneWireIOException, OneWireException {
        if (doConvert[0]) doADConvert(1, state);
    }

    /**
     * Reads the value of the voltages after a <code>doADConvert(boolean[],byte[])</code>
     * method call.  This A/D device must support multi-channel reading, reported by
     * <code>canADMultiChannelRead()</code>, if more than 1 channel conversion was attempted
     * by <code>doADConvert()</code>.
     *
     * @param state current state of the device returned from <code>readDevice()</code>
     * @return array with the voltage values for all channels
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            no 1-Wire device present.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     *                            This is usually a recoverable error.
     * @throws OneWireException   on a communication or setup error with the
     *                            1-Wire adapter.  This is usually a non-recoverable error.
     * @see #doADConvert(boolean[], byte[])
     */
    public double[] getADVoltage(byte[] state) throws OneWireIOException, OneWireException {
        double[] bat = new double[1];

        // get voltage from state buffer and return as a double
        bat[0] = getADVoltage(0, state);

        return bat;
    }

    //--------
    //-------- A/D 'get' Methods
    //--------

    /**
     * Reads the value of the voltages after a <code>doADConvert(int,byte[])</code>
     * method call.  If more than one channel has been read it is more
     * efficient to use the <code>getADVoltage(byte[])</code> method that
     * returns all channel voltage values.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param state   current state of the device returned from <code>readDevice()</code>
     * @return the voltage value for the specified channel
     * @throws OneWireIOException on a 1-Wire communication error such as
     *                            no 1-Wire device present.  This could be
     *                            caused by a physical interruption in the 1-Wire Network due to
     *                            shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
     *                            This is usually a recoverable error.
     * @throws OneWireException   on a communication or setup error with the
     *                            1-Wire adapter.  This is usually a non-recoverable error.
     * @see #doADConvert(int, byte[])
     * @see #getADVoltage(byte[])
     */
    public double getADVoltage(int channel, byte[] state) throws OneWireIOException, OneWireException {
        return (double) (((long) (ad_state[2] & 0x0FF) * (long) 256) + (long) (ad_state[1] & 0x0FF)) / 1024.0;
    }

    /**
     * Reads the value of the specified A/D alarm on the specified channel.
     * Not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel   channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @return the alarm value in volts
     * @throws OneWireException if this device does not have A/D alarms
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #hasADAlarms()
     */
    public double getADAlarm(int channel, int alarmType, byte[] state) throws OneWireException {
        return 0.0;  // not used
    }

    /**
     * Checks to see if the specified alarm on the specified channel is enabled.
     * Not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel   channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @return true if specified alarm is enabled
     * @throws OneWireException if this device does not have A/D alarms
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #hasADAlarms()
     */
    public boolean getADAlarmEnable(int channel, int alarmType, byte[] state) throws OneWireException {
        return false;
    }

    /**
     * Checks the state of the specified alarm on the specified channel.
     * Not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel   channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @return true if specified alarm occurred
     * @throws OneWireException if this device does not have A/D alarms
     * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
     * @see #hasADAlarms()
     * @see #getADAlarmEnable(int, int, byte[])
     * @see #setADAlarmEnable(int, int, boolean, byte[])
     */
    public boolean hasADAlarmed(int channel, int alarmType, byte[] state) throws OneWireException {
        return false;
    }

    /**
     * Returns the currently selected resolution for the specified
     * channel.  This device may not have selectable resolutions,
     * though this method will return a valid value.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param state   current state of the device returned from <code>readDevice()</code>
     * @return the current resolution of <code>channel</code> in volts
     * @see #getADResolutions(int, double)
     * @see #setADResolution(int, double, byte[])
     */
    public double getADResolution(int channel, byte[] state) {
        return 3.5 / 65536;
    }

    //--------
    //-------- A/D 'set' Methods
    //--------

    /**
     * Returns the currently selected range for the specified
     * channel.  This device may not have selectable ranges,
     * though this method will return a valid value.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param state   current state of the device returned from <code>readDevice()</code>
     * @return the input voltage range
     * @see #getADRanges(int)
     * @see #setADRange(int, double, byte[])
     */
    public double getADRange(int channel, byte[] state) {
        return 3.5;
    }

    /**
     * Sets the voltage value of the specified alarm on the specified channel.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel   channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
     * @param alarm     new alarm value
     * @param state     current state of the device returned from <code>readDevice()</code>
     * @throws OneWireException if this device does not have A/D alarms
     * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
     * @see #hasADAlarms()
     * @see #getADAlarm(int, int, byte[])
     * @see #getADAlarmEnable(int, int, byte[])
     * @see #setADAlarmEnable(int, int, boolean, byte[])
     * @see #hasADAlarmed(int, int, byte[])
     */
    public void setADAlarm(int channel, int alarmType, double alarm, byte[] state) throws OneWireException {
        // do nothing, no alarms
    }

    /**
     * Enables or disables the specified alarm on the specified channel.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel     channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param alarmType   the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
     * @param alarmEnable true to enable the alarm, false to disable
     * @param state       current state of the device returned from <code>readDevice()</code>
     * @throws OneWireException if this device does not have A/D alarms
     * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
     * @see #hasADAlarms()
     * @see #getADAlarm(int, int, byte[])
     * @see #setADAlarm(int, int, double, byte[])
     * @see #getADAlarmEnable(int, int, byte[])
     * @see #hasADAlarmed(int, int, byte[])
     */
    public void setADAlarmEnable(int channel, int alarmType, boolean alarmEnable, byte[] state) throws OneWireException {
        // do nothing, no alarms
    }

    /**
     * Sets the conversion resolution value for the specified channel.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel    channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param resolution one of the resolutions returned by <code>getADResolutions(int,double)</code>
     * @param state      current state of the device returned from <code>readDevice()</code>
     * @see #getADResolutions(int, double)
     * @see #getADResolution(int, byte[])
     */
    public void setADResolution(int channel, double resolution, byte[] state) {
        // do nothing, only one resolution
    }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Private initializers
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

    /**
     * Sets the input range for the specified channel.
     * The method <code>writeDevice()</code> must be called to finalize
     * changes to the device.  Note that multiple 'set' methods can
     * be called before one call to <code>writeDevice()</code>.  Also note that
     * not all A/D devices have alarms.  Check to see if this device has
     * alarms first by calling the <code>hasADAlarms()</code> method.
     *
     * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
     * @param range   one of the ranges returned by <code>getADRanges(int)</code>
     * @param state   current state of the device returned from <code>readDevice()</code>
     * @see #getADRanges(int)
     * @see #getADRange(int, byte[])
     */
    public void setADRange(int channel, double range, byte[] state) {
        // do nothing, only one range
    }

    /**
     * Construct the memory banks used for I/O.
     */
    private void initMem() {
        doSpeedEnable = false;

        // scratchpad
        scratch = new MemoryBankScratchFLASHCRCPW(this);

        // User Data Memory
        userDataMemory = new MemoryBankFLASHCRCPW(this, scratch);
        userDataMemory.pageLength = 32;
        userDataMemory.numberPages = 16;
        userDataMemory.size = 16 * 32;
        userDataMemory.bankDescription = "User Data Memory";
        userDataMemory.startPhysicalAddress = 0x0000;
        userDataMemory.generalPurposeMemory = true;
        userDataMemory.readOnly = false;
        userDataMemory.readWrite = true;

        // Register
        register = new MemoryBankFLASHCRCPW(this, scratch);
        register.pageLength = 32;
        register.numberPages = 2;
        register.size = 2 * 32;
        register.bankDescription = "Register control";
        register.startPhysicalAddress = 0x0200;
        register.generalPurposeMemory = false;

        // Register Backup
        registerbackup = new MemoryBankFLASHCRCPW(this, scratch);
        registerbackup.pageLength = 32;
        registerbackup.numberPages = 2;
        registerbackup.size = 2 * 32;
        registerbackup.bankDescription = "Register Mission Backup";
        registerbackup.startPhysicalAddress = 0x0260;
        registerbackup.generalPurposeMemory = false;
        registerbackup.readOnly = true;
        registerbackup.readWrite = false;

        // Data Log
        log = new MemoryBankFLASHCRCPW(this, scratch);
        log.pageLength = 64;
        log.numberPages = 1960;
        log.size = 1960 * 64;
        log.bankDescription = "Data log";
        log.startPhysicalAddress = 0x1000;
        log.generalPurposeMemory = false;
        log.readOnly = true;
        log.readWrite = false;
    }

    /**
     * Sets the following, calculated from the 12-bit code of the 1-Wire Net Address:
     * 1)  The part numbers:
     * DS1925 - Temperature iButton
     */
    private void setContainerVariables(byte[] registerPages) {
        double Tref1 = 60;
        boolean autoLoadCalibration = true;

        // clear this flag..  Gets set later if registerPages!=null
        isContainerVariablesSet = false;

        // reset mission parameters
        isMissionLoaded = false;
        missionRegister = null;
        temperatureLog = null;

        deviceConfigByte = (byte) 0xFF;
        if (registerPages != null) {
            deviceConfigByte = registerPages[DEVICE_CONFIGURATION_BYTE & 0x03F];
        }

        switch (deviceConfigByte) {
            default:
            case DCB_DS1925:
                partNumber = PART_NUMBER_DS1925;
                temperatureRangeLow = -40;
                temperatureRangeWidth = 125;
                Tref1 = 60;
                descriptionString = DESCRIPTION_DS1925;
                break;
        }

        if (registerPages != null) {
            isContainerVariablesSet = true;
        }
    }

    /**
     * helper method for decoding temperature values
     */
    private final double decodeTemperature(byte[] data, int offset, int length, boolean reverse) {
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        if (DEBUG) {
            //Debug.debug("decodeTemperature, data", data, offset, length);
        }
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        double whole, fraction = 0;
        if (reverse && length == 2) {
            fraction = ((data[offset + 1] & 0x0FF) / 512d);
            whole = (data[offset] & 0x0FF) / 2d + (temperatureRangeLow - 1);
        } else if (length == 2) {
            fraction = ((data[offset] & 0x0FF) / 512d);
            whole = (data[offset + 1] & 0x0FF) / 2d + (temperatureRangeLow - 1);
        } else {
            whole = (data[offset] & 0x0FF) / 2d + (temperatureRangeLow - 1);
        }
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        if (DEBUG) {
            //Debug.debug("decodeTemperature, temperatureRangeLow= " + temperatureRangeLow);
            //Debug.debug("decodeTemperature, whole= " + whole);
            //Debug.debug("decodeTemperature, fraction= " + fraction);
            //Debug.debug("decodeTemperature, (whole+fraction)= " + (whole + fraction));
        }
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        return whole + fraction;
    }

    /**
     * helper method for encoding temperature values
     */
    private final void encodeTemperature(double temperature, byte[] data, int offset, int length, boolean reverse) {
        double val = 2 * ((temperature) - (temperatureRangeLow - 1));
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        if (DEBUG)
            //Debug.debug("encodeTemperature, temperature=" + temperature + ", temperatureRangeLow=" + temperatureRangeLow + ", val=" + val);
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

        if (reverse && length == 2) {
            data[offset + 1] = (byte) (0x0C0 & (byte) (val * 256));
            data[offset] = (byte) val;
        } else if (length == 2) {
            data[offset] = (byte) (0x0C0 & (byte) (val * 256));
            data[offset + 1] = (byte) val;
        } else {
            data[offset] = (byte) val;
        }
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
        if (DEBUG) {
            //Debug.debug("encodeTemperature, data", data, offset, length);
        }
        //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
    }
}
