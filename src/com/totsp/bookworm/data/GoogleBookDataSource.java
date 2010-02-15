package com.totsp.bookworm.data;

import android.util.Log;
import android.util.Xml;

import com.totsp.bookworm.Constants;
import com.totsp.bookworm.model.Book;
import com.totsp.bookworm.util.NetworkUtil;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class GoogleBookDataSource implements IBookDataSource {

   // web url http://books.google.com/books?isbn=
   private static final String GDATA_BOOK_URL_PREFIX = "http://books.google.com/books/feeds/volumes?q=isbn:";
   
   // google books uses X FORWARDED FOR header to determine location and what book stuff user can "see"
   private static final String X_FORWARDED_FOR = "X-Forwarded-For";

   private GoogleBooksHandler saxHandler;
   private HttpHelper httpHelper;

   public GoogleBookDataSource() {
      this.saxHandler = new GoogleBooksHandler();
      this.httpHelper = new HttpHelper();
   }  

   public Book getBook(String isbn) {
      // TODO validate isbn
      return this.getBookData(isbn);      
   }
   
   private Book getBookData(String isbn) {
      String url = GDATA_BOOK_URL_PREFIX + isbn;      
      HashMap<String, String> headers = new HashMap<String, String>();
      headers.put(X_FORWARDED_FOR, NetworkUtil.getIpAddress());
      String response = this.httpHelper.performGet(url, null, null, headers);
      Log.d(Constants.LOG_TAG, "HTTP response\n" + response);
      if (response == null || response.contains(HttpHelper.HTTP_RESPONSE_ERROR)) {
         return null; // TODO better error handling
      } 
      return this.parseResponse(response);        
   }   
   
   private Book parseResponse(String response) {
      Book book = null;
      try {
         Xml.parse(new ByteArrayInputStream(response.getBytes("UTF-8")), Xml.Encoding.UTF_8, this.saxHandler);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      ArrayList<Book> books = this.saxHandler.getBooks();
      if (books != null && !books.isEmpty()) {        
         book = books.get(0);         
      } else {
         // TODO error response?
      }
      return book;
   }
}
