package com.gsma.rcs.cms.provider.xms.model;

import com.gsma.services.rcs.RcsService.Direction;
import com.gsma.services.rcs.RcsService.ReadStatus;

import android.util.SparseArray;

public class XmsData {
    
    /* package private */ String mBaseId;
    /* package private */ Long mNativeProviderId;
    /* package private */ Long mNativeThreadId;
    /* package private */ String mContact;
    /* package private */ String mContent;
    /* package private */ long mDate;    
    /* package private */ Direction mDirection;
    /* package private */ MimeType mMimeType;
    /* package private */ ReadStatus mReadStatus = ReadStatus.UNREAD;
    /* package private */ DeleteStatus mDeleteStatus = DeleteStatus.NOT_DELETED;
    /* package private */ PushStatus mPushStatus= PushStatus.PUSHED;
    /* package private */ long mDeliveryDate;
    
    public enum MimeType {

        /**
         * SMS
         */
        SMS(0),

        /**
         * MMS
         */
        MMS(1);

        private final int mValue;

        private static SparseArray<MimeType> mValueToEnum = new SparseArray<MimeType>();
        static {
            for (MimeType entry : MimeType.values()) {
                mValueToEnum.put(entry.toInt(), entry);
            }
        }

        private MimeType(int value) {
            mValue = value;
        }

        public final int toInt() {
            return mValue;
        }

        public final static MimeType valueOf(int value) {
            MimeType entry = mValueToEnum.get(value);
            if (entry != null) {
                return entry;
            }
            throw new IllegalArgumentException(new StringBuilder("No enum const class ")
                    .append(MimeType.class.getName()).append(".").append(value).append("!")
                    .toString());
        }
    }
        
    /**
     * Read status of the message
     */
    public enum ReadStatus {
        /**
         * The message has not yet been displayed in the UI.
         */
        UNREAD(0),
        /**
         * The message has been displayed in the UI and not synchronized with the CMS server
         */
        READ_REQUESTED(1),
        /**
         * The message has been displayed in the UI and synchronized with the CMS server
         */
        READ(2);


        private final int mValue;

        private static SparseArray<ReadStatus> mValueToEnum = new SparseArray<ReadStatus>();
        static {
            for (ReadStatus entry : ReadStatus.values()) {
                mValueToEnum.put(entry.toInt(), entry);
            }
        }

        private ReadStatus(int value) {
            mValue = value;
        }

        /**
         * Gets integer value associated to ReadStatus instance
         * 
         * @return value
         */
        public final int toInt() {
            return mValue;
        }

        /**
         * Returns a ReadStatus instance for the specified integer value.
         * 
         * @param value
         * @return instance
         */
        public final static ReadStatus valueOf(int value) {
            ReadStatus entry = mValueToEnum.get(value);
            if (entry != null) {
                return entry;
            }
            throw new IllegalArgumentException(new StringBuilder("No enum const class ")
                    .append(ReadStatus.class.getName()).append(".").append(value).append("!")
                    .toString());
        }
    }
    
    /**
     * Read status of the message
     */
    public enum DeleteStatus {
        /**
         * The message has not yet been deleted from the UI.
         */
        NOT_DELETED(0),
        /**
         * The message has been deleted from the UI but not synchronized with the CMS server
         */
        DELETED_REQUESTED(1),
        /**
         * The message has been deleted from the UI and synchronized with the CMS server
         */
        DELETED(2);


        private final int mValue;

        private static SparseArray<DeleteStatus> mValueToEnum = new SparseArray<DeleteStatus>();
        static {
            for (DeleteStatus entry : DeleteStatus.values()) {
                mValueToEnum.put(entry.toInt(), entry);
            }
        }

        private DeleteStatus(int value) {
            mValue = value;
        }

        /**
         * Gets integer value associated to ReadStatus instance
         * 
         * @return value
         */
        public final int toInt() {
            return mValue;
        }

