# Online Banking with Java, Spring Boot, Angular 2

Developed an Banking website which lets you deposit or withdraw money, schedule an appointment with a banker, view bank statements, transfer money between primary or savings account or with other customer.

Created two separate server:

- User Frontend
- Admin Portal

## User Frontend 

User-Front is a user-facing system and it includes modules such as User Signup/Login, Account, Transfer, Appointment, Transaction and User Profile.

## Admin Portal

It is mainly used by Admin and it involves User Account and Appointment modules. Admin can enable/disable Users, view statements of every Users, confirm an appointment.

## Technologies Used

**Front-end:** Html5/CSS3, JavaScript, TypeScript, JQuery, Bootstrap, Angular 2 and some JS plugins, JSON, Thymeleaf

**Back-end:** Java 8, Spring Boot, Spring Data, Spring Security, Hibernate, MySQL, Maven, Log4j


## Database configuration

The User Frontend reads its datasource connection from the environment; no credentials are committed.

| Variable | Required | Default |
| --- | --- | --- |
| `DB_URL` | no | `jdbc:mysql://localhost:3306/onlinebanking` |
| `DB_USERNAME` | yes | none |
| `DB_PASSWORD` | yes | none |

The application refuses to start if `DB_USERNAME` is unset or a superuser account (`root`, `admin`, `mysql.sys`, `mysql.session`), if `DB_PASSWORD` is empty, or if `DB_URL` embeds credentials (`user:pass@host`, `user=`/`password=` parameters). Create a dedicated least-privilege account; Hibernate runs with `ddl-auto=update`, so it needs DDL rights on the application schema only (no global privileges):

```sql
CREATE DATABASE IF NOT EXISTS onlinebanking;
CREATE USER 'onlinebanking_app'@'localhost' IDENTIFIED BY '<strong-random-password>';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON onlinebanking.* TO 'onlinebanking_app'@'localhost';
```

Once the first start has created the tables, drop the DDL rights and make the `role` table read-only (the app only writes `user`/`user_role` for signup and admin enable/disable; it never modifies `role`). Hibernate logs a non-fatal warning when it can no longer re-issue its foreign-key DDL:

```sql
REVOKE ALL PRIVILEGES ON onlinebanking.* FROM 'onlinebanking_app'@'localhost';
GRANT SELECT ON onlinebanking.role TO 'onlinebanking_app'@'localhost';
-- repeat for: appointment, hibernate_sequence, primary_account, primary_transaction,
--             recipient, savings_account, savings_transaction, user, user_role
GRANT SELECT, INSERT, UPDATE, DELETE ON onlinebanking.user TO 'onlinebanking_app'@'localhost';
```

```sh
cd UserFront
DB_USERNAME=onlinebanking_app DB_PASSWORD='<strong-random-password>' mvn spring-boot:run
```

The MySQL `root` password that was previously committed in `application.properties` is public in git history: rotate it on every server that used it, and keep MySQL bound to `127.0.0.1` (or firewalled) so port 3306 is not reachable from outside the application host.
