import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Timeclock } from './timeclock';

describe('Timeclock', () => {
  let component: Timeclock;
  let fixture: ComponentFixture<Timeclock>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Timeclock],
    }).compileComponents();

    fixture = TestBed.createComponent(Timeclock);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
