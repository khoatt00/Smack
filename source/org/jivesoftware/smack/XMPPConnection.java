/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright (C) 2002-2003 Jive Software. All rights reserved.
 * ====================================================================
 * The Jive Software License (based on Apache Software License, Version 1.1)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Jive Software (http://www.jivesoftware.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Smack" and "Jive Software" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact webmaster@jivesoftware.com.
 *
 * 5. Products derived from this software may not be called "Smack",
 *    nor may "Smack" appear in their name, without prior written
 *    permission of Jive Software.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL JIVE SOFTWARE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.jivesoftware.smack;

import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.*;

/**
 * Creates a connection to a XMPP server. A simple use of this API might
 * look like the following:
 * <pre>
 * // Create a connection to the jivesoftware.com XMPP server.
 * XMPPConnection con = new XMPPConnection("jivesoftware.com");
 * // Most servers require you to login before performing other tasks.
 * con.login("jsmith", "mypass");
 * // Start a new conversation with John Doe and send him a message.
 * Chat chat = con.createChat("jdoe@jabber.org");
 * chat.sendMessage("Hey, how's it going?");
 * </pre>
 *
 * @author Matt Tucker
 */
public class XMPPConnection {

    private static final String NEWLINE = "\n";

    /**
     * Value that indicates whether debugging is enabled. When enabled, a debug
     * window will apear for each new connection that will contain the following
     * information:<ul>
     *      <li> Client Traffic -- raw XML traffic generated by Smack and sent to the server.
     *      <li> Server Traffic -- raw XML traffic sent by the server to the client.
     *      <li> Interpreted Packets -- shows XML packets from the server as parsed by Smack.
     * </ul>
     *
     * Debugging can be enabled by setting this field to true, or by setting the Java system
     * property <tt>smack.debugEnabled</tt> to true. The system property can be set on the
     * command line such as "java SomeApp -Dsmack.debugEnabled=true".
     */
    public static boolean DEBUG_ENABLED = false;
    static {
        // Use try block since we may not have permission to get a system
        // property (for example, when an applet).
        try {
            DEBUG_ENABLED = Boolean.getBoolean("smack.debugEnabled");
        }
        catch (Exception e) { }
    }
    private JFrame debugFrame = null;

    protected String host;
    protected int port;
    protected Socket socket;

    String connectionID;
    private String username = null;
    private String resource = null;
    private boolean connected = false;
    private boolean authenticated = false;

    private PacketWriter packetWriter;
    private PacketReader packetReader;

    Roster roster = null;
    private AccountManager accountManager = null;

    Writer writer;
    Reader reader;

    /**
     * Constructor for use by classes extending this one.
     */
    protected XMPPConnection() {

    }

    /**
     * Creates a new connection to the specified XMPP server. The default port of 5222 will
     * be used.
     *
     * @param host the name of the XMPP server to connect to; e.g. <tt>jivesoftware.com</tt>.
     * @throws XMPPException if an error occurs while trying to establish a connection.
     */
    public XMPPConnection(String host) throws XMPPException {
        this(host, 5222);
    }

    /**
     * Creates a new connection to the  to the specified XMPP server on the given port.
     *
     * @param host the name of the XMPP server to connect to; e.g. <tt>jivesoftware.com</tt>.
     * @param port the port on the server that should be used; e.g. <tt>5222</tt>.
     * @throws XMPPException if an error occurs while trying to establish a connection.
     */
    public XMPPConnection(String host, int port) throws XMPPException {
        this.host = host;
        this.port = port;
        try {
            this.socket = new Socket(host, port);
        }
        catch (UnknownHostException uhe) {
            throw new XMPPException("Could not connect to " + host + ":" + port + ".", uhe);
        }
        catch (IOException ioe) {
            throw new XMPPException("XMPPError connecting to " + host + ":" + port + ".", ioe);
        }
        init();
    }

    /**
     * Returns the connection ID for this connection, which is the value set by the server
     * when opening a XMPP stream. If the server does not set a connection ID, this value
     * will be null.
     *
     * @return the ID of this connection returned from the XMPP server.
     */
    public String getConnectionID() {
        return connectionID;
    }

    /**
     * Returns the host name of the XMPP server for this connection.
     *
     * @return the host name of the XMPP server.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port number of the XMPP server for this connection. The default port
     * for normal connections is 5222. The default port for SSL connections is 5223.
     *
     * @return the port number of the XMPP server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the username that was used to login or <tt>null</tt> if not logged in yet.
     *
     * @return the username used to login.
     */
    public String getUsername() {
        if (!isAuthenticated()) {
            return null;
        }
        return username;
    }

    /**
     * Returns the resource that was used to login or <tt>null</tt> if not logged in yet.
     * If no resource was used to login, the default resource "Smack" will be returned.
     *
     * @return the resource used to login.
     */
    public String getResource() {
        if (!isAuthenticated()) {
            return null;
        }
        return resource;
    }

    /**
     * Logs in to the server using the strongest authentication mode supported by
     * the server, then set our presence to available. If more than five seconds
     * elapses in each step of the authentication process without a response from
     * the server, or if an error occurs, a XMPPException will be thrown.
     *
     * @param username the username.
     * @param password the password.
     * @throws XMPPException if an error occurs.
     */
    public void login(String username, String password) throws XMPPException {
        login(username, password, "Smack");
    }

    /**
     * Logs in to the server using the strongest authentication mode supported by
     * the server, then set our presence to available. If more than five seconds
     * elapses in each step of the authentication process without a response from
     * the server, or if an error occurs, a XMPPException will be thrown.
     *
     * @param username the username.
     * @param password the password.
     * @param resource the resource.
     * @throws XMPPException if an error occurs.
     * @throws IllegalStateException if not connected to the server, or already logged in
     *      to the serrver.
     */
    public synchronized void login(String username, String password, String resource)
            throws XMPPException
    {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        }
        if (authenticated) {
            throw new IllegalStateException("Already logged in to server.");
        }
        this.username = username;
        this.resource = resource;
        // If we send an authentication packet in "get" mode with just the username,
        // the server will return the list of authentication protocols it supports.
        Authentication discoveryAuth = new Authentication();
        discoveryAuth.setType(IQ.Type.GET);
        discoveryAuth.setUsername(username);

        PacketCollector collector = packetReader.createPacketCollector(
                new PacketIDFilter(discoveryAuth.getPacketID()));
        // Send the packet
        packetWriter.sendPacket(discoveryAuth);
        // Wait up to five seconds for a response from the server.
        IQ response = (IQ)collector.nextResult(5000);
        if (response == null) {
            throw new XMPPException("No response from the server.");
        }
        // If the server replied with an error, throw an exception.
        else if (response.getType() == IQ.Type.ERROR) {
            throw new XMPPException(response.getError());
        }
        // Otherwise, no error so continue processing.
        Authentication authTypes = (Authentication)response;
        collector.cancel();

        // Now, create the authentication packet we'll send to the server.
        Authentication auth = new Authentication();
        auth.setUsername(username);

        // Figure out if we should use digest or plain text authentication.
        if (authTypes.getDigest() != null) {
            auth.setDigest(connectionID, password);
        }
        else if (authTypes.getPassword() != null) {
            auth.setPassword(password);
        }
        else {
            throw new XMPPException("Server does not support compatible authentication mechanism.");
        }

        auth.setResource(resource);

        collector = packetReader.createPacketCollector(
                new PacketIDFilter(auth.getPacketID()));
        // Send the packet.
        packetWriter.sendPacket(auth);
        // Wait up to five seconds for a response from the server.
        response = (IQ)collector.nextResult(5000);
        if (response == null) {
            throw new XMPPException("Authentication failed.");
        }
        else if (response.getType() == IQ.Type.ERROR) {
            throw new XMPPException(response.getError());
        }
        // We're done with the collector, so explicitly cancel it.
        collector.cancel();
        // Set presence to online.
        packetWriter.sendPacket(new Presence(Presence.Type.AVAILABLE));

        // Finally, create the roster.
        this.roster = new Roster(this);
        roster.reload();

        // Indicate that we're now authenticated.
        authenticated = true;

        // If debugging is enabled, change the the debug window title to include the
        // name we are now logged-in as.
        if (DEBUG_ENABLED) {
            String title = "Smack Debug Window -- " + username + "@" + getHost() + ":" + getPort();
            if (resource != null) {
                title += "/" + resource;
            }
            debugFrame.setTitle(title);
        }
    }

    /**
     * Returns the roster for the user logged into the server. If the user has not yet
     * logged into the server, this method will return <tt>null</tt>.
     *
     * @return the user's roster, or <tt>null</tt> if the user has not logged in yet.
     */
    public Roster getRoster() {
        if (roster == null) {
            return null;
        }
        // If this is the first time the user has asked for the roster after calling
        // login, we want to wait up to 2 seconds for the server to send back the
        // user's roster. This behavior shields API users from having to worry about the
        // fact that roster operations are asynchronous, although they'll still have to
        // listen for changes to the roster. Note: because of this waiting logic, internal
        // Smack code should be wary about calling the getRoster method, and may need to
        // access the roster object directly.
        int elapsed = 0;
        while (!roster.rosterInitialized && elapsed <= 2000) {
            try {
                Thread.sleep(500);
            }
            catch (Exception e) { }
            elapsed += 500;
        }
        return roster;
    }

    /**
     * Returns an account manager instance for this connection.
     *
     * @return an account manager for this connection.
     */
    public synchronized AccountManager getAccountManager() {
        if (accountManager == null) {
            accountManager = new AccountManager(this);
        }
        return accountManager;
    }

    /**
     * Creates a new chat with the specified participant. The participant should
     * be a valid XMPP user such as <tt>jdoe@jivesoftware.com</tt> or
     * <tt>jdoe@jivesoftware.com/work</tt>.
     *
     * @param participant the person to start the conversation with.
     * @return a new Chat object.
     */
    public Chat createChat(String participant) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        }
        return new Chat(this, participant);
    }

    /**
     * Creates a new group chat connected to the specified room. The room name
     * should be a valid conference id, such as <tt>chatroom@jivesoftware.com</tt>.
     *
     * @param room the name of the room.
     * @return a new GroupChat object.
     */
    public GroupChat createGroupChat(String room) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        }
        return new GroupChat(this, room);
    }

    /**
     * Returns true if currently connected to the XMPP server.
     *
     * @return true if connected.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns true if currently authenticated by successfully calling the login method.
     *
     * @return true if authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Closes the connection by setting presence to unavailable then closing the stream to
     * the XMPP server. Once a connection has been closed, it cannot be re-opened.
     */
    public void close() {
        // Set presence to offline.
        packetWriter.sendPacket(new Presence(Presence.Type.UNAVAILABLE));
        packetWriter.shutdown();
        packetReader.shutdown();
        // Wait 100 ms for processes to clean-up, then shutdown.
        try {
            Thread.sleep(100);
        }
        catch (Exception e) { }
        try {
            socket.close();
        }
        catch (Exception e) { }
        authenticated = false;
        connected = false;
    }

    /**
     * Sends the specified packet to the server.
     *
     * @param packet the packet to send.
     */
    public void sendPacket(Packet packet) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        }
        packetWriter.sendPacket(packet);
    }

    /**
     * Registers a packet listener with this connection. A packet filter determines
     * which packets will be delivered to the listener.
     *
     * @param packetListener the packet listener to notify of new packets.
     * @param packetFilter the packet filter to use.
     */
    public void addPacketListener(PacketListener packetListener, PacketFilter packetFilter) {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to server.");
        }
        packetReader.addPacketListener(packetListener, packetFilter);
    }

    /**
     * Removes a packet listener from this connection.
     *
     * @param packetListener the packet listener to remove.
     */
    public void removePacketListener(PacketListener packetListener) {
        packetReader.removePacketListener(packetListener);
    }

    /**
     * Creates a new packet collector for this connection. A packet filter determines
     * which packets will be accumulated by the collector.
     *
     * @param packetFilter the packet filter to use.
     * @return a new packet collector.
     */
    public PacketCollector createPacketCollector(PacketFilter packetFilter) {
        return packetReader.createPacketCollector(packetFilter);
    }

    /**
     * Adds a connection listener to this connection that will be notified when
     * the connection closes or fails.
     *
     * @param connectionListener a connection listener.
     */
    public void addConnectionListener(ConnectionListener connectionListener) {
        if (connectionListener == null) {
            return;
        }
        synchronized (packetReader.connectionListeners) {
            if (!packetReader.connectionListeners.contains(connectionListener)) {
                packetReader.connectionListeners.add(connectionListener);
            }
        }
    }

    /**
     * Removes a connection listener from this connection.
     *
     * @param connectionListener a connection listener.
     */
    public void removeConnectionListener(ConnectionListener connectionListener) {
        synchronized (packetReader.connectionListeners) {
            packetReader.connectionListeners.remove(connectionListener);
        }
    }

    /**
     * Initializes the connection by creating a packet reader and writer and opening a
     * XMPP stream to the server.
     *
     * @throws XMPPException if establishing a connection to the server fails.
     */
    void init() throws XMPPException {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        }
        catch (IOException ioe) {
            throw new XMPPException("XMPPError establishing connection with server.", ioe);
        }

        // If debugging is enabled, we open a window and write out all network traffic.
        // The method that creates the debug GUI returns PacketListener that we must add
        // after the packet reader and writer are created.
        PacketListener debugListener = null;
        if (DEBUG_ENABLED) {
            debugListener = createDebug();
        }

        packetWriter = new PacketWriter(this);
        packetReader = new PacketReader(this);

        // If debugging is enabled, we should start the thread that will listen for
        // all packets and then log them.
        if (DEBUG_ENABLED) {
            packetReader.addPacketListener(debugListener, null);
        }
        // Start the packet writer. This will open a XMPP stream to the server
        packetWriter.startup();
        // Start the packet reader. The startup() method will block until we
        // get an opening stream packet back from server.
        packetReader.startup();

        // Make note of the fact that we're now connected.
        connected = true;
    }

    /**
     * Creates the debug process, which is a GUI window that displays XML traffic.
     * This method must be called before the packet reader and writer are created because
     * it wraps the reader and writer objects with special logging implementations.
     * The method returns a PacketListner that must be added after the packet reader is
     * created.
     *
     * @return a PacketListener used by the debugging process that must be added after the packet
     *      reader is created.
     */
    private PacketListener createDebug() {
        // Use the native look and feel.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        debugFrame = new JFrame("Smack Debug Window -- " + getHost() + ":" + getPort());

        // We'll arrange the UI into four tabs. The first tab contains all data, the second
        // client generated XML, the third server generated XML, and the fourth is packet
        // data from the server as seen by Smack.
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel allPane = new JPanel();
        allPane.setLayout(new GridLayout(3, 1));
        tabbedPane.add("All", allPane);

        // Create UI elements for client generated XML traffic.
        final JTextArea sentText1 = new JTextArea();
        final JTextArea sentText2 = new JTextArea();
        sentText1.setEditable(false);
        sentText2.setEditable(false);
        sentText1.setForeground(new Color(112, 3, 3));
        sentText2.setForeground(new Color(112, 3, 3));
        allPane.add(new JScrollPane(sentText1));
        tabbedPane.add("Client", new JScrollPane(sentText2));

        // Create UI elements for server generated XML traffic.
        final JTextArea receivedText1 = new JTextArea();
        final JTextArea receivedText2 = new JTextArea();
        receivedText1.setEditable(false);
        receivedText2.setEditable(false);
        receivedText1.setForeground(new Color(6, 76, 133));
        receivedText2.setForeground(new Color(6, 76, 133));
        allPane.add(new JScrollPane(receivedText1));
        tabbedPane.add("Server", new JScrollPane(receivedText2));

        // Create UI elements for interpreted XML traffic.
        final JTextArea interpretedText1 = new JTextArea();
        final JTextArea interpretedText2 = new JTextArea();
        interpretedText1.setEditable(false);
        interpretedText2.setEditable(false);
        interpretedText1.setForeground(new Color(1, 94, 35));
        interpretedText2.setForeground(new Color(1, 94, 35));
        allPane.add(new JScrollPane(interpretedText1));
        tabbedPane.add("Interpreted Packets", new JScrollPane(interpretedText2));

        debugFrame.getContentPane().add(tabbedPane);

        debugFrame.setSize(550, 400);
        debugFrame.setVisible(true);

        // Create a special Reader that wraps the main Reader and logs data to the GUI.
        Reader debugReader = new Reader() {

            Reader myReader = reader;

            public int read(char cbuf[], int off, int len) throws IOException {
                int count = myReader.read(cbuf, off, len);
                if (count > 0) {
                    String str = new String(cbuf, off, count);
                    int index = str.lastIndexOf(">");
                    if (index != -1) {
                        receivedText1.append(str.substring(0, index+1));
                        receivedText2.append(str.substring(0, index+1));
                        receivedText1.append(NEWLINE);
                        receivedText2.append(NEWLINE);
                        if (str.length() > index) {
                            receivedText1.append(str.substring(index+1));
                            receivedText2.append(str.substring(index+1));
                        }
                    }
                    else {
                        receivedText1.append(str);
                        receivedText2.append(str);
                    }
                }
                return count;
            }

            public void close() throws IOException {
                myReader.close();
            }

            public int read() throws IOException {
                return myReader.read();
            }

            public int read(char cbuf[]) throws IOException {
                return myReader.read(cbuf);
            }

            public long skip(long n) throws IOException {
                return myReader.skip(n);
            }

            public boolean ready() throws IOException {
                return myReader.ready();
            }

            public boolean markSupported() {
                return myReader.markSupported();
            }

            public void mark(int readAheadLimit) throws IOException {
                myReader.mark(readAheadLimit);
            }

            public void reset() throws IOException {
                myReader.reset();
            }
        };

        // Create a special Writer that wraps the main Writer and logs data to the GUI.
        Writer debugWriter = new Writer() {

            Writer myWriter = writer;

            public void write(char cbuf[], int off, int len) throws IOException {
                myWriter.write(cbuf, off, len);
                String str = new String(cbuf, off, len);
                sentText1.append(str);
                sentText2.append(str);
                if (str.endsWith(">")) {
                    sentText1.append(NEWLINE);
                    sentText2.append(NEWLINE);
                }
            }

            public void flush() throws IOException {
                myWriter.flush();
            }

            public void close() throws IOException {
                myWriter.close();
            }

            public void write(int c) throws IOException {
                myWriter.write(c);
            }

            public void write(char cbuf[]) throws IOException {
                myWriter.write(cbuf);
                String str = new String(cbuf);
                sentText1.append(str);
                sentText2.append(str);
                if (str.endsWith(">")) {
                    sentText1.append(NEWLINE);
                    sentText2.append(NEWLINE);
                }
            }

            public void write(String str) throws IOException {
                myWriter.write(str);
                sentText1.append(str);
                sentText2.append(str);
                if (str.endsWith(">")) {
                    sentText1.append(NEWLINE);
                    sentText2.append(NEWLINE);
                }
            }

            public void write(String str, int off, int len) throws IOException {
                myWriter.write(str, off, len);
                str = str.substring(off, off + len);
                sentText1.append(str);
                sentText2.append(str);
                if (str.endsWith(">")) {
                    sentText1.append(NEWLINE);
                    sentText2.append(NEWLINE);
                }
            }
        };

        // Assign the reader/writer objects to use the debug versions. The packet reader
        // and writer will use the debug versions when they are created.
        reader = debugReader;
        writer = debugWriter;

        // Create a thread that will listen for all incoming packets and write them to
        // the GUI. This is what we call "interpreted" packet data, since it's the packet
        // data as Smack sees it and not as it's coming in as raw XML.
        PacketListener debugListener = new PacketListener() {
            public void processPacket(Packet packet) {
                interpretedText1.append(packet.toXML());
                interpretedText2.append(packet.toXML());
                interpretedText1.append(NEWLINE);
                interpretedText2.append(NEWLINE);
            }
        };
        return debugListener;
    }
}
