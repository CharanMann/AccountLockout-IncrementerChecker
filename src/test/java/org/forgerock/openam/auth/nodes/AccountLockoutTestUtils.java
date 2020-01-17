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
 * AccountLockout-IncrementerChecker: Created by Charan Mann on 1/16/20 , 3:28 PM.
 */

package org.forgerock.openam.auth.nodes;

import com.google.common.collect.ImmutableSet;
import org.forgerock.openam.auth.nodes.model.AccountLockout;
import org.forgerock.openam.auth.nodes.model.JsonBuilder;

import java.util.Set;

/**
 * Test utils
 */
public class AccountLockoutTestUtils {

    public Set<String> getAccountLockout(int invalidCount, long lastInvalidAt) throws Exception {

        AccountLockout accountLockout = new AccountLockout();
        accountLockout.setInvalidCount(invalidCount);
        accountLockout.setLastInvalidAt(lastInvalidAt);

        return ImmutableSet.of(JsonBuilder.getJsonBuilder().writeValueAsString(accountLockout));
    }
}
