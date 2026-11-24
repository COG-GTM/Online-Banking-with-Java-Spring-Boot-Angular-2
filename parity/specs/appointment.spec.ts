import { test, expect } from '@playwright/test';
import { login, row, runData, shot } from './helpers';

test('appointment list shows and confirms an appointment', async ({ page }, testInfo) => {
  const { admin, customer, appointment } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await page.getByRole('link', { name: 'Appointment' }).click();
  await expect(page).toHaveURL(/\/appointment$/);
  await expect(page.getByRole('heading', { name: 'Appointment List Page' })).toBeVisible();
  const r = row(page, appointment);
  await expect(r).toHaveCount(1);
  await expect(r.locator('td').nth(1)).toHaveText(customer.username);
  await expect(r.locator('td').nth(2)).toHaveText(/^01\/15\/2027 - \d{2}:\d{2}$/);
  await expect(r.locator('td').nth(4)).toHaveText('false');
  await expect(r.getByText('Confirm')).toBeVisible();
  await shot(page, testInfo, '07-appointments');
  await r.getByText('Confirm').click();
  await expect(r.locator('td').nth(4)).toHaveText('true', { timeout: 10000 });
  await expect(r.getByText('Confirm')).toBeHidden();
});
