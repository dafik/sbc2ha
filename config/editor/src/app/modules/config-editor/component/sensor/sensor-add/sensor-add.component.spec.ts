import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorAddComponent } from './sensor-add.component';

describe('SensorAddComponent', () => {
  let component: SensorAddComponent;
  let fixture: ComponentFixture<SensorAddComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorAddComponent]
    });
    fixture = TestBed.createComponent(SensorAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
