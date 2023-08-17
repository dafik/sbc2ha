import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'hex'
})
export class HexPipe implements PipeTransform {

  transform(value: number | undefined, ...args: unknown[]): string {
    if(!value){
      return ''
    }
    return "0x"+value.toString(16)
  }

}
