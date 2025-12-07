import { defineConfig } from 'vitest/config';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  resolve: {
    mainFields: ['module'],
  },
  plugins: [angular()],
  test: {
    globals: true,
    environment: 'jsdom',
    reporters: [
      'default',
      ['junit', { outputFile: './target/site/jacoco.xml', suiteName: 'Angular Tests' }]
    ],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov', 'clover', 'json'],
      reportsDirectory: './target/coverage',
      include: ['src/**/*.{ts,tsx}'],
      exclude: ['**/*.spec.ts', '**/node_modules/**'],
      clean: true,
      cleanOnRerun: true,
    }
  }
});
