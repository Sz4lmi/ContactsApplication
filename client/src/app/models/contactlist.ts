export interface ContactList {
  id: number;
  firstName: string;
  lastName: string;
  addresses: Address[];
}

export interface Address {
  street: string;
  city: string;
  zipCode: string;
}
