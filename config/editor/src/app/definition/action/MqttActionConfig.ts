import {ActionConfig} from "./ActionConfig";
import {ActionType} from "../enums/ActionType";


export class MqttActionConfig extends ActionConfig {
  topic: string;
  actionMqttMsg: string;

  constructor(topic: string, actionMqttMsg: string) {
    super(ActionType.MQTT);
    this.topic = topic;
    this.actionMqttMsg = actionMqttMsg;
  }
}
