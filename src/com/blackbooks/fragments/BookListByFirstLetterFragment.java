package com.blackbooks.fragments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;

import com.blackbooks.adapters.BookItem;
import com.blackbooks.adapters.BooksByFirstLetterAdapter;
import com.blackbooks.adapters.FirstLetterItem;
import com.blackbooks.adapters.ListItem;
import com.blackbooks.database.SQLiteHelper;
import com.blackbooks.model.nonpersistent.BookInfo;
import com.blackbooks.services.BookServices;

/**
 * Implements {@link AbstractBookListFragment}. A fragment that lists books by
 * the first letter of their title.
 */
public class BookListByFirstLetterFragment extends AbstractBookListFragment {

	@Override
	protected ArrayAdapter<ListItem> getBookListAdapter() {
		return new BooksByFirstLetterAdapter(this.getActivity());
	}

	/**
	 * Return the list of items to display.
	 * 
	 * @return List.
	 */
	@Override
	protected List<ListItem> loadBookList() {
		SQLiteHelper mDbHelper = new SQLiteHelper(this.getActivity());
		SQLiteDatabase db = null;
		List<BookInfo> bookList;
		try {
			db = mDbHelper.getReadableDatabase();
			bookList = BookServices.getBookInfoList(db);
		} finally {
			if (db != null) {
				db.close();
			}
		}

		LinkedHashMap<String, List<BookInfo>> bookMap = new LinkedHashMap<String, List<BookInfo>>();

		for (BookInfo book : bookList) {
			String firstLetter = book.title.substring(0, 1);

			if (!bookMap.containsKey(firstLetter)) {
				bookMap.put(firstLetter, new ArrayList<BookInfo>());
			}
			bookMap.get(firstLetter).add(book);
		}

		List<ListItem> listItems = new ArrayList<ListItem>();
		for (String firstLetter : bookMap.keySet()) {
			FirstLetterItem firstLetterItem = new FirstLetterItem(firstLetter);
			listItems.add(firstLetterItem);

			for (BookInfo book : bookMap.get(firstLetter)) {
				BookItem bookItem = new BookItem(book);
				listItems.add(bookItem);
			}
		}
		return listItems;
	}
}
