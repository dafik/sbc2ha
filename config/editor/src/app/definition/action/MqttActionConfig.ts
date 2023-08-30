import {ActionConfig} from "./ActionConfig";
import {ActionType} from "../enums/ActionType";
import {Expose} from "class-transformer";


export class MqttActionConfig extends ActionConfig {
    topic: string;
    @Expose({name: "action_mqtt_msg"})
    actionMqttMsg: string;

    constructor(topic: string, actionMqttMsg: string) {
        super(ActionType.MQTT);
        this.topic = topic;
        this.actionMqttMsg = actionMqttMsg;
    }
}
