import { defineConfig } from 'vitest/config';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  resolve: {
    mainFields: ['module'],
  },
  plugins: [angular({ jit: true })],
  test: {
    globals: true,
    environment: 'jsdom',
    reporters: [
      'default',
      ['junit', { outputFile: './target/site/jacoco.xml', suiteName: 'Angular Tests' }]
    ],
    coverage: {
      provider: 'istanbul',
      reporter: ['text', 'lcov', 'clover'],
      reportsDirectory: './target/coverage',
      include: ['src/**/*.ts'],
      exclude: ['**/*.spec.ts', '**/node_modules/**'],
      clean: true,
      cleanOnRerun: true,
    }
  }
});
