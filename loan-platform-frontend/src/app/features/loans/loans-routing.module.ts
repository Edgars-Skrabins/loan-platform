import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoanListComponent } from './pages/loan-list/loan-list.component';
import { LoanApplicationComponent } from './pages/loan-application/loan-application.component';
import { LoanDetailComponent } from './pages/loan-detail/loan-detail.component';

const routes: Routes = [
  { path: '', component: LoanListComponent },
  { path: 'apply', component: LoanApplicationComponent },
  { path: ':id', component: LoanDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class LoansRoutingModule { }
