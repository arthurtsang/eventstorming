import {Component, Inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {HttpClient} from '@angular/common/http';
import {FirebaseService} from '../services/firebase.services';
import {AngularFireDatabase} from '@angular/fire/database';
import {Router} from '@angular/router';
import {ListDocComponent} from '../list-doc/list-doc.component';

@Component({
  selector: 'app-delete-doc',
  templateUrl: './delete-doc.component.html',
  styleUrls: ['./delete-doc.component.scss']
})
export class DeleteDocComponent implements OnInit {

  form: FormGroup;
  doc: any;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<DeleteDocComponent>,
    @Inject(MAT_DIALOG_DATA) data,
    private db: AngularFireDatabase,
) {
  this.doc = data.doc;
  }
  ngOnInit(): void {
    this.form = this.formBuilder.group( {
      doc: [ this.doc, []],
    });
  }

  cancel() {
    this.dialogRef.close();
  }

  delete() {
    console.log('delete doc');
    const firebaseRef = this.db.database.ref('docs/' + this.doc.key);
    firebaseRef.remove(onComplete => this.dialogRef.close() ).then(() => {console.log( 'deleted doc'); });
  }

}
