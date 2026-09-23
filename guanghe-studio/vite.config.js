import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  build: {
    rollupOptions: {
      output: {
        entryFileNames: 'assets/index-[hash]-v2.js',
        chunkFileNames: 'assets/[name]-[hash]-v2.js',
        assetFileNames: 'assets/[name]-[hash]-v2[extname]'
      }
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8780',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/dev-api': {
        target: 'http://localhost:8780',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/dev-api/, '')
      },
      '/profile': {
        target: 'http://localhost:8780',
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        charset: false
      }
    }
  }
})
