import { Component, OnInit, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {UserService} from '../user.service';
import { ActivatedRoute, Params } from '@angular/router';
import { DatePipe } from '@angular/common';


@Component({
    selector: 'app-savings-transaction',
    templateUrl: './savings-transaction.component.html',
    styleUrls: ['./savings-transaction.component.css'],
    imports: [DatePipe]
})
export class SavingsTransactionComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private userService = inject(UserService);
  private username = toSignal(this.route.params.pipe(map((params: Params) => params['username'] as string)), { requireSync: true });
  savingsTransactionList = toSignal(
    this.userService.getSavingsTransactionList(this.username()).pipe(
      map(res => res as any[]),
      catchError(error => { console.log(error); return of([]); })
    ),
    { initialValue: [] as any[] }
  );


	ngOnInit() {}
}
