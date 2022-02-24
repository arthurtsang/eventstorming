import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { UserService } from './user.services';


@Injectable()
export class AuthGuard implements CanActivate {

  constructor(
    public userService: UserService,
    private router: Router
  ) {}

  canActivate(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      this.userService.getCurrentUser()
        .then(user => {
          console.log( user.displayName );
          console.log( user.email );
          return resolve(true);
        }, err => {
          console.log( 'error in auth guard' );
          console.log( err );
          return resolve(false);
        });
    });
  }
}
