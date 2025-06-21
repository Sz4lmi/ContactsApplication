export interface ContactlistDTO {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  motherName: string;
  birthDate: string;
  tajNumber: string;
  taxId: string;
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
