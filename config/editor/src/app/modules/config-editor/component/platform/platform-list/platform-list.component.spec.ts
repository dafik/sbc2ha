import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformListComponent } from './platform-list.component';

describe('PlatformListComponent', () => {
  let component: PlatformListComponent;
  let fixture: ComponentFixture<PlatformListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformListComponent]
    });
    fixture = TestBed.createComponent(PlatformListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
