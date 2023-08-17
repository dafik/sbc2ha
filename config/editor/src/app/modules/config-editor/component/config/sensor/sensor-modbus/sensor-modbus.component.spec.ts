import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorModbusComponent } from './sensor-modbus.component';

describe('SensorModbusComponent', () => {
  let component: SensorModbusComponent;
  let fixture: ComponentFixture<SensorModbusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorModbusComponent]
    });
    fixture = TestBed.createComponent(SensorModbusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
