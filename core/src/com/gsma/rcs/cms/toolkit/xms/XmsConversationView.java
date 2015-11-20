package com.gsma.rcs.cms.toolkit.xms;

import com.gsma.rcs.R;
import com.gsma.rcs.cms.CmsService;
import com.gsma.rcs.cms.event.INativeXmsEventListener;
import com.gsma.rcs.cms.imap.service.ImapServiceManager;
import com.gsma.rcs.cms.imap.service.ImapServiceManager.ImapServiceListener;
import com.gsma.rcs.cms.imap.service.ImapServiceNotAvailableException;
import com.gsma.rcs.cms.imap.task.BasicSynchronizationTask;
import com.gsma.rcs.cms.imap.task.BasicSynchronizationTask.BasicSynchronizationTaskListener;
import com.gsma.rcs.cms.observer.XmsObserverUtils;
import com.gsma.rcs.cms.provider.imap.ImapLog;
import com.gsma.rcs.cms.provider.settings.CmsSettings;
import com.gsma.rcs.cms.provider.xms.PartData;
import com.gsma.rcs.cms.provider.xms.PartLog;
import com.gsma.rcs.cms.provider.xms.XmsLogData;
import com.gsma.rcs.cms.provider.xms.XmsLog;
import com.gsma.rcs.cms.provider.xms.model.MmsPart;
import com.gsma.rcs.cms.provider.xms.model.XmsData;
import com.gsma.rcs.cms.provider.xms.model.XmsData.DeleteStatus;
import com.gsma.rcs.cms.provider.xms.model.XmsData.ReadStatus;
import com.gsma.rcs.cms.provider.xms.model.MmsData;
import com.gsma.rcs.cms.provider.xms.model.SmsData;
import com.gsma.rcs.cms.storage.LocalStorage;
import com.gsma.rcs.cms.utils.MmsUtils;
import com.gsma.rcs.utils.logger.Logger;
import com.gsma.services.rcs.RcsService.Direction;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class XmsConversationView extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, INativeXmsEventListener, BasicSynchronizationTaskListener, ImapServiceListener {

    private final static Logger sLogger = Logger.getLogger(XmsConversationView.class.getSimpleName());

    /**
     * The loader's unique ID. Loader IDs are specific to the Activity in which they reside.
     */
    protected static final int LOADER_ID = 1;

    private int SIZE_100_DP;

    private static class Xms{

        private final static String SORT = new StringBuilder(
                XmsLogData.KEY_DATE).append(" ASC").toString();

        private final static String WHERE = new StringBuilder(XmsLogData.KEY_CONTACT)
                .append("=?").append(" AND " ).append(XmsLogData.KEY_DELETE_STATUS).append("=").append(DeleteStatus.NOT_DELETED.toInt()).toString();

        private final static String[] PROJECTION = new String[]{
                XmsLogData.KEY_BASECOLUMN_ID,
                XmsLogData.KEY_NATIVE_PROVIDER_ID,
                XmsLogData.KEY_CONTACT,
                XmsLogData.KEY_CONTENT,
                XmsLogData.KEY_DATE,
                XmsLogData.KEY_DIRECTION,
                XmsLogData.KEY_READ_STATUS,
                XmsLogData.KEY_MIME_TYPE,
                XmsLogData.KEY_MMS_ID,
                XmsLogData.KEY_SUBJECT
        };
    }

    private XmsLogAdapter mAdapter;
    private XmsLog mXmsLog;
    private PartLog mPartLog;
    private final static String EXTRA_CONTACT = "contact";

    private String mContact;
    private EditText mContent;
    private ListView mListView;
    private TextView mSyncButton;
    
    private final static int MENU_ITEM_DELETE_MSG = 1; 
    
    private CmsService mCmsService;

    private class ImageViewOnClickListener implements OnClickListener{

        private MmsPart mMmsPart;

        ImageViewOnClickListener(MmsPart mmsPart){
            mMmsPart = mmsPart;
        }

        @Override
        public void onClick(View view) {
            String nativeId = mMmsPart.getNativeId();
            Uri uri;
            if(nativeId!=null){
                uri =  Uri.parse(XmsObserverUtils.Mms.Part.URI.concat(nativeId));
            }
            else{
                uri = Uri.withAppendedPath(PartData.CONTENT_URI, mMmsPart.getBaseId());
            }
            hasStartedDisplayImageActivity = true;
            startActivity(XmsImageView.forgeIntentToStart(XmsConversationView.this, uri));
        }
    }


    private boolean hasStartedDisplayImageActivity = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SIZE_100_DP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());

        setContentView(R.layout.rcs_cms_toolkit_xms_conversation_view);
        
        mContact = getIntent().getStringExtra(EXTRA_CONTACT);
        mXmsLog = XmsLog.getInstance(getApplicationContext());
        mPartLog = PartLog.getInstance(getApplicationContext());
        mAdapter = new XmsLogAdapter(this);
        TextView emptyView = (TextView) findViewById(android.R.id.empty);
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(emptyView);
        mListView.setAdapter(mAdapter);
        registerForContextMenu(mListView);
                
        mContent = (EditText) findViewById(R.id.rcs_cms_toolkit_xms_send_content);
        (findViewById(R.id.rcs_cms_toolkit_xms_send_message_btn)).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                String messageContent = mContent.getText().toString();
                if(messageContent.isEmpty()){
                    return;
                }
                new SmsSender(getApplicationContext(), mContact, mContent.getText().toString()).send();
                mContent.setText("");
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);                
            }            
        });
        
        mSyncButton = (TextView)findViewById(R.id.rcs_cms_toolkit_sync_btn);
        mSyncButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                displaySyncButton(false);
                try {      
                    new BasicSynchronizationTask(
                            ImapServiceManager.getService(CmsSettings.getInstance()),                 
                            LocalStorage.createInstance(ImapLog.getInstance(getApplicationContext())),
                            XmsConversationView.this
                            ).execute(new String[] {});
                } catch (ImapServiceNotAvailableException e) {                
                    Toast.makeText(XmsConversationView.this, getString(R.string.label_cms_toolkit_xms_sync_already_in_progress), Toast.LENGTH_LONG).show();
                }                 
            }
            
        });
        
        mCmsService = CmsService.getInstance();                     
    }

    @Override
    protected void onResume() {
        super.onResume();              
        mCmsService.registerSmsObserverListener(this);
        if(!hasStartedDisplayImageActivity ) {
            refreshView();
        }
        hasStartedDisplayImageActivity = false;
        if(!mXmsLog.getMessages(mContact, ReadStatus.UNREAD).isEmpty()){
            markConversationAsRead(mContact);
        }
        checkImapServiceStatus();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mCmsService.unregisterSmsObserverListener(this);
        ImapServiceManager.unregisterListener(this);
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCmsService.unregisterSmsObserverListener(this);
        ImapServiceManager.unregisterListener(this);
    }

    /**
     * Messaging log adapter
     */
    private class XmsLogAdapter extends CursorAdapter {

        private static final int VIEW_TYPE_SMS_IN = 0;
        private static final int VIEW_TYPE_SMS_OUT = 1;
        private static final int VIEW_TYPE_MMS_IN = 2;
        private static final int VIEW_TYPE_MMS_OUT = 3;
        private final int[] VIEW_TYPES = new int[]{
                VIEW_TYPE_SMS_IN,
                VIEW_TYPE_SMS_OUT,
                VIEW_TYPE_MMS_IN,
                VIEW_TYPE_MMS_OUT};

        private LayoutInflater mInflater;

        public XmsLogAdapter(Context context) {
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
        }

        private abstract class SmsViewHolder {

            RelativeLayout mItemLayout;
            TextView mContent;
            TextView mDate;
            int contactIdx;
            int contentIdx;
            int dateIdx;
            int directionIdx;

            private SmsViewHolder(View view, Cursor cursor){
                contactIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_CONTACT);
                contentIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_CONTENT);
                dateIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_DATE);
                directionIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_DIRECTION);
                mContent = (TextView) view.findViewById(R.id.rcs_cms_toolkit_xms_content);
                mDate = (TextView) view.findViewById(R.id.rcs_cms_toolkit_xms_date);
            }
        }

        private class MmsViewHolder extends SmsViewHolder {

            TextView mSubject;
            LinearLayout mImagesLayout;
            int mmsIdIdx;
            int subjectIdx;
            LinearLayout.LayoutParams imageParams;

            private MmsViewHolder(View view, Cursor cursor) {
                super(view, cursor);
                mmsIdIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_MMS_ID);
                subjectIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_SUBJECT);
                mSubject = (TextView) view.findViewById(R.id.rcs_cms_toolkit_xms_subject);
                mImagesLayout = (LinearLayout) view.findViewById(R.id.rcs_cms_toolkit_xms_images_layout);
                imageParams = new LinearLayout.LayoutParams(SIZE_100_DP,SIZE_100_DP);
                imageParams.bottomMargin = SIZE_100_DP/10;
            }
        }

        private class SmsInViewHolder extends SmsViewHolder {
            private SmsInViewHolder(View view, Cursor cursor){
                super(view, cursor);
                mItemLayout = (RelativeLayout) view.findViewById(R.id.rcs_cms_toolkit_xms_conv_item_sms_in);
            }
        }

        private class SmsOutViewHolder extends SmsViewHolder {
            private SmsOutViewHolder(View view, Cursor cursor){
                super(view, cursor);
                mItemLayout = (RelativeLayout) view.findViewById(R.id.rcs_cms_toolkit_xms_conv_item_sms_out);
            }
        }

        private class MmsInViewHolder extends MmsViewHolder {
            private MmsInViewHolder(View view, Cursor cursor){
                super(view, cursor);
                mItemLayout = (RelativeLayout) view.findViewById(R.id.rcs_cms_toolkit_xms_conv_item_mms_in);
            }
        }

        private class MmsOutViewHolder extends MmsViewHolder {
            private MmsOutViewHolder(View view, Cursor cursor){
                super(view, cursor);
                mItemLayout = (RelativeLayout) view.findViewById(R.id.rcs_cms_toolkit_xms_conv_item_mms_out);
            }
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            int viewType  = getItemViewType(cursor);
            if(VIEW_TYPE_SMS_IN == viewType){
                final View view = mInflater.inflate(R.layout.rcs_cms_toolkit_xms_conversation_item_sms_in, parent, false);
                view.setTag(new SmsInViewHolder(view, cursor));
                return view;
            }
            if(VIEW_TYPE_SMS_OUT == viewType){
                final View view = mInflater.inflate(R.layout.rcs_cms_toolkit_xms_conversation_item_sms_out, parent, false);
                view.setTag(new SmsOutViewHolder(view, cursor));
                return view;
            }
            else if(VIEW_TYPE_MMS_IN == viewType){
                final View view = mInflater.inflate(R.layout.rcs_cms_toolkit_xms_conversation_item_mms_in, parent, false);
                view.setTag(new MmsInViewHolder(view, cursor));
                return view;
            }
            else { // MMS OUT
                final View view = mInflater.inflate(R.layout.rcs_cms_toolkit_xms_conversation_item_mms_out, parent, false);
                view.setTag(new MmsOutViewHolder(view, cursor));
                return view;
            }
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int viewType  = getItemViewType(cursor);
            if(VIEW_TYPE_SMS_IN  == viewType || VIEW_TYPE_SMS_OUT  == viewType){
                bindSmsView(view, cursor);
            }
            else{ //MMS
                bindMmsView(view, context, cursor);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewType((Cursor)getItem(position));
        }

        private int getItemViewType(Cursor cursor){
            XmsData.MimeType mimeType = XmsData.MimeType.valueOf(cursor.getInt(cursor.getColumnIndex(XmsLogData.KEY_MIME_TYPE)));
            Direction direction = Direction.valueOf(cursor.getInt(cursor.getColumnIndex(XmsLogData.KEY_DIRECTION)));
            if(XmsData.MimeType.SMS == mimeType){
                if(Direction.INCOMING == direction ){
                    return VIEW_TYPE_SMS_IN;
                }
                else{
                    return VIEW_TYPE_SMS_OUT;
                }
            }
            else if(Direction.INCOMING == direction ){
                return VIEW_TYPE_MMS_IN;
            }
            else{
                return VIEW_TYPE_MMS_OUT;
            }
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPES.length;
        }

        private void bindSmsView(View view, Cursor cursor){

            SmsViewHolder holder = (SmsViewHolder)view.getTag();
            // Set the date/time field by mixing relative and absolute times
            long date = cursor.getLong(holder.dateIdx);
            holder.mDate.setText(DateUtils.getRelativeTimeSpanString(date,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE));
            holder.mContent.setText(cursor.getString(holder.contentIdx));
        }

        private void bindMmsView(View view, Context context, Cursor cursor){

            MmsViewHolder holder = (MmsViewHolder)view.getTag();
            // Set the date/time field by mixing relative and absolute times
            long date = cursor.getLong(holder.dateIdx);
            holder.mDate.setText(DateUtils.getRelativeTimeSpanString(date,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE));
            String mmsId = cursor.getString(holder.mmsIdIdx);
            holder.mContent.setText(cursor.getString(holder.contentIdx));
            String subject = cursor.getString(holder.subjectIdx);
            if (subject != null) {
                holder.mSubject.setText(subject);
            }

            holder.mImagesLayout.removeAllViews();
            for(MmsPart mmsPart : mPartLog.getParts(mmsId, true)){
                // filter out content type that are not image
                if(!MmsUtils.CONTENT_TYPE_IMAGE.contains(mmsPart.getContentType())){
                    continue;
                }
                ImageView imageView = new ImageView(context);
                imageView.setOnClickListener(new ImageViewOnClickListener(mmsPart));
                imageView.setLayoutParams(holder.imageParams);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(mmsPart.getThumb(), 0, mmsPart.getThumb().length));
                holder.mImagesLayout.addView(imageView);
            }
        }

    }

    /**
     * Forge intent to start XmsConversationView activity
     * 
     * @param context The context
     * @param contact The address
     * @return intent
     */
    public static Intent forgeIntentToStart(Context context, String contact) {
        Intent intent = new Intent(context, XmsConversationView.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_CONTACT, contact);
        return intent;
    }
    
    private void refreshView(){
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }
    
    @Override
    public void onIncomingSms(SmsData message) {
        if(!message.getContact().equals(mContact)){
            return;
        }
        markMessageAsRead(message.getContact(), message.getBaseId());
        refreshView();
    }

    @Override
    public void onOutgoingSms(SmsData message) {
        refreshView();
    }

    @Override
    public void onDeliverNativeSms(long nativeProviderId, long sentDate) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onDeleteNativeSms(long nativeProviderId) {
        // TODO Auto-generated method stub   
    }

    @Override
    public void onIncomingMms(MmsData message) {
        if(!message.getContact().equals(mContact)){
            return;
        }
        markMessageAsRead(message.getContact(), message.getBaseId());
        refreshView();
    }

    @Override
    public void onOutgoingMms(MmsData message) {
        refreshView();
    }

    @Override
    public void onDeleteNativeMms(String mmsId) {

    }

    @Override
    public void onReadNativeConversation(long nativeThreadId) {

    }

    @Override
    public void onDeleteNativeConversation(long nativeThreadId) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_ITEM_DELETE_MSG, MENU_ITEM_DELETE_MSG, R.string.rcs_cms_toolkit_xms_message_delete);        
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Cursor cursor = (Cursor) (mAdapter.getItem(info.position));

        if(XmsData.MimeType.SMS == XmsData.MimeType.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(XmsLogData.KEY_MIME_TYPE)))){
            CmsService.getInstance().onDeleteRcsSms(
                    mContact,
                    cursor.getString(cursor.getColumnIndex(XmsLogData.KEY_BASECOLUMN_ID)));
        }
        else{ // MMS
            CmsService.getInstance().onDeleteRcsMms(
                    mContact,
                    cursor.getString(cursor.getColumnIndex(XmsLogData.KEY_BASECOLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(XmsLogData.KEY_MMS_ID)));
        }
        refreshView();
        return true;
    }
    
    private void markConversationAsRead(String contact){
        CmsService.getInstance().onReadRcsConversation(contact);
    }

    private void markMessageAsRead(String contact, String baseId){
        CmsService.getInstance().onReadRcsMessage(contact, baseId);
    }

    @Override
    public void onBasicSynchronizationTaskExecuted(String[] params, Boolean result) {
        if(sLogger.isActivated()) {
            sLogger.info("onBasicSynchronizationTaskExecuted");
        }
        if(!result){
            Toast.makeText(this, getString(R.string.label_cms_toolkit_xms_sync_impossible), Toast.LENGTH_LONG).show();
        }
        displaySyncButton(true);
        refreshView();
    }
    
    private void checkImapServiceStatus(){
        if(!ImapServiceManager.isAvailable()){
            ImapServiceManager.registerListener(this);
            displaySyncButton(false);
        } else{
            displaySyncButton(true);
        }
    }
    
    private void displaySyncButton(boolean display){
        if(display){
            mSyncButton.setVisibility(View.VISIBLE); 
            findViewById(R.id.rcs_cms_toolkit_xms_progressbar).setVisibility(View.GONE);            
        }
        else{
            mSyncButton.setVisibility(View.GONE); 
            findViewById(R.id.rcs_cms_toolkit_xms_progressbar).setVisibility(View.VISIBLE);            
            
        }    
    }

    @Override
    public void onImapServiceAvailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displaySyncButton(true);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        /* Create a new CursorLoader with the following query parameters. */
        return new CursorLoader(this, XmsLogData.CONTENT_URI, Xms.PROJECTION, Xms.WHERE, new String[]{mContact}, Xms.SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        /* A switch-case is useful when dealing with multiple Loaders/IDs */
        switch (loader.getId()) {
            case LOADER_ID:
                /*
                 * The asynchronous load is complete and the data is now available for use. Only now
                 * can we associate the queried Cursor with the CursorAdapter.
                 */
                mAdapter.swapCursor(cursor);
                mListView.smoothScrollToPosition(mAdapter.getCount());
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {        /*
         * For whatever reason, the Loader's data is now unavailable. Remove any references to the
         * old data by replacing it with a null Cursor.
         */
        mAdapter.swapCursor(null);
    }
}