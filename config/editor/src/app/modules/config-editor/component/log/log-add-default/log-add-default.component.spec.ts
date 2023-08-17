import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogAddDefaultComponent } from './log-add-default.component';

describe('LogAddDefaultComponent', () => {
  let component: LogAddDefaultComponent;
  let fixture: ComponentFixture<LogAddDefaultComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LogAddDefaultComponent]
    });
    fixture = TestBed.createComponent(LogAddDefaultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
