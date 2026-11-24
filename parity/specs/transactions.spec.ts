import { test, expect } from '@playwright/test';
import { login, row, runData, shot } from './helpers';

test('primary account transactions drill-down', async ({ page }, testInfo) => {
  const { admin, customer } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await page.goto('/userAccount');
  await row(page, customer.username).getByText('450').click();
  await expect(page).toHaveURL(new RegExp(`/primaryTransaction/${customer.username}$`));
  await expect(page.getByRole('heading', { name: 'Primary Account Transaction List' })).toBeVisible();
  await expect(page.locator('thead th')).toHaveText(['Post Date', 'Description', 'Type', 'Status', 'Amount', 'Available Balance']);
  const rows = page.locator('tbody tr');
  await expect(rows).toHaveCount(2);
  await expect(rows.filter({ hasText: 'Deposit to Primary Account' }).locator('td').nth(4)).toHaveText('500');
  await expect(rows.filter({ hasText: 'Deposit to Primary Account' }).locator('td').nth(5)).toHaveText('500');
  await expect(rows.filter({ hasText: 'Withdraw from Primary Account' }).locator('td').nth(4)).toHaveText('50');
  await expect(rows.filter({ hasText: 'Withdraw from Primary Account' }).locator('td').nth(5)).toHaveText('450');
  await expect(rows.first().locator('td').first()).toHaveText(/^\d{2}\/\d{2}\/\d{4}\s*$/);
  await shot(page, testInfo, '05-primary-transactions');
});

test('savings account transactions drill-down', async ({ page }, testInfo) => {
  const { admin, customer } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await page.goto('/userAccount');
  await row(page, customer.username).getByText('300').click();
  await expect(page).toHaveURL(new RegExp(`/savingsTransaction/${customer.username}$`));
  await expect(page.getByRole('heading', { name: 'Savings Account Transaction List' })).toBeVisible();
  const rows = page.locator('tbody tr');
  await expect(rows).toHaveCount(1);
  await expect(rows.first()).toContainText('Deposit to savings Account');
  await expect(rows.first().locator('td').nth(4)).toHaveText('300');
  await shot(page, testInfo, '06-savings-transactions');
});
