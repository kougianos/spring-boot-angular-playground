import { Component, OnInit } from '@angular/core';
import { MongodbService, TestDocument } from '../core/services/mongodb.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-mongodb',
  templateUrl: './mongodb.component.html',
  styleUrls: ['./mongodb.component.scss']
})
export class MongodbComponent implements OnInit {
  documents: TestDocument[] = [];
  isLoading = false;
  displayedColumns: string[] = ['id', 'creator', 'textField', 'numberField', 'booleanFlag', 'dateCreated', 'dateUpdated'];

  constructor(
    private readonly mongodbService: MongodbService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadTestDocuments();
  }

  loadTestDocuments(): void {
    this.isLoading = true;
    this.mongodbService.getAllTestDocuments().subscribe({
      next: (data) => {
        this.documents = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error fetching documents:', error);
        this.snackBar.open('Error loading documents', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }
}
