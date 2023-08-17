import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionMqttComponent } from './action-mqtt.component';

describe('ActionMqttComponent', () => {
  let component: ActionMqttComponent;
  let fixture: ComponentFixture<ActionMqttComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActionMqttComponent]
    });
    fixture = TestBed.createComponent(ActionMqttComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
