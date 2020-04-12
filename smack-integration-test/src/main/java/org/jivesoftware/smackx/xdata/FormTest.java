/**
 *
 * Copyright 2004 Jive Software, 2017-2020 Florian Schmaus.
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
package org.jivesoftware.smackx.xdata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaCollector;
import org.jivesoftware.smack.filter.ThreadFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StanzaBuilder;

import org.jivesoftware.smackx.xdata.FormField.Type;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import org.igniterealtime.smack.inttest.AbstractSmackIntegrationTest;
import org.igniterealtime.smack.inttest.SmackIntegrationTestEnvironment;
import org.igniterealtime.smack.inttest.annotations.SmackIntegrationTest;

/**
 * Tests the DataForms extensions.
 *
 * @author Gaston Dombiak
 */
public class FormTest extends AbstractSmackIntegrationTest {

    public FormTest(SmackIntegrationTestEnvironment environment) {
        super(environment);
    }

    /**
     * 1. Create a form to fill out and send it to the other user
     * 2. Retrieve the form to fill out, complete it and return it to the requestor
     * 3. Retrieve the completed form and check that everything is OK
     *
     * @throws InterruptedException if the calling thread was interrupted.
     * @throws NotConnectedException if the XMPP connection is not connected.
     */
    @SuppressWarnings("deprecation")
    @SmackIntegrationTest
    public void testFilloutForm() throws NotConnectedException, InterruptedException {
        Form formToSend = new Form(DataForm.Type.form);
        formToSend.setInstructions(
            "Fill out this form to report your case.\nThe case will be created automatically.");
        formToSend.setTitle("Case configurations");
        // Add a hidden variable
        FormField.Builder field = FormField.builder("hidden_var");
        field.setType(FormField.Type.hidden);
        field.addValue("Some value for the hidden variable");
        formToSend.addField(field.build());
        // Add a fixed variable
        field = FormField.builder();
        field.addValue("Section 1: Case description");
        field.setType(Type.fixed);
        formToSend.addField(field.build());
        // Add a text-single variable
        field = FormField.builder("name");
        field.setLabel("Enter a name for the case");
        field.setType(FormField.Type.text_single);
        formToSend.addField(field.build());
        // Add a text-multi variable
        field = FormField.builder("description");
        field.setLabel("Enter a description");
        field.setType(FormField.Type.text_multi);
        formToSend.addField(field.build());
        // Add a boolean variable
        field = FormField.builder("time");
        field.setLabel("Is this your first case?");
        field.setType(FormField.Type.bool);
        formToSend.addField(field.build());
        // Add a text variable where an int value is expected
        field = FormField.builder("age");
        field.setLabel("How old are you?");
        field.setType(FormField.Type.text_single);
        formToSend.addField(field.build());

        // Create the chats between the two participants
        org.jivesoftware.smack.chat.Chat chat = org.jivesoftware.smack.chat.ChatManager.getInstanceFor(conOne).createChat(conTwo.getUser(), null);
        StanzaCollector collector = conOne.createStanzaCollector(
                new ThreadFilter(chat.getThreadID()));
        StanzaCollector collector2 = conTwo.createStanzaCollector(
                new ThreadFilter(chat.getThreadID()));

        Message msg = StanzaBuilder.buildMessage()
                .setBody("To enter a case please fill out this form and send it back to me")
                .addExtension(formToSend.getDataFormToSend())
                .build();

        try {
            // Send the message with the form to fill out
            chat.sendMessage(msg);

            // Get the message with the form to fill out
            Message msg2 = collector2.nextResult();
            assertNotNull(msg2, "Message not found");
            // Retrieve the form to fill out
            Form formToRespond = Form.getFormFrom(msg2);
            assertNotNull(formToRespond);
            assertNotNull(formToRespond.getField("name"));
            assertNotNull(formToRespond.getField("description"));
            // Obtain the form to send with the replies
            final Form completedForm = formToRespond.createAnswerForm();
            assertNotNull(completedForm.getField("hidden_var"));
            // Check that a field of type String does not accept booleans
            assertThrows(IllegalArgumentException.class, () -> completedForm.setAnswer("name", true));
            completedForm.setAnswer("name", "Credit card number invalid");
            completedForm.setAnswer(
                "description",
                "The ATM says that my credit card number is invalid. What's going on?");
            completedForm.setAnswer("time", true);
            completedForm.setAnswer("age", 20);
            // Create a new message to send with the completed form
            msg2 = StanzaBuilder.buildMessage()
                    .to(conOne.getUser().asBareJid())
                    .setThread(msg.getThread())
                    .ofType(Message.Type.chat)
                    .setBody("To enter a case please fill out this form and send it back to me")
                    // Add the completed form to the message
                    .addExtension(completedForm.getDataFormToSend())
                    .build();
            // Send the message with the completed form
            conTwo.sendStanza(msg2);

            // Get the message with the completed form
            Message msg3 = collector.nextResult();
            assertNotNull(msg3, "Message not found");
            // Retrieve the completed form
            final Form completedForm2 = Form.getFormFrom(msg3);
            assertNotNull(completedForm2);
            assertNotNull(completedForm2.getField("name"));
            assertNotNull(completedForm2.getField("description"));
            assertEquals(
                 completedForm2.getField("name").getValues().get(0).toString(),
                "Credit card number invalid");
            assertNotNull(completedForm2.getField("time"));
            assertNotNull(completedForm2.getField("age"));
            assertEquals("20", completedForm2.getField("age").getValues().get(0).toString(), "The age is bad");

        }
        finally {
            collector.cancel();
            collector2.cancel();
        }
    }

}
