/**
 *
 * Copyright the original author or authors
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
package org.jivesoftware.smackx.receipts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.jivesoftware.smack.DummyConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.InitExtensions;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import com.jamesmurty.utils.XMLBuilder;

public class DeliveryReceiptTest extends InitExtensions {

    private static Properties outputProperties = new Properties();
    static {
        outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
    }

    @Test
    public void receiptTest() throws Exception {
        XmlPullParser parser;
        String control;
        
        control = XMLBuilder.create("message")
            .a("from", "romeo@montague.com")
            .e("request")
                .a("xmlns", "urn:xmpp:receipts")
            .asString(outputProperties);
        
        parser = PacketParserUtils.getParserFor(control);
        Packet p = PacketParserUtils.parseMessage(parser);

        DeliveryReceiptRequest drr = (DeliveryReceiptRequest)p.getExtension(
                        DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE);
        assertNotNull(drr);

        assertTrue(DeliveryReceiptManager.hasDeliveryReceiptRequest(p));

        Message m = new Message("romeo@montague.com", Message.Type.normal);
        assertFalse(DeliveryReceiptManager.hasDeliveryReceiptRequest(m));
        DeliveryReceiptRequest.addTo(m);
        assertTrue(DeliveryReceiptManager.hasDeliveryReceiptRequest(m));
    }

    @Test
    public void receiptManagerListenerTest() throws Exception {
        DummyConnection c = new DummyConnection();
        c.connect();
        // Ensure SDM is created for this connection
        ServiceDiscoveryManager.getInstanceFor(c);
        DeliveryReceiptManager drm = DeliveryReceiptManager.getInstanceFor(c);

        TestReceiptReceivedListener rrl = new TestReceiptReceivedListener();
        drm.addReceiptReceivedListener(rrl);

        Message m = new Message("romeo@montague.com", Message.Type.normal);
        m.setFrom("julia@capulet.com");
        m.setPacketID("reply-id");
        m.addExtension(new DeliveryReceipt("original-test-id"));
        drm.processPacket(m);

        // ensure the listener got called
        assertEquals("original-test-id", rrl.receiptId);
    }

    private static class TestReceiptReceivedListener implements ReceiptReceivedListener {
        public String receiptId = null;
        @Override
        public void onReceiptReceived(String fromJid, String toJid, String receiptId, Packet receipt) {
            assertEquals("julia@capulet.com", fromJid);
            assertEquals("romeo@montague.com", toJid);
            assertEquals("original-test-id", receiptId);
            this.receiptId = receiptId;
        }
    }

    @Test
    public void receiptManagerAutoReplyTest() throws Exception {
        DummyConnection c = new DummyConnection();
        c.connect();
        // Ensure SDM is created for this connection
        ServiceDiscoveryManager.getInstanceFor(c);
        DeliveryReceiptManager drm = DeliveryReceiptManager.getInstanceFor(c);

        drm.enableAutoReceipts();
        assertTrue(drm.getAutoReceiptsEnabled());

        // test auto-receipts
        Message m = new Message("julia@capulet.com", Message.Type.normal);
        m.setFrom("romeo@montague.com");
        m.setPacketID("test-receipt-request");
        DeliveryReceiptRequest.addTo(m);

        // the DRM will send a reply-packet
        assertEquals(0, c.getNumberOfSentPackets());
        drm.processPacket(m);
        assertEquals(1, c.getNumberOfSentPackets());

        Packet reply = c.getSentPacket();
        DeliveryReceipt r = (DeliveryReceipt)reply.getExtension("received", "urn:xmpp:receipts");
        assertEquals("romeo@montague.com", reply.getTo());
        assertEquals("test-receipt-request", r.getId());
    }
}
