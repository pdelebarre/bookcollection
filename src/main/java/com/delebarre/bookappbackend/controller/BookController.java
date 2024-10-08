package com.delebarre.bookappbackend.controller;

import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//test
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;


    @CrossOrigin(origins = "*")
    @GetMapping("/all")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<Book> getBookById(@RequestParam String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @CrossOrigin(origins = "*")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createBook(
            @RequestParam(required = true) String olid) {
        try {
            Book book;
            book = bookService.createBook(olid);

            return ResponseEntity.status(HttpStatus.CREATED).body(book);
        } catch (BookAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error creating book");
        }
    }

    @CrossOrigin(origins = "*")
    @PutMapping
    public ResponseEntity<Book> updateBook(@RequestParam String id, @RequestBody Book book) {
        Book updatedBook = bookService.updateBook(id, book);
        return ResponseEntity.ok(updatedBook);
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping
    public ResponseEntity<?> deleteBook(@RequestParam String id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = "*")
    @DeleteMapping("/all")
    public void deleteBook() {
        bookService.deleteAll();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn) {
        return bookService.searchBooks(title, author, isbn);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/searchCover")
    public ResponseEntity<Optional<byte[]>> searchCover(
            @RequestParam(required = true) String olid) {
        Optional<byte[]> cover = bookService.searchCover(olid);
        return ResponseEntity.ok(cover);
    }

}
