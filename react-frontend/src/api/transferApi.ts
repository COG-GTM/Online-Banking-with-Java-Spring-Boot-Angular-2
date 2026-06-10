import client from './client';
import type {
  BetweenTransferRequest,
  ExternalTransferRequest,
  RecipientDto,
} from '../types';

export async function transferBetween(data: BetweenTransferRequest): Promise<void> {
  await client.post('/api/transfer/between', data);
}

export async function getRecipients(): Promise<RecipientDto[]> {
  const res = await client.get<RecipientDto[]>('/api/transfer/recipients');
  return res.data;
}

export async function createRecipient(data: RecipientDto): Promise<RecipientDto> {
  const res = await client.post<RecipientDto>('/api/transfer/recipients', data);
  return res.data;
}

export async function updateRecipient(name: string, data: RecipientDto): Promise<RecipientDto> {
  const res = await client.put<RecipientDto>(
    `/api/transfer/recipients/${encodeURIComponent(name)}`,
    data,
  );
  return res.data;
}

export async function deleteRecipient(name: string): Promise<void> {
  await client.delete(`/api/transfer/recipients/${encodeURIComponent(name)}`);
}

export async function transferExternal(data: ExternalTransferRequest): Promise<void> {
  await client.post('/api/transfer/external', data);
}
