import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

interface BankHolidayEvent {
  title: string;
  date: string;
  notes: string;
  bunting: boolean;
}

interface DivisionData {
  division: string;
  events: BankHolidayEvent[];
}

@Injectable({
  providedIn: 'root'
})
export class PublicApiService {
  private readonly apiUrl = `${environment.apiUrl}/public`;

  constructor(private readonly http: HttpClient) { }

  getDisneyCharacters(): Observable<any> {
    return this.http.get(`${this.apiUrl}/disney-characters`);
  }

  getDigitalOceanStatus(): Observable<any> {
    return this.http.get(`${this.apiUrl}/digital-ocean-status`);
  }

  getBankHolidays(): Observable<{ [key: string]: DivisionData }> {
    return this.http.get<{ [key: string]: DivisionData }>(`${this.apiUrl}/bank-holidays`);
  }
}
