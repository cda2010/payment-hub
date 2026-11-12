import { Component } from '@angular/core';
import { FormDesignerPageComponent as RfFormDesignerPage } from '@rapidframe/formio-designer';

@Component({
  selector: 'app-admin-form-designer-page',
  standalone: true,
  imports: [RfFormDesignerPage],
  template: `<rf-form-designer-page />`,
  styles: `
    :host {
      display: block;
      min-height: calc(100vh - 6rem);
    }
  `,
})
export class AdminFormDesignerPageComponent {}
