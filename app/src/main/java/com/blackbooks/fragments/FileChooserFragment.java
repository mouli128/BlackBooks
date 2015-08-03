package com.blackbooks.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.blackbooks.R;
import com.blackbooks.adapters.FileListAdapter;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A fragment to choose a file.
 */
public final class FileChooserFragment extends ListFragment {

    private FileChooserListener mFileChooserListener;
    private File mCurrentDirectory;
    private FileListAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFileChooserListener = (FileChooserListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mAdapter = new FileListAdapter(getActivity());
        setListAdapter(mAdapter);
        mCurrentDirectory = Environment.getExternalStorageDirectory();
        File[] files = listFiles(mCurrentDirectory);
        mAdapter.addAll(files);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_chooser, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHomeButton();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        File selectedFile = mAdapter.getItem(position);

        if (selectedFile.isDirectory()) {
            enterDirectory(selectedFile);
        } else if (selectedFile.isFile()) {
            mFileChooserListener.onFileChosen(selectedFile);
        }
    }

    private void enterDirectory(File selectedFile) {
        mCurrentDirectory = selectedFile;

        mAdapter.clear();
        File[] files = listFiles(selectedFile);
        if (files != null) {
            mAdapter.addAll(files);
        }
        mAdapter.notifyDataSetChanged();

        updateHomeButton();
    }

    private void updateHomeButton() {
        FragmentActivity activity = getActivity();
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            boolean rootDirectory = mCurrentDirectory.getParent() == null;
            actionBar.setDisplayHomeAsUpEnabled(!rootDirectory);
        }
    }

    private File[] listFiles(File selectedFile) {
        File[] files = selectedFile.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    int result;
                    if (f1.isFile() && f2.isFile() || f1.isDirectory() && f2.isDirectory()) {
                        String name1 = f1.getName();
                        String name2 = f2.getName();
                        result = name1.compareTo(name2);
                    } else if (f1.isDirectory()) {
                        result = -1;
                    } else {
                        result = 1;
                    }
                    return result;
                }
            });
        }
        return files;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case android.R.id.home:
                result = true;
                File parentDirectory = mCurrentDirectory.getParentFile();
                if (parentDirectory == null) {
                    Activity activity = getActivity();
                    activity.finish();
                } else {
                    enterDirectory(parentDirectory);
                }
                break;

            default:
                result = super.onOptionsItemSelected(item);
                break;
        }

        return result;
    }

    /**
     * Activities hosting the FileChooserFragment should implement this interface to be notified
     * when a file is chosen.
     */
    public interface FileChooserListener {

        /**
         * Called when a file is picked.
         *
         * @param file The chosen file.
         */
        void onFileChosen(File file);
    }
}
