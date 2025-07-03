import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { HomeComponent } from './home/home.component';
import { AuthGuard } from './core/guards/auth.guard';
import { MongodbComponent } from './mongodb/mongodb.component';
import { PublicApisComponent } from './public-apis/public-apis.component';
import { WebsocketComponent } from './websocket/websocket.component';
import { CacheAnalyticsComponent } from './cache-analytics/cache-analytics.component';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'mongodb', component: MongodbComponent, canActivate: [AuthGuard] },
  { path: 'public-apis', component: PublicApisComponent, canActivate: [AuthGuard] },
  { path: 'websocket', component: WebsocketComponent, canActivate: [AuthGuard] },
  { path: 'cache-analytics', component: CacheAnalyticsComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
