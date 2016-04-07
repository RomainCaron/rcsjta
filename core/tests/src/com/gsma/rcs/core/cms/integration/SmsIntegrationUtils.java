/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010-2016 Orange.
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

package com.gsma.rcs.core.cms.integration;

import com.gsma.rcs.platform.ntp.NtpTrustedTime;
import com.gsma.rcs.provider.cms.CmsObject;
import com.gsma.rcs.provider.cms.CmsObject.DeleteStatus;
import com.gsma.rcs.provider.cms.CmsObject.MessageType;
import com.gsma.rcs.provider.cms.CmsObject.PushStatus;
import com.gsma.rcs.provider.cms.CmsObject.ReadStatus;
import com.gsma.rcs.provider.xms.model.SmsDataObject;
import com.gsma.rcs.utils.ContactUtil;
import com.gsma.rcs.utils.IdGenerator;
import com.gsma.services.rcs.RcsService;
import com.gsma.services.rcs.RcsService.Direction;
import com.gsma.services.rcs.contact.ContactId;

public class SmsIntegrationUtils {

    public static class Test1 {

        public static ContactId contact = ContactUtil
                .createContactIdFromTrustedData("+33640332858");
        public static String folderName = "Default/tel:+33640332858";
        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.UNREAD;

        public static SmsDataObject[] conversation = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contact, "Hello!",
                        Direction.INCOMING, NtpTrustedTime.currentTimeMillis() - 4000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact, "How are you?",
                        Direction.INCOMING, NtpTrustedTime.currentTimeMillis() - 3000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact, "Fine, and you?",
                        Direction.INCOMING, NtpTrustedTime.currentTimeMillis() - 2000, 3l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact, "Fine, thanks",
                        Direction.INCOMING, NtpTrustedTime.currentTimeMillis(), 4l, 1l),
        };
    }

    public static class Test2 {

        public static CmsObject[] cmsObjectReadRequested = new CmsObject[] {
                new CmsObject(Test1.folderName, 1, ReadStatus.READ_REPORT_REQUESTED, DeleteStatus.NOT_DELETED, PushStatus.PUSHED, MessageType.SMS, "1", null),
                new CmsObject(Test1.folderName, 2, ReadStatus.READ_REPORT_REQUESTED, DeleteStatus.NOT_DELETED, PushStatus.PUSHED, MessageType.SMS, "2", null),
                new CmsObject(Test1.folderName, 3, ReadStatus.READ_REPORT_REQUESTED, DeleteStatus.NOT_DELETED, PushStatus.PUSHED, MessageType.SMS, "3", null),
                new CmsObject(Test1.folderName, 4, ReadStatus.READ_REPORT_REQUESTED, DeleteStatus.NOT_DELETED, PushStatus.PUSHED, MessageType.SMS, "4", null),
        };

        public static CmsObject[] cmsObjectDeletedRequested = new CmsObject[] {
                new CmsObject(Test1.folderName, 1, ReadStatus.READ, DeleteStatus.DELETED_REPORT_REQUESTED, PushStatus.PUSHED, MessageType.SMS, "1", null),
                new CmsObject(Test1.folderName, 2, ReadStatus.READ, DeleteStatus.DELETED_REPORT_REQUESTED, PushStatus.PUSHED, MessageType.SMS, "2", null),
                new CmsObject(Test1.folderName, 3, ReadStatus.READ, DeleteStatus.DELETED_REPORT_REQUESTED, PushStatus.PUSHED, MessageType.SMS, "3", null),
                new CmsObject(Test1.folderName, 4, ReadStatus.READ, DeleteStatus.DELETED_REPORT_REQUESTED, PushStatus.PUSHED, MessageType.SMS, "4", null),
        };

    }

    public static class Test5 {

        public static ContactId contactId = ContactUtil
                .createContactIdFromTrustedData("+33640332858");
        public static CmsObject[] cmsObjectDeletedRequested = new CmsObject[] {
                new CmsObject(Test1.folderName, 1, ReadStatus.READ, DeleteStatus.DELETED_REPORT_REQUESTED, PushStatus.PUSHED, MessageType.SMS, "1", null),
                new CmsObject(Test1.folderName, 2, ReadStatus.READ, DeleteStatus.DELETED_REPORT_REQUESTED, PushStatus.PUSHED, MessageType.SMS, "2", null),
        };

        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.READ;

        public static SmsDataObject[] conversation = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() - 4000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "How are you?",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() - 3000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "Fine, and you?",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() - 2000, 3l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "Fine, thanks",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis(), 4l, 1l),
        };
    }

    public static class Test7 {

        public static ContactId contactId = ContactUtil
                .createContactIdFromTrustedData("+33640332858");

        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.UNREAD;
        public static ReadStatus imapReadStatus = ReadStatus.UNREAD;
        public static DeleteStatus imapDeleteStatus = DeleteStatus.NOT_DELETED;

        public static SmsDataObject[] conversation = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() - 4000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "yes",
                        Direction.OUTGOING, readStatus, NtpTrustedTime.currentTimeMillis() - 2000, 3l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "yes",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() - 3000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contactId, "yes",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis(), 4l, 1l),
        };
    }

    public static class Test8 {

        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.UNREAD;
        public static ReadStatus imapReadStatus = ReadStatus.UNREAD;
        public static DeleteStatus imapDeleteStatus = DeleteStatus.NOT_DELETED;

        public static SmsDataObject[] conversation_remote = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
        };

        public static SmsDataObject[] conversation_local = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 3000, 3l, 1l),

        };
    }

    public static class Test9 {

        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.UNREAD;
        public static ReadStatus imapReadStatus = ReadStatus.UNREAD;
        public static DeleteStatus imapDeleteStatus = DeleteStatus.NOT_DELETED;

        public static SmsDataObject[] conversation_remote = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 3000, 2l, 1l),
        };

        public static SmsDataObject[] conversation_local = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), Test1.contact, "Hello!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
        };

    }

    public static class Test10 {

        public static ContactId contact1 = ContactUtil
                .createContactIdFromTrustedData("+33600000001");
        public static ContactId contact2 = ContactUtil
                .createContactIdFromTrustedData("+33600000002");
        public static ContactId contact3 = ContactUtil
                .createContactIdFromTrustedData("+33600000003");

        public static String folder1 = "Default/tel:+33600000001";
        public static String folder2 = "Default/tel:+33600000002";
        public static String folder3 = "Default/tel:+33600000003";

        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.UNREAD;

        public static SmsDataObject[] conversation_1 = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contact1, "Hello 1!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact1, "Hi 1",
                        Direction.INCOMING, RcsService.ReadStatus.READ,
                        NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact1, "Ciao 1",
                        Direction.OUTGOING, RcsService.ReadStatus.READ,
                        NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact1, "Bye 1",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
        };

        public static SmsDataObject[] conversation_2 = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contact2, "Hi 2!",
                        Direction.OUTGOING, RcsService.ReadStatus.READ,
                        NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1L),
                new SmsDataObject(IdGenerator.generateMessageID(), contact2, "Ciao 2",
                        Direction.OUTGOING, RcsService.ReadStatus.READ,
                        NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
        };

        public static SmsDataObject[] conversation_3 = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contact3, "Hello 3!",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 1l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact3, "Bye 3",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 2000, 2l, 1l),
        };
    }

    public static class TestLoad {

        public static int iteration = 3;
        public static ContactId contact1 = ContactUtil
                .createContactIdFromTrustedData("+33600000001");
        public static ContactId contact2 = ContactUtil
                .createContactIdFromTrustedData("+33600000002");
        public static ContactId contact3 = ContactUtil
                .createContactIdFromTrustedData("+33600000003");
        public static RcsService.ReadStatus readStatus = RcsService.ReadStatus.UNREAD;

        public static SmsDataObject[] conversation_1 = new SmsDataObject[] {
            new SmsDataObject(IdGenerator.generateMessageID(), contact1, "Hello !",
                    Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 1l),
        };

        public static SmsDataObject[] conversation_2 = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contact2, "Hi",
                        Direction.INCOMING, RcsService.ReadStatus.READ,
                        NtpTrustedTime.currentTimeMillis() + 1000, 1l, 2l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact2, "Ciao",
                        Direction.INCOMING, RcsService.ReadStatus.READ,
                        NtpTrustedTime.currentTimeMillis() + 1000, 2l, 2l),
        };

        public static SmsDataObject[] conversation_3 = new SmsDataObject[] {
                new SmsDataObject(IdGenerator.generateMessageID(), contact3, "Hello",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 1l, 3l),
                new SmsDataObject(IdGenerator.generateMessageID(), contact3, "Bye",
                        Direction.INCOMING, readStatus, NtpTrustedTime.currentTimeMillis() + 1000, 2l, 3l),
        };
    }
}