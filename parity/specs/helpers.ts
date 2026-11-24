import { Page, TestInfo, expect } from '@playwright/test';
import { readFileSync } from 'fs';
import { join } from 'path';

export function runData(testInfo: TestInfo) {
  const all = JSON.parse(readFileSync(join(__dirname, '..', '.run.json'), 'utf8'));
  return all[testInfo.project.name];
}

export async function login(page: Page, username: string, password: string) {
  await page.goto('/login');
  await page.getByPlaceholder('Username').fill(username);
  await page.getByPlaceholder('Password').fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { name: 'Welcome to Admin Portal!' })).toBeVisible();
}

export async function shot(page: Page, testInfo: TestInfo, name: string) {
  await page.waitForLoadState('networkidle');
  await page.screenshot({
    path: join(__dirname, '..', 'report', 'screens', testInfo.project.name, `${name}.png`),
    fullPage: true,
    animations: 'disabled',
  });
}

export function row(page: Page, text: string) {
  return page.locator('tbody tr').filter({ hasText: text });
}
