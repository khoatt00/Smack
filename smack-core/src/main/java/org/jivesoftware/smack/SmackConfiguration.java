/**
 *
 * Copyright 2003-2007 Jive Software, 2018-2021 Florian Schmaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smack;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;

import org.jivesoftware.smack.c2s.ModularXmppClientToServerConnectionConfiguration;
import org.jivesoftware.smack.c2s.ModularXmppClientToServerConnectionModuleDescriptor;
import org.jivesoftware.smack.compression.XMPPInputOutputStream;
import org.jivesoftware.smack.debugger.ReflectionDebuggerFactory;
import org.jivesoftware.smack.debugger.SmackDebuggerFactory;
import org.jivesoftware.smack.parsing.ExceptionThrowingCallback;
import org.jivesoftware.smack.parsing.ExceptionThrowingCallbackWithHint;
import org.jivesoftware.smack.parsing.ParsingExceptionCallback;
import org.jivesoftware.smack.util.Objects;

/**
 * Represents the configuration of Smack. The configuration is used for:
 * <ul>
 *      <li> Initializing classes by loading them at start-up.
 *      <li> Getting the current Smack version.
 *      <li> Getting and setting global library behavior, such as the period of time
 *          to wait for replies to packets from the server. Note: setting these values
 *          via the API will override settings in the configuration file.
 * </ul>
 *
 * Configuration settings are stored in org.jivesoftware.smack/smack-config.xml.
 *
 * @author Gaston Dombiak
 */
public final class SmackConfiguration {

    public static final String SMACK_URL_STRING = "https://igniterealtime.org/projects/smack";

    public static final URL SMACK_URL;

    static {
        try {
            SMACK_URL = new URL(SMACK_URL_STRING);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int defaultPacketReplyTimeout = 5000;
    private static int packetCollectorSize = 5000;

    private static List<String> defaultMechs = new ArrayList<>();

    static Set<String> disabledSmackClasses = new HashSet<>();

    static final List<XMPPInputOutputStream> compressionHandlers = new ArrayList<>(2);

    static boolean smackInitialized = false;

    /**
     * Value that indicates whether debugging is enabled. When enabled, a debug
     * window will appear for each new connection that will contain the following
     * information:<ul>
     * <li> Client Traffic -- raw XML traffic generated by Smack and sent to the server.
     * <li> Server Traffic -- raw XML traffic sent by the server to the client.
     * <li> Interpreted Packets -- shows XML packets from the server as parsed by Smack.
     * </ul>
     * Debugging can be enabled by setting this field to true, or by setting the Java system
     * property <code>smack.debugEnabled</code> to true. The system property can be set on the
     * command line such as "java SomeApp -Dsmack.debugEnabled=true".
     */
    public static boolean DEBUG = false;

    private static SmackDebuggerFactory DEFAULT_DEBUGGER_FACTORY = ReflectionDebuggerFactory.INSTANCE;

    /**
     * The default parsing exception callback is {@link ExceptionThrowingCallback} which will
     * throw an exception and therefore disconnect the active connection.
     */
    private static ParsingExceptionCallback defaultCallback = new ExceptionThrowingCallbackWithHint();

    private static HostnameVerifier defaultHostnameVerififer;

    /**
     * Returns the Smack version information, eg "1.3.0".
     *
     * @return the Smack version information.
     * @deprecated use {@link Smack#getVersion()} instead.
     */
    @Deprecated
    // TODO: Remove in Smack 4.6
    public static String getVersion() {
        return SmackInitialization.SMACK_VERSION;
    }

    /**
     * Returns the number of milliseconds to wait for a response from
     * the server. The default value is 5000 ms.
     *
     * @return the milliseconds to wait for a response from the server
     */
    public static int getDefaultReplyTimeout() {
        // The timeout value must be greater than 0 otherwise we will answer the default value
        if (defaultPacketReplyTimeout <= 0) {
            defaultPacketReplyTimeout = 5000;
        }
        return defaultPacketReplyTimeout;
    }

    /**
     * Sets the number of milliseconds to wait for a response from
     * the server.
     *
     * @param timeout the milliseconds to wait for a response from the server
     */
    public static void setDefaultReplyTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException();
        }
        defaultPacketReplyTimeout = timeout;
    }

