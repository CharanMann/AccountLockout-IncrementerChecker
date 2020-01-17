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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This returns unlocked or locked based on invalid attempts. Also updates shared message state with appropriate failure message.
 */
@Node.Metadata(outcomeProvider = AccountLockoutCheckerDecisionNode.OutcomeProvider.class,
        configClass = AccountLockoutCheckerDecisionNode.Config.class)
public class AccountLockoutCheckerDecisionNode implements Node {

    final static String LOCKED_OUTCOME = "Locked";
    final static String UNLOCKED_OUTCOME = "Unlocked";
    private static final Logger logger = LoggerFactory.getLogger("amAuth");
    private static final String BUNDLE = "org/forgerock/openam/auth/nodes/decision/AccountLockoutCheckerDecisionNode";
    private static final String ACCOUNT_LOCKED_MSG_KEY = "account.locked";
    private static final String ACCOUNT_LOCKED_WARNING_MSG_KEY = "account.lockWarning";

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
            logger.debug("[AccountLockoutCheckerDecisionNode]: Retrieved Invalid Attempts Attribute: " + accountLockout);

            String outcome = isLockedOut(accountLockout) ? LOCKED_OUTCOME : UNLOCKED_OUTCOME;

            ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
            JsonValue newSharedState = context.sharedState.copy();

            // Update shared failure message
            if (outcome.equals(LOCKED_OUTCOME)) {
                // reset failed attempts if account is locked
                logger.debug("[AccountLockoutCheckerDecisionNode]: Account is locked, Resetting failed attempts counter");

                accountLockout = new AccountLockout();
                logger.debug("[AccountLockoutCheckerDecisionNode]: Updating Invalid Attempts Attribute: " + accountLockout);
                utils.setAccountLockoutData(context, accountLockout, config.invalidAttemptsAttribute());

                // Set shared failure message
                newSharedState.put(config.failureMessageAttr(), bundle.getString(ACCOUNT_LOCKED_MSG_KEY));
            } else if (outcome.equals(UNLOCKED_OUTCOME)) {
                logger.debug("[AccountLockoutCheckerDecisionNode]: Account is still not locked");

                // Set warning message if warn counter has reached
                if (accountLockout.getInvalidCount() >= config.warnCount()) {

                    logger.debug("[AccountLockoutCheckerDecisionNode]: Warning counter has reached");
                    // Set shared failure message, replace placeholder with attempts left
                    String message = MessageFormat.format(bundle.getString(ACCOUNT_LOCKED_WARNING_MSG_KEY), (config.lockoutCount() - accountLockout.getInvalidCount()));
                    newSharedState.put(config.failureMessageAttr(), message);
                }
            }

            return Action.goTo(outcome).replaceSharedState(newSharedState).build();

        } catch (Exception e) {
            throw new NodeProcessException(e);
        }
    }

    private boolean isLockedOut(AccountLockout accountLockout) {
        return accountLockout.getInvalidCount() >= config.lockoutCount();
    }

    public interface Config extends AccountLockoutConfig {

        @Attribute(order = 200)
        default int lockoutCount() {
            return DEFAULT_LOCKOUT_COUNT;
        }

        @Attribute(order = 300)
        default int warnCount() {
            return DEFAULT_WARN_COUNT;
        }

        @Attribute(order = 400)
        default String failureMessageAttr() {
            return DEFAULT_FAILURE_MESSAGE_ATTR;
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
