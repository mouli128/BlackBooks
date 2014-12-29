package com.blackbooks.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MenuItem;

import com.blackbooks.R;
import com.blackbooks.fragments.BookDisplayDetailFragment;
import com.blackbooks.fragments.BookDisplayDetailFragment.BookDisplayListener;
import com.blackbooks.fragments.BookLoanFragment;
import com.blackbooks.model.nonpersistent.BookInfo;

/**
 * Activity to display the info of a book saved in the database.
 */
public final class BookDisplayActivity extends FragmentActivity implements BookDisplayListener, OnPageChangeListener, TabListener {

	/**
	 * Key of the book id when passed as an extra of the activity.
	 */
	public final static String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";

	private static final int TAB_DISPLAY = 0;
	private static final int TAB_LOAN = 1;

	private BookDisplayPagerAdapter mPagerAdapter;
	private ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
		setContentView(R.layout.activity_book_display);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = this.getIntent();
		if (intent != null && intent.hasExtra(EXTRA_BOOK_ID)) {
			long bookId = intent.getLongExtra(EXTRA_BOOK_ID, Long.MIN_VALUE);

			mPagerAdapter = new BookDisplayPagerAdapter(getSupportFragmentManager(), bookId);
			mViewPager = (ViewPager) findViewById(R.id.bookDisplay_viewPager);
			mViewPager.setAdapter(mPagerAdapter);
			mViewPager.setOnPageChangeListener(this);

			ActionBar actionBar = getActionBar();
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			actionBar.addTab(actionBar.newTab().setText(getString(R.string.title_tab_book_display_detail)).setTabListener(this));
			actionBar.addTab(actionBar.newTab().setText(getString(R.string.title_tab_book_display_loan)).setTabListener(this));

		} else {
			this.finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result;
		switch (item.getItemId()) {
		case android.R.id.home:
			result = true;
			finish();
			break;
		default:
			result = super.onOptionsItemSelected(item);
			break;
		}
		return result;
	}

	@Override
	public void onBookLoaded(BookInfo bookInfo) {
		this.setTitle(bookInfo.title);
	}

	@Override
	public void onBookDeleted() {
		this.finish();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// Do nothing.
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// Do nothing.
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// Do nothing.
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// Do nothing.
	}

	@Override
	public void onPageSelected(int position) {
		getActionBar().setSelectedNavigationItem(position);
	}

	/**
	 * Pager adapter.
	 */
	private final class BookDisplayPagerAdapter extends FragmentPagerAdapter {

		private final long mBookId;

		public BookDisplayPagerAdapter(FragmentManager fm, long bookId) {
			super(fm);
			mBookId = bookId;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;

			switch (position) {
			case TAB_DISPLAY:
				fragment = BookDisplayDetailFragment.newInstance(mBookId);
				break;

			case TAB_LOAN:
				fragment = BookLoanFragment.newInstance(mBookId);
				break;
			}

			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}
	}
}
