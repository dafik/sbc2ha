import {Directive, ElementRef, forwardRef, Input} from '@angular/core';
import {MAT_INPUT_VALUE_ACCESSOR} from "@angular/material/input";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";

@Directive({
    selector: 'input[matInputHex]',
    providers: [
        {
            provide: MAT_INPUT_VALUE_ACCESSOR,
            useExisting: MatInputHexDirective
        },
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => MatInputHexDirective),
            multi: true,
        }
    ]
})
export class MatInputHexDirective implements ControlValueAccessor {

    private _value: string | null = null;

    constructor(private elementRef: ElementRef<HTMLInputElement>) {
    }

    get value(): string | null {
        return this._value;
    }

    @Input('value')
    set value(value: string | null) {
        this._value = value;
        this.formatValue(value);
    }

    private formatValue(value: string | null) {
        if (value !== null) {
            this.elementRef.nativeElement.value = "0x"+Number(value).toString(16);
        } else {
            this.elementRef.nativeElement.value = '';
        }
    }

    registerOnChange(fn: any): void {
        this._onChange = fn;
    }

    registerOnTouched(fn: any): void {
    }

    setDisabledState(isDisabled: boolean): void {
    }

    writeValue(value: string | null): void {
        this._value = value;
        this.formatValue(this._value); // format Value
    }

    _onChange(value: any): void {
    }

}
