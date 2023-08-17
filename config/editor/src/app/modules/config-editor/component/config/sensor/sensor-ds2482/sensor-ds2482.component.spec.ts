import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorDs2482Component } from './sensor-ds2482.component';

describe('SensorDs2482Component', () => {
  let component: SensorDs2482Component;
  let fixture: ComponentFixture<SensorDs2482Component>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorDs2482Component]
    });
    fixture = TestBed.createComponent(SensorDs2482Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
