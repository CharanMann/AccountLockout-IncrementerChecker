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
 * AccountLockout-IncrementerChecker: Created by Charan Mann on 1/16/20 , 3:26 PM.
 */

package org.forgerock.openam.auth.nodes.incrementer;

import com.google.common.collect.ImmutableMap;
import com.sun.identity.idm.AMIdentity;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.nodes.AccountLockoutTestUtils;
import org.forgerock.openam.auth.nodes.AccountLockoutUtils;
import org.forgerock.openam.auth.nodes.incrementor.AccountLockoutIncrementerNode;
import org.forgerock.openam.auth.nodes.model.AccountLockout;
import org.forgerock.openam.auth.nodes.model.JsonBuilder;
import org.forgerock.openam.core.CoreWrapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.forgerock.openam.auth.nodes.AccountLockoutConfig.DEFAULT_FAILURE_INTERVAL;
import static org.forgerock.openam.auth.nodes.AccountLockoutConfig.DEFAULT_INVALID_ATTEMPTS_ATTR;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for @{@link AccountLockoutIncrementerNode}
 */
public class AccountLockoutIncrementerNodeTest {
    private final CoreWrapper mockCoreWrapper = mock(CoreWrapper.class);
    private final AMIdentity mockIdentity = mock(AMIdentity.class);
    private final JsonValue sharedState = JsonValue.json(ImmutableMap.of(USERNAME, "demo", REALM, "/"));
    private final TreeContext mockContext = new TreeContext(sharedState, new ExternalRequestContext.Builder().build(), Collections.emptyList());

    private final AccountLockoutUtils accountLockoutUtils = new AccountLockoutUtils(mockCoreWrapper);
    private final AccountLockoutTestUtils accountLockoutTestUtils = new AccountLockoutTestUtils();

    public AccountLockoutIncrementerNodeTest() {
        when(mockCoreWrapper.getIdentity(anyString(), anyString())).then(invocation -> mockIdentity);
    }


    @Test
    public void shouldSetAttemptsToOneForFirstAttempt() throws Exception {
        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            AccountLockout accountLockout = JsonBuilder.getJsonBuilder().readValue(attributes.get(
                    DEFAULT_INVALID_ATTEMPTS_ATTR).iterator().next(), AccountLockout.class);
            assertEquals(1, accountLockout.getInvalidCount());
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(DEFAULT_INVALID_ATTEMPTS_ATTR, DEFAULT_FAILURE_INTERVAL).process(mockContext);
    }

    @Test
    public void shouldIncrementAttemptsWithInFailureWindow() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(2, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));

        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            AccountLockout accountLockout = JsonBuilder.getJsonBuilder().readValue(attributes.get(
                    DEFAULT_INVALID_ATTEMPTS_ATTR).iterator().next(), AccountLockout.class);
            assertEquals(3, accountLockout.getInvalidCount());
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(DEFAULT_INVALID_ATTEMPTS_ATTR, DEFAULT_FAILURE_INTERVAL).process(mockContext);
    }

    @Test
    public void shouldNotIncrementAttemptsOutsideFailureWindow() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(4, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(1801)));

        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            AccountLockout accountLockout = JsonBuilder.getJsonBuilder().readValue(attributes.get(
                    DEFAULT_INVALID_ATTEMPTS_ATTR).iterator().next(), AccountLockout.class);
            assertEquals(1, accountLockout.getInvalidCount());
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        node(DEFAULT_INVALID_ATTEMPTS_ATTR, DEFAULT_FAILURE_INTERVAL).process(mockContext);
    }


    private AccountLockoutIncrementerNode node(String invalidAttemptAttr, int failureInterval) {
        return new AccountLockoutIncrementerNode(new AccountLockoutIncrementerNode.Config() {
            public long failureInterval() {
                return failureInterval;
            }

            public String invalidAttemptsAttribute() {
                return invalidAttemptAttr;
            }
        }, accountLockoutUtils);
    }

}
