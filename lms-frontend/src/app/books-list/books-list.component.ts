import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Books } from '../_model/books';
import { BooksService } from '../_service/books.service';

interface SortConfig {
  column: keyof Books | null;  // Changed from empty string to null
  direction: 'asc' | 'desc';
}

@Component({
  selector: 'app-books-list',
  templateUrl: './books-list.component.html',
  styleUrls: ['./books-list.component.css']
})
export class BooksListComponent implements OnInit {
  books: Books[] = [];
  filteredBooks: Books[] = [];
  isLoading = false;
  searchTerm = '';
  selectedGenre = '';
  uniqueGenres: string[] = [];
  bookToDelete: Books | null = null;
  sortConfig: SortConfig = { column: null, direction: 'asc' };  // Initialize with null
  isDarkMode = false;

  constructor(
    private booksService: BooksService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadBooks();
    const savedTheme = localStorage.getItem('theme');
    this.isDarkMode = savedTheme === 'dark';
  }

  private loadBooks(): void {
    this.isLoading = true;
    this.booksService.getBooksList().subscribe({
      next: (data) => {
        this.books = data;
        this.filteredBooks = data;
        this.extractUniqueGenres();
        this.filterBooks();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading books:', error);
        this.isLoading = false;
      }
    });
  }

  private extractUniqueGenres(): void {
    this.uniqueGenres = [...new Set(this.books.map(book => book.bookGenre))];
  }

  filterBooks(): void {
    this.filteredBooks = this.books.filter(book => {
      const matchesSearch = 
        book.bookName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        book.bookAuthor.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        book.bookGenre.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesGenre = !this.selectedGenre || book.bookGenre === this.selectedGenre;
      
      return matchesSearch && matchesGenre;
    });

    if (this.sortConfig.column) {
      this.applySorting();
    }
  }

  sort(column: keyof Books): void {
    if (this.sortConfig.column === column) {
      this.sortConfig.direction = this.sortConfig.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortConfig.column = column;
      this.sortConfig.direction = 'asc';
    }
    this.applySorting();
  }

  private applySorting(): void {
    const { column, direction } = this.sortConfig;
    if (!column) return; // Skip sorting if no column is selected

    this.filteredBooks.sort((a, b) => {
      const valueA = a[column];
      const valueB = b[column];
      
      if (typeof valueA === 'string' && typeof valueB === 'string') {
        return direction === 'asc' 
          ? valueA.localeCompare(valueB)
          : valueB.localeCompare(valueA);
      }
      
      if (valueA < valueB) return direction === 'asc' ? -1 : 1;
      if (valueA > valueB) return direction === 'asc' ? 1 : -1;
      return 0;
    });
  }

  getSortIcon(column: string): string {
    if (this.sortConfig.column !== column) return 'fa-sort';
    return this.sortConfig.direction === 'asc' ? 'fa-sort-up' : 'fa-sort-down';
  }

  getGenreColor(genre: string): { backgroundColor: string } {
    const colors: { [key: string]: string } = {
      'Fiction': '#3498db',
      'Non-Fiction': '#2ecc71',
      'Science': '#9b59b6',
      'History': '#e67e22',
      'Biography': '#f1c40f'
    };
    return { backgroundColor: colors[genre] || '#95a5a6' };
  }

  getCopiesClass(copies: number): string {
    if (copies === 0) return 'text-danger fw-bold';
    if (copies < 3) return 'text-warning fw-bold';
    return 'text-success';
  }

  updateBook(bookId: number): void {
    this.router.navigate(['update-book', bookId]);
  }

  bookDetails(bookId: number): void {
    this.router.navigate(['book-details', bookId]);
  }


  confirmDelete(book: Books): void {
    this.bookToDelete = book;
  }

  confirmDeleteBook(): void {
    if (this.bookToDelete) {
      this.isLoading = true;
      this.booksService.deleteBook(this.bookToDelete.bookId).subscribe({
        next: () => {
          this.loadBooks();
          this.bookToDelete = null;
          // Optional: Show success message
          alert('Book deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting book:', error);
          this.isLoading = false;
          // Optional: Show error message
          alert('Error deleting book. Please try again.');
        }
      });
    }
  }
}