import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import { Transaction } from '../models';
import { UserService } from '../user.service';

@Component({
  selector: 'app-primary-transaction',
  imports: [DatePipe],
  templateUrl: './primary-transaction.component.html',
  styleUrl: './primary-transaction.component.css',
})
export class PrimaryTransactionComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);

  protected readonly primaryTransactionList = toSignal(
    this.route.paramMap.pipe(
      map((params) => params.get('username') ?? ''),
      switchMap((username) =>
        this.userService.getPrimaryTransactionList(username).pipe(
          catchError((err) => {
            console.log(err);
            return of<Transaction[]>([]);
          }),
        ),
      ),
    ),
    { initialValue: [] as Transaction[] },
  );
}
