import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { appConfig } from './app/config/app.config';

/**
 * @author Peter Szrnka
 */
export async function runApp() {
  try {
    await bootstrapApplication(App, appConfig);
  } catch (err) {
    console.error(err);
  }
}

runApp();