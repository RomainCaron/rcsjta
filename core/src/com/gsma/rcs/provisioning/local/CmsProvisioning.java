/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 * Copyright (C) 2014 Sony Mobile Communications AB.
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
 *
 * NOTE: This file has been modified by Sony Mobile Communications AB.
 * Modifications are licensed under the License.
 ******************************************************************************/

package com.gsma.rcs.provisioning.local;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.gsma.rcs.R;
import com.gsma.rcs.cms.provider.imap.ImapLog;
import com.gsma.rcs.cms.provider.imap.MessageData.MessageType;
import com.gsma.rcs.cms.provider.imap.MessageData.PushStatus;
import com.gsma.rcs.provider.LocalContentResolver;
import com.gsma.rcs.provider.settings.RcsSettings;
import com.gsma.rcs.provider.settings.RcsSettingsData;

import static com.gsma.rcs.provisioning.local.Provisioning.saveCheckBoxParam;
import static com.gsma.rcs.provisioning.local.Provisioning.saveIntegerEditTextParam;
import static com.gsma.rcs.provisioning.local.Provisioning.saveStringEditTextParam;
import static com.gsma.rcs.provisioning.local.Provisioning.setCheckBoxParam;
import static com.gsma.rcs.provisioning.local.Provisioning.setIntegerEditTextParam;
import static com.gsma.rcs.provisioning.local.Provisioning.setStringEditTextParam;

/**
 * Cms parameters provisioning File
 */
public class CmsProvisioning extends Activity {

    private RcsSettings mRcsSettings;

    private boolean isInFront;
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set layout
        setContentView(R.layout.rcs_provisioning_cms);

        // Set buttons callback
        Button btn = (Button) findViewById(R.id.save_btn);
        btn.setOnClickListener(saveBtnListener);
        mRcsSettings = RcsSettings.createInstance(new LocalContentResolver(this));
        updateView(bundle);
        isInFront = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        saveInstanceState(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isInFront == false) {
            isInFront = true;
            // Update UI (from DB)
            updateView(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInFront = false;
    }

    /**
     * Save parameters either in bundle or in RCS settings
     */
    private void saveInstanceState(Bundle bundle) {

        ProvisioningHelper helper = new ProvisioningHelper(this, mRcsSettings, bundle);

        saveStringEditTextParam(R.id.message_store_url, RcsSettingsData.MESSAGE_STORE_URL, helper);
        saveStringEditTextParam(R.id.message_store_auth, RcsSettingsData.MESSAGE_STORE_AUTH, helper);
        saveStringEditTextParam(R.id.message_store_user, RcsSettingsData.MESSAGE_STORE_USER, helper);
        saveStringEditTextParam(R.id.message_store_pwd, RcsSettingsData.MESSAGE_STORE_PWD, helper);

        ImapLog imapLog = ImapLog.getInstance();
        saveCheckBoxParam(R.id.message_store_push_sms, RcsSettingsData.MESSAGE_STORE_PUSH_SMS, helper);
        if(!((CheckBox)findViewById(R.id.message_store_push_sms)).isChecked()){
            imapLog.updatePushStatus(MessageType.SMS, PushStatus.PUSHED);
        }
        saveCheckBoxParam(R.id.message_store_push_mms, RcsSettingsData.MESSAGE_STORE_PUSH_MMS, helper);
        if(!((CheckBox)findViewById(R.id.message_store_push_mms)).isChecked()){
            imapLog.updatePushStatus(MessageType.MMS, PushStatus.PUSHED);
        }
        saveCheckBoxParam(R.id.message_store_update_flag_imap_xms, RcsSettingsData.MESSAGE_STORE_UPDATE_FLAGS_WITH_IMAP_XMS, helper);
        saveStringEditTextParam(R.id.message_store_default_directory_name, RcsSettingsData.MESSAGE_STORE_DEFAULT_DIRECTORY_NAME, helper);
        saveStringEditTextParam(R.id.message_store_default_directory_separator, RcsSettingsData.MESSAGE_STORE_DIRECTORY_SEPARATOR, helper);
        saveIntegerEditTextParam(R.id.data_connection_sync_timer, RcsSettingsData.DATA_CONNECTION_SYNC_TIMER, helper);
        saveIntegerEditTextParam(R.id.message_store_sync_timer, RcsSettingsData.MESSAGE_STORE_SYNC_TIMER, helper);
    }

    /**
     * Update UI (upon creation, rotation, tab switch...)
     * 
     * @param bundle
     */
    private void updateView(Bundle bundle) {
        ProvisioningHelper helper = new ProvisioningHelper(this, mRcsSettings, bundle);

        setStringEditTextParam(R.id.message_store_url, RcsSettingsData.MESSAGE_STORE_URL, helper);
        setStringEditTextParam(R.id.message_store_auth, RcsSettingsData.MESSAGE_STORE_AUTH, helper);
        setStringEditTextParam(R.id.message_store_user, RcsSettingsData.MESSAGE_STORE_USER, helper);
        setStringEditTextParam(R.id.message_store_pwd, RcsSettingsData.MESSAGE_STORE_PWD, helper);

        setCheckBoxParam(R.id.message_store_push_sms, RcsSettingsData.MESSAGE_STORE_PUSH_SMS, helper);
        setCheckBoxParam(R.id.message_store_push_mms, RcsSettingsData.MESSAGE_STORE_PUSH_MMS, helper);
        setCheckBoxParam(R.id.message_store_update_flag_imap_xms, RcsSettingsData.MESSAGE_STORE_UPDATE_FLAGS_WITH_IMAP_XMS, helper);

        setStringEditTextParam(R.id.message_store_default_directory_name, RcsSettingsData.MESSAGE_STORE_DEFAULT_DIRECTORY_NAME, helper);
        setStringEditTextParam(R.id.message_store_default_directory_separator, RcsSettingsData.MESSAGE_STORE_DIRECTORY_SEPARATOR, helper);

        setIntegerEditTextParam(R.id.data_connection_sync_timer, RcsSettingsData.DATA_CONNECTION_SYNC_TIMER, helper);
        setIntegerEditTextParam(R.id.message_store_sync_timer, RcsSettingsData.MESSAGE_STORE_SYNC_TIMER, helper);
    }

    /**
     * Save button listener
     */
    private OnClickListener saveBtnListener = new OnClickListener() {
        public void onClick(View v) {
            // Save parameters
            saveInstanceState(null);
            Toast.makeText(CmsProvisioning.this, getString(R.string.message_store_save_ok), Toast.LENGTH_LONG).show();
        }
    };
}
