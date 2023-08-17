import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformMqttComponent } from './platform-mqtt.component';

describe('PlatformMqttComponent', () => {
  let component: PlatformMqttComponent;
  let fixture: ComponentFixture<PlatformMqttComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformMqttComponent]
    });
    fixture = TestBed.createComponent(PlatformMqttComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
