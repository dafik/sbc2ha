import {Mcp23017BusConfig} from "../platform/bus/Mcp23017BusConfig";
import {BusConfig} from "../platform/bus/BusConfig";
import {Lm75BusConfig} from "../platform/bus/Lm75BusConfig";

export interface ExtensionBoardsDefs {
    vendor: Vendor[]
}

export interface Vendor {
    name: string;
    inputBoard: InputBoard[]
    outputBoard: OutputBoard[];
}

export interface InputBoard {
    name: string;
    digitalInputs: number[];
    analogInputs?: number[];
}

export interface OutputBoard {
    name: string;
    digitalOutputs: number[];
    buses?: BusConfig[]
}

export const definition: ExtensionBoardsDefs = {
    vendor: [
        {
            name: "boneio",
            inputBoard: [
                {
                    name: "input-v0.2",
                    digitalInputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52],
                    analogInputs: [1, 2, 3, 4, 5, 6, 7],
                },
                {
                    name: "input-v0.3",
                    digitalInputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49],
                    analogInputs: [1, 2, 3, 4, 5, 6, 7]
                }
            ],
            outputBoard: [
                {
                    name: "led-32x4A-v0.4",
                    digitalOutputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32]
                },
                {
                    name: "output-24x16A-v0.3",
                    digitalOutputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24],
                    buses: [
                        new Mcp23017BusConfig("mcp1", 0x20),
                        new Mcp23017BusConfig("mcp2", 0x21),
                        new Lm75BusConfig("lm75", 0x48)
                    ]
                },
                {
                    name: "output-24x16A-v0.4",
                    digitalOutputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24],
                    buses: [
                        new Mcp23017BusConfig("mcp1", 0x20),
                        new Mcp23017BusConfig("mcp2", 0x21),
                        new Lm75BusConfig("lm75", 0x48)
                    ]
                },
                {
                    name: "output-32x5A-v0.4",
                    digitalOutputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32]
                }
            ]
        },
        {
            name: "rpi",
            inputBoard: [{
                name: "nohat",
                digitalInputs: [3, 5, 7, 8, 10, 11, 12, 13, 15, 16, 18, 19, 21, 22, 23, 24, 26, 27, 28, 29, 31, 32, 33, 35, 36, 37, 38, 40]
            }],
            outputBoard: [{
                name: "nohat",
                digitalOutputs: [3, 5, 7, 8, 10, 11, 12, 13, 15, 16, 18, 19, 21, 22, 23, 24, 26, 27, 28, 29, 31, 32, 33, 35, 36, 37, 38, 40]
            }]
        },
        {
            name: "fake",
            inputBoard: [{
                name: "in",
                digitalInputs: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
                analogInputs: [1, 2, 3, 4, 5],
            }],
            outputBoard: [{
                name: "out",
                digitalOutputs: [1, 2, 3, 4, 5]
            }]
        }
    ]
};
