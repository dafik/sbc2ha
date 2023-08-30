import {InputConfig} from "./InputConfig";
import {BinarySensorDeviceClassType} from "../../enums/deviceClass/ha/BinarySensorDeviceClassType";
import {PlatformType} from "../../enums/PlatformType";
import {ActionConfig} from "../../action/ActionConfig";
import {Expose, Transform, Type} from "class-transformer";
import {skipDefault} from "../../AppConfig";
import {CoverActionConfig} from "../../action/CoverActionConfig";
import {MqttActionConfig} from "../../action/MqttActionConfig";
import {OutputActionConfig} from "../../action/OutputActionConfig";

export class InputSensorConfig extends InputConfig<ActionsSensor> {
    @Expose({name: "device_class"})
    @Transform(({value}) => skipDefault(value, BinarySensorDeviceClassType.NONE), {toPlainOnly: true})
    deviceClass: BinarySensorDeviceClassType = BinarySensorDeviceClassType.NONE;

    constructor(name: string, input: number) {
        super(PlatformType.DIGITAL, name, input, ActionsSensor);
    }
}

export class ActionsSensor {
    @Type(() => ActionConfig, {
        discriminator: {
            property: "action",
            subTypes: [
                {value: CoverActionConfig, name: "cover"},
                {value: MqttActionConfig, name: "mqtt"},
                {value: OutputActionConfig, name: "output"}
            ]
        }
    })
    pressed?: ActionConfig[]
    @Type(() => ActionConfig, {
        discriminator: {
            property: "action",
            subTypes: [
                {value: CoverActionConfig, name: "cover"},
                {value: MqttActionConfig, name: "mqtt"},
                {value: OutputActionConfig, name: "output"}
            ]
        }
    })
    released?: ActionConfig[]
}
