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

package javax.mail.event;

/**
 * This is the Listener interface for Store Notifications.
 *
 * @author John Mani
 */

public interface StoreListener extends java.util.EventListener {

    /**
     * Invoked when the Store generates a notification event.
     *
     * @param e the StoreEvent
     * @see StoreEvent#ALERT
     * @see StoreEvent#NOTICE
     */
    public void notification(StoreEvent e);
}
