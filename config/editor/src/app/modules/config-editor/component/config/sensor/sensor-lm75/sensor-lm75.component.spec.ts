import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorLm75Component } from './sensor-lm75.component';

describe('SensorLm75Component', () => {
  let component: SensorLm75Component;
  let fixture: ComponentFixture<SensorLm75Component>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorLm75Component]
    });
    fixture = TestBed.createComponent(SensorLm75Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
