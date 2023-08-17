import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorAnalogComponent } from './sensor-analog.component';

describe('SensorAnalogComponent', () => {
  let component: SensorAnalogComponent;
  let fixture: ComponentFixture<SensorAnalogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorAnalogComponent]
    });
    fixture = TestBed.createComponent(SensorAnalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
