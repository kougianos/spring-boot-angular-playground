import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface TestDocument {
  id: string;
  dateCreated: string;
  dateUpdated: string;
  creator: string;
  booleanFlag: boolean;
  textField: string;
  numberField: number;
}

export interface TestDocumentRequest {
  booleanFlag: boolean;
  textField: string;
  numberField: number;
}

@Injectable({
  providedIn: 'root'
})
export class MongodbService {
  private readonly apiUrl = `${environment.apiUrl}/test-documents`;

  constructor(private readonly http: HttpClient) { }

  getAllTestDocuments(): Observable<TestDocument[]> {
    return this.http.get<TestDocument[]>(this.apiUrl);
  }

  createTestDocument(document: TestDocumentRequest): Observable<TestDocument> {
    return this.http.post<TestDocument>(this.apiUrl, document);
  }
  
  updateTestDocument(id: string, document: TestDocumentRequest): Observable<TestDocument> {
    return this.http.put<TestDocument>(`${this.apiUrl}/${id}`, document);
  }
  
  deleteTestDocument(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
