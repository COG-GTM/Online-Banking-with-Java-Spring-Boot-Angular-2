import { test, expect } from '@playwright/test';

/**
 * Smoke test: the SPA boots and the default route redirects to the login page.
 * No backend is required — this exercises routing and rendering only.
 */
test('redirects to the login page and renders the login form', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Please login' })).toBeVisible();
  await expect(page.getByPlaceholder('Username')).toBeVisible();
  await expect(page.getByPlaceholder('Password')).toBeVisible();
});
