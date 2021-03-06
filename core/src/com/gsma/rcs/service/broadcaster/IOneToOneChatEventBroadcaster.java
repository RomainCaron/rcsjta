/*
 * Copyright (C) 2014 Sony Mobile Communications Inc.
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

package com.gsma.rcs.service.broadcaster;

import com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode;
import com.gsma.services.rcs.chat.ChatLog.Message.Content.Status;
import com.gsma.services.rcs.contact.ContactId;

/**
 * Interface to perform broadcast events on ChatListeners
 */
public interface IOneToOneChatEventBroadcaster {

    public void broadcastMessageStatusChanged(ContactId contact, String mimeType, String msgId,
            Status status, ReasonCode reasonCode);

    public void broadcastComposingEvent(ContactId contact, boolean status);

    public void broadcastMessageReceived(String mimeType, String msgId);
}
