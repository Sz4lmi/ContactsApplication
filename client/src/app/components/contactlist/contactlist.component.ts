import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { ContactService } from '../../services/contact.service';
import { ContactList } from '../../models/contactlist';
import { Contactrequest, Address as AddressRequest } from '../../models/contactrequest';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-contactlist',
  standalone: false,
  templateUrl: './contactlist.component.html',
  styleUrls: ['./contactlist.component.css']
})
export class ContactlistComponent implements OnInit {
  contacts: ContactList[] = [];
  showEditModal = false;
  editContactForm!: FormGroup;
  selectedContact: ContactList | null = null;
  expandedContactIds: Set<number> = new Set<number>();

  isAdmin = false;

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
      email: [''],
      mothersName: [''],
      birthDate: [''],
      tajNumber: [''],
      taxId: [''],
      phoneNumbers: this.fb.array([]),
      addresses: this.fb.array([])
    });
  }

  get phoneNumbers(): FormArray {
    return this.editContactForm.get('phoneNumbers') as FormArray;
  }

  get addresses(): FormArray {
    return this.editContactForm.get('addresses') as FormArray;
  }

  addPhoneNumber(): void {
    this.phoneNumbers.push(this.fb.control(''));
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

  openEditModal(contact: ContactList): void {
    console.log('Opening edit modal for contact:', contact);
    this.selectedContact = contact;

    // Fetch the full contact details to populate the form
    this.contactService.getContactById(contact.id).subscribe(
      (fullContact) => {
        console.log('Found full contact:', fullContact);

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
            mothersName: fullContact.motherName,
            birthDate: fullContact.birthDate,
            tajNumber: fullContact.tajNumber,
            taxId: fullContact.taxId
          });

          // Add phone numbers
          if (fullContact.phoneNumbers) {
            fullContact.phoneNumbers.forEach((phone: { phoneNumber: any; }) => {
              this.phoneNumbers.push(this.fb.control(phone.phoneNumber));
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
    if (this.editContactForm.valid && this.selectedContact) {
      const formValue = this.editContactForm.value;

      const contactRequest: Contactrequest = {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        motherName: formValue.mothersName,
        birthDate: formValue.birthDate,
        tajNumber: formValue.tajNumber,
        taxId: formValue.taxId,
        phoneNumbers: formValue.phoneNumbers,
        addresses: formValue.addresses
      };

      this.contactService.updateContact(this.selectedContact.id, contactRequest).subscribe(
        () => {
          this.loadContacts();
          this.closeEditModal();
        },
        (error) => {
          console.error('Error updating contact:', error);
        }
      );
    }
  }

  deleteContact(contact: ContactList): void {
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

  toggleContactExpansion(contact: ContactList, event: MouseEvent): void {
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
