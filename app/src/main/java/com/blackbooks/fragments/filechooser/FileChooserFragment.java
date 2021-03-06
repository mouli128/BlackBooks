package com.blackbooks.fragments.filechooser;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.blackbooks.R;
import com.blackbooks.adapters.FileListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A fragment to choose a file.
 */
public final class FileChooserFragment extends ListFragment {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    private final File mParentDirectoryShortcut = new File((String) null, "..");
    private FileChooserListener mFileChooserListener;
    private File mCurrentDirectory;
    private FileListAdapter mAdapter;
    private TextView mTextCurrentDirectory;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFileChooserListener = (FileChooserListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mAdapter = new FileListAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionGranted) {
                enterDirectory(Environment.getExternalStorageDirectory());
            } else {
                getActivity().finish();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_file_chooser, container, false);
        mTextCurrentDirectory = (TextView) view.findViewById(R.id.fileChooser_textFooter);

        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            enterDirectory(Environment.getExternalStorageDirectory());
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final File selectedFile = mAdapter.getItem(position);

        if (selectedFile == mParentDirectoryShortcut) {
            final File parentFile = mCurrentDirectory.getParentFile();
            enterDirectory(parentFile);
        } else if (selectedFile.isDirectory()) {
            enterDirectory(selectedFile);
        } else if (selectedFile.isFile()) {
            mFileChooserListener.onFileChosen(selectedFile);
        }
    }

    /**
     * Sets the current directory to a given directory and list its content.
     *
     * @param selectedDirectory The directory to enter.
     */
    private void enterDirectory(File selectedDirectory) {
        mCurrentDirectory = selectedDirectory;
        mTextCurrentDirectory.setText(selectedDirectory.getAbsolutePath());

        mAdapter.clear();
        final List<File> files = listFiles(selectedDirectory);
        if (files != null) {
            mAdapter.addAll(files);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * List the files contained in the selected folder.
     *
     * @param selectedFolder The selected folder.
     * @return The list of files in the selected folder plus a shortcut to its parent directory if
     * it has one.
     */
    private List<File> listFiles(File selectedFolder) {
        final List<File> sortedFiles = new ArrayList<File>();

        final File parent = selectedFolder.getParentFile();
        if (parent != null) {
            sortedFiles.add(0, mParentDirectoryShortcut);
        }

        final File[] files = selectedFolder.listFiles();
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
            sortedFiles.addAll(Arrays.asList(files));
        }
        return sortedFiles;
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
