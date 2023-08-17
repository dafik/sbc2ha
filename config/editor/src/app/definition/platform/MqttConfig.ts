import {PlatformConfig} from "./PlatformConfig";
import {PlatformType} from "../enums/PlatformType";
import {HaDiscoveryConfig} from "./HaDiscoveryConfig";

export class MqttConfig extends PlatformConfig {
  host: string;
  username: string;
  password: string;
  port: number = 1883;
  topicPrefix: string = "boneio";
  haDiscovery: HaDiscoveryConfig = new HaDiscoveryConfig();

  public constructor(host: string, username: string, password: string) {
    super(PlatformType.MQTT);
    this.host = host;
    this.username = username;
    this.password = password;
  }

}


