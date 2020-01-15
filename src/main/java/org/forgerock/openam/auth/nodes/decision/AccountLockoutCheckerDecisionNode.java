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
 * AccountLockout-IncrementerChecker: Created by Charan Mann on 1/13/20 , 5:13 PM.
 */

package org.forgerock.openam.auth.nodes.decision;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.auth.nodes.AccountLockoutConfig;
import org.forgerock.openam.auth.nodes.AccountLockoutUtils;
import org.forgerock.openam.auth.nodes.model.AccountLockout;
import org.forgerock.util.i18n.PreferredLocales;

import javax.inject.Inject;
import java.util.List;


@Node.Metadata(outcomeProvider = AccountLockoutCheckerDecisionNode.OutcomeProvider.class,
        configClass = AccountLockoutCheckerDecisionNode.Config.class)
public class AccountLockoutCheckerDecisionNode implements Node {

    final static String LOCKED_OUTCOME = "Locked";
    final static String UNLOCKED_OUTCOME = "Unlocked";
    private final Config config;
    private final AccountLockoutUtils utils;
    @Inject
    public AccountLockoutCheckerDecisionNode(@Assisted Config config, AccountLockoutUtils utils) {
        this.config = config;
        this.utils = utils;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        try {
            AccountLockout accountLockout = utils.getAccountLockoutData(context, config.invalidAttemptsAttribute());
            String outcome = isLockedOut(accountLockout, context) ? LOCKED_OUTCOME : UNLOCKED_OUTCOME;
            return Action.goTo(outcome).build();
        } catch (Exception e) {
            throw new NodeProcessException(e);
        }
    }

    private boolean isLockedOut(AccountLockout accountLockout, TreeContext context) throws Exception {
        boolean accountLocked = accountLockout.getInvalidCount() >= config.lockoutCount();

        // reset failed attempts if account is locked
        if (accountLocked) {
            utils.setAccountLockoutData(context, new AccountLockout(), config.invalidAttemptsAttribute());
        }

        return accountLocked;
    }

    public interface Config extends AccountLockoutConfig {

        @Attribute(order = 200)
        default int lockoutCount() {
            return 5;
        }

        @Attribute(order = 300)
        default int warnCount() {
            return 3;
        }
    }

    public static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            return ImmutableList.of(
                    new Outcome(LOCKED_OUTCOME, LOCKED_OUTCOME),
                    new Outcome(UNLOCKED_OUTCOME, UNLOCKED_OUTCOME)
            );
        }
    }

}
