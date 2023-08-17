import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionsSwitchListComponent } from './actions-switch-list.component';

describe('ActionsSwitchListComponent', () => {
  let component: ActionsSwitchListComponent;
  let fixture: ComponentFixture<ActionsSwitchListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActionsSwitchListComponent]
    });
    fixture = TestBed.createComponent(ActionsSwitchListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
