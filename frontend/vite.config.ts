import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 6001, // 개발 서버 포트를 6001으로 설정 (ERR_UNSAFE_PORT 해결)
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // /api 경로를 유지 (백엔드가 /api를 기대함)
      },
    },
  },
})
