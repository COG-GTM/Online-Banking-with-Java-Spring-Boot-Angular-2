import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// React dev server runs on :3000 and proxies API calls to the Spring Boot
// backend on :8080 so the SPA and API share an origin during development.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
