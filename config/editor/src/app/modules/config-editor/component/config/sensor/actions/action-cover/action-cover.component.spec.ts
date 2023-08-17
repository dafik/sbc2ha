import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionCoverComponent } from './action-cover.component';

describe('ActionCoverComponent', () => {
  let component: ActionCoverComponent;
  let fixture: ComponentFixture<ActionCoverComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActionCoverComponent]
    });
    fixture = TestBed.createComponent(ActionCoverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
