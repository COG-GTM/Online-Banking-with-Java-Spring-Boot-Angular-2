import { request } from '@playwright/test';
import { execFileSync } from 'child_process';
import { randomBytes } from 'crypto';
import { readFileSync, writeFileSync } from 'fs';
import { join } from 'path';

export const BACKEND = process.env.BACKEND_URL || 'http://localhost:8080';

function sql(statement: string) {
  // DB connection comes from env; defaults match UserFront/src/main/resources/application.properties.
  execFileSync('mysql', [
    '-h', process.env.PARITY_DB_HOST || '127.0.0.1',
    '-P', process.env.PARITY_DB_PORT || '3306',
    '-u', process.env.PARITY_DB_USER || 'root',
    process.env.PARITY_DB_NAME || 'onlinebanking',
    '-e', statement,
  ], { env: { ...process.env, MYSQL_PWD: process.env.PARITY_DB_PASSWORD ?? backendDbPassword() }, stdio: 'pipe' });
}

function backendDbPassword(): string {
  const props = readFileSync(join(__dirname, '..', 'UserFront', 'src', 'main', 'resources', 'application.properties'), 'utf8');
  return props.match(/^spring\.datasource\.password[ \t]*=[ \t]*(.*)$/m)?.[1].trim() ?? '';
}

async function signup(username: string, password: string, first: string, last: string) {
  const ctx = await request.newContext({ baseURL: BACKEND, maxRedirects: 0 });
  const res = await ctx.post('/signup', {
    form: { firstName: first, lastName: last, phone: '555' + username.slice(-7), email: `${username}@parity.test`, username, password },
  });
  if (res.status() !== 302) throw new Error(`signup ${username} failed: ${res.status()}`);
  await ctx.dispose();
}

async function asUser(username: string, password: string, fn: (ctx: any) => Promise<void>) {
  const ctx = await request.newContext({ baseURL: BACKEND });
  await ctx.post('/index', { form: { username, password } });
  await fn(ctx);
  await ctx.dispose();
}

export default async function globalSetup() {
  const run = Date.now().toString(36);
  if (process.env.PARITY_RESET_DB === '1') {
    // Opt-in: wipe the local dev database so both apps render the same, small data set.
    sql('SET FOREIGN_KEY_CHECKS=0; DELETE FROM appointment; DELETE FROM primary_transaction; DELETE FROM savings_transaction; ' +
      'DELETE FROM recipient; DELETE FROM user_role; DELETE FROM user; DELETE FROM primary_account; DELETE FROM savings_account; SET FOREIGN_KEY_CHECKS=1');
  }
  sql("INSERT IGNORE INTO role (role_id, name) VALUES (0, 'ROLE_USER'), (1, 'ROLE_ADMIN')");

  const data: Record<string, any> = { run };
  for (const project of ['baseline', 'upgraded']) {
    const tag = project[0];
    const admin = { username: `padm${run}${tag}`, password: randomBytes(9).toString('base64url') };
    const customer = { username: `pcus${run}${tag}`, password: randomBytes(9).toString('base64url'), first: 'Paula', last: `Parity${tag.toUpperCase()}` };
    const toggled = { username: `ptog${run}${tag}`, password: randomBytes(9).toString('base64url'), first: 'Toggle', last: `User${tag.toUpperCase()}` };

    await signup(admin.username, admin.password, 'Ada', 'Admin');
    await signup(customer.username, customer.password, customer.first, customer.last);
    await signup(toggled.username, toggled.password, toggled.first, toggled.last);
    sql(`INSERT INTO user_role (role_id, user_id) SELECT 1, user_id FROM user WHERE username = '${admin.username}'`);

    const appointment = `Parity mortgage review ${run}${tag}`;
    await asUser(customer.username, customer.password, async (ctx) => {
      await ctx.post('/account/deposit', { form: { accountType: 'Primary', amount: '500' } });
      await ctx.post('/account/withdraw', { form: { accountType: 'Primary', amount: '50' } });
      await ctx.post('/account/deposit', { form: { accountType: 'Savings', amount: '300' } });
      await ctx.post('/appointment/create', { form: { dateString: '2027-01-15 10:30', location: 'Chicago, IL', description: appointment } });
    });
    data[project] = { admin, customer, toggled, appointment };
  }
  writeFileSync(join(__dirname, '.run.json'), JSON.stringify(data, null, 2), { mode: 0o600 });
}
