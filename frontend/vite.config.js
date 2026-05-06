import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    // Один `npm run build` — фронт попадает в Spring Boot classpath:/static/
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    // Иначе на Windows Vite часто слушает только [::1], а браузер идёт на 127.0.0.1 → ERR_CONNECTION_REFUSED
    host: true,
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
  preview: {
    host: true,
    port: 4173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
    },
  },
})
