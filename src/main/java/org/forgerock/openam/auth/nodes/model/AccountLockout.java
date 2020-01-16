/*
 * Copyright © 2020 ForgeRock, AS.
 *
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * AccountLockout-IncrementerChecker: Created by Charan Mann on 1/14/20 , 12:12 PM.
 */

package org.forgerock.openam.auth.nodes.model;

import java.util.Objects;

/**
 * Account Lockout data object
 */
public class AccountLockout {

    private int invalidCount;

    private long lastInvalidAt;

    /**
     * Default constructor
     */
    public AccountLockout() {
        this.invalidCount = 0;
        this.lastInvalidAt = 0;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public long getLastInvalidAt() {
        return lastInvalidAt;
    }

    public void setLastInvalidAt(long lastInvalidAt) {
        this.lastInvalidAt = lastInvalidAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountLockout that = (AccountLockout) o;
        return invalidCount == that.invalidCount &&
                Objects.equals(lastInvalidAt, that.lastInvalidAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invalidCount, lastInvalidAt);
    }

    @Override
    public String toString() {
        return "AccountLockout {" +
                "invalidCount=" + invalidCount +
                ", lastInvalidAt=" + lastInvalidAt +
                '}';
    }
}
