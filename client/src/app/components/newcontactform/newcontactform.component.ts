import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl,
  ValidationErrors,
  ReactiveFormsModule,
  ValidatorFn
} from '@angular/forms';
import { ContactService } from '../../services/contact.service';
import { ContactrequestDTO, Address } from '../../models/contactrequestDTO';
import {Router} from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-newcontactform',
  templateUrl: './newcontactform.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule
  ],
  styleUrls: ['./newcontactform.component.css']
})
export class NewcontactformComponent {
  contactForm: FormGroup;
  validationErrors: { [key: string]: string } = {};

  // Custom validator for phone numbers
  phoneNumberValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (!value) {
        return null; // Let required validator handle empty values
      }

      // Check if the value matches the pattern (only digits, spaces, and optional + at the beginning)
      const patternValid = /^(\+)?[0-9 ]+$/.test(value);

      // Count the number of digits (excluding spaces and +)
      const digitCount = value.replace(/[^0-9]/g, '').length;
      const digitCountValid = digitCount === 10 || digitCount === 11;

      if (!patternValid || !digitCountValid) {
        return { 'phoneFormat': true };
      }

      return null;
    };
  }

  constructor(
    private fb: FormBuilder,
    private contactService: ContactService,
    private router: Router
  ) {
    this.contactForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', Validators.email],
      tajNumber: ['', Validators.required],
      taxId: ['', Validators.required],
      birthDate: ['', Validators.required],
      motherName: ['', Validators.required],
      phoneNumbers: this.fb.array([]),
      addresses: this.fb.array([])
    }, { validators: this.emailOrPhoneValidator });
  }

  get phoneNumbers() {
    return this.contactForm.get('phoneNumbers') as FormArray;
  }

  get addresses() {
    return this.contactForm.get('addresses') as FormArray;
  }

  createAddressGroup(): FormGroup {
    return this.fb.group({
      street: ['', Validators.required],
      city: ['', Validators.required],
      zipCode: ['', Validators.required]
    });
  }

  addPhoneNumber() {
    this.phoneNumbers.push(this.fb.control('', [
      Validators.required,
      this.phoneNumberValidator()
    ]));
  }

  removePhoneNumber(index: number) {
    this.phoneNumbers.removeAt(index);
  }

  addAddress() {
    this.addresses.push(this.createAddressGroup());
  }

  removeAddress(index: number) {
    this.addresses.removeAt(index);
  }

  emailOrPhoneValidator(group: AbstractControl): ValidationErrors | null {
    const email = group.get('email')?.value;
    const phoneNumbers = group.get('phoneNumbers') as FormArray;
    if ((!email || email === '') && (!phoneNumbers || phoneNumbers.length === 0 || phoneNumbers.controls.every(c => !c.value))) {
      return { emailOrPhone: true };
    }
    return null;
  }

  onSubmit() {
    // Clear previous validation errors
    this.validationErrors = {};

    if (this.contactForm.valid) {
      // Create a contact request object from form data
      const contactRequest: ContactrequestDTO = {
        firstName: this.contactForm.value.firstName,
        lastName: this.contactForm.value.lastName,
        email: this.contactForm.value.email,
        tajNumber: this.contactForm.value.tajNumber,
        taxId: this.contactForm.value.taxId,
        birthDate: this.contactForm.value.birthDate,
        motherName: this.contactForm.value.motherName,
        phoneNumbers: this.contactForm.value.phoneNumbers || [],
        addresses: this.contactForm.value.addresses || []
      };

      // Send the contact request to the backend
      this.contactService.createContact(contactRequest).subscribe({
        next: (response) => {
          console.log('Contact created successfully:', response);
          // Reset the form after successful submission
          this.contactForm.reset();
          this.phoneNumbers.clear();
          this.addresses.clear();
          this.router.navigate(['/contacts']);
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error creating contact:', error);

          // Handle validation errors from the backend
          if (error.status === 400 && error.error) {
            // Store validation errors
            this.validationErrors = error.error;

            // Mark form controls as touched to trigger validation messages
            Object.keys(this.validationErrors).forEach(field => {
              const control = this.contactForm.get(field);
              if (control) {
                control.markAsTouched();
              }
            });
          }
        }
      });
    }
  }
}
