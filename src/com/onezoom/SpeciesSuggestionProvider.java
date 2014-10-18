package com.onezoom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;


import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;

public class SpeciesSuggestionProvider extends ContentProvider {
    private static final String LOG_TAG = "PlacesSuggestionProvider";

    public static final String AUTHORITY = "com.onezoom.search_suggestion_provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search");

    // UriMatcher constant for search suggestions
    private static final int SEARCH_SUGGEST = 1;

    private static final UriMatcher uriMatcher;

    private static final String[] SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA
    };

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    }

    @Override
    public int delete(Uri uri, String arg1, String[] arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues arg1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        String query = uri.getLastPathSegment();
        
        ArrayList<String> hints = getSearchHints(query);
       
        // Use the UriMatcher to see what kind of query we have
        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                MatrixCursor cursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS, 1);
                for (int i = 0; i < hints.size(); i++) {
                	cursor.addRow(new String[] {
                            Integer.toString(i+1), hints.get(i), hints.get(i)
                    });
                }
                return cursor;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private ArrayList<String> getSearchHints(String query) {
    	CanvasActivity client = MyAppContext.context;
    	 String filename = client.selectedItem.toLowerCase(Locale.ENGLISH) + query.substring(0, 2).toLowerCase();
 		int resourceID = client.getResources().getIdentifier(filename, "raw", client.getPackageName());
 		InputStream is = client.getResources().openRawResource(resourceID);
 		CSVReader reader = new CSVReader(new InputStreamReader(is));
		ArrayList<String> hints = new ArrayList<String>();
 		try {
			String[] line;
			reader.readNext();
			int total = 0;
			while ((line = reader.readNext()) != null && total < 30) {
				if (line[0].toLowerCase(Locale.ENGLISH).contains(query.toLowerCase(Locale.ENGLISH))) {
					hints.add(line[0]);
					total++;
				}				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return hints;
	}

	@Override
    public int update(Uri uri, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException();
    }
}
