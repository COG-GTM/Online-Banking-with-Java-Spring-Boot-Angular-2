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

## Configuration

The User Frontend connects to a MySQL database named `onlinebanking`. Database
credentials are **not** hard-coded; they are read from environment variables
(see `UserFront/src/main/resources/application.properties`):

| Variable      | Description           | Default (local dev only) |
| ------------- | --------------------- | ------------------------ |
| `DB_USERNAME` | MySQL username        | `root`                   |
| `DB_PASSWORD` | MySQL password        | _(empty)_                |

Set them before running the backend, for example:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
cd UserFront && mvn spring-boot:run
```

The defaults are provided only so the app/tests can run against a local,
password-less MySQL. Always supply real credentials via the environment (or a
secrets manager) in any shared or production environment — never commit them.

