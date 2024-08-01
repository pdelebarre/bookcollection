package com.delebarre.bookappbackend.service;

import com.delebarre.bookappbackend.dto.BookCreateRequest;
import com.delebarre.bookappbackend.exception.BookAlreadyExistsException;
import com.delebarre.bookappbackend.exception.BookNotFoundException;
import com.delebarre.bookappbackend.model.Book;
import com.delebarre.bookappbackend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;


    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json";

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
    }

    @Override
    public Book createBook(BookCreateRequest bookCreateRequest) {
        // Check if a book with the same title and author already exists
        if (bookRepository.existsByTitleAndAuthor(bookCreateRequest.getTitle(), bookCreateRequest.getAuthor())) {
            throw new BookAlreadyExistsException("A book with the same title and author already exists");
        }

        Book book = new Book();
        book.setTitle(bookCreateRequest.getTitle());
        book.setAuthor(bookCreateRequest.getAuthor());

        // Fetch additional metadata from Open Library API
        String encodedTitle = URLEncoder.encode(bookCreateRequest.getTitle(), StandardCharsets.UTF_8);
        String encodedAuthor = URLEncoder.encode(bookCreateRequest.getAuthor(), StandardCharsets.UTF_8);
        String searchUrl = String.format("https://openlibrary.org/search.json?title=%s&author=%s", encodedTitle,
                encodedAuthor);

        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        String responseBody = response.getBody();

        if (responseBody != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode docs = rootNode.path("docs");

                if (docs.isArray() && docs.size() > 0) {
                    JsonNode firstBook = docs.get(0);

                    // Set cover image
                    String coverId = firstBook.path("cover_i").asText();
                    if (!coverId.isEmpty()) {
                        String coverUrl = String.format("https://covers.openlibrary.org/b/id/%s-L.jpg", coverId);
                        byte[] coverImage = restTemplate.getForObject(coverUrl, byte[].class);
                        book.setCoverImage(coverImage);
                    }

                    // Set other metadata fields
                    book.setGenre(firstBook.path("subject").asText());
                    book.setISBN(firstBook.path("isbn").get(0).asText());
                    book.setPublicationDate(firstBook.path("first_publish_year").asText());
                    book.setDescription(firstBook.path("subtitle").asText());
                    book.setPublisher(firstBook.path("publisher").get(0).asText());
                    book.setLanguage(firstBook.path("language").get(0).asText());
                    book.setPageCount(firstBook.path("number_of_pages_median").asInt());
                    book.setFormat(firstBook.path("format").asText());
                    book.setSubjects(objectMapper.convertValue(firstBook.path("subject"), List.class));
                    book.setOpenLibraryId(firstBook.path("key").asText());
                    book.setContributors(objectMapper.convertValue(firstBook.path("author_name"), List.class));
                }
            } catch (JsonProcessingException e) {
                // Handle JSON parsing exception
                e.printStackTrace();
            }
        }

        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(String id, Book book) {
        Book existingBook = getBookById(id);
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setCoverImage(book.getCoverImage());
        existingBook.setGenre(book.getGenre());
        existingBook.setISBN(book.getISBN());
        existingBook.setPublicationDate(book.getPublicationDate());
        existingBook.setDescription(book.getDescription());
        existingBook.setPublisher(book.getPublisher());
        existingBook.setLanguage(book.getLanguage());
        existingBook.setPageCount(book.getPageCount());
        existingBook.setFormat(book.getFormat());
        existingBook.setSubjects(book.getSubjects());
        existingBook.setOpenLibraryId(book.getOpenLibraryId());
        existingBook.setContributors(book.getContributors());

        return bookRepository.save(existingBook);
    }

    @Override
    public void deleteBook(String id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> searchBooks(String title, String author) {

        String searchUrl = OPEN_LIBRARY_API + "?title=" + title + "&author=" + author;
        ResponseEntity<String> response = restTemplate.getForEntity(searchUrl, String.class);
        String responseBody = response.getBody();

        List<Book> books = new ArrayList<>();
        if (responseBody != null) {
                        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode docs = rootNode.path("docs");

            for (JsonNode doc : docs) {
                Book book = new Book();
                    
if (doc.has("title")) {
    book.setTitle(doc.path("title").asText());
}
if (doc.has("author_name") && doc.path("author_name").isArray() && !doc.path("author_name").isEmpty()) {
    book.setAuthor(doc.path("author_name").get(0).asText());
}
if (doc.has("subject")) {
    book.setGenre(doc.path("subject").asText());
}
if (doc.has("isbn") && doc.path("isbn").isArray() && !doc.path("isbn").isEmpty()) {
    book.setISBN(doc.path("isbn").get(0).asText());
}
if (doc.has("first_publish_year")) {
    book.setPublicationDate(doc.path("first_publish_year").asText());
}
if (doc.has("subtitle")) {
    book.setDescription(doc.path("subtitle").asText());
}
if (doc.has("publisher") && doc.path("publisher").isArray() && !doc.path("publisher").isEmpty()) {
    book.setPublisher(doc.path("publisher").get(0).asText());
}
if (doc.has("language") && doc.path("language").isArray() && !doc.path("language").isEmpty()) {
    book.setLanguage(doc.path("language").get(0).asText());
}
if (doc.has("number_of_pages_median")) {
    book.setPageCount(doc.path("number_of_pages_median").asInt());
}
if (doc.has("format")) {
    book.setFormat(doc.path("format").asText());
}
if (doc.has("subject")) {
    book.setSubjects(objectMapper.convertValue(doc.path("subject"), List.class));
}
if (doc.has("key")) {
    book.setOpenLibraryId(doc.path("key").asText());
}
if (doc.has("author_name") && doc.path("author_name").isArray()) {
    book.setContributors(objectMapper.convertValue(doc.path("author_name"), List.class));
}
                books.add(book);
            }
            } catch (JsonProcessingException e) {
            // Handle JSON parsing exception
            e.printStackTrace();
        }
        }
        return books;
    }
}
