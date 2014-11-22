package com.blackbooks.search.amazon;

import com.blackbooks.model.nonpersistent.BookInfo;
import com.blackbooks.model.persistent.Author;
import com.blackbooks.model.persistent.Identifier;
import com.blackbooks.search.BookSearchResult;

/**
 * A class used to store the info of a book returned by the Amazon Product
 * Advertising API.
 */
public class AmazonBook implements BookSearchResult {

	public String title;
	public String author;
	public String isbn;
	public String publisher;
	public String smallImageLink;
	public String mediumImageLink;
	public String largeImageLink;
	public byte[] smallImage;
	public byte[] mediumImage;
	public byte[] largeImage;

	@Override
	public BookInfo toBookInfo() {
		BookInfo bookInfo = new BookInfo();
		bookInfo.title = this.title;

		if (this.author != null) {
			Author a = new Author();
			a.name = this.author;

			bookInfo.authors.add(a);
		}
		if (this.isbn != null) {
			Identifier identifier = new Identifier();
			identifier.identifier = this.isbn;
			bookInfo.identifiers.add(identifier);
		}

		if (this.publisher != null) {
			bookInfo.publisher.name = this.publisher;
		}
		bookInfo.smallThumbnail = this.smallImage;
		if (this.largeImage != null) {
			bookInfo.thumbnail = this.largeImage;
		} else {
			bookInfo.thumbnail = this.mediumImage;
		}
		return bookInfo;
	}
}