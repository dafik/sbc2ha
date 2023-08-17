import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorResistanceComponent } from './sensor-resistance.component';

describe('SensorResistanceComponent', () => {
  let component: SensorResistanceComponent;
  let fixture: ComponentFixture<SensorResistanceComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorResistanceComponent]
    });
    fixture = TestBed.createComponent(SensorResistanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
