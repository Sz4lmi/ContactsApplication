import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService, User, UserDTO } from '../../services/user.service';
import { ContactserviceService } from '../../services/contactservice.service';
import { LoggingService } from '../../services/logging.service';

@Component({
  selector: 'app-userlist',
  standalone: false,
  templateUrl: './userlist.component.html',
  styleUrls: ['./userlist.component.css']
})
export class UserlistComponent implements OnInit {
  users: User[] = [];
  showEditModal = false;
  editUserForm!: FormGroup;
  selectedUser: User | null = null;
  expandedUserIds: Set<number> = new Set<number>();
  expandedContactIds: Set<number> = new Set<number>();

  constructor(
    private userService: UserService,
    private contactService: ContactserviceService,
    private fb: FormBuilder,
    private logger: LoggingService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.initForm();
  }

  /**
   * Load all users from the server
   */
  loadUsers(): void {
    this.userService.getAllUsers().subscribe(
      (data) => {
        this.users = data;
        this.logger.debug('UserlistComponent: loaded users =', data);
      },
      (error) => {
        this.logger.error('Error fetching users:', error);
      }
    );
  }

  /**
   * Initialize the form for editing users
   */
  initForm(): void {
    this.editUserForm = this.fb.group({
      username: ['', Validators.required],
      oldPassword: [''],
      newPassword: ['']
    });
  }

  openEditModal(user: User): void {
    this.selectedUser = user;
    this.editUserForm.patchValue({
      username: user.username,
      oldPassword: '',
      newPassword: ''
    });
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedUser = null;
  }

  updateUser(): void {
    if (this.editUserForm.valid && this.selectedUser) {
      const formValue = this.editUserForm.value;

      // Only require old password if username or new password is changed
      if ((formValue.username !== this.selectedUser.username || formValue.newPassword) && !formValue.oldPassword) {
        alert('Old password is required when changing username or password');
        return;
      }

      const userDTO: UserDTO = {
        username: formValue.username,
        password: formValue.newPassword || '',
        oldPassword: formValue.oldPassword || ''
      };

      this.userService.updateUser(this.selectedUser.id, userDTO).subscribe(
        () => {
          this.loadUsers();
          this.closeEditModal();
        },
        (error) => {
          console.error('Error updating user:', error);
          alert('Error updating user: ' + (error.error || 'Unknown error'));
        }
      );
    }
  }

  deleteUser(user: User): void {
    if (confirm(`Are you sure you want to delete user "${user.username}"? This will also delete all of their contacts, phone numbers, and addresses.`)) {
      this.userService.deleteUser(user.id).subscribe(
        () => {
          this.loadUsers();
        },
        (error) => {
          console.error('Error deleting user:', error);
          alert('Error deleting user: ' + (error.error || 'Unknown error'));
        }
      );
    }
  }

  toggleUserExpansion(user: User, event: MouseEvent): void {
    // Prevent the click from triggering if it was on a button
    if (event.target instanceof HTMLButtonElement) {
      return;
    }

    if (this.expandedUserIds.has(user.id)) {
      this.expandedUserIds.delete(user.id);
    } else {
      this.expandedUserIds.add(user.id);
    }
  }

  isUserExpanded(userId: number): boolean {
    return this.expandedUserIds.has(userId);
  }

  toggleContactExpansion(contactId: number, event: MouseEvent): void {
    // Prevent the click from triggering if it was on a button
    if (event.target instanceof HTMLButtonElement) {
      return;
    }

    // Stop propagation to prevent triggering the user card expansion
    event.stopPropagation();

    if (this.expandedContactIds.has(contactId)) {
      this.expandedContactIds.delete(contactId);
    } else {
      this.expandedContactIds.add(contactId);
    }
  }

  isContactExpanded(contactId: number): boolean {
    return this.expandedContactIds.has(contactId);
  }
}
