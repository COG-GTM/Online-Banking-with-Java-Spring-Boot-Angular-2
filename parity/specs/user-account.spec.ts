import { test, expect } from '@playwright/test';
import { login, row, runData, shot } from './helpers';

test('user account list shows seeded customers with balances', async ({ page }, testInfo) => {
  const { admin, customer } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await page.getByRole('link', { name: 'User Account' }).click();
  await expect(page).toHaveURL(/\/userAccount$/);
  await expect(page.getByRole('heading', { name: 'User Account Page' })).toBeVisible();
  await expect(page.locator('thead th')).toHaveText(['User Name', 'First Name', 'Last Name', 'Email', 'Phone', 'Primary Account', 'Savings Account', 'Enabled', 'Action']);
  const r = row(page, customer.username);
  await expect(r).toHaveCount(1);
  await expect(r.locator('td')).toHaveText([
    customer.username, customer.first, customer.last, `${customer.username}@parity.test`,
    '555' + customer.username.slice(-7), '450', '300', 'true', 'Enable', 'Disable',
  ], { useInnerText: true });
  await expect(r.getByText('Disable')).toBeVisible();
  await expect(r.getByText('Enable', { exact: true })).toBeHidden();
  await shot(page, testInfo, '03-user-account');
});

test('admin can disable and re-enable a user', async ({ page }, testInfo) => {
  const { admin, toggled } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await page.goto('/userAccount');
  const r = row(page, toggled.username);
  await expect(r.locator('td').nth(7)).toHaveText('true');
  await r.getByText('Disable').click();
  await expect(r.locator('td').nth(7)).toHaveText('false', { timeout: 10000 });
  await expect(r.getByText('Enable', { exact: true })).toBeVisible();
  await expect(r.getByText('Disable')).toBeHidden();
  await shot(page, testInfo, '04-user-disabled');
  await r.getByText('Enable', { exact: true }).click();
  await expect(r.locator('td').nth(7)).toHaveText('true', { timeout: 10000 });
  await expect(r.getByText('Disable')).toBeVisible();
});

test('without a session the user list is empty', async ({ page }) => {
  await page.goto('/userAccount');
  await expect(page.getByRole('heading', { name: 'User Account Page' })).toBeVisible();
  await page.waitForLoadState('networkidle');
  await expect(page.locator('tbody tr')).toHaveCount(0);
});
