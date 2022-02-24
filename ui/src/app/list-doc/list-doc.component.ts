import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FirebaseService} from '../services/firebase.services';
import {Router} from '@angular/router';
import {toArray, take} from 'rxjs/operators';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {DeleteDocComponent} from '../delete-doc/delete-doc.component';
import {detectChangesInternal} from '@angular/core/src/render3/instructions';
import {a} from '@angular/core/src/render3';
import {UserService} from '../services/user.services';

@Component({
  selector: 'app-list-doc',
  templateUrl: './list-doc.component.html',
  styleUrls: ['./list-doc.component.scss'],
})
export class ListDocComponent implements OnInit {

  docs: Array<any> = [];

  constructor(
    public firebaseService: FirebaseService,
    private router: Router,
    private dialog: MatDialog,
    private userService: UserService
  ) {
  }

  ngOnInit() {
    console.log('init list doc page');
    this.userService.getCurrentUser().then(user => {
      this.firebaseService.listDocs().pipe(toArray()).subscribe((result) => {
        for (const doc of result) {
          doc.isOwner = (doc.owner === user.uid);
        }
        this.docs = result;
      });
    });
  }

  editDoc(doc) {
    this.router.navigate(['/edit/' + doc.key]);
  }

  deleteDoc(doc) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.disableClose = true;
    dialogConfig.autoFocus = true;
    dialogConfig.data = {
      doc: doc
    };
    this.dialog.open(DeleteDocComponent, dialogConfig);
    this.dialog.afterAllClosed.subscribe(() => {
      this.ngOnInit();
    });
  }
}
