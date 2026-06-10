import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Match the backend CORS allow-origin (UserFront expects http://localhost:4200)
    port: 4200,
    host: true,
  },
})
