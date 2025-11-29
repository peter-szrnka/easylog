import { Routes } from '@angular/router';
import { LogViewerComponent } from '../log-viewer/log-viewer.component';

/**
 * @author Peter Szrnka
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: 'logs',
    pathMatch: 'full',
  },
  {
    path: 'logs',
    component: LogViewerComponent,
  },
];
