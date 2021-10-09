/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.model.impl.auth;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.access.DBASession;
import org.jkiss.dbeaver.model.auth.DBAAuthSpace;
import org.jkiss.dbeaver.model.auth.DBAAuthToken;
import org.jkiss.dbeaver.model.auth.DBASessionContext;
import org.jkiss.dbeaver.model.auth.DBASessionProviderService;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Session context implementation
 */
public class SessionContextImpl implements DBASessionContext {
    private static final Log log = Log.getLog(SessionContextImpl.class);

    private final DBASessionContext parentContext;
    private final List<DBASession> sessions = new ArrayList<>();

    public SessionContextImpl(DBASessionContext parentContext) {
        this.parentContext = parentContext;
    }

    @Nullable
    @Override
    public DBASession getSpaceSession(DBAAuthSpace space) throws DBException {
        for (DBASession session : sessions) {
            if (CommonUtils.equalObjects(session.getSessionSpace(), space)) {
                return session;
            }
        }
        DBASession session = parentContext == null ? null : parentContext.getSpaceSession(space);
        if (session == null) {
            DBASessionProviderService sessionProviderService = DBWorkbench.getService(DBASessionProviderService.class);
            if (sessionProviderService != null) {
                try {
                    session = sessionProviderService.acquireSession(space);
                } catch (Exception e) {
                    throw new DBException("Error acquiring session", e);
                }
            }
            if (session != null) {
                //sessions.add(session);
            }
        }
        return session;
    }

    @Override
    public DBAAuthToken[] getSavedTokens() {
        return new DBAAuthToken[0];
    }

    public void addSession(@NotNull DBASession session) {
        sessions.add(session);
    }

    @Override
    public boolean removeSession(@NotNull DBASession session) {
        return sessions.remove(session);
    }

}
