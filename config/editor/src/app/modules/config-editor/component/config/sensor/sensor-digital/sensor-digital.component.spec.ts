import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorDigitalComponent } from './sensor-digital.component';

describe('SensorDigitalComponent', () => {
  let component: SensorDigitalComponent;
  let fixture: ComponentFixture<SensorDigitalComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorDigitalComponent]
    });
    fixture = TestBed.createComponent(SensorDigitalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
