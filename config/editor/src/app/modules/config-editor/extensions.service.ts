import {Injectable} from '@angular/core';
import {AppConfig} from "../../definition/AppConfig";
import {definition, InputBoard, OutputBoard, Vendor} from "../../definition/extentsionBoard/ExtensionBoardsDef";
import {PlatformType} from "../../definition/enums/PlatformType";
import {AnalogSensorConfig} from "../../definition/sensor/analog/AnalogSensorConfig";
import {InputConfig} from "../../definition/sensor/digital/InputConfig";
import {OutputKindType} from "../../definition/enums/OutputKindType";
import {CoverConfig} from "../../definition/actuator/CoverConfig";
import {SensorConfig} from "../../definition/sensor/SensorConfig";
import {PlatformConfig} from "../../definition/platform/PlatformConfig";
import {BusConfig} from "../../definition/platform/bus/BusConfig";
import {ActuatorConfig} from "../../definition/actuator/ActuatorConfig";
import {Lm75BusConfig} from "../../definition/platform/bus/Lm75BusConfig";
import {Mcp23017BusConfig} from "../../definition/platform/bus/Mcp23017BusConfig";
import {PCA9685BusConfig} from "../../definition/platform/bus/PCA9685BusConfig";
import {I2cBusConfig} from "../../definition/platform/bus/I2cBusConfig";
import {parse} from "yaml";
import {map} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {AppConfigFromYamlService} from "../../shared/app-config-from-yaml.service";
import * as uuid from 'uuid';
import {instanceToPlain, plainToInstance} from "class-transformer";
import {UrlHelper} from "../../shared/url-helper";

@Injectable({
  providedIn: 'root'
})
export class ExtensionsService {
  private config!: AppConfig;
  private inputBoard!: InputBoard;
  private outputBoard!: OutputBoard;

  constructor(private hc: HttpClient, private as: AppConfigFromYamlService) {
  }

  public getConfig(): AppConfig {
    return this.config;
  }


  public setConfig(config: AppConfig) {
    this.config = config;

    let vendor: Vendor | undefined = definition.vendor.find(value => {
      return value.name == config.extensionBoards?.vendor
    });
    if (!vendor) {
      throw "Vendor not found"
    }
    let outputBoard = vendor.outputBoard.find(value => {
      return value.name == config.extensionBoards?.outputBoard
    });
    if (outputBoard) {
      this.outputBoard = outputBoard;
      if (outputBoard.buses) {
        outputBoard.buses.forEach(value => {
          let bus: I2cBusConfig
          switch (value.platform) {
            case PlatformType.LM75:
              bus = this.getBus(value.platform, value.busId) as Lm75BusConfig;
              if (bus) {
                bus.busId = value.busId
                bus.address = (value as Lm75BusConfig).address
              } else {
                this.addPlatform(value)
              }
              break;
            case PlatformType.MCP23017:
              bus = this.getBus(value.platform, value.busId) as Mcp23017BusConfig;
              if (bus) {
                bus.busId = value.busId
                bus.address = (value as Mcp23017BusConfig).address
              } else {
                this.addPlatform(value)
              }
              break;
            case PlatformType.PCA9685:
              bus = this.getBus(value.platform, value.busId) as PCA9685BusConfig;
              if (bus) {
                bus.busId = value.busId
                bus.address = (value as PCA9685BusConfig).address
              } else {
                this.addPlatform(value)
              }
              break;
          }
        })
      }
    }
    let inputBoard = vendor.inputBoard.find(value => {
      return value.name == config.extensionBoards?.inputBoard
    });
    if (inputBoard) {
      this.inputBoard = inputBoard
    }
  }

  public getDigitalInputs(): number[] {
    if (this.inputBoard.digitalInputs) {
      let used = this.config.sensor
        .filter(value => {
          return value.platform == PlatformType.DIGITAL || value.platform == PlatformType.SWITCH
        })
        .map(value => {
          return (value as InputConfig<any>).input
        });
      let available = this.inputBoard.digitalInputs.filter(x => !used.includes(x));
      return available;
    }
    return []
  }

