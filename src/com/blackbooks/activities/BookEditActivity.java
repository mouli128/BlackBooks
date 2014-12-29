package com.blackbooks.activities;

import java.security.InvalidParameterException;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blackbooks.R;
import com.blackbooks.database.SQLiteHelper;
import com.blackbooks.fragments.BookEditGeneralFragment;
import com.blackbooks.fragments.BookEditPersonalFragment;
import com.blackbooks.fragments.BookLoadFragment;
import com.blackbooks.fragments.BookLoadFragment.BookLoadListener;
import com.blackbooks.fragments.BookSearchFragment;
import com.blackbooks.fragments.BookSearchFragment.BookSearchListener;
import com.blackbooks.model.nonpersistent.BookInfo;
import com.blackbooks.services.BookServices;
import com.blackbooks.utils.VariableUtils;

/**
 * Activity used to add a new book or edit an existing one.
 */
public final class BookEditActivity extends FragmentActivity implements BookLoadListener, BookSearchListener,
		OnPageChangeListener, TabListener {

	public static final String EXTRA_BOOK_ID = "EXTRA_BOOK_ID";
	public static final String EXTRA_MODE = "EXTRA_MODE";
	public static final String EXTRA_ISBN = "EXTRA_ISBN";

	public static final int MODE_ADD = 1;
	public static final int MODE_EDIT = 2;

	private static final String STATE_MODE = "STATE_MODE";
	private static final String STATE_IS_LOADING = "STATE_IS_LOADING";
	private static final String STATE_IS_SEARCHING = "STATE_IS_SEARCHING";
	private static final String STATE_BOOK_INFO = "STATE_BOOK_INFO";

	private static final int TAB_GENERAL = 0;
	private static final int TAB_PERSONAL = 1;

	private static final String TAG_BOOK_LOAD_FRAGMENT = "TAG_BOOK_LOAD_FRAGMENT";
	private static final String TAG_BOOK_SEARCH_FRAGMENT = "TAG_BOOK_SEARCH_FRAGMENT";

	private ProgressBar mProgressBar;
	private BookEditPagerAdapter mPagerAdapter;
	private ViewPager mViewPager;

	private BookEditGeneralFragment mBookEditGeneralFragment;
	private BookEditPersonalFragment mBookEditPersonalFragment;

	private int mMode;
	private boolean mIsLoading;
	private boolean mIsSearching;
	private BookInfo mBookInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
		setContentView(R.layout.activity_book_edit);
		mProgressBar = (ProgressBar) findViewById(R.id.bookEdit_progressBar);
		mViewPager = (ViewPager) findViewById(R.id.bookEdit_viewPager);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState != null) {
			restoreState(savedInstanceState);
		} else {
			initState();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_MODE, mMode);
		outState.putBoolean(STATE_IS_LOADING, mIsLoading);
		outState.putBoolean(STATE_IS_SEARCHING, mIsSearching);
		outState.putSerializable(STATE_BOOK_INFO, mBookInfo);

		FragmentManager fm = getSupportFragmentManager();
		if (mBookEditGeneralFragment != null) {
			fm.putFragment(outState, BookEditGeneralFragment.class.getName(), mBookEditGeneralFragment);
		}
		if (mBookEditPersonalFragment != null) {
			fm.putFragment(outState, BookEditPersonalFragment.class.getName(), mBookEditPersonalFragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.book_edit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result;
		switch (item.getItemId()) {

		case R.id.bookEdit_actionSave:
			result = true;
			save();
			break;

		case android.R.id.home:
			finish();
			result = true;
			break;

		default:
			result = super.onOptionsItemSelected(item);
			break;
		}
		return result;
	}

	@Override
	public void onBookLoaded(BookInfo bookInfo) {
		mBookInfo = bookInfo;
		setTitleEditMode();
		createTabs();

		mProgressBar.setVisibility(View.GONE);
		mViewPager.setVisibility(View.VISIBLE);
		mIsLoading = false;
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

	@Override
	public void onSearchFinished(BookInfo bookInfo) {
		if (bookInfo != null) {
			mBookInfo = bookInfo;
		} else {
			mBookInfo = new BookInfo();
		}
		createTabs();
		mProgressBar.setVisibility(View.GONE);
		mViewPager.setVisibility(View.VISIBLE);
		mIsSearching = false;
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

	/**
	 * Create the tabs of the activity.
	 */
	private void createTabs() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.title_tab_book_edit_general)).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.title_tab_book_edit_personal)).setTabListener(this));

		mPagerAdapter = new BookEditPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	/**
	 * Initializes the state of the activity depending on the mode (Add or
	 * Edit).
	 */
	private void initState() {
		Intent intent = this.getIntent();
		mMode = intent.getIntExtra(EXTRA_MODE, 0);

		switch (mMode) {
		case MODE_ADD:
			initStateAddMode(intent);
			break;

		case MODE_EDIT:
			initStateEditMode(intent);
			break;

		default:
			throw new InvalidParameterException("Extra " + EXTRA_MODE + " not set.");
		}
	}

	/**
	 * Initializes the state of the activity in Add mode.
	 * 
	 * @param intent
	 *            Intent.
	 */
	private void initStateAddMode(Intent intent) {
		setTitleAddMode();
		if (intent.hasExtra(EXTRA_ISBN)) {
			FragmentManager fm = getSupportFragmentManager();
			BookSearchFragment bookSearchFragment = (BookSearchFragment) fm.findFragmentByTag(TAG_BOOK_SEARCH_FRAGMENT);

			if (bookSearchFragment == null && !mIsSearching) {
				mIsSearching = true;
				mProgressBar.setVisibility(View.VISIBLE);
				mViewPager.setVisibility(View.GONE);
				String isbn = intent.getStringExtra(EXTRA_ISBN);
				bookSearchFragment = BookSearchFragment.newIntance(isbn);
				fm.beginTransaction() //
						.add(bookSearchFragment, TAG_BOOK_SEARCH_FRAGMENT) //
						.commit();
			}
		} else {
			mBookInfo = new BookInfo();
			mProgressBar.setVisibility(View.GONE);
			mViewPager.setVisibility(View.VISIBLE);
			createTabs();
		}
	}

	/**
	 * Initializes the state of the activity in Edit mode.
	 * 
	 * @param intent
	 *            Intent.
	 */
	private void initStateEditMode(Intent intent) {
		if (intent.hasExtra(EXTRA_BOOK_ID)) {
			FragmentManager fm = getSupportFragmentManager();
			BookLoadFragment bookLoadFragment = (BookLoadFragment) fm.findFragmentByTag(TAG_BOOK_LOAD_FRAGMENT);

			if (bookLoadFragment == null && !mIsLoading) {
				mIsLoading = true;
				mProgressBar.setVisibility(View.VISIBLE);
				mViewPager.setVisibility(View.GONE);
				long bookId = intent.getLongExtra(EXTRA_BOOK_ID, 0);
				bookLoadFragment = BookLoadFragment.newInstance(bookId);
				fm.beginTransaction() //
						.add(bookLoadFragment, TAG_BOOK_LOAD_FRAGMENT) //
						.commit();
			}

		} else {
			throw new InvalidParameterException("Extra " + EXTRA_BOOK_ID + " not set.");
		}
	}

	/**
	 * Restore the state of the activity (for instance after the screen
	 * orientation changes).
	 * 
	 * @param savedInstanceState
	 *            Bundle.
	 */
	private void restoreState(Bundle savedInstanceState) {
		FragmentManager fm = getSupportFragmentManager();
		mMode = savedInstanceState.getInt(STATE_MODE);
		mIsLoading = savedInstanceState.getBoolean(STATE_IS_LOADING);
		mIsSearching = savedInstanceState.getBoolean(STATE_IS_SEARCHING);
		mBookInfo = (BookInfo) savedInstanceState.getSerializable(STATE_BOOK_INFO);
		mBookEditGeneralFragment = (BookEditGeneralFragment) fm.getFragment(savedInstanceState,
				BookEditGeneralFragment.class.getName());
		mBookEditPersonalFragment = (BookEditPersonalFragment) fm.getFragment(savedInstanceState,
				BookEditPersonalFragment.class.getName());

		switch (mMode) {
		case MODE_ADD:
			setTitleAddMode();
			break;

		case MODE_EDIT:
			setTitleEditMode();
			break;

		default:
			throw new InvalidParameterException("Invalid STATE_MODE.");
		}

		if (mIsLoading || mIsSearching) {
			mProgressBar.setVisibility(View.VISIBLE);
			mViewPager.setVisibility(View.GONE);
		} else {
			mProgressBar.setVisibility(View.GONE);
			mViewPager.setVisibility(View.VISIBLE);
			createTabs();
		}
	}

	/**
	 * Save the book information in the database.
	 */
	private void save() {
		if (mBookEditGeneralFragment != null && mBookEditPersonalFragment != null) {
			boolean isValid = mBookEditGeneralFragment.readBookInfo(mBookInfo);

			if (!isValid) {
				getActionBar().setSelectedNavigationItem(0);
				return;
			}

			isValid = isValid && mBookEditPersonalFragment.readBookInfo(mBookInfo);

			if (isValid) {
				SQLiteHelper dbHelper = new SQLiteHelper(this);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				BookServices.saveBookInfo(db, mBookInfo);
				db.close();

				VariableUtils.getInstance().setReloadBookList(true);

				String title = mBookInfo.title;
				String message;
				switch (mMode) {
				case MODE_ADD:
					message = String.format(getString(R.string.message_book_added), title);
					Toast.makeText(this, message, Toast.LENGTH_LONG).show();
					NavUtils.navigateUpFromSameTask(this);
					break;

				case MODE_EDIT:
					message = String.format(getString(R.string.message_book_modifed), title);
					Toast.makeText(this, message, Toast.LENGTH_LONG).show();
					setResult(RESULT_OK);
					finish();
					break;

				default:
					throw new InvalidParameterException("Invalid mode.");
				}
			}
		}
	}

	private void setTitleAddMode() {
		setTitle(getString(R.string.title_activity_book_edit_mode_add));
	}

	private void setTitleEditMode() {
		String title = getString(R.string.title_activity_book_edit_mode_edit);
		setTitle(String.format(title, mBookInfo.title));
	}

	/**
	 * FragmentPagerAdapter used to define the different tabs of the activity.
	 */
	private final class BookEditPagerAdapter extends FragmentPagerAdapter {

		/**
		 * Constructor.
		 * 
		 * @param fm
		 *            FragmentManager.
		 */
		public BookEditPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case TAB_GENERAL:
				if (mBookEditGeneralFragment == null) {
					mBookEditGeneralFragment = BookEditGeneralFragment.newInstance(mBookInfo);
				}
				fragment = mBookEditGeneralFragment;
				break;

			case TAB_PERSONAL:
				if (mBookEditPersonalFragment == null) {
					mBookEditPersonalFragment = BookEditPersonalFragment.newInstance(mBookInfo);
				}
				fragment = mBookEditPersonalFragment;
				break;

			default:
				throw new InvalidParameterException("Invalid tab position.");
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}
	}
}
