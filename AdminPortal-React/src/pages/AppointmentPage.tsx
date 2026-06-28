import { useCallback, useEffect, useState } from 'react';
import {
  confirmAppointment,
  getAppointmentList,
} from '../services/appointmentService';
import type { Appointment } from '../types/appointment';
import { formatAppointmentDate } from '../utils/date';

export default function AppointmentPage() {
  const [appointmentList, setAppointmentList] = useState<Appointment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [confirmingId, setConfirmingId] = useState<number | null>(null);

  const refresh = useCallback(async (signal?: AbortSignal) => {
    setLoading(true);
    setError(null);
    try {
      const list = await getAppointmentList(signal);
      setAppointmentList(list);
    } catch (err: unknown) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return;
      }
      setError(
        err instanceof Error ? err.message : 'Failed to load appointments',
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    let active = true;
    getAppointmentList(controller.signal)
      .then((list) => {
        if (active) {
          setAppointmentList(list);
          setLoading(false);
        }
      })
      .catch((err: unknown) => {
        if (!active || (err instanceof DOMException && err.name === 'AbortError')) {
          return;
        }
        setError(
          err instanceof Error ? err.message : 'Failed to load appointments',
        );
        setLoading(false);
      });
    return () => {
      active = false;
      controller.abort();
    };
  }, []);

  const handleConfirm = async (id: number) => {
    setConfirmingId(id);
    try {
      await confirmAppointment(id);
      await refresh();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Failed to confirm appointment');
    } finally {
      setConfirmingId(null);
    }
  };

  return (
    <div>
      <h1>Appointment List Page</h1>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <p>Loading appointments...</p>
      ) : appointmentList.length === 0 ? (
        <p>No appointments found.</p>
      ) : (
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
                <td>{appointment.user?.username}</td>
                <td>{formatAppointmentDate(appointment.date)}</td>
                <td>{appointment.description}</td>
                <td>{String(appointment.confirmed)}</td>
                <td hidden={appointment.confirmed}>
                  <a
                    onClick={() => handleConfirm(appointment.id)}
                    style={{ cursor: 'pointer' }}
                  >
                    {confirmingId === appointment.id ? 'Confirming...' : 'Confirm'}
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
