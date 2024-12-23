import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { UserAuthService } from '../_service/user-auth.service';
import { UsersService } from '../_service/users.service';

interface LoginData {
  username: string;
  password: string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginData: LoginData = {
    username: '',
    password: ''
  };

  isLoading = false;
  errorMessage = '';
  isDarkMode = false;
  showPassword = false;

  constructor(
    private userService: UsersService,
    private userAuthService: UserAuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Check for saved theme preference
    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme === 'dark';
    
    // Redirect if already logged in
    if (this.userAuthService.getToken()) {
      this.redirectBasedOnRole();
    }
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  login(loginForm: NgForm): void {
    if (loginForm.invalid) return;
    
    this.isLoading = true;
    this.errorMessage = '';

    this.userService.login(loginForm.value).subscribe({
      next: (response: any) => {
        this.userAuthService.setRoles(response.user.role);
        this.userAuthService.setToken(response.jwtToken);
        this.userAuthService.setUserId(response.user.userId);
        this.userAuthService.setName(response.user.name);
        this.redirectBasedOnRole();
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Login failed. Please try again.';
        console.error('Login error:', error);
      }
    });
  }

  private redirectBasedOnRole(): void {
    const roles = this.userAuthService.getRoles();
    const isAdmin = roles?.some(role => role === 'Admin');
    this.router.navigate([isAdmin ? '/books' : '/borrow-book']);
  }
}