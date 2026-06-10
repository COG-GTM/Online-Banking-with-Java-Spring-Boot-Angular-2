import { useEffect, useState } from 'react';
import {
  getAppointmentList,
  confirmAppointment,
} from '../services/appointmentService';
import { formatDateTime } from '../utils/formatDate';

export default function AppointmentPage() {
  const [appointmentList, setAppointmentList] = useState([]);

  const fetchAppointments = () => {
    getAppointmentList()
      .then((res) => setAppointmentList(res.data))
      .catch((error) => console.log(error));
  };

  useEffect(() => {
    fetchAppointments();
  }, []);

  const handleConfirm = (id) => {
    confirmAppointment(id)
      .then(() => fetchAppointments())
      .catch((error) => console.log(error));
  };

  return (
    <div>
      <h1>Appointment List Page</h1>

      <table className="table table-striped">
        <thead>
          <tr>
            <th>Appointment Id</th>
            <th>User Name</th>
            <th>Date</th>
            <th>Description</th>
            <th>Confirmed?</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {appointmentList.map((appointment) => (
            <tr key={appointment.id}>
              <td>{appointment.id}</td>
              <td>{appointment.user && appointment.user.username}</td>
              <td>{formatDateTime(appointment.date)}</td>
              <td>{appointment.description}</td>
              <td>{String(appointment.confirmed)}</td>
              {!appointment.confirmed ? (
                <td>
                  <a
                    onClick={() => handleConfirm(appointment.id)}
                    style={{ cursor: 'pointer' }}
                  >
                    Confirm
                  </a>
                </td>
              ) : (
                <td></td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
