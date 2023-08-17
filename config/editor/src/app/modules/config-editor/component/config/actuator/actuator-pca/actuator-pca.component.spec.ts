import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorPcaComponent } from './actuator-pca.component';

describe('AngularPcaComponent', () => {
  let component: ActuatorPcaComponent;
  let fixture: ComponentFixture<ActuatorPcaComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorPcaComponent]
    });
    fixture = TestBed.createComponent(ActuatorPcaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
