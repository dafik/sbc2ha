import {Register} from "./Register";
import {ModbusRegisterType} from "../../enums/ModbusRegisterType";

export class RegisterBase {
    base: number;
    length: number;
    registers: Register[]
    modbusRegisterType: ModbusRegisterType = ModbusRegisterType.INPUT;
}
