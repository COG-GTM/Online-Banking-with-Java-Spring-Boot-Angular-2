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

The `UserFront` backend reads its datasource configuration from environment
variables instead of committing credentials to source control. Set the
following before running the backend:

| Variable      | Required | Default                                            | Description              |
| ------------- | -------- | -------------------------------------------------- | ------------------------ |
| `DB_USERNAME` | yes      | —                                                  | Database username        |
| `DB_PASSWORD` | yes      | —                                                  | Database password        |
| `DB_URL`      | no       | `jdbc:mysql://localhost:3306/onlinebanking`        | JDBC connection URL      |

Example:

```bash
export DB_USERNAME=onlinebanking
export DB_PASSWORD='<your-password>'
# optionally: export DB_URL=jdbc:mysql://db-host:3306/onlinebanking
cd UserFront && mvn spring-boot:run
```

> **Note:** A database password was previously committed to this repository.
> That credential must be considered compromised and rotated on the database
> server; do not reuse it.

