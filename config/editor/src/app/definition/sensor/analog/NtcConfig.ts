import {SingleSourceConfig} from "../SingleSourceConfig";
import {PlatformType} from "../../enums/PlatformType";
import {Expose} from "class-transformer";

export class NtcConfig extends SingleSourceConfig {
    @Expose({name: "b_calibration"})
    bCalibration?: BconstantCalibration;
    @Expose({name: "v_calibration"})
    vCalibration?: ValueCalibration;


    constructor(name: string, sensor: string, calibration: ValueCalibration | BconstantCalibration) {
        super(PlatformType.NTC, name, sensor);

        if (calibration instanceof ValueCalibration) {
            this.vCalibration = calibration;
        } else {
            this.bCalibration = calibration;
        }
    }
}

export class BconstantCalibration {
    /*b_constant: 3950
    reference_temperature: 25°C
    reference_resistance: 10kOhm*/

    bConstant: number;
    referenceTemperature: string;
    referenceResistance: string;

    constructor(bConstant: number, referenceTemperature: string, referenceResistance: string) {
        this.bConstant = bConstant;
        this.referenceTemperature = referenceTemperature;
        this.referenceResistance = referenceResistance;
    }
}


export class ValueCalibration {
    low: SetPoint;
    mid: SetPoint;
    high: SetPoint;

    /*- 10.0kOhm -> 25°C
    - 27.219kOhm -> 0°C
    - 14.674kOhm -> 15°C*/

    constructor(low: SetPoint, mid: SetPoint, high: SetPoint) {
        this.low = low;
        this.mid = mid;
        this.high = high;
    }
}


export class SetPoint {
    temperature: string;
    resistance: string;

    constructor(temperature: string, resistance: string) {
        this.temperature = temperature;
        this.resistance = resistance;
    }
}


