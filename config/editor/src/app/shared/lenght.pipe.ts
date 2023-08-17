import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'lenght'
})
export class LenghtPipe implements PipeTransform {

    transform(value: string | undefined, length: number): unknown {
        if (!value) {
            return;
        }
        if (value.length < length) {
            return value.padEnd(length, ' ');
        }
        return value;
    }

}
