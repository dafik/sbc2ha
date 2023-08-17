import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorSwitchComponent } from './sensor-switch.component';

describe('SensorSwitchComponent', () => {
  let component: SensorSwitchComponent;
  let fixture: ComponentFixture<SensorSwitchComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorSwitchComponent]
    });
    fixture = TestBed.createComponent(SensorSwitchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
