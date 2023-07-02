package com.dfi.sbc2ha.config.boneio.definition.enums.deviceClass.ha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoneIoSensorDeviceClassType implements BoneIoEnumLabel {
    /**
     * Generic sensor. This is the default and does’t need to be set.
     */
    NONE("None"),

    /**
     * Apparent power in VA.
     */
    APPARENT_POWER("apparent_power"),

    /**
     * Air Quality Index (unitless).
     */
    AQI("aqi"),

    /**
     * Atmospheric pressure in cbar, bar, hPa, inHg, kPa, mbar, Pa or psi
     */
    ATMOSPHERIC_PRESSURE("atmospheric_pressure"),

    /**
     * Percentage of battery that is left in %
     */
    BATTERY("battery"),

    /**
     * Carbon Dioxide in CO2 (Smoke) in ppm
     */
    CARBON_DIOXIDE("carbon_dioxide"),

    /**
     * Carbon Monoxide in CO (Gas CNG/LPG) in ppm
     */
    CARBON_MONOXIDE("carbon_monoxide"),

    /**
     * Current in A, mA
     */
    CURRENT("current"),

    /**
     * Data rate in bit/s, kbit/s, Mbit/s, Gbit/s, B/s, kB/s, MB/s, GB/s, KiB/s, MiB/s or GiB/s
     */
    DATA_RATE("data_rate"),

    /**
     * Data size in bit, kbit, Mbit, Gbit, B, kB, MB, GB, TB, PB, EB, ZB, YB, KiB, MiB, GiB, TiB, PiB, EiB, ZiB or YiB
     */
    DATA_SIZE("data_size"),

    /**
     * Date string (ISO 8601)
     */
    DATE("date"),

    /**
     * Generic distance in km, m, cm, mm, mi, yd, or in
     */
    DISTANCE("distance"),

    /**
     * Duration in d, h, min, or s
     */
    DURATION("duration"),

    /**
     * Energy in Wh, kWh, MWh, MJ, or GJ
     */
    ENERGY("energy"),

    /**
     * Stored energy in Wh, kWh, MWh, MJ, or GJ
     */
    ENERGY_STORAGE("energy_storage"),

    /**
     * Has a limited set of (non-numeric) states
     */
    ENUM("enum"),
    /**
     * Frequency in Hz, kHz, MHz, or GHz
     */
    FREQUENCY("frequency"),

    /**
     * Gasvolume in m³, ft³ or CCF
     */
    GAS("gas"),

    /**
     * Percentage of humidity in the air in %
     */
    HUMIDITY("humidity"),

    /**
     * The current light level in lx
     */
    ILLUMINANCE("illuminance"),

    /**
     * Irradiance in W/m² or BTU/(h⋅ft²)
     */
    IRRADIANCE("irradiance"),

    /**
     * Percentage of water in a substance in %
     */
    MOISTURE("moisture"),

    /**
     * The monetary value (ISO 4217)
     */
    MONETARY("monetary"),

    /**
     * Concentration of Nitrogen Dioxide in µg/m³
     */
    NITROGEN_DIOXIDE("nitrogen_dioxide"),

    /**
     * Concentration of Nitrogen Monoxide in µg/m³
     */
    NITROGEN_MONOXIDE("nitrogen_monoxide"),

    /**
     * Concentration of Nitrous Oxide in µg/m³
     */
    NITROUS_OXIDE("nitrous_oxide"),

    /**
     * Concentration of Ozone in µg/m³
     */
    OZONE("ozone"),

    /**
     * Concentration of particulate matter less than 1 micrometer in µg/m³
     */
    PM1("pm1"),

    /**
     * Concentration of particulate matter less than 2.5 micrometers in µg/m³
     */
    PM25("pm25"),

    /**
     * Concentration of particulate matter less than 10 micrometers in µg/m³
     */
    PM10("pm10"),

    /**
     * Power factor (unitless), unit may be None or %
     */
    POWER_FACTOR("power_factor"),

    /**
     * Power in W or kW
     */
    POWER("power"),

    /**
     * Accumulated precipitation in cm, in or mm
     */
    PRECIPITATION("precipitation"),

    /**
     * Precipitation intensity in in/d, in/h, mm/d or mm/h
     */
    PRECIPITATION_INTENSITY("precipitation_intensity"),

    /**
     * Pressure in Pa, kPa, hPa, bar, cbar, mbar, mmHg, inHg or psi
     */
    PRESSURE("pressure"),

    /**
     * Reactive power in var
     */
    REACTIVE_POWER("reactive_power"),

    /**
     * Signal strength in dB or dBm
     */
    SIGNAL_STRENGTH("signal_strength"),

    /**
     * Sound pressure in dB or dBA
     */
    SOUND_PRESSURE("sound_pressure"),

    /**
     * Generic speed in ft/s, in/d, in/h, km/h, kn, m/s, mph or mm/d
     */
    SPEED("speed"),

    /**
     * Concentration of sulphur dioxide in µg/m³
     */
    SULPHUR_DIOXIDE("sulphur_dioxide"),

    /**
     * Temperature in °C, °F or K
     */
    TEMPERATURE("temperature"),

    /**
     * Datetime object or timestamp string (ISO 8601)
     */
    TIMESTAMP("timestamp"),

    /**
     * Concentration of volatile organic compounds in µg/m³
     */
    VOLATILE_ORGANIC_COMPOUNDS("volatile_organic_compounds"),

    /**
     * Ratio of volatile organic compounds in ppm or ppb
     */
    VOLATILE_ORGANIC_COMPOUNDS_PARTS("volatile_organic_compounds_parts"),

    /**
     * Voltage in V, mV
     */
    VOLTAGE("voltage"),

    /**
     * Generic volume in L, mL, gal, fl. oz., m³, ft³, or CCF
     */
    VOLUME("volume"),

    /**
     * Generic stored volume in L, mL, gal, fl. oz., m³, ft³, or CCF
     */
    VOLUME_STORAGE("volume_storage"),

    /**
     * Water consumption in L, gal, m³, ft³, or CCF
     */
    WATER("water"),

    /**
     * Generic mass in kg, g, mg, µg, oz, lb, or st
     */
    WEIGHT("weight"),

    /**
     * Wind speed in ft/s, km/h, kn, m/s, or mph
     */
    WIND_SPEED("wind_speed");


    private final String label;
}
