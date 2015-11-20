
package com.gsma.rcs.cms.toolkit.xms;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gsma.rcs.R;
import com.gsma.rcs.cms.CmsService;
import com.gsma.rcs.cms.event.INativeXmsEventListener;
import com.gsma.rcs.cms.imap.service.ImapServiceManager;
import com.gsma.rcs.cms.imap.service.ImapServiceManager.ImapServiceListener;
import com.gsma.rcs.cms.imap.service.ImapServiceNotAvailableException;
import com.gsma.rcs.cms.imap.task.BasicSynchronizationTask;
import com.gsma.rcs.cms.imap.task.BasicSynchronizationTask.BasicSynchronizationTaskListener;
import com.gsma.rcs.cms.provider.imap.ImapLog;
import com.gsma.rcs.cms.provider.settings.CmsSettings;
import com.gsma.rcs.cms.provider.xms.XmsLogData;
import com.gsma.rcs.cms.provider.xms.model.XmsData.DeleteStatus;
import com.gsma.rcs.cms.provider.xms.model.XmsData.ReadStatus;
import com.gsma.rcs.cms.provider.xms.model.MmsData;
import com.gsma.rcs.cms.provider.xms.model.SmsData;
import com.gsma.rcs.cms.storage.LocalStorage;
import com.gsma.rcs.utils.logger.Logger;
import com.gsma.services.rcs.RcsService.Direction;

public class XmsList extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor>, INativeXmsEventListener, BasicSynchronizationTaskListener, ImapServiceListener {

    /**
     * The loader's unique ID. Loader IDs are specific to the Activity in which they reside.
     */
    protected static final int LOADER_ID = 1;
    private static final Logger sLogger = Logger.getLogger(XmsList.class.getSimpleName());
    private final static String SORT = new StringBuilder(
            XmsLogData.KEY_DATE).append(" DESC").toString();

    private final static String WHERE_CLAUSE = new StringBuilder(XmsLogData.KEY_DELETE_STATUS).append("=").append(DeleteStatus.NOT_DELETED.toInt()).append(" group by ")
            .append(XmsLogData.KEY_CONTACT).toString();

    private final static String[] PROJECTION = new String[]{
            XmsLogData.KEY_BASECOLUMN_ID,
            XmsLogData.KEY_NATIVE_PROVIDER_ID,
            XmsLogData.KEY_CONTACT,
            XmsLogData.KEY_CONTENT,
            XmsLogData.KEY_DATE,
            XmsLogData.KEY_DIRECTION,
            XmsLogData.KEY_READ_STATUS
    };


    private final static int MESSAGE_MAX_SIZE = 25;

    private final static int MENU_ITEM_DELETE_CONV = 1;

    private ListView mListview;
    private SmsLogAdapter mAdapter;
    private CmsService mCmsService;

