import {PlatformConfig} from "./PlatformConfig";
import {PlatformType} from "../enums/PlatformType";
import {HaDiscoveryConfig} from "./HaDiscoveryConfig";
import {Expose, Transform} from "class-transformer";
import {skipDefault} from "../AppConfig";


function createDefault(value: any) {
    if (value === undefined) {
        return new HaDiscoveryConfig();
    }
    return new HaDiscoveryConfig(value.enabled!, value.topic_prefix!);
}

export class MqttConfig extends PlatformConfig {
    host: string;
    username: string;
    password: string;
    @Transform(({value}) => skipDefault(value, 1883), {toPlainOnly: true})
    port: number = 1883;
    @Expose({name: "topic_prefix"})
    @Transform(({value}) => skipDefault(value, "boneio"), {toPlainOnly: true})
    topicPrefix: string = "boneio";
    @Expose({name: "ha_discovery"})
    @Transform(({value}) => skipDefault(value, new HaDiscoveryConfig()), {toPlainOnly: true})
    @Transform(({value}) => createDefault(value), {toClassOnly: true})
    haDiscovery: HaDiscoveryConfig = new HaDiscoveryConfig();

    public constructor(host: string, username: string, password: string) {
        super(PlatformType.MQTT);
        this.host = host;
        this.username = username;
        this.password = password;
    }

}


