import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * @author Peter Szrnka
 */
@Component({
  standalone: true,
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html'
})
export class App {
}
