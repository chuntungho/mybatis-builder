/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.mybatis.generator.config.ColumnRenamingRule;
import org.mybatis.generator.config.DomainObjectRenamingRule;
import org.mybatis.generator.config.TableConfiguration;

/**
 * Mutable bean holding default TableConfiguration settings.
 * Cannot extend TableConfiguration in MBG 2.0.0 because its constructor is protected.
 *
 * @author Tony Ho
 */
public class TableConfigurationWrapper {
    private boolean insertStatementEnabled = true;
    private boolean updateByPrimaryKeyStatementEnabled = true;
    private boolean selectByPrimaryKeyStatementEnabled = true;
    private boolean deleteByPrimaryKeyStatementEnabled = true;

    private boolean selectByExampleStatementEnabled = true;
    private boolean countByExampleStatementEnabled = true;
    private boolean updateByExampleStatementEnabled = true;
    private boolean deleteByExampleStatementEnabled = true;

    private RenamingRule domainObjectRenamingRule = new RenamingRule();
    private RenamingRule columnRenamingRule = new RenamingRule();

    private GeneratedKeyWrapper generatedKeyWrapper = new GeneratedKeyWrapper();

    public TableConfigurationWrapper() {
    }

    public boolean isInsertStatementEnabled() {
        return insertStatementEnabled;
    }

    public void setInsertStatementEnabled(boolean insertStatementEnabled) {
        this.insertStatementEnabled = insertStatementEnabled;
    }

    public boolean isUpdateByPrimaryKeyStatementEnabled() {
        return updateByPrimaryKeyStatementEnabled;
    }

    public void setUpdateByPrimaryKeyStatementEnabled(boolean updateByPrimaryKeyStatementEnabled) {
        this.updateByPrimaryKeyStatementEnabled = updateByPrimaryKeyStatementEnabled;
    }

    public boolean isSelectByPrimaryKeyStatementEnabled() {
        return selectByPrimaryKeyStatementEnabled;
    }

    public void setSelectByPrimaryKeyStatementEnabled(boolean selectByPrimaryKeyStatementEnabled) {
        this.selectByPrimaryKeyStatementEnabled = selectByPrimaryKeyStatementEnabled;
    }

    public boolean isDeleteByPrimaryKeyStatementEnabled() {
        return deleteByPrimaryKeyStatementEnabled;
    }

    public void setDeleteByPrimaryKeyStatementEnabled(boolean deleteByPrimaryKeyStatementEnabled) {
        this.deleteByPrimaryKeyStatementEnabled = deleteByPrimaryKeyStatementEnabled;
    }

    public boolean isSelectByExampleStatementEnabled() {
        return selectByExampleStatementEnabled;
    }

    public void setSelectByExampleStatementEnabled(boolean selectByExampleStatementEnabled) {
        this.selectByExampleStatementEnabled = selectByExampleStatementEnabled;
    }

    public boolean isCountByExampleStatementEnabled() {
        return countByExampleStatementEnabled;
    }

    public void setCountByExampleStatementEnabled(boolean countByExampleStatementEnabled) {
        this.countByExampleStatementEnabled = countByExampleStatementEnabled;
    }

    public boolean isUpdateByExampleStatementEnabled() {
        return updateByExampleStatementEnabled;
    }

    public void setUpdateByExampleStatementEnabled(boolean updateByExampleStatementEnabled) {
        this.updateByExampleStatementEnabled = updateByExampleStatementEnabled;
    }

    public boolean isDeleteByExampleStatementEnabled() {
        return deleteByExampleStatementEnabled;
    }

    public void setDeleteByExampleStatementEnabled(boolean deleteByExampleStatementEnabled) {
        this.deleteByExampleStatementEnabled = deleteByExampleStatementEnabled;
    }

    public RenamingRule getDomainObjectRenamingRule() {
        return domainObjectRenamingRule;
    }

    public void setDomainObjectRenamingRule(RenamingRule domainObjectRenamingRule) {
        this.domainObjectRenamingRule = domainObjectRenamingRule;
    }

    public RenamingRule getColumnRenamingRule() {
        return columnRenamingRule;
    }

    public void setColumnRenamingRule(RenamingRule columnRenamingRule) {
        this.columnRenamingRule = columnRenamingRule;
    }

    public GeneratedKeyWrapper getGeneratedKeyWrapper() {
        return generatedKeyWrapper;
    }

    public void setGeneratedKeyWrapper(GeneratedKeyWrapper generatedKeyWrapper) {
        this.generatedKeyWrapper = generatedKeyWrapper;
    }

    /**
     * Returns a partially-configured TableConfiguration.Builder with all default statement flags
     * and renaming rules applied. Callers add table-specific settings (tableName, domainName, etc.).
     */
    public TableConfiguration.Builder createTableConfigBuilder() {
        TableConfiguration.Builder builder = new TableConfiguration.Builder()
                .withInsertStatementEnabled(insertStatementEnabled)
                .withUpdateByPrimaryKeyStatementEnabled(updateByPrimaryKeyStatementEnabled)
                .withSelectByPrimaryKeyStatementEnabled(selectByPrimaryKeyStatementEnabled)
                .withDeleteByPrimaryKeyStatementEnabled(deleteByPrimaryKeyStatementEnabled)
                .withSelectByExampleStatementEnabled(selectByExampleStatementEnabled)
                .withCountByExampleStatementEnabled(countByExampleStatementEnabled)
                .withUpdateByExampleStatementEnabled(updateByExampleStatementEnabled)
                .withDeleteByExampleStatementEnabled(deleteByExampleStatementEnabled);

        if (StringUtil.stringHasValue(domainObjectRenamingRule.getSearchString())) {
            builder.withDomainObjectRenamingRule(new DomainObjectRenamingRule(
                    domainObjectRenamingRule.getSearchString(),
                    domainObjectRenamingRule.getReplaceString()));
        }

        if (StringUtil.stringHasValue(columnRenamingRule.getSearchString())) {
            builder.withColumnRenamingRule(new ColumnRenamingRule(
                    columnRenamingRule.getSearchString(),
                    columnRenamingRule.getReplaceString()));
        }

        return builder;
    }
}
