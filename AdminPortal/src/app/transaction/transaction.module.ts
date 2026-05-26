import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpModule } from '@angular/http';
import { TransactionRoutingModule } from './transaction-routing.module';
import { PrimaryTransactionComponent } from './primary-transaction/primary-transaction.component';
import { SavingsTransactionComponent } from './savings-transaction/savings-transaction.component';
import { TransactionService } from './transaction.service';

@NgModule({
  declarations: [PrimaryTransactionComponent, SavingsTransactionComponent],
  imports: [CommonModule, HttpModule, TransactionRoutingModule],
  providers: [TransactionService]
})
export class TransactionModule { }
