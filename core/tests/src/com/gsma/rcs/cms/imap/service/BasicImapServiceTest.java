package com.gsma.rcs.cms.imap.service;

import android.content.Context;
import android.test.AndroidTestCase;

import com.gsma.rcs.cms.integration.RcsSettingsMock;
import com.gsma.rcs.provider.settings.RcsSettings;
import com.gsma.rcs.utils.logger.Logger;
import com.gsma.services.rcs.contact.ContactUtil;
import com.sonymobile.rcs.imap.ImapException;
import com.sonymobile.rcs.imap.IoService;
import com.sonymobile.rcs.imap.SocketIoService;

import junit.framework.Assert;

import java.io.IOException;
import java.net.SocketTimeoutException;

public class BasicImapServiceTest extends AndroidTestCase {

    private static final Logger sLogger = Logger.getLogger(BasicImapServiceTest.class.getSimpleName());

    private RcsSettings mSettings;
    private boolean mIsBlocked = true;

    protected void setUp() throws Exception {
        super.setUp();
        Context context = getContext();
        ContactUtil.getInstance(getContext());
        mSettings = RcsSettingsMock.getMockSettings(context);
    }

    public void testWithoutSoTimeout() throws IOException, ImapException, InterruptedException {
        final IoService io = new SocketIoService(mSettings.getMessageStoreUrl());
        final BasicImapService service = new BasicImapService(io);
        service.setAuthenticationDetails(mSettings.getMessageStoreUser(),
                mSettings.getMessageStorePwd(), null, null, false);
        service.init();
        mIsBlocked = true;

        Thread myThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    io.readLine();
                } catch (Exception e) {}
                mIsBlocked = false;
            }
        });

        myThread.start();
        //wait 5 seconds
        sLogger.info("wait for 5 seconds ...");
        myThread.join(5000);
        sLogger.info("after join, check if thread is always waiting on IO");
        Assert.assertTrue(mIsBlocked);
        myThread.interrupt();
        service.close();
    }

    public void testWithSoTimeout() throws IOException, ImapException, InterruptedException {
        final IoService io = new SocketIoService(mSettings.getMessageStoreUrl(), 3000);
        final BasicImapService service = new BasicImapService(io);
        service.setAuthenticationDetails(mSettings.getMessageStoreUser(),
                mSettings.getMessageStorePwd(), null, null, false);
        service.init();
        mIsBlocked = true;

        Thread myThread =  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    io.readLine();
                } catch (SocketTimeoutException e) {
                    sLogger.info("--> SocketTimeoutException");
                    mIsBlocked = false;
                } catch (Exception e) {}
            }
        });

        myThread.start();
        //wait 5 seconds
        sLogger.info("wait for 5 seconds ...");
        myThread.join(5000);
        sLogger.info("after join, check if thread is always waiting on IO");
        Assert.assertFalse(mIsBlocked);
        service.close();
    }

}
