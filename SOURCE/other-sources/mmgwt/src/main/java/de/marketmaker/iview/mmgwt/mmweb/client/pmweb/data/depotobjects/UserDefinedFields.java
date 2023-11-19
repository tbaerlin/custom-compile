/*
 * UserDefinedFields.java
 *
 * Created on 24.04.13 08:34
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ObjectQuery;
import de.marketmaker.iview.pmxml.UserField;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Markus Dick
 */
public class UserDefinedFields {

    public static abstract class AbstractUserFieldsTalker<T extends ObjectQuery>
            extends AbstractMmTalker<T, List<UserDefinedFields>, UserDefinedFields> {

        public AbstractUserFieldsTalker(Formula root) {
            super(root);
        }

        @Override
        public List<UserDefinedFields> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }

        @Override
        public MmTalkWrapper<UserDefinedFields> createWrapper(Formula formula) {
            return UserDefinedFields.createWrapper(formula);
        }
    }

    public static class Talker extends AbstractUserFieldsTalker<DatabaseIdQuery> {
        public Talker(Formula root) {
            super(root);
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }
    }

    public static class PortfolioTalker extends Talker {

        public void setEffectiveSince(String pmDate) {
            new EffectivePortfolioVersionMethod(pmDate, this.wrapper).invoke();
        }

        public PortfolioTalker(List<PmWebSupport.UserFieldCategory> categories) {
            super(Formula.create("Portfolio.PortfolioVersion[$" + Portfolio.EFFECTIVE_SINCE_PARAM + "]") //$NON-NLS$
                    .withMmTalkParam(Portfolio.EFFECTIVE_SINCE_PARAM, MmTalkHelper.nowAsDIDateTime())
                    .withUserFieldCategories(categories));
        }
    }

    public static MmTalkWrapper<UserDefinedFields> createWrapper(Formula formula) {
        final List<PmWebSupport.UserFieldCategory> categories = formula.getUserFieldCategories();
        final MmTalkWrapper<UserDefinedFields> cols = MmTalkWrapper.create(formula, UserDefinedFields.class);
        if (categories == null) {
            return cols;
        }
        for (int categoryIndex = 0; categoryIndex < categories.size(); categoryIndex++) {
            final PmWebSupport.UserFieldCategory category = categories.get(categoryIndex);

            final List<UserFieldDeclarationDesc> declarationDescList = category.getUserFieldDeclarationDescs();
            final int declarationDescListSize = declarationDescList.size();

            for (int declarationIndex = 0; declarationIndex < declarationDescListSize; declarationIndex++) {
                final UserFieldDeclarationDesc declaration = declarationDescList.get(declarationIndex);
                final String fieldName = declaration.getName();

                final boolean hasValueHistory = declaration.getDecl().getParsedTypeInfo().isHasValueHistory();
                addDefaultUserFieldMapper(cols, categoryIndex, category, declarationDescListSize, declarationIndex, declaration, fieldName);
                if (hasValueHistory) {
                    addDefaultUserFieldHistoryNodeMapper(cols, fieldName);
                }
            }
        }

        return cols;
    }

    private static void addDefaultUserFieldMapper(MmTalkWrapper<UserDefinedFields> cols, int categoryIndex, PmWebSupport.UserFieldCategory category, int declarationDescListSize, int declarationIndex, UserFieldDeclarationDesc declaration, String fieldName) {
        final String columnFormula = "UserField[\"" + fieldName + "\"]"; //$NON-NLS$

        final MmTalkColumnMapper<UserDefinedFields> columnMapper =
                new UserFieldMapper(columnFormula, category.getName(), categoryIndex, declarationDescListSize, declarationIndex, declaration);
        cols.appendColumnMapper(columnMapper);
    }

    private static void addDefaultUserFieldHistoryNodeMapper(MmTalkWrapper<UserDefinedFields> cols, final String fieldName) {
        addHistoryNodeMapper(cols, fieldName, HistoryItem.createWrapper(newHistoryItemWrapperPreFormula(fieldName)));
    }

    private static String newHistoryItemWrapperPreFormula(String fieldName) {
        return "$obj := object.ToList.Nth[0];" + //$NON-NLS$
                "map(#[](" +                                        //$NON-NLS$
                "makeCollection.add['Wert'; $obj.USERFIELD['" + fieldName + "'; object]]" + //$NON-NLS$
                ");" +                                              //$NON-NLS$
                "USERFIELDTRANSITIONDATES['" + fieldName + "'])";   //$NON-NLS$
    }

    private static void addHistoryNodeMapper(MmTalkWrapper<UserDefinedFields> cols, final String fieldName, MmTalkWrapper<HistoryItem> historyItemWrapper) {
        MmTalkNodeMapper<UserDefinedFields, HistoryItem> historyNode =
                new MmTalkNodeMapper<UserDefinedFields, HistoryItem>(historyItemWrapper) {
                    @Override
                    public void setValue(UserDefinedFields object, MmTalkWrapper<HistoryItem> wrapper, MMTable table) {
                        List<HistoryItem> values = wrapper.createResultObjectList(table);
                        object.historyLists.put(fieldName, values);
                    }
                };

        cols.appendNodeMapper(historyNode);
    }

    private void setField(String categoryName, int categoryListIndex, int declarationListSize, int declarationIndex, UserFieldDeclarationDesc declarationDesc, MM dataItem) {
        final PmWebSupport.UserFieldCategoryWithUserFields category = handleCategory(categoryName, categoryListIndex, declarationListSize);

        final List<UserField> fields = category.getUserFields();
        final List<UserFieldDeclarationDesc> declarations = category.getUserFieldDeclarationDescs();

        final UserField userField = new UserField();
        userField.setName(declarationDesc.getName());

        userField.setDataItem(dataItem);

        fields.set(declarationIndex, userField);
        declarations.set(declarationIndex, declarationDesc);
    }

    private PmWebSupport.UserFieldCategoryWithUserFields handleCategory(String categoryName, int categoryListIndex, int declarationListSize) {
        final PmWebSupport.UserFieldCategoryWithUserFields category;
        if (categoryListIndex == this.categories.size()) {
            category = addCategory(categoryName, declarationListSize);
        }
        else if (categoryListIndex < this.categories.size()) {
            final PmWebSupport.UserFieldCategoryWithUserFields temp = this.categories.get(categoryListIndex);
            if (temp == null) {
                category = initCategory(categoryName, categoryListIndex, declarationListSize);
            }
            else {
                category = temp;
            }
        }
        else {
            while (this.categories.size() < categoryListIndex) {
                this.categories.add(null);
            }
            category = addCategory(categoryName, declarationListSize);
        }
        return category;
    }

    private PmWebSupport.UserFieldCategoryWithUserFields initCategory(String categoryName, int categoryListIndex, int declarationListSize) {
        final PmWebSupport.UserFieldCategoryWithUserFields category = newCategory(categoryName, declarationListSize);

        this.categories.set(categoryListIndex, category);
        return category;
    }

    private PmWebSupport.UserFieldCategoryWithUserFields addCategory(String categoryName, int declarationListSize) {
        final PmWebSupport.UserFieldCategoryWithUserFields category = newCategory(categoryName, declarationListSize);

        this.categories.add(category);
        return category;
    }

    private PmWebSupport.UserFieldCategoryWithUserFields newCategory(String categoryName, int declarationListSize) {
        final PmWebSupport.UserFieldCategoryWithUserFields category;
        category = new PmWebSupport.UserFieldCategoryWithUserFields(categoryName);

        final List<UserField> userFields = category.getUserFields();
        final List<UserFieldDeclarationDesc> userFieldDeclarationDescs = category.getUserFieldDeclarationDescs();

        while (userFieldDeclarationDescs.size() < declarationListSize) {
            userFieldDeclarationDescs.add(null);
            userFields.add(null);
        }

        return category;
    }

    private static class UserFieldMapper extends MmTalkColumnMapper<UserDefinedFields> {
        private final String categoryName;
        private final int categoryIndex;
        private final int declarationListSize;
        private final int declarationIndex;
        private final UserFieldDeclarationDesc declarationDesc;

        public UserFieldMapper(String columnFormula, String categoryName, int categoryIndex, int declarationListSize, int declarationIndex, UserFieldDeclarationDesc declarationDesc) {
            super(columnFormula);

            this.categoryName = categoryName;
            this.categoryIndex = categoryIndex;
            this.declarationDesc = declarationDesc;
            this.declarationListSize = declarationListSize;
            this.declarationIndex = declarationIndex;
        }

        @Override
        public void setValue(UserDefinedFields userDefinedFields, MM dataItem) {
            userDefinedFields.setField(this.categoryName, this.categoryIndex, this.declarationListSize, this.declarationIndex, this.declarationDesc, dataItem);
        }
    }

    private List<PmWebSupport.UserFieldCategoryWithUserFields> categories = new ArrayList<>();
    private HashMap<String, List<HistoryItem>> historyLists = new HashMap<>();

    public List<PmWebSupport.UserFieldCategoryWithUserFields> getCategories() {
        return this.categories;
    }

    public List<HistoryItem> getHistoryList(String fieldName) {
        return this.historyLists.get(fieldName);
    }
}
