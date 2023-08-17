import {ActuatorConfig} from "./ActuatorConfig";
import {Duration} from "../Duration";
import {OutputKindType} from "../enums/OutputKindType";
import {ActuatorType} from "../enums/ActuatorType";

export class CoverConfig extends ActuatorConfig {
    openRelay: string;
    openRelayBusId: string;
    openRelayBusType: string;
    closeRelay: string;
    closeRelayBusId: string;
    closeRelayBusType: string;
    openTime: Duration;
    closeTime: Duration;
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