  public getAnalogInputs(): number[] {
    if (this.inputBoard.analogInputs) {
      let used = this.config.sensor
        .filter(value => {
          return value.platform == PlatformType.ANALOG
        })
        .map(value => {
          return (value as AnalogSensorConfig).analog
        });
      let available = this.inputBoard.analogInputs.filter(x => {
        let b = !used.includes(x);
        return b;
      });
      return available;
      //return this.inputBoard.analogInputs
    }
    return []
  }

  public getOutputs(): number[] {
    if (this.outputBoard.digitalOutputs) {
      let used = this.config.actuator
        .filter(value => {
          return value.kind != OutputKindType.COVER
        })
        .map(value => {
          return value.output
        });

      this.config.actuator
        .filter(value => {
          return value.kind == OutputKindType.COVER
        })
        .forEach(value => {
          used.push(Number((value as CoverConfig).openRelay))
          used.push(Number((value as CoverConfig).closeRelay))
        })

      let available = this.outputBoard.digitalOutputs.filter(x => !used.includes(x));
      return available;
    }
    return []
  }

  getSensors(platform: PlatformType): string[] {
    return this.config.sensor
      .filter(value => value.platform == platform)
      .map(value => value.name)
  }

  addSensor(sensor: SensorConfig) {
    this.config.sensor.push(sensor);
  }

  getBuses(type: PlatformType) {
    return this.config.platform
      .filter(value => value.platform == type)
      .map(value => (value as BusConfig).busId)
  }

  addPlatform(platformConfig: PlatformConfig) {
    this.config.platform.push(platformConfig)
  }

  getActuators() {
    return this.config.actuator
      .filter(value => value.kind != OutputKindType.COVER)
      .map(value => ({
        name: value.name,
        output: value.output
      }))
  }

  getActuator(output: number) {
    return this.config.actuator
      .find(value => value.output == output)
  }

  getActuatorsCover() {
    return this.config.actuator
      .filter(value => value.kind == OutputKindType.COVER)
      .map(value => value.name)
  }

  addActuator(actuator: ActuatorConfig) {
    this.config.actuator.push(actuator)
  }

  getPlatforms() {
    return this.config.platform;
  }

  private getBus(platform: PlatformType, busId: string) {
    let busConfigs: BusConfig[] = this.config.platform
      .filter(value => {
        return value.platform == platform
      }) as BusConfig[];
    let found = busConfigs.filter(value => value.busId == busId);
    if (found.length == 1) {
      return found[0]
    } else if (found.length == 0) {
      return null
    } else {
      throw "found multiple"
    }

  }

  public getCurrentConfig() {
    return this.hc.get(UrlHelper.getApiUrl() + "config/json", {observe: "body", responseType: "text"})
      .pipe(
        map(src => {
          let parsed = parse(src);
          let appConfig: AppConfig = plainToInstance(AppConfig, parsed, {exposeUnsetFields: false});
          return appConfig
        })
      )
  }

  asPlain(ac: AppConfig): Object {
    let plain = instanceToPlain(ac, {
      exposeDefaultValues: false,
      exposeUnsetFields: false,

    });
    return plain;
  }

  asJson(obj: any) {
    return JSON.stringify(obj)
  }

  saveConfig(ac: AppConfig) {
    const id = uuid.v4();
    const plain = this.asPlain(ac)
    localStorage.setItem(id, this.asJson(plain))
    return id
  }

  loadConfig(uuid: string) {
    let item = localStorage.getItem(uuid);
    if (item) {
      let parsed = JSON.parse(item);

      let appConfig: AppConfig = plainToInstance(AppConfig, parsed, {exposeUnsetFields: false});

      let appConfigOld = new AppConfig();
      Object.assign(appConfigOld, parsed)
      this.setConfig(appConfig);
    }

  }
}
