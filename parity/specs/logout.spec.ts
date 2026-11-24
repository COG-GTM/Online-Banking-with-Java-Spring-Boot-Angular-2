import { test, expect } from '@playwright/test';
import { login, runData, shot } from './helpers';

test('logout ends the backend session', async ({ page }, testInfo) => {
  const { admin } = runData(testInfo);
  await login(page, admin.username, admin.password);
  await page.getByText('Logout', { exact: true }).click();
  await page.waitForLoadState('networkidle');
  await page.goto('/userAccount');
  await page.waitForLoadState('networkidle');
  await expect(page.locator('tbody tr')).toHaveCount(0);
  await shot(page, testInfo, '08-after-logout');
});
