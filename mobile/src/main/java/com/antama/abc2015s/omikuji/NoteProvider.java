package com.antama.abc2015s.omikuji;

import java.util.Scanner;
import java.io.IOException;

import android.content.Context;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.net.Uri;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;

public class NoteProvider extends ContentProvider {
    public static final int COL_AREA = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_DESCRIPTION = 3;
    public static final String[] COLUMNS = {"_id", "area", "title", "description"};

    @Override
    public String getType(Uri uri) {
        return new ProviderMap(uri).getContentType();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            final MatrixCursor c = new MatrixCursor(COLUMNS);
            for (CSVRecord r : CSVParser.parse(getData(), CSVFormat.DEFAULT)) {
                c.addRow(new Object[] {c.getCount(), r.get("市区町村"), r.get("タイトル"), r.get("内容")});
            }
            return c;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private String getData() {
        return rawResourceAsString(getContext(), R.raw.saitama);
    }

    private static String rawResourceAsString(final Context c, final int id) {
        return new Scanner(c.getResources().openRawResource(id)).useDelimiter("\\A").next();
    }
}
