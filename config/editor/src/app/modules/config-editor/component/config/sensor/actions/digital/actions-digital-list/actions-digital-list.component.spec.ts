import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActionsDigitalListComponent } from './actions-digital-list.component';

describe('ActionsDigitalListComponent', () => {
  let component: ActionsDigitalListComponent;
  let fixture: ComponentFixture<ActionsDigitalListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ActionsDigitalListComponent]
    });
    fixture = TestBed.createComponent(ActionsDigitalListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
