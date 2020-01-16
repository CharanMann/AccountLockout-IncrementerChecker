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

package org.forgerock.openam.auth.nodes.incrementor;

import com.google.inject.assistedinject.Assisted;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.auth.nodes.AccountLockoutConfig;
import org.forgerock.openam.auth.nodes.AccountLockoutUtils;
import org.forgerock.openam.auth.nodes.model.AccountLockout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * A node which contributes a configurable set of properties to be added to the user's session, if/when it is created.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class,
        configClass = AccountLockoutIncrementerNode.Config.class)
public class AccountLockoutIncrementerNode extends SingleOutcomeNode {

    private static final Logger logger = LoggerFactory.getLogger("amAuth");

    private final Config config;
    private final AccountLockoutUtils utils;

    @Inject
    public AccountLockoutIncrementerNode(@Assisted Config config, AccountLockoutUtils utils) {
        this.config = config;
        this.utils = utils;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        try {
            AccountLockout accountLockout = utils.getAccountLockoutData(context, config.invalidAttemptsAttribute());
            logger.debug("[AccountLockoutIncrementerNode]: Retrieved Invalid Attempts Attribute: " + accountLockout);

            int attempts = utils.isWithinFailureDuration(accountLockout, config.failureInterval())
                    ? accountLockout.getInvalidCount() + 1
                    : 1;

            logger.debug("[AccountLockoutIncrementerNode]: Current Invalid Attempts Attribute: " + attempts);
            accountLockout.setInvalidCount(attempts);
            accountLockout.setLastInvalidAt(System.currentTimeMillis());

            logger.debug("[AccountLockoutIncrementerNode]: Updating Invalid Attempts Attribute: " + accountLockout);
            utils.setAccountLockoutData(context, accountLockout, config.invalidAttemptsAttribute());
            return goToNext().build();
        } catch (Exception e) {
            throw new NodeProcessException(e);
        }
    }

    /**
     * Configuration for the node.
     */
    public interface Config extends AccountLockoutConfig {

        @Attribute(order = 200)
        default long failureInterval() {
            return DEFAULT_FAILURE_INTERVAL;
        }
    }

}
