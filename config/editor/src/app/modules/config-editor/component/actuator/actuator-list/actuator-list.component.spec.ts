import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorListComponent } from './actuator-list.component';

describe('ActuatorListComponent', () => {
  let component: ActuatorListComponent;
  let fixture: ComponentFixture<ActuatorListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorListComponent]
    });
    fixture = TestBed.createComponent(ActuatorListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
