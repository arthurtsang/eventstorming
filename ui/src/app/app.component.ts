import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {FirebaseService} from './services/firebase.services';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent implements OnInit {
  title: 'WebEventStorming';

  constructor ( private router: Router,
                private fb: FirebaseService
                ) {}

  ngOnInit() {
    console.log( 'init app component' );
    this.fb.doGoogleLogin().then( (user) => {
      if ( user ) {
        console.log( 'Anonymous user: ' + user.isAnonymous + ' with uid: ' + user.uid );
      } else {
        console.log( 'no user' );
      }
    });
  }
}
