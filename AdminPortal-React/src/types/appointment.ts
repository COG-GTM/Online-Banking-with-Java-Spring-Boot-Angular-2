export interface AppointmentUser {
  username: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
}

export interface Appointment {
  id: number;
  date: string;
  location: string;
  description: string;
  confirmed: boolean;
  user: AppointmentUser;
}
