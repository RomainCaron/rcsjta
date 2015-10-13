/*
 * Copyright (C) 2015 Sony Mobile Communications Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gsma.rcs.cms.sync.adapter;

import com.gsma.rcs.utils.logger.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the sync adapter class, allowing the sync adapter
 * framework to call onPerformSync().
 */
public class CmsSyncService extends Service {

    private static Logger sLogger = Logger.getLogger(CmsSyncService.class.getSimpleName());

    private CmsSyncAdapter mMessagingSyncAdapter;

    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        sLogger.error("CmsSyncService.onCreate");

        mMessagingSyncAdapter = CmsSyncAdapter.getInstance(getApplicationContext());
    }

    /**
     * Return an object that allows the system to invoke the sync adapter.
     */
    @Override
    public IBinder onBind(Intent intent) {
        sLogger.error("CmsSyncService.onBind");
        /*
         * Get the object that allows external processes to call onPerformSync(). The object is
         * created in the base class code when the SyncAdapter constructors call super()
         */
        return mMessagingSyncAdapter.getSyncAdapterBinder();
    }
}
