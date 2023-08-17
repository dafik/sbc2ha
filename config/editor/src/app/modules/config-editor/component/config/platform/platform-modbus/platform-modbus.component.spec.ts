import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformModbusComponent } from './platform-modbus.component';

describe('PlatformModbusComponent', () => {
  let component: PlatformModbusComponent;
  let fixture: ComponentFixture<PlatformModbusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformModbusComponent]
    });
    fixture = TestBed.createComponent(PlatformModbusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
