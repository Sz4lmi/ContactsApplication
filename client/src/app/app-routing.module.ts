import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ContactlistComponent} from './components/contactlist/contactlist.component';
import {NewcontactformComponent} from './components/newcontactform/newcontactform.component';
import {NewuserformComponent} from './components/newuserform/newuserform.component';
import {LoginComponent} from './components/login/login.component';
import {UserlistComponent} from './components/userlist/userlist.component';

const routes: Routes = [
  {path: "", pathMatch: "full", redirectTo: "login"},
  {path: "contacts", component: ContactlistComponent},
  {path: "new-contact", component: NewcontactformComponent},
  {path: "new-user", component: NewuserformComponent},
  {path: "users", component: UserlistComponent},
  {path: "login", component: LoginComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
