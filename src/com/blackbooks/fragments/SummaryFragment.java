package com.blackbooks.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackbooks.R;
import com.blackbooks.activities.BookListActivity;
import com.blackbooks.database.SQLiteHelper;
import com.blackbooks.model.nonpersistent.Summary;
import com.blackbooks.services.SummaryServices;

/**
 * A fragment displaying statistics of the library.
 */
public class SummaryFragment extends Fragment {

	private LinearLayout mLayoutBooks;
	private LinearLayout mLayoutAuthors;
	private LinearLayout mLayoutCategories;
	private LinearLayout mLayoutLanguages;
	private LinearLayout mLayoutSeries;
	private LinearLayout mLayoutLocations;
	private LinearLayout mLayoutRead;
	private LinearLayout mLayoutLoaned;

	private TextView mTextBooksCount;
	private TextView mTextAuthorsCount;
	private TextView mTextCategoriesCount;
	private TextView mTextLanguagesCount;
	private TextView mTextSeriesCount;
	private TextView mTextLocationsCount;
	private TextView mTextReadCount;
	private TextView mTextLoanedCount;

	private TextView mTextLabelBookCount;
	private TextView mTextLabelAuthorCount;
	private TextView mTextLabelCategoryCount;
	private TextView mTextLabelLanguageCount;
	private TextView mTextLabelSeriesCount;
	private TextView mTextLabelLocationCount;
	private TextView mTextLabelReadCount;
	private TextView mTextLabelLoanedCount;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_summary, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		View view = getView();
		mLayoutBooks = (LinearLayout) view.findViewById(R.id.summary_layoutBooks);
		mLayoutAuthors = (LinearLayout) view.findViewById(R.id.summary_layoutAuthors);
		mLayoutCategories = (LinearLayout) view.findViewById(R.id.summary_layoutCategories);
		mLayoutLanguages = (LinearLayout) view.findViewById(R.id.summary_layoutLanguages);
		mLayoutSeries = (LinearLayout) view.findViewById(R.id.summary_layoutSeries);
		mLayoutLocations = (LinearLayout) view.findViewById(R.id.summary_layoutLocations);
		mLayoutRead = (LinearLayout) view.findViewById(R.id.summary_layoutRead);
		mLayoutLoaned = (LinearLayout) view.findViewById(R.id.summary_layoutLoaned);

		mLayoutBooks.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startBookListActivity(BookListByFirstLetterFragment.class);
			}
		});
		mLayoutAuthors.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startBookListActivity(BookListByAuthorFragment.class);
			}
		});
		mLayoutCategories.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startBookListActivity(BookListByCategoryFragment.class);
			}
		});
		mLayoutLanguages.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startBookListActivity(BookListByLanguageFragment.class);
			}
		});
		mLayoutSeries.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startBookListActivity(BookListBySeriesFragment.class);
			}
		});
		mLayoutLocations.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startBookListActivity(BookListByBookLocationFragment.class);
			}
		});
		mLayoutRead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		mLayoutLoaned.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		mTextBooksCount = (TextView) view.findViewById(R.id.summary_booksCount);
		mTextAuthorsCount = (TextView) view.findViewById(R.id.summary_authorsCount);
		mTextCategoriesCount = (TextView) view.findViewById(R.id.summary_categoriesCount);
		mTextLanguagesCount = (TextView) view.findViewById(R.id.summary_languagesCount);
		mTextSeriesCount = (TextView) view.findViewById(R.id.summary_seriesCount);
		mTextLocationsCount = (TextView) view.findViewById(R.id.summary_locationsCount);
		mTextReadCount = (TextView) view.findViewById(R.id.summary_readCount);
		mTextLoanedCount = (TextView) view.findViewById(R.id.summary_loanedCount);

		mTextLabelBookCount = (TextView) view.findViewById(R.id.summary_textLabelBookCount);
		mTextLabelAuthorCount = (TextView) view.findViewById(R.id.summary_textLabelAuthorCount);
		mTextLabelCategoryCount = (TextView) view.findViewById(R.id.summary_textLabelCategoryCount);
		mTextLabelLanguageCount = (TextView) view.findViewById(R.id.summary_textLabelLanguageCount);
		mTextLabelSeriesCount = (TextView) view.findViewById(R.id.summary_textLabelSeriesCount);
		mTextLabelLocationCount = (TextView) view.findViewById(R.id.summary_textLabelLocationCount);
		mTextLabelReadCount = (TextView) view.findViewById(R.id.summary_textLabelReadCount);
		mTextLabelLoanedCount = (TextView) view.findViewById(R.id.summary_textLabelLoanedCount);

		SQLiteHelper dbHelper = new SQLiteHelper(getActivity());
		SQLiteDatabase db = null;
		try {
			db = dbHelper.getReadableDatabase();
			Summary summary = SummaryServices.getSummary(db);

			mTextBooksCount.setText(String.valueOf(summary.books));
			mTextAuthorsCount.setText(String.valueOf(summary.authors));
			mTextCategoriesCount.setText(String.valueOf(summary.categories));
			mTextLanguagesCount.setText(String.valueOf(summary.languages));
			mTextSeriesCount.setText(String.valueOf(summary.series));
			mTextLocationsCount.setText(String.valueOf(summary.bookLocations));
			mTextReadCount.setText(String.valueOf(summary.read));
			mTextLoanedCount.setText(String.valueOf(summary.loaned));

			Resources res = getResources();

			mTextLabelBookCount.setText(res.getQuantityText(R.plurals.label_summary_books, summary.books));
			mTextLabelAuthorCount.setText(res.getQuantityText(R.plurals.label_summary_authors, summary.authors));
			mTextLabelCategoryCount.setText(res.getQuantityText(R.plurals.label_summary_categories, summary.categories));
			mTextLabelLanguageCount.setText(res.getQuantityText(R.plurals.label_summary_languages, summary.languages));
			mTextLabelSeriesCount.setText(res.getQuantityText(R.plurals.label_summary_series, summary.series));
			mTextLabelLocationCount.setText(res.getQuantityText(R.plurals.label_summary_locations, summary.bookLocations));
			mTextLabelReadCount.setText(res.getQuantityText(R.plurals.label_summary_read, summary.read));
			mTextLabelLoanedCount.setText(res.getQuantityText(R.plurals.label_summary_loaned, summary.loaned));
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	/**
	 * Start the {@link BookListActivity}.
	 * 
	 * @param fragmentClass
	 *            Name of the fragment's class to be displayed.
	 */
	private void startBookListActivity(Class<?> fragmentClass) {
		String className = fragmentClass.getName();

		SharedPreferences sharedPref = getActivity().getSharedPreferences(BookListActivity.PREFERENCES, Context.MODE_PRIVATE);
		String defaultList = sharedPref.getString(BookListActivity.PREF_DEFAULT_LIST, null);
		if (!className.equals(defaultList)) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(BookListActivity.PREF_DEFAULT_LIST, className);
			editor.commit();
		}
		Intent i = new Intent(getActivity(), BookListActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
}
