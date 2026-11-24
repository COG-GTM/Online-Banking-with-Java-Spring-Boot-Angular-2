import { test, expect } from '@playwright/test';
import { login, runData, shot } from './helpers';

test('login page shows the sign-in form', async ({ page }, testInfo) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Please login' })).toBeVisible();
  await expect(page.getByPlaceholder('Username')).toBeVisible();
  await expect(page.getByPlaceholder('Password')).toBeVisible();
  await expect(page.getByText('Remember me')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Login' })).toBeEnabled();
  await expect(page.getByRole('link', { name: 'Admin Portal' })).toBeVisible();
  await shot(page, testInfo, '01-login');
});

test('admin can sign in and sees the menu', async ({ page }, testInfo) => {
  const { admin } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await expect(page.getByRole('heading', { name: 'Please login' })).toBeHidden();
  await expect(page.getByRole('link', { name: 'User Account' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Appointment' })).toBeVisible();
  await expect(page.getByText('Logout', { exact: true })).toBeVisible();
  await shot(page, testInfo, '02-welcome');
});

test('wrong password still flips to the welcome screen but loads no data (legacy behaviour)', async ({ page }, testInfo) => {
  const { admin } = runData(testInfo);
  await login(page, admin.username, 'wrong-' + Date.now());
  await page.getByRole('link', { name: 'User Account' }).click();
  await expect(page.getByRole('heading', { name: 'User Account Page' })).toBeVisible();
  await page.waitForLoadState('networkidle');
  await expect(page.locator('tbody tr')).toHaveCount(0);
});
