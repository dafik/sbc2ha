import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogAddWriterComponent } from './log-add-writer.component';

describe('LogAddWriterComponent', () => {
  let component: LogAddWriterComponent;
  let fixture: ComponentFixture<LogAddWriterComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LogAddWriterComponent]
    });
    fixture = TestBed.createComponent(LogAddWriterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
