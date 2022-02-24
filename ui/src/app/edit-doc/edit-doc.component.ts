import {AfterViewInit, ChangeDetectorRef, Component, isDevMode, OnInit, ViewChild} from '@angular/core';
import {AceEditorComponent} from 'ng2-ace-editor';
import {ActivatedRoute} from '@angular/router';
import {AngularFireDatabase} from '@angular/fire/database';
import {HttpClient} from '@angular/common/http';
import * as Firepad from 'firepad';
import {FirebaseService} from '../services/firebase.services';
import {UserService} from '../services/user.services';

@Component({
  selector: 'app-edit-doc',
  templateUrl: './edit-doc.component.html',
  styleUrls: ['./edit-doc.component.scss']
})
export class EditDocComponent implements OnInit, AfterViewInit {

  constructor(private activatedRoute: ActivatedRoute,
              private db: AngularFireDatabase,
              private fb: FirebaseService,
              private usr: UserService,
              private http: HttpClient,
              private changeDetection: ChangeDetectorRef
  ) {}

  docName = '';
  emlTextValue = '';
  emlPng = '';
  encoded = '';
  options: any = {maxLines: 5000, printMargin: false};
  users = {};

  @ViewChild('firepad') ace: AceEditorComponent;

  ngAfterViewInit() {
    this.ace.setTheme('eclipse');
    this.ace.setMode('java');
    this.ace.setOptions(this.options);
    this.ace.setReadOnly(false);
    this.ace.setAutoUpdateContent(true);
  }

  ngOnInit(): void {
    const docKey = this.activatedRoute.snapshot.paramMap.get('id');
    this.fb.getDoc(docKey).then( (doc) => this.docName = doc.name );
    const editor = this.ace.getEditor();
    const firebaseRef = this.db.database.ref('docs/' + docKey);
    console.log('firebase ref ' + firebaseRef);
    this.usr.getCurrentUser().then( user => {
        const firepad = Firepad.fromACE(firebaseRef, editor, { userId: user.uid + '-' + user.displayName });
        firepad.on('ready', () => {
          console.log('firepad is ready');
          console.log('---' + this.emlTextValue + '---');
          if (this.emlTextValue.length === 0) {
            this.emlTextValue = 'command ->\nevent';
            this.onSubmit();
          }
        });
      }
    );
    const userListRef = this.db.database.ref('docs/' + docKey + '/users');
    userListRef.once( 'value', (snapshot) => {
      snapshot.forEach((childSnapshot => {
        const userDoc = childSnapshot.val();
        const userId = childSnapshot.key;
        console.log( 'adding ' + userId + ' with color ' + userDoc.color );
        this.users[userId] = userDoc.color;
      }));
    });
    userListRef.on( 'child_added', (userSnapshot, prevChildName) => {
      const userId = userSnapshot.key;
      const color = userSnapshot.child('color').val();
      console.log( 'updating ' + userId + ' with color ' + color );
      this.users[userId] = color;
      this.changeDetection.detectChanges();
    });
/*    userListRef.on( 'child_changed', this.updateChild);
    userListRef.on( 'child_moved', this.updateChild);*/
    userListRef.on( 'child_removed', removedSnapshot => {
      const userId = removedSnapshot.key;
      delete this.users[userId];
      this.changeDetection.detectChanges();
    });
  }

  onSubmit() {
    const url = (isDevMode()) ? 'http://localhost:8081/webeventstorming/encode' : '../encode';
    const pngUrlBase = (isDevMode()) ? 'http://localhost:8081/webeventstorming/png/' : '../png/';
    this.http.post(url, this.emlTextValue.trim(), {responseType: 'text'})
      .subscribe(encoded => {
        this.emlPng = pngUrlBase + encoded;
      });
  }
}
