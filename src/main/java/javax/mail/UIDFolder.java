/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.mail;

import java.util.NoSuchElementException;

/**
 * The <code>UIDFolder</code> interface is implemented by Folders
 * that can support the "disconnected" mode of operation, by providing
 * unique-ids for messages in the folder. This interface is based on
 * the IMAP model for supporting disconnected operation. <p>
 * <p>
 * A Unique identifier (UID) is a positive long value, assigned to
 * each message in a specific folder. Unique identifiers are assigned
 * in a strictly <strong>ascending</strong> fashion in the mailbox.
 * That is, as each message is added to the mailbox it is assigned a
 * higher UID than the message(s) which were added previously. Unique
 * identifiers persist across sessions. This permits a client to
 * resynchronize its state from a previous session with the server. <p>
 * <p>
 * Associated with every mailbox is a unique identifier validity value.
 * If unique identifiers from an earlier session fail to persist to
 * this session, the unique identifier validity value
 * <strong>must</strong> be greater than the one used in the earlier
 * session. <p>
 * <p>
 * Refer to <A HREF="http://www.ietf.org/rfc/rfc2060.txt">RFC 2060</A>
 * for more information.
 * <p>
 * All the Folder objects returned by the default IMAP provider implement
 * the UIDFolder interface.  Use it as follows:
 * <blockquote><pre>
 *
 * 	Folder f = store.getFolder("whatever");
 * 	UIDFolder uf = (UIDFolder)f;
 * 	long uid = uf.getUID(msg);
 *
 * </pre></blockquote><p>
 *
 * @author Bill Shannon
 * @author John Mani
 */

public interface UIDFolder {

    /**
     * This is a special value that can be used as the <code>end</code>
     * parameter in <code>getMessagesByUID(start, end)</code>, to denote the
     * UID of the last message in the folder.
     *
     * @see #getMessagesByUID
     */
    long LASTUID = -1;
    /**
     * The largest value possible for a UID, a 32-bit unsigned integer.
     * This can be used to fetch all new messages by keeping track of the
     * last UID that was seen and using:
     * <blockquote><pre>
     *
     * 	Folder f = store.getFolder("whatever");
     * 	UIDFolder uf = (UIDFolder)f;
     * 	Message[] newMsgs =
     * 		uf.getMessagesByUID(lastSeenUID + 1, UIDFolder.MAXUID);
     *
     * </pre></blockquote><p>
     *
     * @since JavaMail 1.6
     */
    long MAXUID = 0xffffffffL; // max 32-bit unsigned int

    /**
     * Returns the UIDValidity value associated with this folder. <p>
     * <p>
     * Clients typically compare this value against a UIDValidity
     * value saved from a previous session to insure that any cached
     * UIDs are not stale.
     *
     * @return UIDValidity
     * @throws MessagingException for failures
     */
    long getUIDValidity() throws MessagingException;

    /**
     * Get the Message corresponding to the given UID. If no such
     * message exists, <code>null</code> is returned.
     *
     * @param uid UID for the desired message
     * @return the Message object. <code>null</code> is returned
     * if no message corresponding to this UID is obtained.
     * @throws MessagingException for failures
     */
    Message getMessageByUID(long uid) throws MessagingException;

    /**
     * Get the Messages specified by the given range. The special
     * value LASTUID can be used for the <code>end</code> parameter
     * to indicate the UID of the last message in the folder. <p>
     * <p>
     * Note that <code>end</code> need not be greater than <code>start</code>;
     * the order of the range doesn't matter.
     * Note also that, unless the folder is empty, use of LASTUID ensures
     * that at least one message will be returned - the last message in the
     * folder.
     *
     * @param start start UID
     * @param end   end UID
     * @return array of Message objects
     * @throws MessagingException for failures
     * @see #LASTUID
     */
    Message[] getMessagesByUID(long start, long end)
            throws MessagingException;

    /**
     * Get the Messages specified by the given array of UIDs. If any UID is
     * invalid, <code>null</code> is returned for that entry. <p>
     * <p>
     * Note that the returned array will be of the same size as the specified
     * array of UIDs, and <code>null</code> entries may be present in the
     * array to indicate invalid UIDs.
     *
     * @param uids array of UIDs
     * @return array of Message objects
     * @throws MessagingException for failures
     */
    Message[] getMessagesByUID(long[] uids)
            throws MessagingException;

    /**
     * Get the UID for the specified message. Note that the message
     * <strong>must</strong> belong to this folder. Otherwise
     * java.util.NoSuchElementException is thrown.
     *
     * @param message Message from this folder
     * @return UID for this message
     * @throws NoSuchElementException if the given Message
     *                                is not in this Folder.
     * @throws MessagingException     for other failures
     */
    long getUID(Message message) throws MessagingException;

    /**
     * Returns the predicted UID that will be assigned to the
     * next message that is appended to this folder.
     * Messages might be appended to the folder after this value
     * is retrieved, causing this value to be out of date.
     * This value might only be updated when a folder is first opened.
     * Note that messages may have been appended to the folder
     * while it was open and thus this value may be out of
     * date. <p>
     * <p>
     * If the value is unknown, -1 is returned.  <p>
     *
     * @return the UIDNEXT value, or -1 if unknown
     * @throws MessagingException for failures
     * @since JavaMail 1.6
     */
    long getUIDNext() throws MessagingException;

    /**
     * A fetch profile item for fetching UIDs.
     * This inner class extends the <code>FetchProfile.Item</code>
     * class to add new FetchProfile item types, specific to UIDFolders.
     * The only item currently defined here is the <code>UID</code> item.
     *
     * @see FetchProfile
     */
    class FetchProfileItem extends FetchProfile.Item {
        /**
         * UID is a fetch profile item that can be included in a
         * <code>FetchProfile</code> during a fetch request to a Folder.
         * This item indicates that the UIDs for messages in the specified
         * range are desired to be prefetched. <p>
         * <p>
         * An example of how a client uses this is below:
         * <blockquote><pre>
         *
         * 	FetchProfile fp = new FetchProfile();
         * 	fp.add(UIDFolder.FetchProfileItem.UID);
         * 	folder.fetch(msgs, fp);
         *
         * </pre></blockquote>
         */
        public static final FetchProfileItem UID =
                new FetchProfileItem("UID");

        protected FetchProfileItem(String name) {
            super(name);
        }
    }
}
