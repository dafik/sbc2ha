import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorAddComponent } from './actuator-add.component';

describe('ActuatorAddComponent', () => {
  let component: ActuatorAddComponent;
  let fixture: ComponentFixture<ActuatorAddComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorAddComponent]
    });
    fixture = TestBed.createComponent(ActuatorAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
