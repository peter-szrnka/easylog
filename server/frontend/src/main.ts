import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { appConfig } from './app/config/app.config';

/**
 * @author Peter Szrnka
 */
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
