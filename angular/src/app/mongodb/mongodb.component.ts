import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MongodbService, TestDocument, TestDocumentRequest } from '../core/services/mongodb.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-mongodb',
  templateUrl: './mongodb.component.html',
  styleUrls: ['./mongodb.component.scss']
})
export class MongodbComponent implements OnInit {
  documents: TestDocument[] = [];
  isLoading = false;
  displayedColumns: string[] = ['id', 'creator', 'textField', 'numberField', 'booleanFlag',
    'dateCreated', 'dateUpdated', 'actions'];
  
  documentForm: FormGroup;
  isEditing = false;
  currentDocumentId: string | null = null;
  showForm = false;
  
  // Info section collapsible state
  infoSectionExpanded = false;

  constructor(
    private readonly mongodbService: MongodbService,
    private readonly snackBar: MatSnackBar,
    private readonly formBuilder: FormBuilder,
    private readonly dialog: MatDialog
  ) {
    this.documentForm = this.formBuilder.group({
      textField: ['', [Validators.required]],
      numberField: [0, [Validators.required, Validators.min(0)]],
      booleanFlag: [true]
    });
  }

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

  openCreateForm(): void {
    this.isEditing = false;
    this.currentDocumentId = null;
    this.documentForm.reset({
      textField: '',
      numberField: 0,
      booleanFlag: true
    });
    this.showForm = true;
  }

  openEditForm(document: TestDocument): void {
    this.isEditing = true;
    this.currentDocumentId = document.id;
    this.documentForm.setValue({
      textField: document.textField,
      numberField: document.numberField,
      booleanFlag: document.booleanFlag
    });
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.documentForm.reset();
    this.currentDocumentId = null;
    this.isEditing = false;
  }

  saveDocument(): void {
    if (this.documentForm.invalid) {
      return;
    }

    const documentRequest: TestDocumentRequest = {
      textField: this.documentForm.value.textField,
      numberField: this.documentForm.value.numberField,
      booleanFlag: this.documentForm.value.booleanFlag
    };

    this.isLoading = true;

    if (this.isEditing && this.currentDocumentId) {
      // Update existing document
      this.mongodbService.updateTestDocument(this.currentDocumentId, documentRequest).subscribe({
        next: () => {
          this.snackBar.open('Document updated successfully', 'Close', { duration: 3000 });
          this.loadTestDocuments();
          this.cancelForm();
        },
        error: (error) => {
          console.error('Error updating document:', error);
          this.snackBar.open('Error updating document', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    } else {
      // Create new document
      this.mongodbService.createTestDocument(documentRequest).subscribe({
        next: () => {
          this.snackBar.open('Document created successfully', 'Close', { duration: 3000 });
          this.loadTestDocuments();
          this.cancelForm();
        },
        error: (error) => {
          console.error('Error creating document:', error);
          this.snackBar.open('Error creating document', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    }
  }

  deleteDocument(id: string): void {
    if (confirm('Are you sure you want to delete this document?')) {
      this.isLoading = true;
      this.mongodbService.deleteTestDocument(id).subscribe({
        next: () => {
          this.snackBar.open('Document deleted successfully', 'Close', { duration: 3000 });
          this.loadTestDocuments();
        },
        error: (error) => {
          console.error('Error deleting document:', error);
          this.snackBar.open('Error deleting document', 'Close', { duration: 3000 });
          this.isLoading = false;
        }
      });
    }
  }

  toggleInfoSection(): void {
    this.infoSectionExpanded = !this.infoSectionExpanded;
  }
}
