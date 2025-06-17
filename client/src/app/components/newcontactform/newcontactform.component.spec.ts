import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewcontactformComponent } from './newcontactform.component';

describe('NewcontactformComponent', () => {
  let component: NewcontactformComponent;
  let fixture: ComponentFixture<NewcontactformComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewcontactformComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NewcontactformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
