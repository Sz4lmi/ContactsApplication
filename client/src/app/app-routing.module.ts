import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {ContactlistComponent} from './components/contactlist/contactlist.component';
import {NewcontactformComponent} from './components/newcontactform/newcontactform.component';
import {LoginComponent} from './components/login/login.component';

const routes: Routes = [
  {path: "", pathMatch: "full", redirectTo: "login"},
  {path: "contacts", component: ContactlistComponent},
  {path: "new-contact", component: NewcontactformComponent},
  {path: "login", component: LoginComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