    public static void setDefaultSmackDebuggerFactory(SmackDebuggerFactory debuggerFactory) {
        DEFAULT_DEBUGGER_FACTORY = Objects.requireNonNull(debuggerFactory, "Debugger factory must not be null");
    }

    public static SmackDebuggerFactory getDefaultSmackDebuggerFactory() {
        return DEFAULT_DEBUGGER_FACTORY;
    }

    /**
     * Gets the default max size of a stanza collector before it will delete
     * the older packets.
     *
     * @return The number of packets to queue before deleting older packets.
     */
    public static int getStanzaCollectorSize() {
        return packetCollectorSize;
    }

    /**
     * Sets the default max size of a stanza collector before it will delete
     * the older packets.
     *
     * @param collectorSize the number of packets to queue before deleting older packets.
     */
    public static void setStanzaCollectorSize(int collectorSize) {
        packetCollectorSize = collectorSize;
    }

    /**
     * Add a SASL mechanism to the list to be used.
     *
     * @param mech the SASL mechanism to be added
     */
    public static void addSaslMech(String mech) {
        if (!defaultMechs.contains(mech)) {
            defaultMechs.add(mech);
        }
    }

    /**
     * Add a Collection of SASL mechanisms to the list to be used.
     *
     * @param mechs the Collection of SASL mechanisms to be added
     */
    public static void addSaslMechs(Collection<String> mechs) {
        for (String mech : mechs) {
            addSaslMech(mech);
        }
    }

    /**
     * Remove a SASL mechanism from the list to be used.
     *
     * @param mech the SASL mechanism to be removed
     */
    public static void removeSaslMech(String mech) {
        defaultMechs.remove(mech);
    }

    /**
     * Remove a Collection of SASL mechanisms to the list to be used.
     *
     * @param mechs the Collection of SASL mechanisms to be removed
     */
    public static void removeSaslMechs(Collection<String> mechs) {
        defaultMechs.removeAll(mechs);
    }

    /**
     * Returns the list of SASL mechanisms to be used. If a SASL mechanism is
     * listed here it does not guarantee it will be used. The server may not
     * support it, or it may not be implemented.
     *
     * @return the list of SASL mechanisms to be used.
     */
    public static List<String> getSaslMechs() {
        return Collections.unmodifiableList(defaultMechs);
    }

    /**
     * Set the default parsing exception callback for all newly created connections.
     *
     * @param callback TODO javadoc me please
     * @see ParsingExceptionCallback
     */
    public static void setDefaultParsingExceptionCallback(ParsingExceptionCallback callback) {
        defaultCallback = callback;
    }

    /**
     * Returns the default parsing exception callback.
     *
     * @return the default parsing exception callback
     * @see ParsingExceptionCallback
     */
    public static ParsingExceptionCallback getDefaultParsingExceptionCallback() {
        return defaultCallback;
    }

    public static void addCompressionHandler(XMPPInputOutputStream xmppInputOutputStream) {
        compressionHandlers.add(xmppInputOutputStream);
    }

    /**
     * Get compression handlers.
     *
     * @return a list of compression handlers.
     */
    public static List<XMPPInputOutputStream> getCompressionHandlers() {
        List<XMPPInputOutputStream> res = new ArrayList<>(compressionHandlers.size());
        for (XMPPInputOutputStream ios : compressionHandlers) {
            if (ios.isSupported()) {
                res.add(ios);
            }
        }
        return res;
    }

    /**
     * Set the default HostnameVerifier that will be used by XMPP connections to verify the hostname
     * of a TLS certificate. XMPP connections are able to overwrite this settings by supplying a
     * HostnameVerifier in their ConnectionConfiguration with
     * {@link ConnectionConfiguration.Builder#setHostnameVerifier(HostnameVerifier)}.
     *
     * @param verifier HostnameVerifier
     */
    public static void setDefaultHostnameVerifier(HostnameVerifier verifier) {
        defaultHostnameVerififer = verifier;
    }

