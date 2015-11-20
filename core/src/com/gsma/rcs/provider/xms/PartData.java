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

package com.gsma.rcs.provider.xms;

import com.gsma.services.rcs.cms.MmsPartLog;
import com.gsma.services.rcs.contact.ContactId;

import android.net.Uri;

/**
 * Xms data constants
 */
public class PartData {
    /**
     * Database URIs
     */
    public static final Uri CONTENT_URI = Uri.parse("content://com.gsma.rcs.xms/part");

    /**
     * Primary key unique ID
     */
    public static final String KEY_PART_ID = MmsPartLog.BASECOLUMN_ID;

    /**
     * Unique message identifier.
     */
    public static final String KEY_MESSAGE_ID = MmsPartLog.MESSAGE_ID;

    /**
     * Multipurpose Internet Mail Extensions (MIME) type of message.
     */
    public static final String KEY_MIME_TYPE = MmsPartLog.MIME_TYPE;

    /**
     * File name.
     */
    public static final String KEY_FILENAME = MmsPartLog.FILENAME;

    /**
     * File size.
     */
    public static final String KEY_FILESIZE = MmsPartLog.FILESIZE;

    /**
     * Byte array of the content.
     */
    public static final String KEY_CONTENT = MmsPartLog.CONTENT;

    /**
     * Byte array of the file icon.
     */
    public static final String KEY_FILEICON = MmsPartLog.FILEICON;

    /**
     * ContactId formatted number of remote contact.
     */
    public static final String KEY_CONTACT = MmsPartLog.CONTACT;

    private final long mId;
    private final String mMessageId;
    private final ContactId mContact;
    private final String mMimeType;
    private final String mFilename;
    private final Long mFileSize;
    private final byte[] mContent;
    private final byte[] mFileIcon;

    public PartData(long id, String messageId, ContactId contact, String mimeType, String filename,
            Long fileSize, byte[] content, byte[] fileIcon) {
        mId = id;
        mMessageId = messageId;
        mMimeType = mimeType;
        mFilename = filename;
        mFileSize = fileSize;
        mContent = content;
        mFileIcon = fileIcon;
        mContact = contact;
    }

    public long getId() {
        return mId;
    }

    public String getMessageId() {
        return mMessageId;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public String getFilename() {
        return mFilename;
    }

    public Long getFileSize() {
        return mFileSize;
    }

    public byte[] getContent() {
        return mContent;
    }

    public byte[] getFileIcon() {
        return mFileIcon;
    }

    public ContactId getContact() {
        return mContact;
    }
}