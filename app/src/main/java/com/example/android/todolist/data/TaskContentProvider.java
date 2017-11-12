/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.todolist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.net.URI;
import java.net.URL;

// TODO (1) Verify that TaskContentProvider extends from ContentProvider and implements required methods
public class TaskContentProvider extends ContentProvider {
    private TaskDbHelper mTaskDbHelper;

    // TODO (6) Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int  TASKS = 100;
    public static final int TASK_WITH_ID = 101;
    // TODO (8) Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // TODO (7) Define a static buildUriMatcher method that associates URI's with their int match
    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //Add matches with addURI(String authrity, String path, int code)
        //directory
        uriMatcher.addURI(TaskContract.AUTHORITY,TaskContract.PATH_TASKS, TASKS);
        //single item
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS + "/#",TASK_WITH_ID);
        return uriMatcher;
    }




    /* onCreate() is where you should initialize anything you’ll need to setup
    your underlying data source.
    In this case, you’re working with a SQLite database, so you’ll need to
    initialize a DbHelper to gain access to it.
     */
    @Override
    public boolean onCreate() {
        // TODO (2) Complete onCreate() and initialize a TaskDbhelper on startup
        // [Hint] Declare the DbHelper as a global variable
        Context context =getContext();
        mTaskDbHelper = new TaskDbHelper(context);

        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // TODO (9) Get access to the task database (to write new data to)
        final SQLiteDatabase database = mTaskDbHelper.getWritableDatabase();

        // TODO (10) Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);

        // TODO (11) Insert new values into the database
        // TODO (12) Set the value for the returnedUri and write the default case for unknown URI's

        // TODO (13) Notify the resolver if the uri has been changed, and return the newly inserted URI
        Uri returnUri;
        switch (match){
            case TASKS:
                //Inserting values into  tasks table
                long id= database.insert(TaskContract.TaskEntry.TABLE_NAME,null,values);
                if (id>0){
                    //sucess
                    returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id);
                }else {
                    throw new android.database.SQLException("Falid to inset row into " + uri);
                }
                break;
            //Defualt case throws an UnsupportedOpeationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;

    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // TODO (14) Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();

        // TODO (15) Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        // TODO (16) Query for the tasks directory and write a default case
        Cursor retCursor;
        switch (match){
            case TASKS:
                retCursor = db.query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_WITH_ID:
                //using selection and selectionArgs
                //URI: content://<authority>/tasks/#
                String id = uri.getPathSegments().get(1);
                //Selection id the _ID column = ? and the Selection args = the row ID from the URI
                String mselection = "_id=?";
                String [] mSelectionArgs =new String[]{id};
                retCursor = db.query(TaskContract.TaskEntry.TABLE_NAME, projection,mselection,mSelectionArgs,
                        null,null, sortOrder);
                break;
                //Defualt excption
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);
        }
        // TODO (17) Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        // TODO (19) Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        //keep track of the number of deleted tasks
        int tasksDeleted;  //starts as 0

        // TODO (20) Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match){
            //Handle the single item case, recognized by Id included in the URI path
            case TASK_WITH_ID:
                //Get the task ID from the URI path
                String id = uri.getPathSegments().get(1);
                //Get selections/selectionArgs to filter for this ID
                tasksDeleted =db.delete(TaskContract.TaskEntry.TABLE_NAME,"_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        // TODO (21) Notify the resolver of a change and return the number of items deleted
        if (tasksDeleted != 0){
            //A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri,null);
        }
        //Return the number of tasks deleted
        return tasksDeleted;

    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

}
