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

package com.gsma.rcs.platform.network;

import com.gsma.rcs.core.ims.network.ImsNetworkInterface.DnsResolvedFields;
import com.gsma.rcs.platform.FactoryException;
import com.gsma.rcs.provider.settings.RcsSettings;

/**
 * Network factory
 * 
 * @author jexa7410
 */
public abstract class NetworkFactory {
    /**
     * Current platform factory
     */
    private static NetworkFactory mFactory;

    /**
     * Load the factory
     * 
     * @param classname Factory classname
     * @throws FactoryException
     */
    public static void loadFactory(String classname) throws FactoryException {
        if (mFactory != null) {
            return;
        }

        try {
            mFactory = (NetworkFactory) Class.forName(classname).newInstance();
        } catch (Exception e) {
            throw new FactoryException("Can't load the factory ".concat(classname), e);
        }
    }

    /**
     * Load the factory
     * 
     * @param classname
     * @param rcsSettings
     * @throws FactoryException
     */
    public static void loadFactory(String classname, RcsSettings rcsSettings)
            throws FactoryException {
        if (mFactory != null) {
            return;
        }
        Class<?>[] rcsSettingsArgsClass = new Class[] {
            RcsSettings.class
        };
        Object[] rcsSettingsArgs = new Object[] {
            rcsSettings
        };
        try {
            mFactory = (NetworkFactory) Class.forName(classname)
                    .getConstructor(rcsSettingsArgsClass).newInstance(rcsSettingsArgs);
        } catch (Exception e) {
            throw new FactoryException("Can't load the factory ".concat(classname), e);
        }
    }

    /**
     * Returns the current factory
     * 
     * @return Factory
     */
    public static NetworkFactory getFactory() {
        return mFactory;
    }

    /**
     * Returns the local IP address of a given network interface
     * 
     * @param dnsEntry address to be connected to
     * @param type the type of the network interface, should be either
     *            {@link android.net.ConnectivityManager#TYPE_WIFI} or
     *            {@link android.net.ConnectivityManager#TYPE_MOBILE}
     * @return Address
     */
    // Changed by Deutsche Telekom
    public abstract String getLocalIpAddress(DnsResolvedFields dnsEntry, int type);

    /**
     * Create a datagram connection
     * 
     * @return Datagram connection
     */
    public abstract DatagramConnection createDatagramConnection();

    /**
     * Create a datagram connection with a specific SO timeout
     * 
     * @param timeout SO timeout
     * @return Datagram connection
     */
    public abstract DatagramConnection createDatagramConnection(int timeout);

    /**
     * Create a socket client connection
     * 
     * @return Socket connection
     */
    public abstract SocketConnection createSocketClientConnection();

    /**
     * Create a secure socket client connection
     * 
     * @return Socket connection
     */
    public abstract SocketConnection createSecureSocketClientConnection();

    // Changed By Deutsche Telekom
    /**
     * Create a secure socket client connection w/o checking certificates
     * 
     * @param fingerprint
     * @return Socket connection
     */
    public abstract SocketConnection createSimpleSecureSocketClientConnection(String fingerprint);

    /**
     * Create a socket server connection
     * 
     * @return Socket server connection
     */
    public abstract SocketServerConnection createSocketServerConnection();

    /**
     * Create an HTTP connection
     * 
     * @return HTTP connection
     */
    public abstract HttpConnection createHttpConnection();
}