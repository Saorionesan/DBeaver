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
package org.jkiss.dbeaver.ext.db2.data;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.data.DBDFormatSettings;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.data.handlers.JDBCNumberValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * DECFLOAT type support
 */
public class DB2DecFloatValueHandler extends JDBCNumberValueHandler {

    final static int DECFLOAT_SPECIALVALUE_ENCOUNTERED = -4231;

    public DB2DecFloatValueHandler(DBSTypedObject type, DBDFormatSettings formatSettings) {
        super(type, formatSettings);
    }

    @Nullable
    @Override
    protected Object fetchColumnValue(DBCSession session, JDBCResultSet resultSet, DBSTypedObject type, int index) throws DBCException, SQLException {
        try {
            return resultSet.getBigDecimal(index);
        } catch (SQLException e) {
            if (e.getErrorCode() == DECFLOAT_SPECIALVALUE_ENCOUNTERED) {
                return resultSet.getDouble(index);
            } else {
                throw e;
            }
        }
    }

    @Override
    protected void bindParameter(JDBCSession session, JDBCPreparedStatement statement, DBSTypedObject paramType, int paramIndex, Object value) throws SQLException, DBCException {
        if (value instanceof BigDecimal) {
            statement.setBigDecimal(paramIndex, (BigDecimal) value);
        } else if (value instanceof Double) {
            statement.setDouble(paramIndex, (Double) value);
        } else {
            super.bindParameter(session, statement, paramType, paramIndex, value);
        }
    }
}