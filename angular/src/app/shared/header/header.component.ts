import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UserInfo } from '../../core/services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  @Input() currentUser: UserInfo | null = null;
  @Output() logoutEvent = new EventEmitter<void>();
  
  logout(): void {
    this.logoutEvent.emit();
  }
}
