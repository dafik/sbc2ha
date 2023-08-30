import {Expose, Transform} from "class-transformer";
import {skipDefault} from "../AppConfig";

export class HaDiscoveryConfig {
    @Transform(({value}) => skipDefault(value, true), {toPlainOnly: true})
    enabled: boolean = true;
    @Expose({name: "topic_prefix"})
    @Transform(({value}) => skipDefault(value, "homeassistant"), {toPlainOnly: true})
    topicPrefix: string = "homeassistant"


    constructor(enabled?: boolean, topicPrefix?: string) {
        if (enabled) {
            this.enabled = enabled;
        }
        if (topicPrefix) {
            this.topicPrefix = topicPrefix;
        }
    }
}

