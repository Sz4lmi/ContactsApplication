import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl,
  ValidationErrors,
  ReactiveFormsModule
} from '@angular/forms';
import { ContactserviceService } from '../../services/contactservice.service';
import { Contactrequest, Address } from '../../models/contactrequest';
import {Router} from '@angular/router';

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

  constructor(
    private fb: FormBuilder,
    private contactService: ContactserviceService,
    private router: Router
  ) {
    this.contactForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', Validators.email],
      tajNumber: ['', Validators.required],
      taxId: ['', Validators.required],
      birthDate: ['', Validators.required],
      mothersName: ['', Validators.required],
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
    this.phoneNumbers.push(this.fb.control('', Validators.required));
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
    if (this.contactForm.valid) {
      // Create a contact request object from form data
      const contactRequest: Contactrequest = {
        firstName: this.contactForm.value.firstName,
        lastName: this.contactForm.value.lastName,
        email: this.contactForm.value.email,
        tajNumber: this.contactForm.value.tajNumber,
        taxId: this.contactForm.value.taxId,
        birthDate: this.contactForm.value.birthDate,
        motherName: this.contactForm.value.mothersName, // Note the field name difference
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
        error: (error) => {
          console.error('Error creating contact:', error);
          // Handle error (show error message, etc.)
        }
      });
    }
  }
}
