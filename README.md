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

User-Front reads its database credentials from environment variables:

| Variable      | Required | Default | Purpose                                   |
|---------------|----------|---------|-------------------------------------------|
| `DB_USER`     | no       | `root`  | MySQL user (`spring.datasource.username`) |
| `DB_PASSWORD` | yes      | none    | MySQL password (`spring.datasource.password`) |

Startup fails immediately if `DB_PASSWORD` is not set.

```
cd UserFront
DB_PASSWORD=<your-password> mvn spring-boot:run
```

For local development you can instead copy `UserFront/application-local.properties.example` to `UserFront/application-local.properties` (git-ignored), fill in your credentials, and run from `UserFront/` with `SPRING_PROFILES_ACTIVE=local`.
