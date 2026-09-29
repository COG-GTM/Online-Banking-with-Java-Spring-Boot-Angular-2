import { expect, test } from '@playwright/test';

const API = 'http://localhost:8080';

const users = [
  {
    username: 'jdoe',
    firstName: 'John',
    lastName: 'Doe',
    email: 'jdoe@example.com',
    phone: '555-0100',
    enabled: true,
    primaryAccount: { accountBalance: 1250.5 },
    savingsAccount: { accountBalance: 8000 },
  },
  {
    username: 'asmith',
    firstName: 'Alice',
    lastName: 'Smith',
    email: 'asmith@example.com',
    phone: '555-0101',
    enabled: false,
    primaryAccount: { accountBalance: 320 },
    savingsAccount: { accountBalance: 15000 },
  },
];

test('renders the login page', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Please login' })).toBeVisible();
  await expect(page.getByPlaceholder('Username')).toBeVisible();
  await expect(page.getByPlaceholder('Password')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Login' })).toBeVisible();
});

test('a successful login navigates to the user list', async ({ page }) => {
  let loginBody = '';
  await page.route(`${API}/index`, async (route) => {
    loginBody = route.request().postData() ?? '';
    await route.fulfill({ status: 200, contentType: 'text/html', body: '<html></html>' });
  });
  await page.route(`${API}/api/user/all`, (route) => route.fulfill({ json: users }));

  await page.goto('/login');
  await page.getByPlaceholder('Username').fill('admin');
  await page.getByPlaceholder('Password').fill('admin');
  await page.getByRole('button', { name: 'Login' }).click();

  await expect(page).toHaveURL(/\/userAccount$/);
  await expect(page.getByRole('heading', { name: 'User Account Page' })).toBeVisible();
  await expect(page.locator('#userTable tbody tr')).toHaveCount(2);
  await expect(page.getByRole('cell', { name: 'jdoe@example.com' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Logout' })).toBeVisible();
  expect(loginBody).toBe('username=admin&password=admin');
});
