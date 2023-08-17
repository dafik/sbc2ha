import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorDallasComponent } from './sensor-dallas.component';

describe('SensorDallasComponent', () => {
  let component: SensorDallasComponent;
  let fixture: ComponentFixture<SensorDallasComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorDallasComponent]
    });
    fixture = TestBed.createComponent(SensorDallasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
