import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Proxy backend requests to the Spring Boot server during development
      // to avoid CORS issues when calling relative URLs.
      // autoRewrite rewrites the host/port in 302 Location headers from the
      // backend (e.g. http://localhost:8080/userFront) back to the dev-server
      // origin so the browser follows redirects same-origin instead of
      // triggering a cross-origin (CORS) request.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        autoRewrite: true,
      },
      '/index': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        autoRewrite: true,
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        autoRewrite: true,
      },
      '/userFront': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        autoRewrite: true,
      },
    },
  },
})
