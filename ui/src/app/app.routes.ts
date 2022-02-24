import { Routes } from '@angular/router';
import {EditDocComponent} from './edit-doc/edit-doc.component';
import {NewDocComponent} from './new-doc/new-doc.component';
import {ListDocComponent} from './list-doc/list-doc.component';
import {AuthGuard} from './services/auth-guard.services';
import {HomeComponent} from './home/home.component';

export const routerConfig: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'list', component: ListDocComponent, canActivate: [AuthGuard] },
  { path: 'new', component: NewDocComponent, canActivate: [AuthGuard] },
  { path: 'edit/:id', component: EditDocComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/home', pathMatch: 'full' }
];

