export interface ContactList {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumbers: PhoneNumber[];
  addresses: Address[];
}

export interface PhoneNumber {
  phoneNumber: string;
}

export interface Address {
  street: string;
  city: string;
  zipCode: string;
}
