/**
 *
 * Copyright 2019 Aditya Borikar.
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
package org.jivesoftware.smackx.usertune;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;

import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.pep.PepListener;
import org.jivesoftware.smackx.pep.PepManager;
import org.jivesoftware.smackx.pubsub.EventElement;
import org.jivesoftware.smackx.pubsub.ItemsExtension;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubException.NotALeafNodeException;
import org.jivesoftware.smackx.usertune.element.UserTuneElement;

import org.jxmpp.jid.EntityBareJid;

/**
 * Entry point for Smacks API for XEP-0118: User Tune.
 * <br>
 * To publish a UserTune, please use {@link #publishUserTune(UserTuneElement)} method. This will publish the node.
 * <br>
 * To stop publishing a UserTune, please use {@link #clearUserTune()} method. This will send a disabling publish signal.
 * <br>
 * To add a UserTune listener in order to remain updated with other users UserTune, use {@link #addUserTuneListener(UserTuneListener)} method.
 * <br>
 * To link a UserTuneElement with {@link Message}, use 'message.addExtension(userTuneElement)'.
 * <br>
 * An example to illustrate is provided inside UserTuneElementTest inside the test package.
 * <br>
 * @see <a href="https://xmpp.org/extensions/xep-0118.html">
 *     XEP-0118: User Tune</a>
 */
public final class UserTuneManager extends Manager {

    public static final String USERTUNE_NODE = "http://jabber.org/protocol/tune";
    public static final String USERTUNE_NOTIFY = USERTUNE_NODE + "+notify";

    private static final Map<XMPPConnection, UserTuneManager> INSTANCES = new WeakHashMap<>();

    private static boolean ENABLE_USER_TUNE_NOTIFICATIONS_BY_DEFAULT = true;

    private final Set<UserTuneListener> userTuneListeners = new CopyOnWriteArraySet<>();
    private final ServiceDiscoveryManager serviceDiscoveryManager;
    private final PepManager pepManager;

    public static synchronized UserTuneManager getInstanceFor(XMPPConnection connection) throws NotLoggedInException {
        UserTuneManager manager = INSTANCES.get(connection);
        if (manager == null) {
            manager = new UserTuneManager(connection);
            INSTANCES.put(connection, manager);
        }
        return manager;
    }

    private UserTuneManager(XMPPConnection connection) throws NotLoggedInException {
        super(connection);
        pepManager = PepManager.getInstanceFor(connection);
        pepManager.addPepListener(new PepListener() {
            @Override
            public void eventReceived(EntityBareJid from, EventElement event, Message message) {
                if (!USERTUNE_NODE.equals(event.getEvent().getNode())) {
                    return;
                }

                ItemsExtension itemsExtension = (ItemsExtension) event.getEvent();
                List<ExtensionElement> items = itemsExtension.getExtensions();
                @SuppressWarnings("unchecked")
                PayloadItem<UserTuneElement> payload = (PayloadItem<UserTuneElement>) items.get(0);
                UserTuneElement tune = payload.getPayload();

                for (UserTuneListener listener : userTuneListeners) {
                    listener.onUserTuneUpdated(from, tune, message);
                }
            }
        });
        serviceDiscoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);
        if (ENABLE_USER_TUNE_NOTIFICATIONS_BY_DEFAULT) {
            enableUserTuneNotifications();
        }
    }

    public static void setUserTuneNotificationsEnabledByDefault(boolean bool) {
        ENABLE_USER_TUNE_NOTIFICATIONS_BY_DEFAULT = bool;
    }

    public void enableUserTuneNotifications() {
        serviceDiscoveryManager.addFeature(USERTUNE_NOTIFY);
    }

    public void disableUserTuneNotifications() {
        serviceDiscoveryManager.removeFeature(USERTUNE_NOTIFY);
    }

    public void clearUserTune() throws NotLoggedInException, NotALeafNodeException, NoResponseException, NotConnectedException, XMPPErrorException, InterruptedException {
        publishUserTune(UserTuneElement.EMPTY_USER_TUNE);
    }

    public void publishUserTune(UserTuneElement userTuneElement) throws NotLoggedInException, NotALeafNodeException, NoResponseException, NotConnectedException, XMPPErrorException, InterruptedException {
        // TODO: To prevent a large number of updates when a user is skipping through tracks, an implementation SHOULD wait several seconds before publishing new tune information.
        pepManager.publish(USERTUNE_NODE, new PayloadItem<>(userTuneElement));
    }

    public boolean addUserTuneListener(UserTuneListener listener) {
        return userTuneListeners.add(listener);
    }

    public boolean removeUserTuneListener(UserTuneListener listener) {
        return userTuneListeners.remove(listener);
    }
}
