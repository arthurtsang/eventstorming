import {Injectable} from '@angular/core';
import * as firebase from 'firebase/app';
import {Router} from '@angular/router';

@Injectable()
export class UserService {

  constructor(
    private router: Router
  ) {
  }
  getCurrentUser() {
    return new Promise<any>((resolve, reject) => {
      const _user = firebase.auth().onAuthStateChanged((user) => {
        if (user) {
          console.log( user.email );
          if ( user.email.endsWith('@roche.com') || user.email.endsWith('@gene.com') ) {
            resolve(user);
          } else {
            this.router.navigate( ['home']);
            reject( 'only roche email is allowed' );
          }
        } else {
          console.log( 'rejecting user' );
          this.router.navigate( ['home']);
          reject('No user logged in');
        }
      });
    });
  }
}
