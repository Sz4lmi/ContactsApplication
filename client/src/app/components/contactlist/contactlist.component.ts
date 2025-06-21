import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ContactService } from '../../services/contact.service';
import { ContactlistDTO } from '../../models/contactlistDTO';
import { ContactrequestDTO, Address as AddressRequest } from '../../models/contactrequestDTO';
import { AuthService } from '../../services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-contactlist',
  standalone: false,
  templateUrl: './contactlist.component.html',
  styleUrls: ['./contactlist.component.css']
})
export class ContactlistComponent implements OnInit {
  contacts: ContactlistDTO[] = [];
  showEditModal = false;
  editContactForm!: FormGroup;
  selectedContact: ContactlistDTO | null = null;
  expandedContactIds: Set<number> = new Set<number>();
  validationErrors: { [key: string]: string } = {};

  isAdmin = false;

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

  // Validator to ensure either email or phone is provided
  emailOrPhoneValidator(group: AbstractControl): ValidationErrors | null {
    const email = group.get('email')?.value;
    const phoneNumbers = group.get('phoneNumbers') as FormArray;
    if ((!email || email === '') && (!phoneNumbers || phoneNumbers.length === 0 || phoneNumbers.controls.every(c => !c.value))) {
      return { emailOrPhone: true };
    }
    return null;
  }

  constructor(
    private contactService: ContactService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.isAdmin = this.authService.isAdmin();
    console.log('ContactlistComponent: isAdmin =', this.isAdmin);
    console.log('ContactlistComponent: userRole =', this.authService.getUserRole());
  }

  ngOnInit(): void {
    this.loadContacts();
    this.initForm();
  }

  loadContacts(): void {
    this.contactService.getContactList().subscribe(
      (data) => {
        this.contacts = data;
        console.log('ContactlistComponent: loaded contacts =', data);
        console.log('ContactlistComponent: contacts length =', data.length);
      },
      (error) => {
        console.error('Error fetching contacts:', error);
      }
    );
  }

  initForm(): void {
    this.editContactForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', Validators.email],
      motherName: ['', Validators.required],
      birthDate: ['', Validators.required],
      tajNumber: ['', Validators.required],
      taxId: ['', Validators.required],
      phoneNumbers: this.fb.array([]),
      addresses: this.fb.array([])
    }, { validators: this.emailOrPhoneValidator });
  }

  get phoneNumbers(): FormArray {
    return this.editContactForm.get('phoneNumbers') as FormArray;
  }

  get addresses(): FormArray {
    return this.editContactForm.get('addresses') as FormArray;
  }

  addPhoneNumber(): void {
    this.phoneNumbers.push(this.fb.control('', [
      Validators.required,
      this.phoneNumberValidator()
    ]));
  }

  removePhoneNumber(index: number): void {
    this.phoneNumbers.removeAt(index);
  }

  addAddress(): void {
    this.addresses.push(
      this.fb.group({
        street: ['', Validators.required],
        city: ['', Validators.required],
        zipCode: ['', Validators.required]
      })
    );
  }

  removeAddress(index: number): void {
    this.addresses.removeAt(index);
  }

  openEditModal(contact: ContactlistDTO): void {
    console.log('Opening edit modal for contact:', contact);
    this.selectedContact = contact;

    // Clear previous validation errors
    this.validationErrors = {};

    // Fetch the full contact details to populate the form
    this.contactService.getContactById(contact.id).subscribe(
      (fullContact) => {
        console.log('Full contact:', fullContact);

        if (fullContact) {
          // Reset form arrays
          while (this.phoneNumbers.length) {
            this.phoneNumbers.removeAt(0);
          }
          while (this.addresses.length) {
            this.addresses.removeAt(0);
          }

          // Populate the form
          this.editContactForm.patchValue({
            firstName: fullContact.firstName,
            lastName: fullContact.lastName,
            email: fullContact.email,
            motherName: fullContact.motherName,
            birthDate: fullContact.birthDate ? fullContact.birthDate.substring(0, 10) : '',
            tajNumber: fullContact.tajNumber,
            taxId: fullContact.taxId
          });

          // Add phone numbers
          if (fullContact.phoneNumbers) {
            fullContact.phoneNumbers.forEach((phone: { phoneNumber: any; }) => {
              this.phoneNumbers.push(this.fb.control(phone.phoneNumber, [
                Validators.required,
                this.phoneNumberValidator()
              ]));
            });
          }

          // Add addresses
          if (fullContact.addresses) {
            fullContact.addresses.forEach((address: { street: any; city: any; zipCode: any; }) => {
              this.addresses.push(
                this.fb.group({
                  street: [address.street, Validators.required],
                  city: [address.city, Validators.required],
                  zipCode: [address.zipCode, Validators.required]
                })
              );
            });
          }

          this.showEditModal = true;
          console.log('Modal should be visible now, showEditModal =', this.showEditModal);
        } else {
          console.error('Could not find full contact details for contact ID:', contact.id);
        }
      },
      (error) => {
        console.error('Error fetching contact details:', error);
      }
    );
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedContact = null;
  }

  updateContact(): void {
    // Clear previous validation errors
    this.validationErrors = {};

    if (this.editContactForm.valid && this.selectedContact) {
      const formValue = this.editContactForm.value;

      const contactRequest: ContactrequestDTO = {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        motherName: formValue.motherName,
        birthDate: formValue.birthDate,
        tajNumber: formValue.tajNumber,
        taxId: formValue.taxId,
        phoneNumbers: formValue.phoneNumbers,
        addresses: formValue.addresses
      };

      this.contactService.updateContact(this.selectedContact.id, contactRequest).subscribe({
        next: () => {
          this.loadContacts();
          this.closeEditModal();
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error updating contact:', error);

          // Handle validation errors from the backend
          if (error.status === 400 && error.error) {
            // Store validation errors
            this.validationErrors = error.error;

            // Mark form controls as touched to trigger validation messages
            Object.keys(this.validationErrors).forEach(field => {
              const control = this.editContactForm.get(field);
              if (control) {
                control.markAsTouched();
              }
            });
          }
        }
      });
    }
  }

  deleteContact(contact: ContactlistDTO): void {
    if (confirm(`Are you sure you want to delete ${contact.firstName} ${contact.lastName}?`)) {
      this.contactService.deleteContact(contact.id).subscribe(
        () => {
          this.loadContacts();
        },
        (error) => {
          console.error('Error deleting contact:', error);
        }
      );
    }
  }

  toggleContactExpansion(contact: ContactlistDTO, event: MouseEvent): void {
    // Prevent the click from triggering if it was on a button
    if (event.target instanceof HTMLButtonElement) {
      return;
    }

    if (this.expandedContactIds.has(contact.id)) {
      this.expandedContactIds.delete(contact.id);
    } else {
      this.expandedContactIds.add(contact.id);
    }
  }

  isContactExpanded(contactId: number): boolean {
    return this.expandedContactIds.has(contactId);
  }
}