    private TextView mSyncButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rcs_cms_toolkit_xms_list);

        mAdapter = new SmsLogAdapter(this);
        TextView emptyView = (TextView) findViewById(android.R.id.empty);
        mListview = (ListView) findViewById(android.R.id.list);
        mListview.setEmptyView(emptyView);
        mListview.setAdapter(mAdapter);
        registerForContextMenu(mListview);
        mListview.setOnItemClickListener(getOnItemClickListener());
        Button sendButton = (Button) findViewById(R.id.rcs_cms_toolkit_xms_sendBtn);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(XmsList.this, XmsSendView.class));
            }

        });

        mSyncButton = (TextView) findViewById(R.id.rcs_cms_toolkit_sync_btn);
        mSyncButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySyncButton(false);
                try {
                    new BasicSynchronizationTask(
                            ImapServiceManager.getService(CmsSettings.getInstance()),
                            LocalStorage.createInstance(ImapLog.getInstance(getApplicationContext())),
                            XmsList.this
                    ).execute(new String[]{});
                } catch (ImapServiceNotAvailableException e) {
                    Toast.makeText(XmsList.this, getString(R.string.label_cms_toolkit_xms_sync_already_in_progress), Toast.LENGTH_LONG).show();
                }
            }

        });

        mCmsService = CmsService.getInstance();
    }

    @Override
    protected void onResume() {
        if (sLogger.isActivated()) {
            sLogger.debug("onResume");
        }
        super.onResume();
        mCmsService.registerSmsObserverListener(this);
        checkImapServiceStatus();
        refreshView();
    }

    @Override
    protected void onPause() {
        if (sLogger.isActivated()) {
            sLogger.debug("onPause");
        }
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

    private OnItemClickListener getOnItemClickListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                Cursor cursor = (Cursor)(parent.getAdapter()).getItem(pos);
                startActivity(XmsConversationView.forgeIntentToStart(XmsList.this, cursor.getString(cursor.getColumnIndex(XmsLogData.KEY_CONTACT))));
            }
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_ITEM_DELETE_CONV, MENU_ITEM_DELETE_CONV, R.string.rcs_cms_toolkit_xms_conversation_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Cursor cursor = (Cursor) (mAdapter.getItem(info.position));
        CmsService.getInstance().onDeleteRcsConversation(cursor.getString(cursor.getColumnIndex(XmsLogData.KEY_CONTACT)));
        refreshView();
        return true;
    }

    private void refreshView() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onIncomingSms(SmsData message) {
        if (sLogger.isActivated()) {
            sLogger.debug("onIncomingSms");
        }
        refreshView();
    }

    @Override
    public void onOutgoingSms(SmsData message) {
        if (sLogger.isActivated()) {
            sLogger.debug("onOutgoingSms");
        }
        refreshView();
    }

    @Override
    public void onDeliverNativeSms(long nativeProviderId, long sentDate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteNativeConversation(long nativeThreadId) {
        if (sLogger.isActivated()) {
            sLogger.debug("onDeleteNativeConversation");
        }
        refreshView();
    }

    @Override
    public void onDeleteNativeSms(long nativeProviderId) {
        if (sLogger.isActivated()) {
            sLogger.debug("onDeleteNativeSms");
        }
        refreshView();
    }

    @Override
    public void onIncomingMms(MmsData message) {
        if (sLogger.isActivated()) {
            sLogger.debug("onIncomingMms");
        }
        refreshView();
    }

    @Override
    public void onOutgoingMms(MmsData message) {
        if (sLogger.isActivated()) {
            sLogger.debug("onOutgoingMms");
        }
        refreshView();
    }

    @Override
    public void onDeleteNativeMms(String mmsId) {
        if (sLogger.isActivated()) {
            sLogger.debug("onDeleteNativeMms");
        }
        refreshView();
    }

    @Override
    public void onReadNativeConversation(long nativeThreadId) {
        if (sLogger.isActivated()) {
            sLogger.debug("onReadNativeConversation");
        }
        refreshView();
    }

    @Override
    public void onBasicSynchronizationTaskExecuted(String[] params, Boolean result) {
        if (sLogger.isActivated()) {
            sLogger.info("onBasicSynchronizationTaskExecuted");
        }
        if (!result) {
            Toast.makeText(this, getString(R.string.label_cms_toolkit_xms_sync_impossible), Toast.LENGTH_LONG).show();
        }
        displaySyncButton(true);
        refreshView();
        mListview.setSelection(0);
    }

    private String truncate(String content) {
        if (content!=null && content.length() > MESSAGE_MAX_SIZE) {
            content = content.substring(0, MESSAGE_MAX_SIZE);
            content = content.concat("...");
        }
        return content;

    }

    private void checkImapServiceStatus() {
        if (!ImapServiceManager.isAvailable()) {
            ImapServiceManager.registerListener(this);
            displaySyncButton(false);
        } else {
            displaySyncButton(true);
        }
    }

    private void displaySyncButton(boolean display) {
        if (display) {
            mSyncButton.setVisibility(View.VISIBLE);
            findViewById(R.id.rcs_cms_toolkit_xms_progressbar).setVisibility(View.GONE);
        } else {
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
        return new CursorLoader(this, XmsLogData.CONTENT_URI, PROJECTION, WHERE_CLAUSE, null, SORT);
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

    /**
     * Messaging log adapter
     */
    private class SmsLogAdapter extends CursorAdapter {

        private LayoutInflater mInflater;
        private Drawable mDrawableIncoming;
        private Drawable mDrawableOutgoing;

        public SmsLogAdapter(Context context) {
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
            mContext = context;

            mDrawableIncoming = context.getResources().getDrawable(
                    R.drawable.rcs_cms_toolkit_xms_incoming_call);
            mDrawableOutgoing = context.getResources().getDrawable(
                    R.drawable.rcs_cms_toolkit_xms_outgoing_call);
            mContext = context;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();

            // Set the date/time field by mixing relative and absolute times
            long date = cursor.getLong(holder.dateIdx);
            holder.mDate.setText(DateUtils.getRelativeTimeSpanString(date,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE));

            // Set the status text and destination icon
            Direction direction = Direction.valueOf(cursor.getInt(holder.directionIdx));
            switch (direction) {
                case INCOMING:
                    holder.mDirection.setImageDrawable(mDrawableIncoming);
                    break;
                case OUTGOING:
                    holder.mDirection.setImageDrawable(mDrawableOutgoing);
                case IRRELEVANT:
                    break;
            }
            holder.mContact.setText(cursor.getString(holder.contactIdx));
            holder.mContent.setText(truncate(cursor.getString(holder.contentIdx)));
            holder.mContact.setTypeface(null, Typeface.NORMAL);
            holder.mContent.setTypeface(null, Typeface.NORMAL);
            holder.mContent.setTextColor(holder.defaultColor);
            holder.mContact.setTextColor(holder.defaultColor);
            ReadStatus readStatus = ReadStatus.valueOf(cursor.getInt(holder.readIdx));
            if (readStatus == ReadStatus.UNREAD) {
                holder.mContact.setTextColor(Color.GREEN);
                holder.mContact.setTypeface(null, Typeface.BOLD | Typeface.ITALIC);
                holder.mContent.setTextColor(Color.GREEN);
                holder.mContent.setTypeface(null, Typeface.BOLD | Typeface.ITALIC);
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mInflater.inflate(R.layout.rcs_cms_toolkit_xms_list_item, parent, false);
            view.setTag(new ViewHolder(view, cursor));
            return view;
        }

        private class ViewHolder {

            TextView mContact;
            TextView mContent;
            TextView mDate;
            ImageView mDirection;
            int defaultColor;

            int baseIdIdx;
            int nativeProviderIdIdx;
            int contactIdx;
            int contentIdx;
            int dateIdx;
            int directionIdx;
            int readIdx;

            ViewHolder(View view, Cursor cursor) {

                baseIdIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_BASECOLUMN_ID);
                nativeProviderIdIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_NATIVE_PROVIDER_ID);
                contactIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_CONTACT);
                contentIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_CONTENT);
                dateIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_DATE);
                directionIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_DIRECTION);
                readIdx = cursor.getColumnIndexOrThrow(XmsLogData.KEY_READ_STATUS);

                mContact = (TextView) view.findViewById(R.id.rcs_cms_toolkit_xms_contact);
                mContent = (TextView) view.findViewById(R.id.rcs_cms_toolkit_xms_content);
                mDate = (TextView) view.findViewById(R.id.rcs_cms_toolkit_xms_date);
                mDirection = (ImageView) view.findViewById(R.id.rcs_cms_toolkit_xms_direction_icon);
                defaultColor = mContent.getTextColors().getDefaultColor();
            }
        }
    }
}