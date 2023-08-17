import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorGpiopwmComponent } from './actuator-gpiopwm.component';

describe('ActuatorGpiopwmComponent', () => {
  let component: ActuatorGpiopwmComponent;
  let fixture: ComponentFixture<ActuatorGpiopwmComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorGpiopwmComponent]
    });
    fixture = TestBed.createComponent(ActuatorGpiopwmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
