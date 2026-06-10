# Online Banking with Java, Spring Boot, Angular 2

Developed an Banking website which lets you deposit or withdraw money, schedule an appointment with a banker, view bank statements, transfer money between primary or savings account or with other customer.

Created two separate server:

- User Frontend
- Admin Portal

## User Frontend 

User-Front is a user-facing system and it includes modules such as User Signup/Login, Account, Transfer, Appointment, Transaction and User Profile.

## Admin Portal

It is mainly used by Admin and it involves User Account and Appointment modules. Admin can enable/disable Users, view statements of every Users, confirm an appointment.

The Admin Portal has been migrated from Angular 4 to **React (Vite)** and lives in
[`AdminPortal-React/`](./AdminPortal-React). The original Angular implementation is
preserved in [`AdminPortal/`](./AdminPortal) for reference. The React frontend talks
to the same `UserFront` Spring Boot backend and requires no backend changes. See
[`AdminPortal-React/README.md`](./AdminPortal-React/README.md) for how to run it.

## Technologies Used

**Admin Portal (front-end):** HTML5/CSS3, JavaScript, React 19, Vite, react-router-dom, axios, Bootstrap

**User Frontend / legacy Admin Portal:** Html5/CSS3, JavaScript, TypeScript, JQuery, Bootstrap, Angular and some JS plugins, JSON, Thymeleaf

**Back-end:** Java 8, Spring Boot, Spring Data, Spring Security, Hibernate, MySQL, Maven, Log4j

