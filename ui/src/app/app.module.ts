import {BrowserModule} from '@angular/platform-browser';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {AceEditorModule} from 'ng2-ace-editor';
import {AngularFireModule, FirebaseNameOrConfigToken, FirebaseOptionsToken} from '@angular/fire';
import {AngularFireDatabaseModule} from '@angular/fire/database';
import {AngularFireAuthModule} from '@angular/fire/auth';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {environment} from '../environments/environment';
import {AppComponent} from './app.component';
import {FirebaseService} from './services/firebase.services';
import {NewDocComponent} from './new-doc/new-doc.component';
import {ListDocComponent} from './list-doc/list-doc.component';
import {EditDocComponent} from './edit-doc/edit-doc.component';
import {DeleteDocComponent} from './delete-doc/delete-doc.component';

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule, MatInputModule, MatDialogModule} from '@angular/material';
import {routerConfig} from './app.routes';
import {UserService} from './services/user.services';
import {AuthGuard} from './services/auth-guard.services';
import {HomeComponent} from './home/home.component';

@NgModule({
  declarations: [
    AppComponent,
    NewDocComponent,
    EditDocComponent,
    ListDocComponent,
    DeleteDocComponent,
    HomeComponent
  ],
  entryComponents: [ListDocComponent, DeleteDocComponent],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule.forRoot(routerConfig, { useHash: false, onSameUrlNavigation: 'reload' }),
    AceEditorModule,
    AngularFireModule,
    AngularFireAuthModule,
    AngularFireDatabaseModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatInputModule,
    MatDialogModule
  ],
  providers: [
    FirebaseService, UserService, AuthGuard,
    { provide: FirebaseOptionsToken, useValue: environment.firebase },
    { provide: FirebaseNameOrConfigToken, useValue: '[DEFAULT]' },
  ],
  bootstrap: [AppComponent],
  schemas: [
    CUSTOM_ELEMENTS_SCHEMA
  ]
})
export class AppModule {
}
