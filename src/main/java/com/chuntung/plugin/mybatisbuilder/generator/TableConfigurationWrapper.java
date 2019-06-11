/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import com.chuntung.plugin.mybatisbuilder.util.StringUtil;
import org.mybatis.generator.config.*;

/**
 * Use wrapper due to TableConfiguration has no default constructor that can't be persisted.
 *
 * @author Tony Ho
 */
public class TableConfigurationWrapper extends TableConfiguration{
    private GeneratedKeyWrapper generatedKeyWrapper;

    public TableConfigurationWrapper() {
        super(new Context(null));

        // init prototype
        generatedKeyWrapper = new GeneratedKeyWrapper();
        setDomainObjectRenamingRule(new DomainObjectRenamingRule());
        setColumnRenamingRule(new ColumnRenamingRule());
    }

    public GeneratedKeyWrapper getGeneratedKeyWrapper() {
        return generatedKeyWrapper;
    }

    public void setGeneratedKeyWrapper(GeneratedKeyWrapper generatedKeyWrapper) {
        this.generatedKeyWrapper = generatedKeyWrapper;
    }

    public TableConfiguration createTableConfig(Context context) {
        TableConfiguration tableConfig = new TableConfiguration(context);
        tableConfig.setInsertStatementEnabled(isInsertStatementEnabled());
        tableConfig.setUpdateByPrimaryKeyStatementEnabled(isUpdateByPrimaryKeyStatementEnabled());
        tableConfig.setSelectByPrimaryKeyStatementEnabled(isSelectByPrimaryKeyStatementEnabled());
        tableConfig.setDeleteByPrimaryKeyStatementEnabled(isDeleteByPrimaryKeyStatementEnabled());

        tableConfig.setSelectByExampleStatementEnabled(isSelectByExampleStatementEnabled());
        tableConfig.setCountByExampleStatementEnabled(isCountByExampleStatementEnabled());
        tableConfig.setUpdateByExampleStatementEnabled(isUpdateByExampleStatementEnabled());
        tableConfig.setDeleteByExampleStatementEnabled(isDeleteByExampleStatementEnabled());

        if (StringUtil.stringHasValue(getDomainObjectRenamingRule().getSearchString())) {
            tableConfig.setDomainObjectRenamingRule(getDomainObjectRenamingRule());
        }

        if (StringUtil.stringHasValue(getColumnRenamingRule().getSearchString())) {
            tableConfig.setColumnRenamingRule(getColumnRenamingRule());
        }

        return tableConfig;
    }
}
