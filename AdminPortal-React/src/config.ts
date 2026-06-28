// Base URL of the Spring Boot UserFront backend. The Angular app hard-coded
// http://localhost:8080; keep that as the default but allow overriding via env.
export const API_BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';
