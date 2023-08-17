import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformBusComponent } from './platform-bus.component';

describe('PlatformBusComponent', () => {
  let component: PlatformBusComponent;
  let fixture: ComponentFixture<PlatformBusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformBusComponent]
    });
    fixture = TestBed.createComponent(PlatformBusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
