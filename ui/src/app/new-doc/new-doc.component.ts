import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Router} from '@angular/router';
import {FirebaseService} from '../services/firebase.services';
import {UserService} from '../services/user.services';

@Component({
  selector: 'app-new-doc',
  templateUrl: './new-doc.component.html',
  styleUrls: ['./new-doc.component.scss']
})
export class NewDocComponent implements OnInit {
  newDocForm: FormGroup;
  validation_messages = {
    'name': [
      { type: 'required', message: 'Name is required.' }
    ],
    'description': [
      { type: 'required', message: 'Description is required.' }
    ]
  };

  constructor(
    private fb: FormBuilder,
    private router: Router,
    public firebaseService: FirebaseService,
    public userService: UserService
  ) { }

  ngOnInit() {
    this.newDocForm = this.fb.group({
      name: ['', Validators.required ],
      description: ['', Validators.required ]
    });
  }

  resetFields() {
    this.newDocForm = this.fb.group({
      name: new FormControl('', Validators.required),
      surname: new FormControl('', Validators.required),
      age: new FormControl('', Validators.required),
    });
  }

  onSubmit(value) {
    this.userService.getCurrentUser()
      .then( user => {
        console.log( 'creating doc with owner ' + user.uid);
          this.firebaseService.createDoc(user.uid, value.name, value.description)
            .then(
              res => {
                console.log('ref for new doc is ' + res);
                this.resetFields();
                this.router.navigate(['/list']);
              }
            );
        }
      );
  }
}
