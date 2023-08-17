import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformOledComponent } from './platform-oled.component';

describe('PlatformOledComponent', () => {
  let component: PlatformOledComponent;
  let fixture: ComponentFixture<PlatformOledComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformOledComponent]
    });
    fixture = TestBed.createComponent(PlatformOledComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
