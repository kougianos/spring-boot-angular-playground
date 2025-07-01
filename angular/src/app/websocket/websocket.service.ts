import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';

export interface ChatMessage {
  id?: string;
  type: 'CHAT' | 'JOIN' | 'LEAVE';
  content: string;
  sender: string;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private readonly client: Client;
  private readonly connected = new BehaviorSubject<boolean>(false);
  private readonly messages = new Subject<ChatMessage>();
  private readonly connectedUsers = new Subject<string[]>();
  private topicsSubscribed = false;

  constructor() {
    this.client = new Client({
      brokerURL: `ws://localhost:8080/websocket`,
      connectHeaders: {},
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.setupConnectionHandlers();
  }

  private setupConnectionHandlers() {
    this.client.onConnect = () => {
      console.log('Connected to WebSocket');
      this.connected.next(true);
      this.subscribeToTopics();
    };

    this.client.onDisconnect = () => {
      console.log('Disconnected from WebSocket');
      this.connected.next(false);
      this.topicsSubscribed = false; // Reset subscription flag on disconnect
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      this.connected.next(false);
      this.topicsSubscribed = false; // Reset subscription flag on error
    };

    this.client.onWebSocketError = (error) => {
      console.error('WebSocket error:', error);
      console.error('Make sure the Spring Boot application is running on http://localhost:8080');
      this.connected.next(false);
      this.topicsSubscribed = false; // Reset subscription flag on error
    };
  }

  private subscribeToTopics() {
    // Only subscribe if not already subscribed to avoid duplicates
    if (!this.topicsSubscribed) {
      // Subscribe to public messages
      this.client.subscribe('/topic/public', (message: Message) => {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        this.messages.next(chatMessage);
      });

      // Subscribe to connected users updates
      this.client.subscribe('/topic/users', (message: Message) => {
        const users: string[] = JSON.parse(message.body);
        this.connectedUsers.next(users);
      });

      this.topicsSubscribed = true;
    }
  }

  connect(): Observable<boolean> {
    if (!this.client.connected) {
      this.client.activate();
    }
    return this.connected.asObservable();
  }

  disconnect() {
    if (this.client.connected) {
      this.client.deactivate().then();
    }
  }

  sendMessage(message: ChatMessage) {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message)
      });
    }
  }

  addUser(message: ChatMessage) {
    if (this.client.connected) {
      this.client.publish({
        destination: '/app/chat.addUser',
        body: JSON.stringify(message)
      });
    }
  }

  getMessages(): Observable<ChatMessage> {
    return this.messages.asObservable();
  }

  getConnectedUsers(): Observable<string[]> {
    return this.connectedUsers.asObservable();
  }

}
