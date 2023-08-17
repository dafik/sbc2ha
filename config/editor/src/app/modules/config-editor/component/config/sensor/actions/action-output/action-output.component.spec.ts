import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionOutputComponent } from './action-output.component';

describe('ActionOutputComponent', () => {
  let component: ActionOutputComponent;
  let fixture: ComponentFixture<ActionOutputComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActionOutputComponent]
    });
    fixture = TestBed.createComponent(ActionOutputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
