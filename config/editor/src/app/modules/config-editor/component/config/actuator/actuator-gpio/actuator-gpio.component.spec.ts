import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorGpioComponent } from './actuator-gpio.component';

describe('ActuatorGpioComponent', () => {
  let component: ActuatorGpioComponent;
  let fixture: ComponentFixture<ActuatorGpioComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorGpioComponent]
    });
    fixture = TestBed.createComponent(ActuatorGpioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
