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

import org.forgerock.openam.auth.node.api.AbstractNodeAmPlugin;
import org.forgerock.openam.auth.node.api.Node;

import java.util.Collections;
import java.util.Map;

/**
 * Core nodes installed by default with no engine dependencies.
 */
public class AccountLockoutCheckerDecisionNodePlugin extends AbstractNodeAmPlugin {

    static private String currentVersion = "1.0.0";


    @Override
    protected Map<String, Iterable<? extends Class<? extends Node>>> getNodesByVersion() {
        return Collections.singletonMap(AccountLockoutCheckerDecisionNodePlugin.currentVersion,
                Collections.singletonList(AccountLockoutCheckerDecisionNode.class));
    }

    @Override
    public String getPluginVersion() {
        return AccountLockoutCheckerDecisionNodePlugin.currentVersion;
    }

}
