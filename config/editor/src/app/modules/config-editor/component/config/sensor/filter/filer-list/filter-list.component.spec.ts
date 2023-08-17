import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FilterListComponent } from './filter-list.component';

describe('FilerListComponent', () => {
  let component: FilterListComponent;
  let fixture: ComponentFixture<FilterListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FilterListComponent]
    });
    fixture = TestBed.createComponent(FilterListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
