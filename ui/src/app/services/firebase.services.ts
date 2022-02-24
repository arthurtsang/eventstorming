import {Injectable} from '@angular/core';
import {AngularFireDatabase, DatabaseSnapshot} from '@angular/fire/database';
import Reference = firebase.database.Reference;
import {Observable} from 'rxjs';
import * as firebase from 'firebase';
import {AngularFireAuth} from '@angular/fire/auth';

@Injectable({
  providedIn: 'root'
})
export class FirebaseService {
  firebaseReference: Reference;

  constructor(public db: AngularFireDatabase,
              public afAuth: AngularFireAuth) {
    this.firebaseReference = db.database.ref('docs');
  }

  createDoc(uid: string, name: string, description: string): Promise<any> {
    return this.firebaseReference.push({
      owner: uid,
      name: name,
      description: description
    });
  }

  listDocs(): Observable<any> {
    return Observable.create(observer => {
      this.firebaseReference.once('value', (snapshot) => {
        snapshot.forEach((childSnapshot) => {
          const doc = childSnapshot.val();
          doc.key = childSnapshot.key;
          observer.next(doc);
        });
        observer.complete();
      });
    });
  }

  getDoc(key: string): Promise<any> {
    return new Promise<any>(resolve =>
      this.firebaseReference.child(key).on('value', (snapshot) => resolve(snapshot.val()))
    );
  }

  doGoogleLogin() {
    return new Promise<any>((resolve, reject) => {
      const provider = new firebase.auth.GoogleAuthProvider();
      provider.addScope('profile');
      provider.addScope('email');
      this.afAuth.auth
        .signInWithPopup(provider)
        .then(res => {
          resolve(res.user);
        }, err => {
          console.log(err);
          reject(err);
        });
    });
  }
}
