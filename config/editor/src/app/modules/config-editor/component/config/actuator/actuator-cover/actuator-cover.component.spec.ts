import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorCoverComponent } from './actuator-cover.component';

describe('ActuatorCoverComponent', () => {
  let component: ActuatorCoverComponent;
  let fixture: ComponentFixture<ActuatorCoverComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorCoverComponent]
    });
    fixture = TestBed.createComponent(ActuatorCoverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
