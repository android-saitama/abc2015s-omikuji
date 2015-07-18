package com.antama.abc2015s.omikuji;

import java.util.Scanner;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.security.SecureRandom;

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

public class OracleProvider extends ContentProvider {
    public static final int COL_TOTAL = 1;
    public static final int COL_NUMBER = 2;
    public static final int COL_COLOR_NAME = 3;
    public static final int COL_COLOR_RGB = 4;
    public static final int COL_AREA = 5;
    public static final int COL_TITLE = 6;
    public static final int COL_DESCRIPTION = 7;
    public static final String[] COLUMNS = {"_id", "total", "number", "color_name", "color_rgb", "area", "title", "description"};

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
        final MatrixCursor c = new MatrixCursor(COLUMNS);
        final List<Object> row = new ArrayList<Object>();

        row.add(0);
        row.addAll(pickTotal());
        row.addAll(pickNumber());
        row.addAll(pickColor());
        row.addAll(pickArea());

        c.addRow(row);
        return c;
    }

    private static <T> T anyOf(T[] t) {
        return anyOf(Arrays.asList(t));
    }

    private static <T> T anyOf(final List<T> t) {
        final List<T> o = new LinkedList(t);
        Collections.shuffle(o);
        return o.get(0);
    }

    private List<String> pickTotal() {
        return Arrays.asList(anyOf(Arrays.asList("大吉", "中吉", "小吉", "末吉", "凶")));
    }

    private List<Integer> pickNumber() {
        return Arrays.asList(new SecureRandom().nextInt(1000));
    }

    private List<Object> pickColor() {
        try {
            final List<Object[]> well = new ArrayList<Object[]>();
            for (CSVRecord r : CSVParser.parse(rawResourceAsString(getContext(), R.raw.color), CSVFormat.DEFAULT)) {
                well.add(new Object[] {r.get("色"), r.get("慣用色名")});
            }
            return Arrays.asList(anyOf(well));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> pickArea() {
        try {
            final List<Object[]> well = new ArrayList<Object[]>();
            for (CSVRecord r : CSVParser.parse(rawResourceAsString(getContext(), R.raw.saitama), CSVFormat.DEFAULT)) {
                well.add(new Object[] {r.get("市区町村"), r.get("タイトル"), r.get("内容")});
            }
            return Arrays.asList(anyOf(well));
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

    private static String rawResourceAsString(final Context c, final int id) {
        return new Scanner(c.getResources().openRawResource(id)).useDelimiter("\\A").next();
    }
}
