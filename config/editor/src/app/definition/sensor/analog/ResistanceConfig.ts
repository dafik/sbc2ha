import {PlatformType} from "../../enums/PlatformType";
import {ResistanceDirectionType} from "../../enums/ResistanceDirectionType";
import {SingleSourceConfig} from "../SingleSourceConfig";


export class ResistanceConfig extends SingleSourceConfig {
  direction: ResistanceDirectionType;
  resistor: string;
  referenceVoltage: string;


  constructor(name: string, sensor: string, direction: ResistanceDirectionType, resistor: string, referenceVoltage: string) {
    super(PlatformType.RESISTANCE, name, sensor);
    this.direction = direction;
    this.resistor = resistor;
    this.referenceVoltage = referenceVoltage;
  }
}