    /**
     * Convenience method for {@link #addDisabledSmackClass(String)}.
     *
     * @param clz the Smack class to disable
     */
    public static void addDisabledSmackClass(Class<?> clz) {
        addDisabledSmackClass(clz.getName());
    }

    /**
     * Add a class to the disabled smack classes.
     * <p>
     * {@code className} can also be a package name, in this case, the entire
     * package is disabled (but can be manually enabled).
     * </p>
     *
     * @param className TODO javadoc me please
     */
    public static void addDisabledSmackClass(String className) {
        disabledSmackClasses.add(className);
    }

    /**
     * Add the given class names to the list of disabled Smack classes.
     *
     * @param classNames the Smack classes to disable.
     * @see #addDisabledSmackClass(String)
     */
    public static void addDisabledSmackClasses(String... classNames) {
        for (String className : classNames) {
            addDisabledSmackClass(className);
        }
    }

    public static boolean isDisabledSmackClass(String className) {
        for (String disabledClassOrPackage : disabledSmackClasses) {
            if (disabledClassOrPackage.equals(className)) {
                return true;
            }
            int lastDotIndex = disabledClassOrPackage.lastIndexOf('.');
            // Security check to avoid NPEs if someone entered 'foo.bar.'
            if (disabledClassOrPackage.length() > lastDotIndex
                            // disabledClassOrPackage is not an Class
                            && !Character.isUpperCase(disabledClassOrPackage.charAt(lastDotIndex + 1))
                            // classToLoad startsWith the package disabledClassOrPackage disables
                            && className.startsWith(disabledClassOrPackage)) {
                // Skip the class because the whole package was disabled
                return true;
            }
        }
        return false;
    }

    /**
     * Check if Smack was successfully initialized.
     *
     * @return true if smack was initialized, false otherwise
     */
    public static boolean isSmackInitialized() {
        return smackInitialized;
    }

    /**
     * Get the default HostnameVerifier
     *
     * @return the default HostnameVerifier or <code>null</code> if none was set
     */
    static HostnameVerifier getDefaultHostnameVerifier() {
        return defaultHostnameVerififer;
    }

    public enum UnknownIqRequestReplyMode {
        doNotReply,
        reply,
    }

    private static UnknownIqRequestReplyMode unknownIqRequestReplyMode = UnknownIqRequestReplyMode.reply;

    public static UnknownIqRequestReplyMode getUnknownIqRequestReplyMode() {
        return unknownIqRequestReplyMode;
    }

    public static void setUnknownIqRequestReplyMode(UnknownIqRequestReplyMode unknownIqRequestReplyMode) {
        SmackConfiguration.unknownIqRequestReplyMode = Objects.requireNonNull(unknownIqRequestReplyMode, "Must set mode");
    }

    private static final int defaultConcurrencyLevelLimit;

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors < 8) {
            defaultConcurrencyLevelLimit = 8;
        } else {
            defaultConcurrencyLevelLimit = (int) (availableProcessors * 1.1);
        }
    }

    public static int getDefaultConcurrencyLevelLimit() {
        return defaultConcurrencyLevelLimit;
    }

    private static final Set<Class<? extends ModularXmppClientToServerConnectionModuleDescriptor>> KNOWN_MODULES = new HashSet<>();

    public static boolean addModule(Class<? extends ModularXmppClientToServerConnectionModuleDescriptor> moduleDescriptor) {
        synchronized (KNOWN_MODULES) {
            return KNOWN_MODULES.add(moduleDescriptor);
        }
    }

    public static void addAllKnownModulesTo(ModularXmppClientToServerConnectionConfiguration.Builder builder) {
        synchronized (KNOWN_MODULES) {
            for (Class<? extends ModularXmppClientToServerConnectionModuleDescriptor> moduleDescriptor : KNOWN_MODULES) {
                builder.addModule(moduleDescriptor);
            }
        }
    }
}
