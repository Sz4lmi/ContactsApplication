
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NewuserformComponent} from './newuserform.component';

describe('NewuserformComponent', () => {
  let component: NewuserformComponent;
  let fixture: ComponentFixture<NewuserformComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewuserformComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(NewuserformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
