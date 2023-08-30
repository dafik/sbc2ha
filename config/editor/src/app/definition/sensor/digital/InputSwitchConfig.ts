import {InputConfig} from "./InputConfig";
import {ButtonState} from "../../enums/ButtonState";
import {PlatformType} from "../../enums/PlatformType";
import {ActionConfig} from "../../action/ActionConfig";
import {Expose, Transform, Type} from "class-transformer";
import {skipDefault} from "../../AppConfig";
import {MqttActionConfig} from "../../action/MqttActionConfig";
import {OutputActionConfig} from "../../action/OutputActionConfig";
import {CoverActionConfig} from "../../action/CoverActionConfig";

export class InputSwitchConfig extends InputConfig<ActionsSwitch> {
    @Expose({name: "click_detection"})
    @Transform(({value}) => skipDefault(value, ButtonState.SINGLE), {toPlainOnly: true})
    clickDetection: ButtonState = ButtonState.SINGLE;

    constructor(name: string, input: number) {
        super(PlatformType.SWITCH, name, input, ActionsSwitch);
    }
}

export class ActionsSwitch {
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
    single?: ActionConfig[]
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
    double?: ActionConfig[]
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
    long?: ActionConfig[]
}
