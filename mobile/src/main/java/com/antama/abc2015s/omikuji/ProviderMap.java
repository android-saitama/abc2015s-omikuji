package com.antama.abc2015s.omikuji;

import android.content.UriMatcher;
import android.net.Uri;

public class ProviderMap {
    public static final String AUTHORITY_NOTE = NoteProvider.class.getCanonicalName();

    private static UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int NOTES = 1;

    private static final String SINGLE_ITEM_TYPE = "vnd.android.cursor.item/%s";
    private static final String MULTIPLE_ITEM_TYPE = "vnd.android.cursor.dir/%s";

    static {
        sMatcher.addURI(AUTHORITY_NOTE, "notes", NOTES);
    }

    private final Uri mUri;

    public ProviderMap(final Uri uri) {
        mUri = uri;
    }

    public int getResourceType() {
        return sMatcher.match(mUri);
    }

    public String getContentType() {
        switch (getResourceType()) {
            case NOTES:
                return String.format(MULTIPLE_ITEM_TYPE, "notes");
            default:
                return null;
        }
    }

    public static Uri getContentUri(final int type) {
        switch (type) {
            case NOTES:
                return Uri.parse(String.format("content://%s/%s", AUTHORITY_NOTE, "notes"));
            default:
                return null;
        }
    }
}