        /**
         * Returns a ReadStatus instance for the specified integer value.
         * 
         * @param value
         * @return instance
         */
        public final static DeleteStatus valueOf(int value) {
            DeleteStatus entry = mValueToEnum.get(value);
            if (entry != null) {
                return entry;
            }
            throw new IllegalArgumentException(new StringBuilder("No enum const class ")
                    .append(DeleteStatus.class.getName()).append(".").append(value).append("!")
                    .toString());
        }
    }
    
    /**
     * Push status of the message
     */
    public enum PushStatus {
        /**
         * The message should be pushed on CMS
         */
        PUSH_REQUESTED(0),
        /**
         * The message has been pushed on CMS
         */
        PUSHED(1);


        private final int mValue;

        private static SparseArray<PushStatus> mValueToEnum = new SparseArray<PushStatus>();
        static {
            for (PushStatus entry : PushStatus.values()) {
                mValueToEnum.put(entry.toInt(), entry);
            }
        }

        private PushStatus(int value) {
            mValue = value;
        }

        /**
         * Gets integer value associated to ReadStatus instance
         * 
         * @return value
         */
        public final int toInt() {
            return mValue;
        }

        /**
         * Returns a ReadStatus instance for the specified integer value.
         * 
         * @param value
         * @return instance
         */
        public final static PushStatus valueOf(int value) {
            PushStatus entry = mValueToEnum.get(value);
            if (entry != null) {
                return entry;
            }
            throw new IllegalArgumentException(new StringBuilder("No enum const class ")
                    .append(PushStatus.class.getName()).append(".").append(value).append("!")
                    .toString());
        }
    }
    
//    XmsLogData(){
//    }
//
//    XmsLogData(Long nativeProviderId, String contact, String content, long date, Direction direction) {
//        mNativeProviderId = nativeProviderId;
//        mContact = contact;
//        mContent = content;
//        mDate = date;
//        mDirection = direction;
//    };
    
//    XmsLogData(long nativeProviderId, String contact, String content, long date, Direction direction,
//            ReadStatus readStatus, long deliveryDate) {
//        mNativeProviderId = nativeProviderId;
//        mContact = contact;
//        mContent = content;
//        mDate = date;
//        mDirection = direction;
//        mReadStatus = readStatus;
//        mDeliveryDate = deliveryDate;
//    };

    public String getBaseId() {
        return mBaseId;
    }

    public void setBaseId(String mBaseId) {
        this.mBaseId = mBaseId;
    }

    public Long getNativeProviderId() {
        return mNativeProviderId;
    }

    public void setNativeProviderId(Long mNativeProviderId) {
        this.mNativeProviderId = mNativeProviderId;
    }

    public Long getNativeThreadId() {
        return mNativeThreadId;
    }

    public void setNativeThreadId(Long mNativeThreadId) {
        this.mNativeThreadId = mNativeThreadId;
    }
    public String getContact() {
        return mContact;
    }

    public void setContact(String mContact) {
        this.mContact = mContact;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long mDate) {
        this.mDate = mDate;
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void setDirection(Direction mDirection) {
        this.mDirection = mDirection;
    }

    public MimeType getMimeType() {
        return mMimeType;
    }

    public void setMimeType(MimeType mMimeType) {
        this.mMimeType = mMimeType;
    }

    public ReadStatus getReadStatus() {
        return mReadStatus;
    }

    public void setReadStatus(ReadStatus mReadStatus) {
        this.mReadStatus = mReadStatus;
    }

    public DeleteStatus getDeleteStatus() {
        return mDeleteStatus;
    }

    public void setDeleteStatus(DeleteStatus mDeleteStatus) {
        this.mDeleteStatus = mDeleteStatus;
    }

    public PushStatus getPushStatus() {
        return mPushStatus;
    }

    public void setPushStatus(PushStatus mPushStatus) {
        this.mPushStatus = mPushStatus;
    }

    public long getDeliveryDate() {
        return mDeliveryDate;
    }

    public void setDeliveryDate(long mDeliveryDate) {
        this.mDeliveryDate = mDeliveryDate;
    }

}