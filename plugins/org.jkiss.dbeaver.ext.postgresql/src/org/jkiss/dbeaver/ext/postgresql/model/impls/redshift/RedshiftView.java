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
package org.jkiss.dbeaver.ext.postgresql.model.impls.redshift;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreDataType;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreSchema;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreTableColumn;
import org.jkiss.dbeaver.ext.postgresql.model.PostgreView;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RedshiftView
 */
public class RedshiftView extends PostgreView
{
    private static final Log log = Log.getLog(RedshiftView.class);

    private List<PostgreTableColumn> lateBindingColumns = null;

    public RedshiftView(PostgreSchema catalog) {
        super(catalog);
    }

    public RedshiftView(PostgreSchema catalog, ResultSet dbResult) {
        super(catalog, dbResult);
    }

    @Override
    public List<? extends PostgreTableColumn> getAttributes(@NotNull DBRProgressMonitor monitor) throws DBException {
        if (lateBindingColumns != null) {
            return lateBindingColumns;
        }

        List<? extends PostgreTableColumn> attrs = super.getAttributes(monitor);
        if (isPersisted() && CommonUtils.isEmpty(attrs) && isViewVwithNoSchemaBinding(monitor)) {
            lateBindingColumns = readLateBindingColumns(monitor);
            if (!CommonUtils.isEmpty(lateBindingColumns)) {
                return lateBindingColumns;
            }
        }
        return attrs;
    }

    @Override
    public PostgreTableColumn getAttribute(@NotNull DBRProgressMonitor monitor, @NotNull String attributeName) throws DBException {
        if (lateBindingColumns != null) {
            return DBUtils.findObject(lateBindingColumns, attributeName);
        }
        return super.getAttribute(monitor, attributeName);
    }

    private boolean isViewVwithNoSchemaBinding(DBRProgressMonitor monitor) throws DBException {
        getObjectDefinitionText(monitor, Collections.emptyMap());
        String viewDefinition = getSource();
        return viewDefinition.contains("with no schema binding");
    }

    private List<PostgreTableColumn> readLateBindingColumns(DBRProgressMonitor monitor) throws DBException {
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Read redshift view late binding columns")) {
            try (JDBCPreparedStatement dbStat = session.prepareStatement("select * from pg_get_late_binding_view_cols() \n" +
                "cols(view_schema name, view_name name, col_name name, col_type varchar, col_num int)\n" +
                "WHERE view_schema=? AND view_name=?")) {
                dbStat.setString(1, getSchema().getName());
                dbStat.setString(2, getName());
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    List<PostgreTableColumn> columns = new ArrayList<>();
                    while (dbResult.next()) {
                        String colName = JDBCUtils.safeGetString(dbResult, "col_name");
                        String colType = JDBCUtils.safeGetString(dbResult, "col_type");
                        int colNum = JDBCUtils.safeGetInt(dbResult, "col_num");

                        PostgreTableColumn viewColumn = new PostgreTableColumn(this);
                        viewColumn.setName(colName);
                        viewColumn.setPersisted(true);

                        String colTypeName;
                        int colPrecision = -1, colScale = -1;
                        int divPos = colType.indexOf("(");
                        if (divPos != -1) {
                            colTypeName = colType.substring(0, divPos);
                            int divPos2 = colType.lastIndexOf(")");
                            if (divPos2 < 0) {
                                log.error("Bad column type name '" + colType + "'");
                                continue;
                            }
                            String typeMod = colType.substring(divPos + 1, divPos2).trim();
                            divPos2 = typeMod.indexOf(",");
                            if (divPos2 < 0) {
                                colPrecision = CommonUtils.toInt(typeMod);
                            } else {
                                colPrecision = CommonUtils.toInt(typeMod.substring(0, divPos2).trim());
                                colScale = CommonUtils.toInt(typeMod.substring(divPos2 + 1).trim());
                            }
                        } else {
                            colTypeName = colType;
                        }

                        PostgreDataType dataType = (PostgreDataType) getDataSource().resolveDataType(monitor, colTypeName);
                        if (dataType == null) {
                            log.error("Column type name '" + colType + "' not found");
                            continue;
                        }
                        viewColumn.setDataType(dataType);
                        if (colPrecision >= 0) viewColumn.setPrecision(colPrecision);
                        if (colScale >= 0) viewColumn.setScale(colScale);
                        viewColumn.setOrdinalPosition(colNum);
                        columns.add(viewColumn);

                    }
                    return columns;
                }
            }

        } catch (SQLException e) {
            throw new DBException("Error reading view definition: " + e.getMessage(), e);
        }
    }


}
