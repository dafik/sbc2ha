import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogAddPackageComponent } from './log-add-package.component';

describe('LogAddPackageComponent', () => {
  let component: LogAddPackageComponent;
  let fixture: ComponentFixture<LogAddPackageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LogAddPackageComponent]
    });
    fixture = TestBed.createComponent(LogAddPackageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
