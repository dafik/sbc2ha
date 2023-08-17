import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformI2cComponent } from './platform-i2c.component';

describe('PlatformI2cComponent', () => {
  let component: PlatformI2cComponent;
  let fixture: ComponentFixture<PlatformI2cComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformI2cComponent]
    });
    fixture = TestBed.createComponent(PlatformI2cComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
