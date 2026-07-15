import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The UserFront Spring Boot backend serves the JSON API on :8080. Proxying the
// backend paths keeps the browser same-origin in dev, which sidesteps CORS/CSRF
// friction and lets the session cookie flow through unchanged.
const backend = 'http://localhost:8080'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: backend, changeOrigin: true },
      '/index': { target: backend, changeOrigin: true },
      '/logout': { target: backend, changeOrigin: true },
      '/signup': { target: backend, changeOrigin: true },
    },
  },
})
