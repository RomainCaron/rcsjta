/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 * Copyright (C) 2015 Sony Mobile Communications Inc.
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
 * NOTE: This file has been modified by Sony Mobile Communications Inc.
 * Modifications are licensed under the License.
 ******************************************************************************/

package com.gsma.rcs.core.ims.service;

import com.gsma.rcs.platform.ntp.NtpTrustedTime;
import com.gsma.rcs.core.ims.network.NetworkException;
import com.gsma.rcs.core.ims.protocol.PayloadException;
import com.gsma.rcs.provider.settings.RcsSettings;
import com.gsma.rcs.utils.PeriodicRefresher;
import com.gsma.rcs.utils.logger.Logger;

/**
 * Session activity manager which manages the idle state of the session. It maintains a timer that
 * is canceled and restarted when the session has activity, i.e. when MSRP chunks are received or
 * emitted. If the timer expires, the session is aborted.
 */
public class SessionActivityManager extends PeriodicRefresher {
    /**
     * Last activity timestamp
     */
    private long mActivityTimestamp = 0L;

    /**
     * ImsServiceSession
     */
    private ImsServiceSession mSession;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * RcsSettings
     */
    private RcsSettings mRcsSettings;

    /**
     * Constructor
     * 
     * @param session IM session
     * @param rcsSettings
     */
    public SessionActivityManager(ImsServiceSession session, RcsSettings rcsSettings) {
        mSession = session;
        mRcsSettings = rcsSettings;
    }

    /**
     * Update the session activity
     */
    public void updateActivity() {
        mActivityTimestamp = NtpTrustedTime.currentTimeMillis();
    }

    /**
     * Start manager
     */
    public void start() {
        long timeout = mRcsSettings.getChatIdleDuration();
        if (logger.isActivated()) {
            logger.info(new StringBuilder("Start the activity manager for ").append(timeout)
                    .append("ms").toString());
        }

        // Reset the inactivity timestamp
        updateActivity();

        // Start a timer to check if the inactivity period has been reach or not each 10seconds
        startTimer(NtpTrustedTime.currentTimeMillis(), timeout);
    }

    /**
     * Stop manager
     */
    public void stop() {
        if (logger.isActivated()) {
            logger.info("Stop the activity manager");
        }

        // Stop timer
        stopTimer();
    }

    /**
     * Periodic processing
     * 
     * @throws NetworkException
     * @throws PayloadException
     */
    public void periodicProcessing() throws PayloadException, NetworkException {
        long timeout = mRcsSettings.getChatIdleDuration();
        long inactivityPeriod = NtpTrustedTime.currentTimeMillis() - mActivityTimestamp;
        long remainingPeriod = timeout - inactivityPeriod;
        if (logger.isActivated()) {
            logger.debug(new StringBuilder("Check inactivity period: inactivity=")
                    .append(inactivityPeriod).append(", remaining=").append(remainingPeriod)
                    .toString());
        }

        if (inactivityPeriod >= timeout) {
            if (logger.isActivated()) {
                logger.debug(new StringBuilder("No activity on the session during ")
                        .append(timeout).append("ms: abort the session").toString());
            }
            mSession.handleInactivityEvent();
        } else {
            // Restart timer
            startTimer(NtpTrustedTime.currentTimeMillis(), remainingPeriod);
        }
    }
}
