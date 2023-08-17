import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorListComponent } from './sensor-list.component';

describe('SensorListComponent', () => {
  let component: SensorListComponent;
  let fixture: ComponentFixture<SensorListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SensorListComponent]
    });
    fixture = TestBed.createComponent(SensorListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
