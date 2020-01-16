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

package org.forgerock.openam.auth.nodes.decision;

import com.google.common.collect.ImmutableMap;
import com.sun.identity.idm.AMIdentity;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.nodes.AccountLockoutConfig;
import org.forgerock.openam.auth.nodes.AccountLockoutTestUtils;
import org.forgerock.openam.auth.nodes.AccountLockoutUtils;
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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.forgerock.openam.auth.nodes.AccountLockoutConfig.*;

public class AccountLockoutCheckerDecisionNodeTest {
    private final CoreWrapper mockCoreWrapper = mock(CoreWrapper.class);
    private final AMIdentity mockIdentity = mock(AMIdentity.class);
    private final JsonValue sharedState = JsonValue.json(ImmutableMap.of(USERNAME, "demo", REALM, "/"));
    private final TreeContext mockContext = new TreeContext(sharedState, new ExternalRequestContext.Builder().build(), Collections.emptyList());

    private final AccountLockoutUtils accountLockoutUtils = new AccountLockoutUtils(mockCoreWrapper);
    private final AccountLockoutTestUtils accountLockoutTestUtils = new AccountLockoutTestUtils();

    public AccountLockoutCheckerDecisionNodeTest() {
        when(mockCoreWrapper.getIdentity(anyString(), anyString())).then(invocation -> mockIdentity);
    }

    @Test
    public void shouldReturnUnlockedIfThereIsNoData() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(Collections.emptySet());
        assertEquals(AccountLockoutCheckerDecisionNode.UNLOCKED_OUTCOME, node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext).outcome);
    }

    @Test
    public void shouldReturnUnlockedIfTheMaxAttemptsAreNotExceeded() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(4, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));
        assertEquals(AccountLockoutCheckerDecisionNode.UNLOCKED_OUTCOME, node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext).outcome);
    }

    @Test
    public void shouldReturnLockedIfTheMaxAttemptsAreExceeded() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(5, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));
        assertEquals(AccountLockoutCheckerDecisionNode.LOCKED_OUTCOME, node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext).outcome);
    }

    @Test
    public void shouldResetInvalidAttemptsWhenAccountIsLocked() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(5, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));

        doAnswer(invocation -> {
            Map<String, Set<String>> attributes = invocation.getArgument(0);
            AccountLockout accountLockout = JsonBuilder.getJsonBuilder().readValue(attributes.get(
                    DEFAULT_INVALID_ATTEMPTS_ATTR).iterator().next(), AccountLockout.class);
            assertEquals(0, accountLockout.getInvalidCount());
            return null;
        }).when(mockIdentity).setAttributes(anyMap());

        assertEquals(AccountLockoutCheckerDecisionNode.LOCKED_OUTCOME,  node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext).outcome);
    }

    @Test
    public void shouldUpdateSharedStateMessageWhenAccountIsLocked() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(5, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));

        Action action = node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext);
        assertEquals(AccountLockoutCheckerDecisionNode.LOCKED_OUTCOME, action.outcome);
        assertEquals("\"Your Account has been locked.\"", action.sharedState.get(DEFAULT_FAILURE_MESSAGE_ATTR).toString());
    }

    @Test
    public void shouldNotUpdateSharedStateMessageWhenAccountIsNotLocked() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(2, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));

        Action action = node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext);
        assertEquals(AccountLockoutCheckerDecisionNode.UNLOCKED_OUTCOME, action.outcome);
        assertEquals("null", action.sharedState.get(DEFAULT_FAILURE_MESSAGE_ATTR).toString());
    }

    @Test
    public void shouldUpdateSharedStateMessageWhenWarnCounterHasReached() throws Exception {
        when(mockIdentity.getAttribute(anyString()))
                .thenReturn(accountLockoutTestUtils.getAccountLockout(3, System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));

        Action action = node(DEFAULT_INVALID_ATTEMPTS_ATTR,
                DEFAULT_LOCKOUT_COUNT, DEFAULT_WARN_COUNT, DEFAULT_FAILURE_MESSAGE_ATTR).process(mockContext);
        assertEquals(AccountLockoutCheckerDecisionNode.UNLOCKED_OUTCOME, action.outcome);
        assertEquals("\"Authentication Failed Warning: You will be locked out after 2 more failure(s).\"",
                action.sharedState.get(DEFAULT_FAILURE_MESSAGE_ATTR).toString());
    }

    private AccountLockoutCheckerDecisionNode node(String invalidAttemptAttr, int lockoutCount, int warnCount, String failureMessageAttr) {
        return new AccountLockoutCheckerDecisionNode(new AccountLockoutCheckerDecisionNode.Config() {
            public String invalidAttemptsAttribute() {
                return invalidAttemptAttr;
            }

            public int lockoutCount() {
                return lockoutCount;
            }

            public int warnCount() {
                return warnCount;
            }

            public String failureMessageAttr() {
                return failureMessageAttr;
            }
        }, accountLockoutUtils);
    }


}
