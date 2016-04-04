/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010-2016 Orange.
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

package com.gsma.rcs.ri.messaging.adapter;

import com.gsma.rcs.ri.R;
import com.gsma.services.rcs.history.HistoryLog;

import android.database.Cursor;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A ViewHolder class keeps references to children views to avoid unnecessary calls to
 * findViewById() or getColumnIndex() on each row.
 */
public class BasicViewHolder {
    protected final int mColumnContactIdx;
    protected final TextView mDateSeparator;
    protected final TextView mStatusText;
    protected final TextView mTimestampText;
    protected final int mColumnDirectionIdx;
    protected final int mColumnTimestampIdx;
    protected final TextView mContactText;
    protected final int mColumnStatusIdx;
    protected final int mColumnReasonCodeIdx;
    protected final int mColumnMimetypeIdx;
    protected final RelativeLayout mDivider;
    protected final int mColumnReadStatusIdx;
    protected final int mColumnIdIdx;

    /**
     * Constructor
     *
     * @param base view
     * @param cursor cursor
     */
    public BasicViewHolder(View base, Cursor cursor) {
        /* Save column indexes */
        mColumnDirectionIdx = cursor.getColumnIndexOrThrow(HistoryLog.DIRECTION);
        mColumnTimestampIdx = cursor.getColumnIndexOrThrow(HistoryLog.TIMESTAMP);
        mColumnStatusIdx = cursor.getColumnIndexOrThrow(HistoryLog.STATUS);
        mColumnMimetypeIdx = cursor.getColumnIndexOrThrow(HistoryLog.MIME_TYPE);
        mColumnReasonCodeIdx = cursor.getColumnIndexOrThrow(HistoryLog.REASON_CODE);
        mColumnContactIdx = cursor.getColumnIndexOrThrow(HistoryLog.CONTACT);
        mColumnReadStatusIdx = cursor.getColumnIndexOrThrow(HistoryLog.READ_STATUS);
        mColumnIdIdx = cursor.getColumnIndexOrThrow(HistoryLog.ID);
        /* Save children views */
        mDateSeparator = (TextView) base.findViewById(R.id.date_separator);
        mDivider = (RelativeLayout) base.findViewById(R.id.talk_item_divider);
        mStatusText = (TextView) base.findViewById(R.id.status_text);
        mTimestampText = (TextView) base.findViewById(R.id.timestamp_text);
        mContactText = (TextView) base.findViewById(R.id.contact_text);
    }

    public TextView getStatusText() {
        return mStatusText;
    }

    public TextView getTimestampText() {
        return mTimestampText;
    }

    public TextView getContactText() {
        return mContactText;
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

    public int getColumnContactIdx() {
        return mColumnContactIdx;
    }

    public int getColumnReadStatusIdx() {
        return mColumnReadStatusIdx;
    }

    public int getColumnIdIdx() {
        return mColumnIdIdx;
    }
}
