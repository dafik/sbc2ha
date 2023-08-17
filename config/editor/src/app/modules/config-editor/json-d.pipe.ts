import {Pipe, PipeTransform} from '@angular/core';
import {Duration} from "../../definition/Duration";

@Pipe({
    name: 'jsonD'
})
export class JsonDPipe implements PipeTransform {

    transform(value: Object): string {
        return JSON.stringify(value, (key: string, value: any) => {
            if (value instanceof Duration) {
                return value.duration
            }
            if (value instanceof Array) {
                return value
            }
            if (value && typeof value === 'object' && key!='logs') {
                var replacement = {};
                for (var k in value) {
                    if (Object.hasOwnProperty.call(value, k)) {
                        (replacement as any)[this.toSnakeCase(k)] = value[k];
                    }
                }
                return replacement;
            }
            return value;
        }, 2);
    }

    private toSnakeCase(inputString: string) {
        return inputString.split('').map((character) => {
            if (character != '.' && character == character.toUpperCase()) {
                return '_' + character.toLowerCase();
            } else {
                return character;
            }
        }).join('');
    }

}
