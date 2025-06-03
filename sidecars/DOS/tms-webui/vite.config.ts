import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,          // So you can use describe, it, expect globally without import
    environment: 'jsdom',   // React testing usually needs jsdom
    setupFiles: './src/setupTests.ts',  // Optional: for any global setup (e.g. React Testing Library)
    coverage: {
      provider: 'c8',       // For coverage reporting
      reporter: ['text', 'lcov'], 
    },
  },
})
