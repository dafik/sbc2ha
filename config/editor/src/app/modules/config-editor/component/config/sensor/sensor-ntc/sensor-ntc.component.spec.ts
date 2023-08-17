import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorNtcComponent } from './sensor-ntc.component';

describe('SensorNtcComponent', () => {
  let component: SensorNtcComponent;
  let fixture: ComponentFixture<SensorNtcComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorNtcComponent]
    });
    fixture = TestBed.createComponent(SensorNtcComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
