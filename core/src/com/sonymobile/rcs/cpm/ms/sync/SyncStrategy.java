/*
 * Copyright (C) 2015 Sony Mobile Communications Inc.
 * Copyright (C) 2010-2016 Orange.
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

package com.sonymobile.rcs.cpm.ms.sync;

import com.sonymobile.rcs.cpm.ms.CpmMessageStoreException;
import com.sonymobile.rcs.cpm.ms.MessageStore;

import java.io.Serializable;

public interface SyncStrategy extends Serializable {

    String getName();

    void execute(MessageStore localStore, MessageStore remoteStore, MutableReport report)
            throws Exception;

    void pause() throws CpmMessageStoreException;

    void resume() throws CpmMessageStoreException;

    void cancel() throws CpmMessageStoreException;

}