import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformAddComponent } from './platform-add.component';

describe('PlatformAddComponent', () => {
  let component: PlatformAddComponent;
  let fixture: ComponentFixture<PlatformAddComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformAddComponent]
    });
    fixture = TestBed.createComponent(PlatformAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
