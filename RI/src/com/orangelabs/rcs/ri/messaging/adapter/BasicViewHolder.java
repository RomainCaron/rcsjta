/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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
 ******************************************************************************/

package com.orangelabs.rcs.ri.messaging.adapter;

import com.gsma.services.rcs.history.HistoryLog;

import com.orangelabs.rcs.ri.R;

import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

/**
 * A ViewHolder class keeps references to children views to avoid unnecessary calls to
 * findViewById() or getColumnIndex() on each row.
 */
public abstract class BasicViewHolder {
    protected final TextView mDateSeparator;
    protected final TextView mStatusText;
    protected final TextView mTimestampText;
    protected final int mColumnDirectionIdx;
    protected final int mColumnTimestampIdx;
    protected final int mColumnStatusIdx;
    protected final int mColumnReasonCodeIdx;
    protected final int mColumnMimetypeIdx;

    /**
     * Constructor
     *
     * @param base view
     * @param cursor cursor
     */
    BasicViewHolder(View base, Cursor cursor) {
        /* Save column indexes */
        mColumnDirectionIdx = cursor.getColumnIndexOrThrow(HistoryLog.DIRECTION);
        mColumnTimestampIdx = cursor.getColumnIndexOrThrow(HistoryLog.TIMESTAMP);
        mColumnStatusIdx = cursor.getColumnIndexOrThrow(HistoryLog.STATUS);
        mColumnMimetypeIdx = cursor.getColumnIndexOrThrow(HistoryLog.MIME_TYPE);
        mColumnReasonCodeIdx = cursor.getColumnIndexOrThrow(HistoryLog.REASON_CODE);
        /* Save children views */
        mDateSeparator = (TextView) base.findViewById(R.id.date_separator);
        mStatusText = (TextView) base.findViewById(R.id.status_text);
        mTimestampText = (TextView) base.findViewById(R.id.timestamp_text);
    }

    public TextView getStatusText() {
        return mStatusText;
    }

    public TextView getTimestampText() {
        return mTimestampText;
    }

    public int getColumnDirectionIdx() {
        return mColumnDirectionIdx;
    }

    public int getColumnTimestampIdx() {
        return mColumnTimestampIdx;
    }

    public int getColumnStatusIdx() {
        return mColumnStatusIdx;
    }

    public int getColumnMimetypeIdx() {
        return mColumnMimetypeIdx;
    }

    public int getColumnReasonCodeIdx() {
        return mColumnReasonCodeIdx;
    }
}
