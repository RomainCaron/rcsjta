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

package com.orangelabs.rcs.ri.capabilities;

import com.gsma.services.rcs.RcsServiceException;
import com.gsma.services.rcs.capability.Capabilities;

import com.orangelabs.rcs.api.connection.ConnectionManager.RcsServiceName;
import com.orangelabs.rcs.api.connection.utils.RcsActivity;
import com.orangelabs.rcs.ri.R;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * My capabilities
 */
public class MyCapabilities extends RcsActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.capabilities_mine);

        // Register to API connection manager
        if (!isServiceConnected(RcsServiceName.CAPABILITY)) {
            showMessageThenExit(R.string.label_service_not_available);
            return;
        }
        startMonitorServices(RcsServiceName.CAPABILITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isExiting()) {
            return;
        }
        try {
            // Get the current capabilities from the RCS contacts API
            Capabilities capabilities = getCapabilityApi().getMyCapabilities();

            // Set capabilities
            CheckBox imageCSh = (CheckBox) findViewById(R.id.image_sharing);
            imageCSh.setChecked(capabilities.isImageSharingSupported());
            CheckBox videoCSh = (CheckBox) findViewById(R.id.video_sharing);
            videoCSh.setChecked(capabilities.isVideoSharingSupported());
            CheckBox ft = (CheckBox) findViewById(R.id.file_transfer);
            ft.setChecked(capabilities.isFileTransferSupported());
            CheckBox im = (CheckBox) findViewById(R.id.im);
            im.setChecked(capabilities.isImSessionSupported());
            CheckBox geolocationPush = (CheckBox) findViewById(R.id.geoloc_push);
            geolocationPush.setChecked(capabilities.isGeolocPushSupported());

            // Set extensions
            TextView extensions = (TextView) findViewById(R.id.extensions);
            extensions.setText(RequestCapabilities.getExtensions(capabilities));

            // Set automata
            CheckBox automata = (CheckBox) findViewById(R.id.automata);
            automata.setChecked(capabilities.isAutomata());

        } catch (RcsServiceException e) {
            showExceptionThenExit(e);
        }
    }

}
