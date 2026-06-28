// Base URL of the Spring Boot UserFront backend. The Angular app hard-coded
// http://localhost:8080; keep the same default but allow a Vite env override.
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
