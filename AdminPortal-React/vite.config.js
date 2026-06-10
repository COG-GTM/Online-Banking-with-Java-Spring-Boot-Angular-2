import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Proxy backend requests to the Spring Boot server during development
      // to avoid CORS issues when calling relative URLs.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/index': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
