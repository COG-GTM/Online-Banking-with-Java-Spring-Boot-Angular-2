import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, startWith, switchMap } from 'rxjs';
import { Transaction } from '../models';
import { UserService } from '../user.service';

@Component({
  selector: 'app-savings-transaction',
  imports: [DatePipe],
  templateUrl: './savings-transaction.component.html',
  styleUrl: './savings-transaction.component.css',
})
export class SavingsTransactionComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);

  protected readonly savingsTransactionList = toSignal(
    this.route.paramMap.pipe(
      map((params) => params.get('username') ?? ''),
      switchMap((username) =>
        this.userService.getSavingsTransactionList(username).pipe(
          catchError((err) => {
            console.log(err);
            return of<Transaction[]>([]);
          }),
          startWith<Transaction[]>([]),
        ),
      ),
    ),
    { initialValue: [] as Transaction[] },
  );
}
