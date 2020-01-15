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
 * AccountLockout-IncrementerChecker: Created by Charan Mann on 1/14/20 , 12:38 PM.
 */

package org.forgerock.openam.auth.nodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.nodes.model.AccountLockout;
import org.forgerock.openam.auth.nodes.model.JsonBuilder;
import org.forgerock.openam.core.CoreWrapper;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

public class AccountLockoutUtils {

    private final CoreWrapper coreWrapper;

    private final ObjectMapper jacksonObjectMapper = JsonBuilder.getJsonBuilder();


    @Inject
    public AccountLockoutUtils(CoreWrapper coreWrapper) {
        this.coreWrapper = coreWrapper;
    }

    public AccountLockout getAccountLockoutData(TreeContext context, String invalidAttemptsAttribute) throws IdRepoException, SSOException, IOException {
        AMIdentity userIdentity = getUserIdentity(context);

        if (userIdentity == null) {
            throw new SSOException("User not found");
        }

        Set<String> attributes = userIdentity.getAttribute(invalidAttemptsAttribute);
        return attributes.isEmpty() ? new AccountLockout() : jacksonObjectMapper.readValue(attributes.iterator().next(), AccountLockout.class);
    }

    public void setAccountLockoutData(TreeContext context, AccountLockout accountLockout, String invalidAttemptsAttribute) throws IdRepoException, SSOException, JsonProcessingException {

        AMIdentity userIdentity = getUserIdentity(context);
        userIdentity.setAttributes(ImmutableMap.of(invalidAttemptsAttribute, ImmutableSet.of(jacksonObjectMapper.writeValueAsString(accountLockout))));
        userIdentity.store();
    }

    private AMIdentity getUserIdentity(TreeContext context) {
        String username = context.sharedState.get(USERNAME).asString();
        String realm = context.sharedState.get(REALM).asString();
        return coreWrapper.getIdentity(username, realm);
    }

    public boolean isWithinFailureDuration(AccountLockout accountLockout, int lockoutDuration) {
        long startOfLockoutDuration = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(lockoutDuration);

        // If this is first account lockout attempt, then time comparison is not required
        if (accountLockout.getInvalidCount() == 0) {
            return false;
        }

        // Check if last account lockout was within failure duration window
        return accountLockout.getLastInvalidAt() >= startOfLockoutDuration;
    }

}
