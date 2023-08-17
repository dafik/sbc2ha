import {StateClassType} from "../../enums/StateClassType";
import {SensorDeviceClassType} from "../../enums/deviceClass/ha/SensorDeviceClassType";
import {ModbusReturnType} from "../../enums/ModbusReturnType";
import {ModbusRegisterType} from "../../enums/ModbusRegisterType";

export class Register {
    name: string;
    address: number;
    unitOfMeasurement: string;
    stateClass: StateClassType;
    deviceClass: SensorDeviceClassType;
    returnType: ModbusReturnType;
    modbusRegisterType: ModbusRegisterType = ModbusRegisterType.INPUT;
    haFilter: string = "round(2)";
}
