import { Page, expect, test } from '@playwright/test';

const API = 'http://localhost:8080';

const users = [
  {
    userId: 1,
    username: 'jdoe',
    firstName: 'John',
    lastName: 'Doe',
    email: 'jdoe@example.com',
    phone: '5551234',
    enabled: true,
    primaryAccount: { id: 1, accountNumber: 11223101, accountBalance: 1500 },
    savingsAccount: { id: 1, accountNumber: 11223102, accountBalance: 3200 },
  },
];

async function mockBackend(page: Page) {
  await page.route(`${API}/index`, (route) =>
    route.fulfill({ status: 200, contentType: 'text/html', body: '<html></html>' }),
  );
  await page.route(`${API}/api/user/all`, (route) => route.fulfill({ json: users }));
  await page.route(`${API}/api/user/primary/transaction?username=jdoe`, (route) =>
    route.fulfill({
      json: [
        {
          id: 1,
          date: '2017-06-15T10:00:00',
          description: 'Initial deposit',
          type: 'Account',
          status: 'Finished',
          amount: 1500,
          availableBalance: 1500,
        },
      ],
    }),
  );
  await page.route(`${API}/api/appointment/all`, (route) =>
    route.fulfill({
      json: [
        {
          id: 7,
          date: '2017-07-01T09:30:00',
          location: 'Downtown',
          description: 'Mortgage consult',
          confirmed: false,
          user: users[0],
        },
      ],
    }),
  );
}

test('logs in and browses users, transactions and appointments', async ({ page }) => {
  await mockBackend(page);

  await page.goto('/');
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('heading', { name: 'Please login' })).toBeVisible();

  await page.getByPlaceholder('Username').fill('admin');
  await page.getByPlaceholder('Password').fill('admin');
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page.getByRole('heading', { name: 'Welcome to Admin Portal!' })).toBeVisible();

  await page.getByRole('link', { name: /User Account/ }).click();
  await expect(page.getByRole('cell', { name: 'jdoe@example.com' })).toBeVisible();

  await page.getByRole('button', { name: '1500' }).click();
  await expect(page).toHaveURL(/\/primaryTransaction\/jdoe$/);
  await expect(page.getByRole('cell', { name: 'Initial deposit' })).toBeVisible();
  await expect(page.getByRole('cell', { name: '06/15/2017' })).toBeVisible();

  await page.getByRole('link', { name: /Appointment/ }).click();
  await expect(page.getByRole('cell', { name: 'Mortgage consult' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Confirm' })).toBeVisible();
});
