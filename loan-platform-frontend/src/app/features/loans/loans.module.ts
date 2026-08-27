import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';

import { LoansRoutingModule } from './loans-routing.module';
import { LoanListComponent } from './pages/loan-list/loan-list.component';
import { LoanApplicationComponent } from './pages/loan-application/loan-application.component';
import { LoanDetailComponent } from './pages/loan-detail/loan-detail.component';
import { LoanStatusPipe } from './pipes/loan-status.pipe';

@NgModule({
  declarations: [
    LoanListComponent,
    LoanApplicationComponent,
    LoanDetailComponent,
    LoanStatusPipe
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    LoansRoutingModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
  ]
})
export class LoansModule { }
