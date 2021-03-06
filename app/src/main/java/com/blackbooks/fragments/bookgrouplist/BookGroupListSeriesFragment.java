package com.blackbooks.fragments.bookgrouplist;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.blackbooks.R;
import com.blackbooks.fragments.dialogs.SeriesDeleteFragment;
import com.blackbooks.fragments.dialogs.SeriesEditFragment;
import com.blackbooks.model.nonpersistent.BookGroup;
import com.blackbooks.repositories.SeriesRepository;
import com.blackbooks.services.BookGroupService;
import com.blackbooks.utils.VariableUtils;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment to display the series in the library.
 */
public final class BookGroupListSeriesFragment extends AbstractBookGroupListFragment implements SeriesEditFragment.SeriesEditListener, SeriesDeleteFragment.SeriesDeleteListener {

    private static final int ITEM_SERIES_EDIT = 1;
    private static final int ITEM_SERIES_DELETE = 2;
    private static final String TAG_FRAGMENT_SERIES_EDIT = "TAG_FRAGMENT_SERIES_EDIT";
    private static final String TAG_FRAGMENT_SERIES_DELETE = "TAG_FRAGMENT_SERIES_DELETE";

    @Inject
    BookGroupService bookGroupService;

    @Inject
    SeriesRepository seriesService;

    @Inject
    SeriesRepository summaryService;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    protected BookGroup.BookGroupType getBookGroupType() {
        return BookGroup.BookGroupType.SERIES;
    }

    @Override
    protected int getBookGroupCount() {
        return summaryService.getSeriesCount();
    }

    @Override
    protected List<BookGroup> loadBookGroupList(int limit, int offset) {
        return bookGroupService.getBookGroupListSeries(limit, offset);
    }

    @Override
    protected String getFooterText(int displayedBookGroupCount, int totalBookGroupCount) {
        Resources res = getResources();
        return res.getQuantityString(R.plurals.footer_fragment_book_groups_series, displayedBookGroupCount, displayedBookGroupCount, totalBookGroupCount);
    }

    @Override
    protected String getMoreGroupsLoadedText(int bookGroupCount) {
        Resources res = getResources();
        return res.getQuantityString(R.plurals.message_book_groups_loaded_series, bookGroupCount, bookGroupCount);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BookGroup bookGroup = (BookGroup) getListView().getAdapter().getItem(info.position);
        menu.setHeaderTitle(bookGroup.name);
        menu.add(Menu.NONE, ITEM_SERIES_EDIT, Menu.NONE, R.string.action_edit_series);
        menu.add(Menu.NONE, ITEM_SERIES_DELETE, Menu.NONE, R.string.action_delete_series);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean result;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final BookGroup bookGroup = (BookGroup) getListAdapter().getItem(info.position);

        switch (item.getItemId()) {
            case ITEM_SERIES_EDIT:
                SeriesEditFragment seriesEditFragment = SeriesEditFragment.newInstance(bookGroup);
                seriesEditFragment.setTargetFragment(this, 0);
                seriesEditFragment.show(getFragmentManager(), TAG_FRAGMENT_SERIES_EDIT);
                result = true;
                break;

            case ITEM_SERIES_DELETE:
                SeriesDeleteFragment seriesDeleteFragment = SeriesDeleteFragment.newInstance(bookGroup);
                seriesDeleteFragment.setTargetFragment(this, 0);
                seriesDeleteFragment.show(getFragmentManager(), TAG_FRAGMENT_SERIES_DELETE);
                result = true;
                break;

            default:
                result = super.onContextItemSelected(item);
        }
        return result;
    }

    @Override
    public void onSeriesEdit(BookGroup bookGroup, String newName) {
        seriesService.updateSeries((Long) bookGroup.id, newName);

        String message = getString(R.string.message_series_modifed, bookGroup.name);
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

        super.reloadBookGroups();
    }

    @Override
    public void onSeriesDeleted(BookGroup bookGroup) {
        seriesService.deleteSeries((Long) bookGroup.id);

        VariableUtils.getInstance().setReloadBookList(true);
        String message = getString(R.string.message_series_deleted, bookGroup.name);
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_SHORT).show();

        super.removeBookGroupFromAdapter(bookGroup);
    }
}
