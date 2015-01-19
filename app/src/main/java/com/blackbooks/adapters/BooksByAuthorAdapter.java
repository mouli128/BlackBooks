package com.blackbooks.adapters;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.blackbooks.R;
import com.blackbooks.cache.ThumbnailManager;
import com.blackbooks.model.nonpersistent.AuthorInfo;
import com.blackbooks.model.nonpersistent.BookInfo;
import com.blackbooks.model.persistent.Series;

/**
 * An adapter handling instances of ListItem representing either an author or a
 * book.
 */
public class BooksByAuthorAdapter extends ArrayAdapter<ListItem> implements SectionIndexer {

	private final LayoutInflater mInflater;
	private final ThumbnailManager mThumbnailManager;
	private final Map<String, Integer> mSectionPositionMap;
	private final SparseArray<String> mPositionSectionMap;
	private String[] mSections;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            context.
	 * @param items
	 *            Items.
	 */
	public BooksByAuthorAdapter(Context context) {
		super(context, 0);
		this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mThumbnailManager = ThumbnailManager.getInstance();
		this.mSectionPositionMap = new TreeMap<String, Integer>();
		this.mPositionSectionMap = new SparseArray<String>();
		this.mSections = new String[] {};
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		ListItem item = this.getItem(position);
		if (item != null) {
			ListItemType itemType = item.getListItemType();
			if (itemType == ListItemType.ENTRY) {
				view = getViewBook(parent, item);
			} else if (itemType == ListItemType.HEADER_2) {
				view = getViewSeries(parent, item);
			} else if (itemType == ListItemType.HEADER) {
				view = getViewAuthor(parent, item);
			}
		}
		return view;
	}

	@Override
	public void addAll(Collection<? extends ListItem> collection) {
		super.addAll(collection);
		mSectionPositionMap.clear();
		mPositionSectionMap.clear();

		int position = 0;
		String currentSection = null;
		for (ListItem listItem : collection) {
			if (listItem.getListItemType() == ListItemType.HEADER) {
				AuthorItem authorItem = (AuthorItem) listItem;
				AuthorInfo author = authorItem.getAuthor();
				String authorName = author.name;
				currentSection = authorName.substring(0, 1);
				if (!mSectionPositionMap.containsKey(currentSection)) {
					mSectionPositionMap.put(currentSection, position);
				}
			}
			mPositionSectionMap.put(position, currentSection);
			position++;
		}
		mSections = mSectionPositionMap.keySet().toArray(new String[mSectionPositionMap.size()]);
	}

	@Override
	public Object[] getSections() {
		return mSections;
	}

	@Override
	public int getPositionForSection(int section) {
		int index = section;
		if (index >= mSections.length) {
			index = mSections.length - 1;
		}
		int position = mSectionPositionMap.get(mSections[index]);
		return position;
	}

	@Override
	public int getSectionForPosition(int position) {
		String currentSection = mPositionSectionMap.get(position);
		int sectionIndex = 0;

		for (int i = 0; i < mSections.length; i++) {
			String section = mSections[i];
			if (section.equals(currentSection)) {
				break;
			}
			sectionIndex++;
		}
		return sectionIndex;
	}

	private View getViewAuthor(ViewGroup parent, ListItem item) {
		View view;
		AuthorItem header = (AuthorItem) item;
		AuthorInfo author = header.getAuthor();

		view = mInflater.inflate(R.layout.list_books_by_author_item_author, parent, false);

		TextView textViewName = (TextView) view.findViewById(R.id.books_by_author_name);
		textViewName.setText(author.name);

		TextView textViewTotalBooks = (TextView) view.findViewById(R.id.books_by_author_item_total);
		String total = this.getContext().getString(R.string.label_total);
		total = String.format(total, author.books.size());
		textViewTotalBooks.setText(total);
		return view;
	}

	private View getViewSeries(ViewGroup parent, ListItem item) {
		View view;
		SeriesItem seriesItem = (SeriesItem) item;
		Series series = seriesItem.getSeries();

		if (series.id != null) {
			view = mInflater.inflate(R.layout.list_books_by_author_item_series, parent, false);

			TextView textViewName = (TextView) view.findViewById(R.id.books_by_author_item_series_name);
			textViewName.setText(series.name);
		} else {
			view = new View(getContext());
			view.setVisibility(View.GONE);
		}
		return view;
	}

	private View getViewBook(ViewGroup parent, ListItem item) {
		View view;
		BookItem entry = (BookItem) item;
		BookInfo book = entry.getBook();

		view = mInflater.inflate(R.layout.list_books_by_author_item_book, parent, false);
		ImageView imageView = (ImageView) view.findViewById(R.id.books_by_author_item_book_small_thumbnail);
		TextView textNumber = (TextView) view.findViewById(R.id.books_by_author_item_book_number);
		TextView textTitle = (TextView) view.findViewById(R.id.books_by_author_item_book_title);
		TextView textDescription = (TextView) view.findViewById(R.id.books_by_author_item_book_description);
		ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.books_by_author_item_book_progressBar);
		ImageView imageRead = (ImageView) view.findViewById(R.id.books_by_author_item_book_imageRead);
		ImageView imageFavourite = (ImageView) view.findViewById(R.id.books_by_author_item_book_imageFavourite);
		ImageView imageLoaned = (ImageView) view.findViewById(R.id.books_by_author_item_book_imageLoaned);

		mThumbnailManager.drawSmallThumbnail(book.id, getContext(), imageView, progressBar);

		if (book.number != null) {
			textNumber.setText(book.number.toString());
		} else {
			textNumber.setVisibility(View.GONE);
		}
		textTitle.setText(book.title);
		if (book.description != null) {
			textDescription.setText(book.description);
		} else {
			textDescription.setVisibility(View.GONE);
		}

		if (book.isRead != 0) {
			imageRead.setVisibility(View.VISIBLE);
		} else {
			imageRead.setVisibility(View.GONE);
		}

		if (book.isFavourite != 0) {
			imageFavourite.setVisibility(View.VISIBLE);
		} else {
			imageFavourite.setVisibility(View.GONE);
		}
		if (book.loanedTo != null) {
			imageLoaned.setVisibility(View.VISIBLE);
		} else {
			imageLoaned.setVisibility(View.GONE);
		}
		return view;
	}

	public static final class AuthorItem implements ListItem {

		private final AuthorInfo mAuthor;

		public AuthorItem(AuthorInfo author) {
			this.mAuthor = author;
		}

		@Override
		public ListItemType getListItemType() {
			return ListItemType.HEADER;
		}

		public AuthorInfo getAuthor() {
			return mAuthor;
		}
	}

	public static final class SeriesItem implements ListItem {

		private final Series mSeries;

		public SeriesItem(Series series) {
			mSeries = series;
		}

		@Override
		public ListItemType getListItemType() {
			return ListItemType.HEADER_2;
		}

		public Series getSeries() {
			return mSeries;
		}
	}
}