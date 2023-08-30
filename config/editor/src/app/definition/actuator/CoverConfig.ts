import {ActuatorConfig} from "./ActuatorConfig";
import {Duration} from "../Duration";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";
import {Expose, Transform} from "class-transformer";
import {fromDuration, toDuration} from "../AppConfig";

export class CoverConfig extends ActuatorConfig {
    @Expose({name: "open_relay"})
    openRelay: string;

    @Expose({name: "open_relay_bus_id"})
    openRelayBusId: string;

    @Expose({name: "open_relay_bus_type"})
    openRelayBusType: string;

    @Expose({name: "close_relay"})
    closeRelay: string;

    @Expose({name: "close_relay_bus_id"})
    closeRelayBusId: string;

    @Expose({name: "close_relay_bus_type"})
    closeRelayBusType: string;

    @Transform(({value}) => toDuration(value), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Expose({name: "open_time"})
    openTime: Duration;

    @Transform(({value}) => toDuration(value), {toClassOnly: true})
    @Transform(({value}) => fromDuration(value), {toPlainOnly: true})
    @Expose({name: "close_time"})
    closeTime: Duration;

    @Expose({name: "device_class"})
    deviceClass: string;


    constructor(name: string, outputType: ActuatorType, output: number, openRelay: string, openRelayBusId: string, openRelayBusType: string, closeRelay: string, closeRelayBusId: string, closeRelayBusType: string, openTime: Duration, closeTime: Duration, deviceClass: string) {
        super(OutputKindType.COVER, name, outputType, output);
        this.openRelay = openRelay;
        this.openRelayBusId = openRelayBusId;
        this.openRelayBusType = openRelayBusType;
        this.closeRelay = closeRelay;
        this.closeRelayBusId = closeRelayBusId;
        this.closeRelayBusType = closeRelayBusType;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.deviceClass = deviceClass;
    }
}
