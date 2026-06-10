# Online Banking with Java, Spring Boot, Angular 2

Developed an Banking website which lets you deposit or withdraw money, schedule an appointment with a banker, view bank statements, transfer money between primary or savings account or with other customer.

Created two separate server:

- User Frontend
- Admin Portal

## User Frontend 

User-Front is a user-facing system and it includes modules such as User Signup/Login, Account, Transfer, Appointment, Transaction and User Profile.

## Admin Portal

It is mainly used by Admin and it involves User Account and Appointment modules. Admin can enable/disable Users, view statements of every Users, confirm an appointment.

The Admin Portal lives in `AdminPortal-React/` and is a React 18+ single-page app
(migrated from the original Angular 4 implementation). It talks to the UserFront
Spring Boot REST API and uses cookie-based authentication (`withCredentials`).

### Running the Admin Portal

```bash
cd AdminPortal-React
npm install
npm start          # dev server on http://localhost:4200
npm run build      # production build to dist/
npm run lint       # eslint
```

The dev server runs on port `4200` to match the backend CORS allow-origin
(`UserFront` permits `http://localhost:4200`). The API base URL can be overridden
with the `VITE_API_BASE_URL` environment variable (defaults to
`http://localhost:8080`).

## Technologies Used

**Front-end (Admin Portal):** React 18+, TypeScript, Vite, React Router, Axios, Bootstrap 5

**Back-end:** Java, Spring Boot, Spring Data, Spring Security, Hibernate, MySQL, Maven, Log4j

