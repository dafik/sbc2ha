import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionsAddTypeComponent } from './actions-add-type.component';

describe('ActionsAddTypeComponent', () => {
  let component: ActionsAddTypeComponent;
  let fixture: ComponentFixture<ActionsAddTypeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActionsAddTypeComponent]
    });
    fixture = TestBed.createComponent(ActionsAddTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
