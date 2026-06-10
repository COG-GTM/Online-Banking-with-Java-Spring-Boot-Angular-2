import { useCallback, useEffect, useState } from 'react';
import { useAppointments } from '../hooks/useAppointments';
import { formatDateTime } from '../utils/format';
import type { Appointment } from '../types';

export function AppointmentPage() {
  const { getAll, confirm } = useAppointments();
  const [appointments, setAppointments] = useState<Appointment[]>([]);

  const loadAppointments = useCallback(() => {
    getAll()
      .then((res) => setAppointments(res.data))
      .catch((error) => console.log(error));
  }, [getAll]);

  useEffect(() => {
    loadAppointments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleConfirm = async (id: number) => {
    try {
      await confirm(id);
    } catch (error) {
      console.log(error);
    }
    loadAppointments();
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
          {appointments.map((appointment) => (
            <tr key={appointment.id}>
              <td>{appointment.id}</td>
              <td>{appointment.user?.username}</td>
              <td>{formatDateTime(appointment.date)}</td>
              <td>{appointment.description}</td>
              <td>{String(appointment.confirmed)}</td>
              {!appointment.confirmed && (
                <td>
                  <a
                    onClick={() => handleConfirm(appointment.id)}
                    style={{ cursor: 'pointer' }}
                  >
                    Confirm
                  </a>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
