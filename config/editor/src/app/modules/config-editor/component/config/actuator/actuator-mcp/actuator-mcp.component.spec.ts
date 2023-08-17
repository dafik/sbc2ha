import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActuatorMcpComponent } from './actuator-mcp.component';

describe('ActuatorMcpComponent', () => {
  let component: ActuatorMcpComponent;
  let fixture: ComponentFixture<ActuatorMcpComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActuatorMcpComponent]
    });
    fixture = TestBed.createComponent(ActuatorMcpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
