import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService, User, UserDTO } from '../../services/user.service';
import { ContactService } from '../../services/contact.service';
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

  // Pagination properties
  currentPage = 0;
  pageSize = 3;
  totalPages = 0;

  constructor(
    private userService: UserService,
    private contactService: ContactService,
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
        this.calculateTotalPages();
        this.logger.debug('UserlistComponent: loaded users =', data);
      },
      (error) => {
        this.logger.error('Error fetching users:', error);
      }
    );
  }

  /**
   * Calculate the total number of pages based on the users array length and page size
   */
  calculateTotalPages(): void {
    this.totalPages = Math.ceil(this.users.length / this.pageSize);
    // Reset to first page if current page is out of bounds
    if (this.currentPage >= this.totalPages) {
      this.currentPage = Math.max(0, this.totalPages - 1);
    }
  }

  /**
   * Get the users for the current page
   */
  getCurrentPageUsers(): User[] {
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    return this.users.slice(startIndex, endIndex);
  }

  /**
   * Navigate to the previous page
   */
  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
    }
  }

  /**
   * Navigate to the next page
   */
  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
    }
  }

  /**
   * Navigate to a specific page
   */
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
    }
  }

  /**
   * Initialize the form for editing users
   */
  initForm(): void {
    this.editUserForm = this.fb.group({
      username: ['', Validators.required],
      adminPassword: [''],
      newPassword: ['']
    });
  }

  openEditModal(user: User): void {
    this.selectedUser = user;
    this.editUserForm.patchValue({
      username: user.username,
      adminPassword: '',
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

      // Only require admin password if username or new password is changed
      if ((formValue.username !== this.selectedUser.username || formValue.newPassword) && !formValue.adminPassword) {
        alert('Admin password is required when changing username or password');
        return;
      }

      const userDTO: UserDTO = {
        username: formValue.username,
        password: formValue.newPassword || '',
        adminPassword: formValue.oldPassword || ''
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
