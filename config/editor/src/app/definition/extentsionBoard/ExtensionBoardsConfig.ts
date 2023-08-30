import {Expose} from "class-transformer";

export class ExtensionBoardsConfig {
    vendor: string;

    @Expose({name: "input_board"})
    inputBoard: string;

    @Expose({name: "output_board"})
    outputBoard: string;

    constructor(vendor: string, inputBoard: string, outputBoard: string) {
        this.vendor = vendor;
        this.inputBoard = inputBoard;
        this.outputBoard = outputBoard;
    }
}
