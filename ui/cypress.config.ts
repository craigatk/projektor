import { defineConfig } from 'cypress'

export default defineConfig({
  reporter: 'junit',
  reporterOptions: {
    mochaFile: 'test-results/cypress-[hash].xml',
  },
  videoUploadOnPasses: false,
  defaultCommandTimeout: 10000,
  e2e: {
    setupNodeEvents(on, config) {},
  },
})
