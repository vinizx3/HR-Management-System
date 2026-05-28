import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Vacations } from './vacations';

describe('Vacations', () => {
  let component: Vacations;
  let fixture: ComponentFixture<Vacations>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Vacations],
    }).compileComponents();

    fixture = TestBed.createComponent(Vacations);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
