import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WebOnlyComponent } from './web-only.component';

describe('WebOnlyComponent', () => {
  let component: WebOnlyComponent;
  let fixture: ComponentFixture<WebOnlyComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WebOnlyComponent]
    });
    fixture = TestBed.createComponent(WebOnlyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
