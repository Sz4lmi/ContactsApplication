export interface Contactrequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumbers: string[];
  addresses: Address[];
  tajNumber: string;
  taxId: string;
  motherName: string;
  birthDate: string;
}

export interface Address {
  street: string;
  city: string;
  zipCode: string;
}
