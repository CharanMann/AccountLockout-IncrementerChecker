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

import org.forgerock.openam.annotations.sm.Attribute;

/**
 * Common interface for Node configurations
 */
public interface AccountLockoutConfig {

    String DEFAULT_INVALID_ATTEMPTS_ATTR = "sunAMAuthInvalidAttemptsData";
    int DEFAULT_FAILURE_INTERVAL = 1800;
    int DEFAULT_LOCKOUT_COUNT = 5;
    int DEFAULT_WARN_COUNT = 3;
    String DEFAULT_FAILURE_MESSAGE_ATTR = "failureMessageAttr";

    /**
     * The user profile properties for storing invalid attempts
     *
     * @return
     */
    @Attribute(order = 100)
    default String invalidAttemptsAttribute() {
        return DEFAULT_INVALID_ATTEMPTS_ATTR;
    }

}
