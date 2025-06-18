import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService, UserDTO } from '../../services/user.service';

@Component({
  selector: 'app-newuserform',
  templateUrl: './newuserform.component.html',
  styleUrls: ['./newuserform.component.css'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule
  ]
})
export class NewuserformComponent {
  userForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isSubmitting: boolean = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router
  ) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      role: ['ROLE_USER', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const userData: UserDTO = {
      username: this.userForm.value.username,
      password: this.userForm.value.password,
      role: this.userForm.value.role
    };

    this.userService.createUser(userData).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.successMessage = `User ${response.username} created successfully!`;
        this.userForm.reset({
          username: '',
          password: '',
          role: 'ROLE_USER'
        });
      },
      error: (error) => {
        this.isSubmitting = false;
        if (error.error && typeof error.error === 'string') {
          this.errorMessage = error.error;
        } else if (error.message) {
          this.errorMessage = error.message;
        } else {
          this.errorMessage = 'An error occurred while creating the user.';
        }
      }
    });
  }
}
