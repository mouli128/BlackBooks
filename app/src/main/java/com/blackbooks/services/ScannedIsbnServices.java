package com.blackbooks.services;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.blackbooks.model.nonpersistent.BookInfo;
import com.blackbooks.model.persistent.ScannedIsbn;
import com.blackbooks.sql.Broker;
import com.blackbooks.sql.BrokerManager;

import java.util.Date;
import java.util.List;

/**
 * ScannedIsbn services.
 */
public class ScannedIsbnServices {

    /**
     * Delete all the scanned ISBNs.
     *
     * @param db SQLiteDatabase.
     */
    public static void deleteAllScannedIsbns(SQLiteDatabase db) {
        db.delete(ScannedIsbn.NAME, null, null);
    }

    /**
     * Get the list of all the scanned ISBNs to look up.
     *
     * @param db SQLiteDatabase.
     */
    public static List<ScannedIsbn> getScannedIsbnListToLookUp(SQLiteDatabase db) {
        ScannedIsbn criteria = new ScannedIsbn();
        criteria.lookedUp = 0L;
        return BrokerManager.getBroker(ScannedIsbn.class).getAllByCriteria(db, criteria);
    }

    /**
     * Save a scanned ISBN. If it already in the database, just update the date.
     *
     * @param db   SQLiteDatabase.
     * @param isbn String.
     */
    public static void saveScannedIsbn(SQLiteDatabase db, String isbn) {
        db.beginTransaction();
        try {
            Broker<ScannedIsbn> broker = BrokerManager.getBroker(ScannedIsbn.class);

            ScannedIsbn criteria = new ScannedIsbn();
            criteria.isbn = isbn;
            ScannedIsbn scannedIsbn = broker.getByCriteria(db, criteria);

            if (scannedIsbn == null) {
                scannedIsbn = new ScannedIsbn();
                scannedIsbn.isbn = isbn;
            }
            scannedIsbn.scanDate = new Date();

            broker.save(db, scannedIsbn);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Save a BookInfo and mark the corresponding ScannedIsbn as looked up.
     *
     * @param db            SQLiteDatabase.
     * @param bookInfo      BookInfo.
     * @param scannedIsbnId Id of the ScannedIsbn.
     */
    public static void saveBookInfo(SQLiteDatabase db, BookInfo bookInfo, long scannedIsbnId) {
        db.beginTransaction();
        try {
            BookServices.saveBookInfo(db, bookInfo);
            markScannedIsbnLookedUp(db, scannedIsbnId, true);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Mrk a ScannedIsbn as looked up.
     *
     * @param db               SQLiteDatabase.
     * @param scannedIsbnId    Id of the ScannedIsbn.
     * @param searchSuccessFul True if the search returned a result, false otherwise.
     */
    public static void markScannedIsbnLookedUp(SQLiteDatabase db, long scannedIsbnId, boolean searchSuccessFul) {
        db.beginTransaction();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ScannedIsbn.Cols.SCI_LOOKED_UP, 1L);
            contentValues.put(ScannedIsbn.Cols.SCI_SEARCH_SUCCESSFUL, searchSuccessFul ? 1L : 0L);

            String whereClause = ScannedIsbn.Cols.SCI_ID + " = ?";
            String[] whereArgs = new String[]{String.valueOf(scannedIsbnId)};
            db.update(ScannedIsbn.NAME, contentValues, whereClause, whereArgs);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
