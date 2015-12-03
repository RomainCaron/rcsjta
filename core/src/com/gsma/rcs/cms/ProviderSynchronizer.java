package com.gsma.rcs.cms;

import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.provider.Telephony.BaseMmsColumns;
import android.provider.Telephony.TextBasedSmsColumns;

import com.gsma.rcs.cms.observer.XmsObserverUtils;
import com.gsma.rcs.cms.observer.XmsObserverUtils.Mms;
import com.gsma.rcs.cms.observer.XmsObserverUtils.Mms.Part;
import com.gsma.rcs.cms.provider.imap.ImapLog;
import com.gsma.rcs.cms.provider.imap.MessageData;
import com.gsma.rcs.cms.provider.imap.MessageData.DeleteStatus;
import com.gsma.rcs.cms.provider.imap.MessageData.MessageType;
import com.gsma.rcs.cms.provider.imap.MessageData.PushStatus;
import com.gsma.rcs.cms.utils.CmsUtils;
import com.gsma.rcs.cms.utils.MmsUtils;
import com.gsma.rcs.provider.CursorUtil;
import com.gsma.rcs.provider.settings.RcsSettings;
import com.gsma.rcs.provider.xms.XmsLog;
import com.gsma.rcs.provider.xms.model.MmsDataObject;
import com.gsma.rcs.provider.xms.model.MmsDataObject.MmsPart;
import com.gsma.rcs.provider.xms.model.SmsDataObject;
import com.gsma.rcs.utils.ContactUtil;
import com.gsma.rcs.utils.ContactUtil.PhoneNumber;
import com.gsma.rcs.utils.IdGenerator;
import com.gsma.rcs.utils.logger.Logger;
import com.gsma.services.rcs.RcsService.Direction;
import com.gsma.services.rcs.RcsService.ReadStatus;
import com.gsma.services.rcs.contact.ContactId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ProviderSynchronizer extends AsyncTask<String,String,Boolean> {

    private static final Logger sLogger = Logger.getLogger(ProviderSynchronizer.class.getSimpleName());

    private static Uri sSmsUri = Uri.parse("content://sms/");
    private static Uri sMmsUri = Uri.parse("content://mms/");

    private final String[] PROJECTION_SMS = new String[]{
            BaseColumns._ID,
            TextBasedSmsColumns.THREAD_ID,
            TextBasedSmsColumns.ADDRESS,
            TextBasedSmsColumns.DATE,
            TextBasedSmsColumns.DATE_SENT,
            TextBasedSmsColumns.PROTOCOL,
            TextBasedSmsColumns.BODY,
            TextBasedSmsColumns.READ};

    private final String[] PROJECTION_ID_READ = new String[]{
            BaseColumns._ID,
            TextBasedSmsColumns.READ
    };

    private static final String SELECTION_CONTACT_NOT_NULL = new StringBuilder(TextBasedSmsColumns.ADDRESS).append(" is not null").toString();
    static final String SELECTION_BASE_ID = new StringBuilder(BaseColumns._ID).append("=?").append(" AND ").append(SELECTION_CONTACT_NOT_NULL).toString();

    private ContentResolver mContentResolver;
    private XmsLog mXmsLog;
    private ImapLog mImapLog;
    private RcsSettings mSettings;

    private List<Long> mNativeIds;
    private List<Long> mNativeReadIds;

    public ProviderSynchronizer(Context context,RcsSettings settings, XmsLog xmsLog, ImapLog imapLog){
        mContentResolver = context.getContentResolver();
        mXmsLog = xmsLog;
        mImapLog = imapLog;
        mSettings = settings;
    }

    private void syncSms(){
        getNativeSmsIds();
        Map<Long,MessageData> rcsMessages = getRcsMessages(MessageType.SMS);
        checkDeletedMessages(MessageType.SMS, rcsMessages);
        checkNewMessages(MessageType.SMS, rcsMessages);
        checkReadMessages(MessageType.SMS, rcsMessages);
    }

    private void syncMms(){
        getNativeMmsIds();
        Map<Long,MessageData> rcsMessages = getRcsMessages(MessageType.MMS);
        checkDeletedMessages(MessageType.MMS, rcsMessages);
        checkNewMessages(MessageType.MMS, rcsMessages);
        checkReadMessages(MessageType.MMS, rcsMessages);
    }

    private void getNativeSmsIds(){
        mNativeIds = new ArrayList<>();
        mNativeReadIds = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(sSmsUri, PROJECTION_ID_READ, null, null, null);
            int idIdx = cursor.getColumnIndex(BaseColumns._ID);
            int readIdx = cursor.getColumnIndex(TextBasedSmsColumns.READ);
            CursorUtil.assertCursorIsNotNull(cursor,sSmsUri);
            while(cursor.moveToNext()) {
                Long id = cursor.getLong(idIdx);
                mNativeIds.add(id);
                if(cursor.getInt(readIdx) == 1){
                    mNativeReadIds.add(id);
                }
            }
        } finally {
            CursorUtil.close(cursor);
        }
    }

    private void getNativeMmsIds(){
        Cursor cursor = null;
        mNativeIds = new ArrayList<>();
        mNativeReadIds = new ArrayList<>();
        try {
            cursor = mContentResolver.query(sMmsUri, PROJECTION_ID_READ, null, null, null);
            int idIdx = cursor.getColumnIndex(BaseColumns._ID);
            int readIdx = cursor.getColumnIndex(TextBasedSmsColumns.READ);
            CursorUtil.assertCursorIsNotNull(cursor,sMmsUri);
            while(cursor.moveToNext()) {
                Long id = cursor.getLong(idIdx);
                mNativeIds.add(id);
                if(cursor.getInt(readIdx) == 1){
                    mNativeReadIds.add(id);
                }
            }
        } finally {
            CursorUtil.close(cursor);
        }
    }

    private Map<Long,MessageData> getRcsMessages(MessageType messageType){
        return mImapLog.getNativeMessages(messageType);
    }

    private void purgeDeletedMessages(){
        int nb = mImapLog.purgeMessages();
        if(sLogger.isActivated()){
            sLogger.debug(nb + " messages have been removed from Imap data");
        }
    }

    /**
     * Check messages deleted from native provider
     * @param messageType
     */
    private void checkDeletedMessages(MessageData.MessageType messageType, Map<Long,MessageData> rcsMessages){
        boolean isActivated = sLogger.isActivated();
        List<Long> deletedIds = new ArrayList<>(rcsMessages.keySet());
        deletedIds.removeAll(mNativeIds);
        for(Long id : deletedIds){
            MessageData messageData = rcsMessages.get(id);
            DeleteStatus deleteStatus = messageData.getDeleteStatus();
            if(DeleteStatus.NOT_DELETED == deleteStatus){
                if(isActivated){
                    sLogger.debug(messageType.toString() + " message is marked as DELETED_REPORT_REQUESTED :" + id);
                }
                deleteStatus =  DeleteStatus.DELETED_REPORT_REQUESTED;
            }
            mImapLog.updateDeleteStatus(messageType, id, deleteStatus);
        }
    }

    private void checkNewMessages(MessageData.MessageType messageType, Map<Long,MessageData> rcsMessages){
        boolean isActivated = sLogger.isActivated();
        List<Long> newIds = new ArrayList<>(mNativeIds);
        newIds.removeAll(rcsMessages.keySet());
        for(Long id : newIds){
            if(MessageType.SMS == messageType){
                SmsDataObject smsData = getSmsFromNativeProvider(id);
                if(smsData!=null){
                    if(isActivated){
                        sLogger.debug(" Importing new SMS message :" + id);
                    }
                    mXmsLog.addSms(smsData);
                    mImapLog.addMessage(new MessageData(
                            CmsUtils.contactToCmsFolder(mSettings, smsData.getContact()),
                            smsData.getReadStatus()== ReadStatus.UNREAD ? MessageData.ReadStatus.UNREAD : MessageData.ReadStatus.READ_REPORT_REQUESTED ,
                            MessageData.DeleteStatus.NOT_DELETED,
                            mSettings.getCmsPushSms() ? PushStatus.PUSH_REQUESTED : PushStatus.PUSHED,
                            MessageType.SMS,
                            smsData.getMessageId(),
                            smsData.getNativeProviderId()
                    ));
                }
            }
            else if (MessageType.MMS == messageType){
                for(MmsDataObject mmsData : getMmsFromNativeProvider(id)){
                    try {
                        if(isActivated){
                            sLogger.debug(" Importing new MMS message :" + id);
                        }
                        mXmsLog.addMms(mmsData);
                        mImapLog.addMessage(new MessageData(
                                CmsUtils.contactToCmsFolder(mSettings, mmsData.getContact()),
                                mmsData.getReadStatus()== ReadStatus.UNREAD ? MessageData.ReadStatus.UNREAD : MessageData.ReadStatus.READ_REPORT_REQUESTED ,
                                MessageData.DeleteStatus.NOT_DELETED,
                                mSettings.getCmsPushMms() ? PushStatus.PUSH_REQUESTED : PushStatus.PUSHED,
                                MessageType.MMS,
                                mmsData.getMessageId(),
                                mmsData.getNativeProviderId()
                        ));
                    } catch (RemoteException e) {//TODO FGI exception handling
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void checkReadMessages(MessageData.MessageType messageType, Map<Long,MessageData> rcsMessages){
        boolean isActivated = sLogger.isActivated();
        List<Long> readIds = new ArrayList<>(mNativeReadIds);
        readIds.retainAll(rcsMessages.keySet());
        for(Long id : readIds){
            if(MessageData.ReadStatus.UNREAD == rcsMessages.get(id).getReadStatus()){
                if(isActivated){
                    sLogger.debug(messageType.toString() + " message is marked as READ_REPORT_REQUESTED :" + id);
                }
                mImapLog.updateReadStatus(messageType, id, MessageData.ReadStatus.READ_REPORT_REQUESTED);
            }
        }
    }

    private SmsDataObject getSmsFromNativeProvider(Long id){

        Cursor cursor = null;
        try {
            cursor = mContentResolver.query(sSmsUri, PROJECTION_SMS, SELECTION_BASE_ID, new String[]{String.valueOf(id)}, null);
            CursorUtil.assertCursorIsNotNull(cursor, sSmsUri);

            if(!cursor.moveToFirst()) {
                return null;
            }
            Long _id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            Long threadId = cursor.getLong(cursor.getColumnIndex(TextBasedSmsColumns.THREAD_ID));
            String  address = cursor.getString(cursor.getColumnIndex(TextBasedSmsColumns.ADDRESS));
            PhoneNumber phoneNumber = ContactUtil.getValidPhoneNumberFromAndroid(address);
            if(phoneNumber==null){
                return null;
            }
            ContactId contactId = ContactUtil.createContactIdFromValidatedData(phoneNumber);
            long  date = cursor.getLong(cursor.getColumnIndex(TextBasedSmsColumns.DATE));
            long date_sent = cursor.getLong(cursor.getColumnIndex(TextBasedSmsColumns.DATE_SENT));
            String  protocol = cursor.getString(cursor.getColumnIndex(TextBasedSmsColumns.PROTOCOL));
            String  body = cursor.getString(cursor.getColumnIndex(TextBasedSmsColumns.BODY));
            int read = cursor.getInt(cursor.getColumnIndex(TextBasedSmsColumns.READ));

            Direction direction = Direction.OUTGOING;
            if(protocol!=null){
                direction = Direction.INCOMING;
            }

            ReadStatus readStatus = ReadStatus.READ;
            if(read==0){
                readStatus = ReadStatus.UNREAD;
            }
            SmsDataObject smsDataObject = new SmsDataObject(
                    IdGenerator.generateMessageID(),
                    contactId,
                    body,
                    direction,
                    readStatus,
                    date,
                    _id,
                    threadId
            );
            smsDataObject.setTimestampDelivered(date_sent);
            return smsDataObject;
        } finally {
            CursorUtil.close(cursor);
        }
    }

    private Collection<MmsDataObject> getMmsFromNativeProvider(Long id){
        List<MmsDataObject> mmsDataObject = new ArrayList<>();
        Long threadId, date;
        date = -1l;
        String mmsId;
        Direction direction = Direction.INCOMING;
        Set<ContactId> contacts = new HashSet<>();
        ReadStatus readStatus;
        Cursor cursor = null;
        try{
            cursor = mContentResolver.query(XmsObserverUtils.Mms.URI, null, new StringBuilder(XmsObserverUtils.Mms.WHERE).append(" AND ").append(BaseColumns._ID).append("=?").toString(), new String[]{String.valueOf(id)}, Telephony.BaseMmsColumns._ID);
            CursorUtil.assertCursorIsNotNull(cursor, XmsObserverUtils.Mms.URI);
            if (!cursor.moveToNext()) {
                return mmsDataObject;
            }
            threadId = cursor.getLong(cursor.getColumnIndex(Telephony.BaseMmsColumns.THREAD_ID));
            mmsId = cursor.getString(cursor.getColumnIndex(Telephony.BaseMmsColumns.MESSAGE_ID));
            readStatus = cursor.getInt(cursor.getColumnIndex(Telephony.BaseMmsColumns.READ))==0 ? ReadStatus.UNREAD : ReadStatus.READ;
            int messageType = cursor.getInt(cursor.getColumnIndex(Telephony.BaseMmsColumns.MESSAGE_TYPE));
            if(128 == messageType){
                direction = Direction.OUTGOING;
            }
            date = cursor.getLong(cursor.getColumnIndex(Telephony.BaseMmsColumns.DATE));
        }
        finally{
            CursorUtil.close(cursor);
        }

        // Get recipients
        Map<ContactId,String> messageIds = new HashMap<>();
        try {
            int type = XmsObserverUtils.Mms.Addr.FROM;
            if(direction == Direction.OUTGOING){
                type = XmsObserverUtils.Mms.Addr.TO;
            }
            cursor = mContentResolver.query(Uri.parse(String.format(XmsObserverUtils.Mms.Addr.URI,id)), XmsObserverUtils.Mms.Addr.PROJECTION, XmsObserverUtils.Mms.Addr.WHERE, new String[]{String.valueOf(type)}, null);
            CursorUtil.assertCursorIsNotNull(cursor, XmsObserverUtils.Mms.Addr.URI);
            int adressIdx = cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS);
            while(cursor.moveToNext()){
                String address = cursor.getString(adressIdx);
                PhoneNumber phoneNumber = ContactUtil.getValidPhoneNumberFromAndroid(address);
                if(phoneNumber == null){
                    if(sLogger.isActivated()){
                        sLogger.info(new StringBuilder("Bad format for contact : ").append(address).toString());
                    }
                    continue;
                }
                ContactId contact = ContactUtil.createContactIdFromValidatedData(phoneNumber);
                messageIds.put(contact, IdGenerator.generateMessageID());
                contacts.add(contact);
            }
        } finally {
            CursorUtil.close(cursor);
        }

        // Get part
        Map<ContactId,List<MmsPart>> mmsParts= new HashMap<>();
        String textContent = null;
        try {
            cursor = mContentResolver.query(Uri.parse(Mms.Part.URI), Mms.Part.PROJECTION, Mms.Part.WHERE, new String[]{String.valueOf(id)}, null);
            CursorUtil.assertCursorIsNotNull(cursor, Mms.Part.URI);
            int _idIdx = cursor.getColumnIndexOrThrow(BaseMmsColumns._ID);
            int filenameIdx = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.NAME);
            int contentTypeIdx = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE);
            int textIdx = cursor.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT);
            int dataIdx = cursor.getColumnIndexOrThrow(Telephony.Mms.Part._DATA);

            while(cursor.moveToNext()){
                String contentType = cursor.getString(contentTypeIdx);
                String text = cursor.getString(textIdx);
                String filename = cursor.getString(filenameIdx);
                String content;
                long fileSize = 0l;
                byte[] fileIcon = null;
                if(Constants.CONTENT_TYPE_TEXT.equals(contentType)){
                    textContent = text;
                }
                String data = cursor.getString(dataIdx);
                if(data != null){
                    content = Part.URI.concat(cursor.getString(_idIdx));
                    byte[] bytes = MmsUtils.getContent(mContentResolver, Uri.parse(content));
                    fileSize = bytes.length;
                    fileIcon = MmsUtils.createThumb(bytes);
                }
                else{
                    content = text;
                }

                for(ContactId contact : contacts){
                    List<MmsPart> mmsPart = mmsParts.get(contact);
                    if(mmsPart == null){
                        mmsPart = new ArrayList<>();
                        mmsParts.put(contact, mmsPart);
                    }
                    mmsPart.add(new MmsPart(
                            messageIds.get(contact),
                            contact,
                            contentType,
                            filename,
                            fileSize,
                            content,
                            fileIcon
                    ));
                }
            }
        }
        finally {
            CursorUtil.close(cursor);
        }

        Iterator<Entry<ContactId, List<MmsPart>>> iter = mmsParts.entrySet().iterator();
        while(iter.hasNext()){
            Entry<ContactId, List<MmsPart>> entry = iter.next();
            ContactId contact = entry.getKey();
            mmsDataObject.add(new MmsDataObject(
                    mmsId,
                    messageIds.get(contact),
                    contact,
                    textContent,
                    direction,
                    readStatus,
                    date*1000,
                    id,
                    threadId,
                    entry.getValue()
            ));
        }
        return mmsDataObject;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        boolean isActivated = sLogger.isActivated();
        if(isActivated){
            sLogger.info(" >>> start sync between providers ...");
        }
        purgeDeletedMessages();
        syncSms();
        syncMms();
        if(isActivated){
            sLogger.info(" <<< end of sync");
        }

        return true;
    }
}
