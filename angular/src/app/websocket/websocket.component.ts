import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../core/services/auth.service';
import { WebSocketService } from './websocket.service';

export interface ChatMessage {
  id?: string;
  type: 'CHAT' | 'JOIN' | 'LEAVE';
  content: string;
  sender: string;
  timestamp: number;
}

export interface User {
  username: string;
  connected: boolean;
  lastSeen?: number;
}

@Component({
  selector: 'app-websocket',
  templateUrl: './websocket.component.html',
  styleUrls: ['./websocket.component.scss']
})
export class WebsocketComponent implements OnInit, OnDestroy {
  infoSectionExpanded = false;
  messages: ChatMessage[] = [];
  users: User[] = [];
  messageInput = '';
  currentUser: string = '';
  isConnected = false;

  constructor(
    private readonly authService: AuthService,
    private readonly webSocketService: WebSocketService
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.getUsername() ?? 'Anonymous';
    this.connectToWebSocket();
  }

  ngOnDestroy() {
    this.webSocketService.disconnect();
  }

  toggleInfoSection() {
    this.infoSectionExpanded = !this.infoSectionExpanded;
  }

  connectToWebSocket() {
    this.webSocketService.connect().subscribe({
      next: (connected: boolean) => {
        this.isConnected = connected;
        if (connected) {
          this.subscribeToMessages();
          this.subscribeToUsers();
          this.addUser();
        }
      }
    });
  }

  private subscribeToMessages() {
    this.webSocketService.getMessages().subscribe((message: ChatMessage) => {
      this.messages.push(message);
      this.scrollToBottom();
    });
  }

  private subscribeToUsers() {
    this.webSocketService.getConnectedUsers().subscribe((connectedUsers: string[]) => {
      // Update existing users or add new ones
      connectedUsers.forEach(username => {
        const existingUser = this.users.find(u => u.username === username);
        if (existingUser) {
          existingUser.connected = true;
        } else {
          this.users.push({ username, connected: true });
        }
      });

      // Mark users as disconnected if they're not in the connected list
      this.users.forEach(user => {
        if (!connectedUsers.includes(user.username)) {
          user.connected = false;
          user.lastSeen = Date.now();
        }
      });
    });
  }

  private addUser() {
    const message: ChatMessage = {
      type: 'JOIN',
      content: '',
      sender: this.currentUser,
      timestamp: Date.now()
    };
    this.webSocketService.addUser(message);
  }

  sendMessage() {
    if (this.messageInput.trim() && this.isConnected) {
      const message: ChatMessage = {
        type: 'CHAT',
        content: this.messageInput.trim(),
        sender: this.currentUser,
        timestamp: Date.now()
      };
      
      this.webSocketService.sendMessage(message);
      this.messageInput = '';
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom() {
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container');
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    });
  }

  getConnectedUsers() {
    return this.users.filter(user => user.connected);
  }

  getDisconnectedUsers() {
    return this.users.filter(user => !user.connected);
  }

  formatTimestamp(timestamp: number): string {
    return new Date(timestamp).toLocaleTimeString();
  }

  isSystemMessage(message: ChatMessage): boolean {
    return message.type === 'JOIN' || message.type === 'LEAVE';
  }

  isOwnMessage(message: ChatMessage): boolean {
    return message.sender === this.currentUser && message.type === 'CHAT';
  }
}
