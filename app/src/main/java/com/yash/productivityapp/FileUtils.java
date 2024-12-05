package com.yash.productivityapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;

public class FileUtils {

    // Converts Uri to file path
    public static String getPath(Context context, Uri uri) {
        // Check if URI is not null
        if (uri == null) {
            return null;
        }

        // Check for content Uri (file path)
        if (TextUtils.equals(uri.getScheme(), "content")) {
            Cursor cursor = null;
            try {
                // Query the content resolver for the DISPLAY_NAME column
                cursor = context.getContentResolver().query(uri,
                        new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);

                // Ensure the cursor is valid and contains data
                if (cursor != null && cursor.moveToFirst()) {
                    // Get the file name from the cursor
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle the exception gracefully (you can log this or show a toast)
            } finally {
                if (cursor != null) {
                    cursor.close(); // Close cursor to avoid memory leaks
                }
            }
        }
        // Handle file URI case (direct file path)
        else if (TextUtils.equals(uri.getScheme(), "file")) {
            return uri.getPath(); // Directly return file path for file URIs
        }

        // Return null if no valid file path is found
        return null;
    }
}
