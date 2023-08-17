import {Pipe, PipeTransform} from '@angular/core';
import {Duration} from "../../definition/Duration";
import {stringify} from 'yaml'

@Pipe({
    name: 'yaml',
    pure: false
})
export class YamlPipe implements PipeTransform {

    transform(value: Object): unknown {
        let snakeCase = this.convertCase(value);
        return stringify(snakeCase);
    }

    convertCase(value: Object) {
        let s = JSON.stringify(value, (key: string, value: any) => {
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
        return JSON.parse(s);
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
