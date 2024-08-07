/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.movementrules.service.mapper.search;

import fish.focus.schema.movementrules.search.v1.CustomRuleListCriteria;
import fish.focus.schema.movementrules.search.v1.CustomRuleSearchKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CustomRuleSearchFieldMapper {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRuleSearchFieldMapper.class);

    /**
     * Creates a search SQL based on the search fields
     *
     * @param searchFields
     * @param isDynamic
     * @return
     */
    public static String createSelectSearchSql(List<CustomRuleSearchValue> searchFields, boolean isDynamic) {
        StringBuilder selectBuffer = new StringBuilder();
        selectBuffer.append("SELECT ")
                .append(CustomRuleSearchTables.CUSTOM_RULE.getTableAlias())
                .append(" FROM ")
                .append(CustomRuleSearchTables.CUSTOM_RULE.getTableName())
                .append(" ")
                .append(CustomRuleSearchTables.CUSTOM_RULE.getTableAlias())
                .append(" ");
        if (searchFields != null) {
            selectBuffer.append(createSearchSql(searchFields, isDynamic));
        }
        selectBuffer
                .append(" ORDER BY ")
                .append(CustomRuleSearchTables.CUSTOM_RULE.getTableAlias())
                .append(".")
                .append(CustomRuleSearchField.NAME.getFieldName())
                .append(" DESC");
        LOG.info("[ SQL: ] {}", selectBuffer);
        return selectBuffer.toString();
    }


    /**
     * Creates the complete search SQL with joins and sets the values based on
     * the criteria
     *
     * @param criteriaList
     * @param dynamic
     * @return
     */
    private static String createSearchSql(List<CustomRuleSearchValue> criteriaList, boolean dynamic) {

        String operator = " OR ";
        if (dynamic) {
            operator = " AND ";
        }

        StringBuilder builder = new StringBuilder();

        HashMap<CustomRuleSearchField, List<CustomRuleSearchValue>> orderedValues = combineSearchFields(criteriaList);

        builder.append(buildJoin(orderedValues));
        if (!orderedValues.isEmpty()) {

            // Never select any archived rules
            builder
                    .append("WHERE ")
                    .append(CustomRuleSearchTables.CUSTOM_RULE.getTableAlias())
                    .append(".")
                    .append(CustomRuleSearchField.ARCHIVED.getFieldName())
                    .append(" = false AND (");

            boolean first = true;

            for (Entry<CustomRuleSearchField, List<CustomRuleSearchValue>> criteria : orderedValues.entrySet()) {
                first = createOperator(first, builder, operator);
                createCriteria(criteria.getValue(), criteria.getKey(), builder);
            }

        }
        builder.append(")");

        return builder.toString();
    }

    private static boolean createOperator(boolean first, StringBuilder builder, String operator) {
        if (!first) {
            builder.append(operator);
        }
        return false;
    }

    /**
     * Creates the where condition. If the list has more than one value the
     * condition will be 'IN(value1, value2)' If the list has one value the
     * condition will be '= value'
     *
     * @param criteria
     * @param builder
     */
    private static void createCriteria(List<CustomRuleSearchValue> criteria, CustomRuleSearchField field, StringBuilder builder) {
        if (criteria.size() == 1) {
            CustomRuleSearchValue searchValue = criteria.get(0);
            builder
                    .append(buildTableAliasName(searchValue.getField()))
                    .append(addParameters(searchValue));
        } else {
            builder
                    .append(buildInSqlStatement(criteria, field));
        }


    }

    private static String buildInSqlStatement(List<CustomRuleSearchValue> searchValues, CustomRuleSearchField field) {
        StringBuilder builder = new StringBuilder();

        builder.append(buildTableAliasName(field));

        builder.append(" IN ( ");
        boolean first = true;
        for (CustomRuleSearchValue searchValue : searchValues) {
            if (first) {
                first = false;
                builder.append(buildValueFromClassType(searchValue));
            } else {
                builder.append(", ").append(buildValueFromClassType(searchValue));
            }
        }
        builder.append(" )");
        return builder.toString();
    }

    private static String buildValueFromClassType(CustomRuleSearchValue entry) {
        StringBuilder builder = new StringBuilder();
        if (entry.getField().getClazz().isAssignableFrom(Integer.class)) {
            builder.append(entry.getValue());
        } else if (entry.getField().getClazz().isAssignableFrom(Double.class)) {
            builder.append(entry.getValue());
        } else {
            builder.append("'").append(entry.getValue()).append("'");
        }
        return builder.toString();
    }

    /**
     * Created the Join statement based on the join type. The resulting String
     * can be:
     * <p>
     * JOIN LEFT JOIN JOIN FETCH ( based on fetch )
     *
     * @param fetch create a JOIN FETCH or plain JOIN
     * @param type
     * @return
     */
    private static String getJoin(boolean fetch, JoinType type) {
        StringBuilder builder = new StringBuilder();
        builder.append(" ").append(type.name()).append(" ");
        builder.append("JOIN ");
        if (fetch) {
            builder.append("FETCH ");
        }
        return builder.toString();
    }

    /**
     * Builds JPA joins based on the search criteria provided. In some cases
     * there is no need for a join and the JQL query runs faster
     *
     * @param orderedValues
     * @return
     */
    private static String buildJoin(HashMap<CustomRuleSearchField, List<CustomRuleSearchValue>> orderedValues) {
        StringBuilder builder = new StringBuilder();

        if (orderedValues.containsKey(CustomRuleSearchField.TICKET_ACTION_USER)) {
            builder.append(getJoin(false, JoinType.INNER))
                    .append(CustomRuleSearchTables.CUSTOM_RULE.getTableAlias())
                    .append(".")
                    .append("ruleActionList ")
                    .append(CustomRuleSearchTables.ACTION.getTableAlias()).append(" ");
        }
        return builder.toString();
    }

    /**
     * Creates at String that sets values based on what class the SearchValue
     * has. A String class returns [ = 'value' ] A Integer returns [ = value ]
     * Date is specifically handled and can return [ >= 'datavalue' ] or [ <=
     * 'datavalue' ]
     *
     * @param entry
     * @return
     */
    private static String addParameters(CustomRuleSearchValue entry) {
        StringBuilder builder = new StringBuilder();

        switch (entry.getField()) {
            case NAME:
                builder.append(" = ");
                break;
            case GUID:
                builder.append(" = ");
                break;
            case TYPE:
                builder.append(" = ");
                break;
            case AVAILABILITY:
                builder.append(" = ");
                break;
            case RULE_USER:
                builder.append(" = ");
                break;
            case TICKET_ACTION_USER:
                builder.append(" = ");
                break;
            default:
                break;
        }
        builder.append(buildValueFromClassType(entry)).append(" ");
        return builder.toString();
    }

    /**
     * Builds a table alias for the query based on the search field
     * <p>
     * EG [ theTableAlias.theColumnName ]
     *
     * @param field
     * @return
     */
    private static String buildTableAliasName(CustomRuleSearchField field) {
        StringBuilder builder = new StringBuilder();
        builder.append(field.getSearchTables().getTableAlias()).append(".").append(field.getFieldName());
        return builder.toString();
    }

    /**
     * Takes all the search values and categorizes them in lists to a key
     * according to the SearchField
     *
     * @param searchValues
     * @return
     */
    private static HashMap<CustomRuleSearchField, List<CustomRuleSearchValue>> combineSearchFields(List<CustomRuleSearchValue> searchValues) {
        HashMap<CustomRuleSearchField, List<CustomRuleSearchValue>> values = new HashMap<>();
        for (CustomRuleSearchValue search : searchValues) {
            if (values.containsKey(search.getField())) {
                values.get(search.getField()).add(search);
            } else {
                values.put(search.getField(), new ArrayList<>(Arrays.asList(search)));
            }
        }
        return values;
    }

    /**
     * Converts List<CustomRuleListCriteria> to List<CustomRuleSearchValue> so that a JPQL query can
     * be built based on the criteria
     *
     * @param criteriaList
     * @return
     */
    public static List<CustomRuleSearchValue> mapSearchField(List<CustomRuleListCriteria> criteriaList) {

        if (criteriaList == null || criteriaList.isEmpty()) {
            LOG.debug(" Non valid search criteria when mapping CustomRuleListCriteria to CustomRuleSearchValue, List is null or empty");
            return new ArrayList<>();
        }

        List<CustomRuleSearchValue> searchFields = new ArrayList<>();
        for (CustomRuleListCriteria criteria : criteriaList) {
            try {
                CustomRuleSearchField field = mapCriteria(criteria.getKey());
                searchFields.add(new CustomRuleSearchValue(field, criteria.getValue()));
            } catch (IllegalArgumentException ex) {
                LOG.debug("[ Error with criteria {} when mapping to search field... continuing with other criteria ]", criteria);
            }
        }

        return searchFields;
    }

    /**
     * Maps the Search Key to a SearchField.
     *
     * @param key
     * @return
     */
    private static CustomRuleSearchField mapCriteria(CustomRuleSearchKey key) {
        switch (key) {
            case NAME:
                return CustomRuleSearchField.NAME;
            case GUID:
                return CustomRuleSearchField.GUID;
            case TYPE:
                return CustomRuleSearchField.TYPE;
            case AVAILABILITY:
                return CustomRuleSearchField.AVAILABILITY;
            case RULE_USER:
                return CustomRuleSearchField.RULE_USER;
            case TICKET_ACTION_USER:
                return CustomRuleSearchField.TICKET_ACTION_USER;
            default:
                throw new IllegalArgumentException("No field found: " + key.name());
        }
    }

    /**
     * The supported JOIN types see method getJoin for more info
     */
    public enum JoinType {
        INNER, LEFT;
    }

}