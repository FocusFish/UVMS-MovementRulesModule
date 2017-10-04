/*
 *
 * Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package eu.europa.ec.fisheries.uvms.rules.service.business;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.europa.ec.fisheries.remote.RulesDomainModel;
import eu.europa.ec.fisheries.schema.rules.rule.v1.ErrorType;
import eu.europa.ec.fisheries.schema.rules.template.v1.FactType;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FishingActivityWithIdentifiers;
import eu.europa.ec.fisheries.uvms.rules.service.business.fact.*;
import eu.europa.ec.fisheries.uvms.rules.service.constants.FishingActivityType;
import eu.europa.ec.fisheries.uvms.rules.service.constants.MDRAcronymType;
import eu.europa.ec.fisheries.uvms.rules.service.constants.ServiceConstants;
import eu.europa.ec.fisheries.uvms.rules.service.mapper.xpath.util.XPathRepository;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import un.unece.uncefact.data.standard.mdr.communication.ColumnDataType;
import un.unece.uncefact.data.standard.mdr.communication.ObjectRepresentation;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.ContactPerson;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.DelimitedPeriod;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FACatch;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXLocation;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.TextType;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.PatternSyntaxException;

@Slf4j
@ToString
public abstract class AbstractFact {

    private static final String THE_LIST = "The list [";
    private static final String DOESN_T_EXIST_IN_MDR_MODULE = "] doesn't exist in MDR module or in MDRAcronymType class! Check it and try again!";

    protected FactType factType;

    public String senderOrReceiver;

    protected List<RuleWarning> warnings;

    protected List<RuleError> errors;

    protected List<String> uniqueIds;

    protected boolean ok = true;

    private Integer sequence = 0;

    protected RulesDomainModel rulesDomainModel;

    public AbstractFact() {
        this.uniqueIds = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public abstract void setFactType();

    public void addWarningOrError(String type, String msg, String brId, String level, String propertyNames) {
        final List<String> xpathsForProps = getXpathsForProps(propertyNames);
        if (type.equalsIgnoreCase(ErrorType.ERROR.value())) {
            RuleError ruleError = new RuleError(brId, msg, level, xpathsForProps);
            errors.add(ruleError);
        } else {
            RuleWarning ruleWarning = new RuleWarning(brId, msg, level, xpathsForProps);
            warnings.add(ruleWarning);
        }
    }

    private List<String> getXpathsForProps(String propertyNames) {
        List<String> xpathsList = new ArrayList<>();
        if (StringUtils.isNotEmpty(propertyNames)) {
            String propNamesTrimmed = StringUtils.deleteWhitespace(propertyNames);
            String[] propNames = propNamesTrimmed.split(",");
            for (String propName : propNames) {
                xpathsList.add(XPathRepository.INSTANCE.getMapForSequence(this.getSequence(), propName));
            }
        }
        return xpathsList;
    }


    public boolean schemeIdContainsAll(List<IdType> idTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(idTypes)) {
            return true;
        }
        int valLength = valuesToMatch.length;
        int hits = 0;
        for (String val : valuesToMatch) {
            for (IdType IdType : idTypes) {
                if (IdType != null && val.equals(IdType.getSchemeId())) {
                    hits++;
                }
            }
        }
        return valLength > hits;
    }

    public boolean idListContainsValue(List<IdType> idTypes, String valueToMatch, String schemeIdToSearchFor) {
        if (StringUtils.isEmpty(valueToMatch) || StringUtils.isEmpty(schemeIdToSearchFor)) {
            return false;
        }
        String flagStateToMatch = StringUtils.EMPTY;
        for (IdType idType : idTypes) {
            if (schemeIdToSearchFor.equals(idType.getSchemeId())) {
                flagStateToMatch = idType.getValue();
            }
        }
        return StringUtils.equals(valueToMatch, flagStateToMatch);
    }

    public boolean valueContainsAll(List<IdType> idTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(idTypes)) {
            return true;
        }
        int valLength = valuesToMatch.length;
        int hits = 0;
        for (String val : valuesToMatch) {
            for (IdType IdType : idTypes) {
                if (IdType != null && val.equals(IdType.getValue())) {
                    hits++;
                }
            }
        }
        return valLength > hits;
    }

    public boolean valueStartsWith(List<IdType> idTypes, String... valuesToMatch) {
        if (isEmpty(idTypes) || ArrayUtils.isEmpty(valuesToMatch)) {
            return false;
        }

        int hits = 0;
        for (String valueToMatch : valuesToMatch) {
            for (IdType idType : idTypes) {
                if (valueStartsWith(idType, valueToMatch)) {
                    hits++;
                }
            }
        }

        return valuesToMatch.length <= hits;
    }

    public boolean valueStartsWith(IdType idType, String valueToMatch) {
        if (valueToMatch == null || idType == null) {
            return false;
        }

        if (idType != null && idType.getValue() != null && idType.getValue().startsWith(valueToMatch)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the schemeId Contains Any then it checks if it contains all.
     * Otherwise it means that it contains none.
     *
     * @param idTypes
     * @param valuesToMatch
     * @return
     */
    public boolean schemeIdContainsAllOrNone(List<IdType> idTypes, String... valuesToMatch) {
        return !schemeIdContainsAny(idTypes, valuesToMatch) && schemeIdContainsAll(idTypes, valuesToMatch);
    }

    /**
     * Checks if one of the String... array elements exists in the idType.
     *
     * @param idType
     * @param values
     * @return
     */
    public boolean schemeIdContainsAny(IdType idType, String... values) {
        return schemeIdContainsAny(Arrays.asList(idType), values);
    }

    /**
     * Checks if one of the String... array elements exists in the idTypes list.
     *
     * @param idTypes
     * @param values
     * @return
     */
    public boolean schemeIdContainsAny(List<IdType> idTypes, String... values) {
        if (values == null || values.length == 0 || CollectionUtils.isEmpty(idTypes)) {
            return true;
        }

        idTypes = new ArrayList<>(idTypes);
        CollectionUtils.filter(idTypes, PredicateUtils.notNullPredicate());

        for (String val : values) {
            for (IdType IdType : idTypes) {
                if (val.equals(IdType.getSchemeId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isSchemeIdPresent(IdType idType) {
        if (idType == null) {
            return true;
        }

        return StringUtils.isNotBlank(idType.getSchemeId());
    }

    public boolean isAllSchemeIdsPresent(List<IdType> idTypes) {
        if (CollectionUtils.isEmpty(idTypes)) {
            return true;
        }

        idTypes = new ArrayList<>(idTypes);
        CollectionUtils.filter(idTypes, PredicateUtils.notNullPredicate());


        for (IdType idType : idTypes) {
            if (!isSchemeIdPresent(idType)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if one of the String... array elements exists in the idTypes list.
     * Depending on checkEmptyness value it also checks (or not) if the values are empty.
     * Depending on isGivenName value it checks for GivenName or FamilyName.
     *
     * @param contactPersons
     * @param checkEmptyness
     * @return true/false
     */
    public boolean checkContactListContainsAny(List<ContactPerson> contactPersons, boolean checkEmptyness, boolean isGivenName) {
        if (CollectionUtils.isEmpty(contactPersons)) {
            return true;
        }
        for (ContactPerson contPers : contactPersons) {
            TextType givenName = contPers.getGivenName();
            TextType familyName = contPers.getFamilyName();
            TextType nameToConsider = isGivenName ? givenName : familyName;
            TextType alias = contPers.getAlias();
            if (checkWithEmptyness(checkEmptyness, nameToConsider, alias) || checkWithoutEmptyness(nameToConsider, alias)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkWithoutEmptyness(TextType nameToConsider, TextType alias) {
        return (nameToConsider == null || nameToConsider.getValue() == null)
                && (alias == null || alias.getValue() == null);
    }

    private boolean checkWithEmptyness(boolean checkEmptyness, TextType nameToConsider, TextType alias) {
        return checkEmptyness && ((nameToConsider == null || StringUtils.isEmpty(nameToConsider.getValue()))
                && (alias == null || StringUtils.isEmpty(alias.getValue())));
    }

    public boolean checkAliasFromContactList(List<ContactPerson> contactPersons, boolean checkAliasEmptyness) {
        if (CollectionUtils.isEmpty(contactPersons)) {
            return true;
        }
        for (ContactPerson contPers : contactPersons) {
            TextType givenName = contPers.getGivenName();
            TextType familyName = contPers.getFamilyName();
            TextType alias = contPers.getAlias();
            if (givenName == null || familyName == null) {
                if (alias == null || (checkAliasEmptyness && StringUtils.isEmpty(alias.getValue()))) {
                    return true;
                }
            } else if (checkAliasEmptyness && alias != null && StringUtils.isEmpty(alias.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate the format of the value depending on the schemeId for List<IdType>
     *
     * @param ids
     * @return
     */
    public boolean validateFormat(List<IdType> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        for (IdType id : ids) {
            if (validateFormat(id)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Validate the format of the value depending on the schemeId for List<CodeType>
     *
     * @param codeTypes
     * @return boolean
     */
    public boolean validateFormatCodeTypes(List<CodeType> codeTypes) {
        if (CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }
        for (CodeType id : codeTypes) {
            if (validateFormat(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate the format of the value depending on the schemeId for single IdType
     *
     * @param id IdType
     * @return
     */
    public boolean validateFormat(IdType id) {
        if (id == null || id.getSchemeId() == null) {
            return true;
        }
        try {
            if (!validateFormat(id.getValue(), FORMATS.valueOf(id.getSchemeId()).getFormatStr())) {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            log.error("The SchemeId : '" + id.getSchemeId() + "' is not mapped in the AbstractFact.validateFormat(List<IdType> ids) method.", ex.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Validate the format of the value depending on the codeType for single CodeType
     *
     * @param codeType CodeType
     * @return
     */
    public boolean validateFormat(CodeType codeType) {
        if (codeType == null) {
            return true;
        }
        try {
            if (!validateFormat(codeType.getValue(), FORMATS.valueOf(codeType.getListId()).getFormatStr())) {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            log.error("The codeType : '" + codeType.getListId() + "' is not mapped in the AbstractFact.validateFormat(List<CodeType> codeTypes) method.", ex.getMessage());
            return false;
        }
        return false;
    }

    /**
     * If controlList contains at leat one of the elements of the elementsToMatchList returns true;
     *
     * @param controlList
     * @param elementsToMatchList
     * @return
     */
    public boolean listContainsAtLeastOneFromTheOtherList(List<IdType> controlList, List<IdType> elementsToMatchList) {
        if (CollectionUtils.isEmpty(controlList)) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(elementsToMatchList)) {
            for (IdType idToMatch : elementsToMatchList) {
                if (controlList.contains(idToMatch)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the list size is the expected one [listSize].
     *
     * @param list
     * @param listSize
     * @return
     */
    public boolean listSizeIs(List<?> list, int listSize) {
        return !(isEmpty(list) || list.size() != listSize);
    }

    /**
     * This method returns true if activityTypes list contains other elements then the ones contained in permitedElements.
     *
     * @param activityTypes
     * @return
     */
    public boolean listContainsEitherThen(List<String> activityTypes, String... permitedElements) {
        if (CollectionUtils.isEmpty(activityTypes) || permitedElements == null || permitedElements.length == 0) {
            return false;
        }
        List<String> permitedElementsList = Arrays.asList(permitedElements);
        boolean containsEitherThen = false;
        for (String type : activityTypes) {
            if (!permitedElementsList.contains(type)) {
                containsEitherThen = true;
                break;
            }
        }
        return containsEitherThen;
    }

    public boolean validateFormat(String value, String format) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(format)) {
            return false;
        }
        return value.matches(format);
    }

    public boolean isIsoDateStringValidFormat(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        return value.matches(FORMATS.ISO_8601_WITH_OPT_MILLIS.getFormatStr());
    }

    public boolean isIdTypeValidFormat(String requiredSchemeId, IdType idType) {
        if (idType == null || isEmpty(requiredSchemeId) || isEmpty(idType.getSchemeId()) || isEmpty(idType.getValue()) || idType.getSchemeId() != requiredSchemeId) {
            return false;
        }
        try {
            return validateFormat(idType.getValue(), FORMATS.valueOf(requiredSchemeId).getFormatStr());
        } catch (IllegalArgumentException ex) {
            log.error("The SchemeId : '" + requiredSchemeId + "' is not mapped in the AbstractFact.FORMATS enum.", ex.getMessage());
            return false;
        }
    }

    public boolean isCodeTypeValidFormat(String requiredListId, CodeType codeType) {
        if (codeType == null || isEmpty(requiredListId) || isEmpty(codeType.getListId()) || isEmpty(codeType.getValue()) || !codeType.getListId().equals(requiredListId)) {
            return false;
        }
        try {
            return validateFormat(codeType.getValue(), FORMATS.valueOf(requiredListId).getFormatStr());
        } catch (IllegalArgumentException ex) {
            log.error("The ListId : '" + requiredListId + "' is not mapped in the AbstractFact.FORMATS enum.", ex.getMessage());
            return false;
        }
    }

    public boolean listIdContainsAll(List<CodeType> codeTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }
        for (String val : valuesToMatch) {
            for (CodeType IdType : codeTypes) {
                if (!val.equals(IdType.getListId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if valuesToMatch strings are ALL present in list of measureTypes
     *
     * @param codeType
     * @param valuesToMatch
     * @return
     */
    public boolean listIdDoesNotContainAll(CodeType codeType, String... valuesToMatch) {
        return listIdDoesNotContainAll(Arrays.asList(codeType), valuesToMatch);
    }


    public boolean salesPartiesValueDoesNotContainAny(List<SalesPartyFact> salesPartyTypes, String... valuesToMatch) {
        List<CodeType> codeTypes = new ArrayList<>();
        HashSet<String> valuesToBeFound = new HashSet<>(Arrays.asList(valuesToMatch));

        for (SalesPartyFact salesPartyFact : salesPartyTypes) {
            codeTypes.addAll(salesPartyFact.getRoleCodes());
        }

        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }

        for (CodeType codeType : codeTypes) {
            String value = codeType.getValue();

            if (valuesToBeFound.contains(value)) {
                return false;
            }
        }

        return true;
    }

    public boolean listIdDoesNotContainAll(List<CodeType> codeTypes, String... valuesToMatch) {
        HashSet<String> valuesFoundInListOfCodeTypes = new HashSet<>();
        HashSet<String> valuesToBeFound = new HashSet<>(Arrays.asList(valuesToMatch));

        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }

        for (CodeType codeType : codeTypes) {
            if (codeType != null) {
                String listId = codeType.getListId();

                if (valuesToBeFound.contains(listId)) {
                    valuesFoundInListOfCodeTypes.add(listId);
                }
            }
        }

        return !valuesFoundInListOfCodeTypes.equals(valuesToBeFound);
    }

    public boolean valueDoesNotContainAll(List<CodeType> codeTypes, String... valuesToMatch) {
        HashSet<String> valuesFoundInListOfCodeTypes = new HashSet<>();
        HashSet<String> valuesToBeFound = new HashSet<>(Arrays.asList(valuesToMatch));

        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }

        for (CodeType codeType : codeTypes) {
            String value = codeType.getValue();

            if (valuesToBeFound.contains(value)) {
                valuesFoundInListOfCodeTypes.add(value);
            }
        }

        return !valuesFoundInListOfCodeTypes.equals(valuesToBeFound);
    }

    /**
     * This method will return false if any codeType do not have matching value from the list valuesToBe matched
     *
     * @param codeTypes
     * @param valuesToMatch
     * @return
     */
    public boolean codeTypeValueContainsMatch(List<CodeType> codeTypes, String... valuesToMatch) {
        if (CollectionUtils.isEmpty(codeTypes) || valuesToMatch == null) {
            return false;
        }
        HashSet<String> valuesToBeFound = new HashSet<>(Arrays.asList(valuesToMatch));

        if (CollectionUtils.isEmpty(codeTypes)) {
            return false;
        }

        for (CodeType codeType : codeTypes) {
            if (codeType == null || codeType.getValue() == null || !valuesToBeFound.contains(codeType.getValue())) {
                return false;
            }

        }

        return true;

    }

    public boolean unitCodeContainsAll(List<MeasureType> measureTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(measureTypes)) {
            return true;
        }
        int hits = 0;
        for (String val : valuesToMatch) {
            for (MeasureType measureType : measureTypes) {
                if (val.equals(measureType.getUnitCode())) {
                    hits++;
                }
            }
        }
        return valuesToMatch.length > hits;
    }

    public boolean validateDelimitedPeriod(List<DelimitedPeriod> delimitedPeriods, boolean start, boolean end) {
        if (delimitedPeriods == null || delimitedPeriods.isEmpty()) {
            return true;
        }
        for (DelimitedPeriod delimitedPeriod : delimitedPeriods) {
            if ((start && end && delimitedPeriod.getStartDateTime() == null && delimitedPeriod.getEndDateTime() == null)
                    || (start && !end && delimitedPeriod.getStartDateTime() == null)
                    || (end && !start && delimitedPeriod.getEndDateTime() == null)) {
                return true;
            }
        }
        return false;
    }

    public boolean schemeIdContainsAll(IdType idType, String... values) {
        return idType == null || schemeIdContainsAll(Collections.singletonList(idType), values);
    }

    public boolean listIdContainsAll(CodeType codeType, String... values) {
        return codeType == null || listIdContainsAll(Collections.singletonList(codeType), values);
    }

    public Date dateNow() {
        return new Date();
    }

    public Date dateNow(int hours) {
        DateTime now = new DateTime(dateNow());
        if (hours > 0) {
            now.plusHours(hours);
        } else if (hours < 0) {
            now.minusHours(hours);
        }
        return now.toDate();
    }

    public boolean containsSameDayMoreTheOnce(List<Date> dateList) {
        if (CollectionUtils.isEmpty(dateList)) {
            return true;
        }
        int listSize = dateList.size();
        for (int i = 0; i < listSize; i++) {
            Date comparisonDate = dateList.get(i);
            for (int j = i + 1; j < listSize; j++) {
                if (isSameDay(comparisonDate, dateList.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSameDay(Date date1, Date date2) {
        return DateUtils.isSameDay(date1, date2);
    }

    public List<RuleWarning> getWarnings() {
        return warnings;
    }

    public List<RuleError> getErrors() {
        return errors;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public FactType getFactType() {
        return factType;
    }

    public List<String> getUniqueIds() {
        return uniqueIds;
    }

    public void setUniqueIds(List<String> uniqueIds) {
        this.uniqueIds = uniqueIds;
    }

    public boolean listIdNotContains(CodeType codeType, String... values) {
        return listIdNotContains(Collections.singletonList(codeType), values);
    }

    public boolean listIdNotContains(List<CodeType> codeTypes, String... values) {
        if (values == null || values.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }
        for (String val : values) {
            for (CodeType codeType : codeTypes) {
                if (val.equals(codeType.getListId())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean valueNotContains(List<CodeType> codeTypes, String value, int hits) {
        if (value == null || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }
        int found = 0;
        for (CodeType codeType : codeTypes) {
            if (value.equals(codeType.getValue())) {
                found++;
            }
        }

        return hits != found;
    }

    public boolean valueContainsAny(CodeType codeType, String... valuesToMatch) {
        return codeType == null || valueContainsAny(Collections.singletonList(codeType), valuesToMatch);
    }

    public boolean valueContainsAny(List<CodeType> codeTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }
        ImmutableList<CodeType> removeNull = ImmutableList.copyOf(Iterables.filter(codeTypes, Predicates.notNull()));
        boolean isMatchFound = false;
        for (String val : valuesToMatch) {
            for (CodeType CodeTypes : removeNull) {
                if (val.equals(CodeTypes.getValue())) {
                    isMatchFound = true;
                    break;
                }
            }
        }
        return !isMatchFound;
    }

    public int numberOfDecimals(BigDecimal value) {
        if (value == null) {
            return -1;
        }

        int i = value.subtract(value.setScale(0, RoundingMode.FLOOR)).movePointRight(value.scale()).intValue();
        return Integer.toString(i).length();
    }

    public boolean isPositive(MeasureType value) {
        return isPositive(Collections.singletonList(value));
    }

    public boolean isPositive(List<MeasureType> value) {
        if (value == null) {
            return true;
        }
        for (MeasureType type : value) {
            BigDecimal val = type.getValue();
            if (val == null || BigDecimal.ZERO.compareTo(val) < 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isPositiveInteger(List<MeasureType> value) {
        if (value == null) {
            return true;
        }
        for (MeasureType type : value) {
            BigDecimal val = type.getValue();
            if (val == null || BigDecimal.ZERO.compareTo(val) > 0 || !isIntegerValue(val)) {
                return false;
            }
        }
        return true;
    }


    private boolean isIntegerValue(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return false;
        }

        if (bigDecimal.signum() == 0 || bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * This method will check if all values passed  to this method are greater than zero.
     *
     * @param values
     * @return TRUE : If all values are greater than zero
     * FALSE: If any one value is null OR less than OR equal to zero
     */
    public boolean isGreaterThanZero(List<MeasureType> values) {
        if (CollectionUtils.isEmpty(values)) {
            return false;
        }
        for (MeasureType type : values) {
            BigDecimal val = type.getValue();
            if (val == null || BigDecimal.ZERO.compareTo(val) > -1) {
                return false;
            }
        }
        return true;
    }

    public boolean valueIdTypeContainsAny(String value, String... valuesToMatch) {
        IdType idType = new IdType();
        idType.setValue(value);
        return valueIdTypeContainsAny(Arrays.asList(idType), valuesToMatch);
    }

    public boolean valueIdTypeContainsAny(IdType idType, String... valuesToMatch) {
        return valueIdTypeContainsAny(Arrays.asList(idType), valuesToMatch);
    }

    public boolean valueIdTypeContainsAny(List<IdType> idTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(idTypes)) {
            return true;
        }

        boolean isMatchFound = false;
        for (String val : valuesToMatch) {
            for (IdType idType : idTypes) {
                if (val.equals(idType.getValue())) {
                    isMatchFound = true;
                    break;
                }
            }
        }
        return !isMatchFound;
    }

    public boolean codeTypeValuesUnique(List<CodeType> codeTypes) {
        if (CollectionUtils.isEmpty(codeTypes)) {
            return false;
        }
        Set<String> stringSet = new HashSet<>();

        for (CodeType codeType : codeTypes) {
            stringSet.add(codeType.getValue());
        }

        return codeTypes.size() == stringSet.size();
    }

    public boolean valueCodeTypeContainsAny(List<CodeType> codeTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }

        boolean isMatchFound = false;
        for (String val : valuesToMatch) {
            for (CodeType codeType : codeTypes) {
                if (val.equals(codeType.getValue())) {
                    isMatchFound = true;
                    break;
                }
            }
        }
        return !isMatchFound;
    }

    public boolean isPositive(BigDecimal value) {
        if (value == null) {
            return true;
        }
        return value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean isInRange(BigDecimal value, int min, int max) {
        if (value == null) {
            return true;
        }
        return !((value.compareTo(new BigDecimal(min)) > 0) && (value.compareTo(new BigDecimal(max)) < 0));
    }

    public boolean anyValueContainsAll(List<CodeType> codeTypes, String... valuesToMatch) {
        if (valuesToMatch == null || valuesToMatch.length == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }
        ImmutableList<CodeType> removeNull = ImmutableList.copyOf(Iterables.filter(codeTypes, Predicates.notNull()));
        boolean isMatchFound = false;

        outer:
        for (String val : valuesToMatch) {
            for (CodeType IdType : removeNull) {
                if (val.equals(IdType.getValue())) {
                    isMatchFound = true;
                    continue outer;
                }
            }
            isMatchFound = false;
        }
        return !isMatchFound;
    }

    public boolean allValueContainsMatch(List<CodeType> codeTypes, String valueToMatch) {
        if (valueToMatch == null || valueToMatch.length() == 0 || CollectionUtils.isEmpty(codeTypes)) {
            return true;
        }

        for (CodeType codeType : codeTypes) {
            if (!valueToMatch.equals(codeType.getValue())) {
                return true;
            }
        }
        return false;
    }


    public boolean allIdValuesStartWith(List<IdType> ids, String startSequenceToMatch){
        if(CollectionUtils.isEmpty(ids) || StringUtils.isEmpty(startSequenceToMatch)){
            return false;
        }
        for(IdType idType : ids){
            if(!StringUtils.startsWith(idType.getValue(), startSequenceToMatch)){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if FaCatch list contains at least one or more SpecifiedFLUXLocations list  .
     *
     * @param faCatches
     * @return false/true
     */
    public boolean validateFluxLocationsForFaCatch(List<FACatch> faCatches) {
        boolean isValid = true;
        for (FACatch faCatch : faCatches) {
            List<FLUXLocation> checkList = faCatch.getSpecifiedFLUXLocations();
            if (checkList == null || checkList.isEmpty()) {
                isValid = false;
            }
        }
        return !isValid;
    }

    public boolean isEmpty(List<?> list) {
        return CollectionUtils.isEmpty(list);
    }

    /**
     * Checks if the list of strings contains empty (null / "") elements.
     *
     * @param stringsList
     * @return true / false
     */
    public boolean containsEmptyStrings(List<String> stringsList) {
        if (!isEmpty(stringsList)) {
            return stringsList.contains(null) || stringsList.contains("");
        }
        return true;
    }

    public boolean containsOnlyEmptyStrings(List<String> stringsList) {
        if (!isEmpty(stringsList)) {
            for (String str : stringsList) {
                if (StringUtils.isNotEmpty(str)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isNumeric(List<NumericType> list) {
        for (NumericType type : list) {
            if (type.getValue() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    public boolean isBlank(eu.europa.ec.fisheries.schema.sales.TextType textType) {
        return textType == null || StringUtils.isBlank(textType.getValue());
    }

    public boolean isBlank(IdType id) {
        return id == null || StringUtils.isBlank(id.getValue());
    }

    public boolean isListEmptyOrBetweenNumberOfItems(List sourceList, int minNumberOfItems, int maxNumberOfItems) {
        compareMinimumToMaximum(minNumberOfItems, maxNumberOfItems);

        return (sourceList != null && sourceList.isEmpty()) || (sourceList.size() <= maxNumberOfItems && sourceList.size() >= minNumberOfItems);
    }

    public boolean isListNotEmptyAndBetweenNumberOfItems(List sourceList, int minNumberOfItems, int maxNumberOfItems) {
        compareMinimumToMaximum(minNumberOfItems, maxNumberOfItems);

        return (sourceList != null && !sourceList.isEmpty()) && sourceList.size() <= maxNumberOfItems && sourceList.size() >= minNumberOfItems;
    }

    private void compareMinimumToMaximum(int minNumberOfItems, int maxNumberOfItems) {
        if (minNumberOfItems > maxNumberOfItems) {
            throw new IllegalArgumentException("minNumberOfItems '" + minNumberOfItems + "' can't be bigger than '" + maxNumberOfItems + "'.");
        }
    }

    public boolean isListEmptyOrAllValuesUnique(List<CodeType> sourceList) {
        if (isEmpty(sourceList)) {
            return true;
        }

        List<String> values = new ArrayList<>();
        for (CodeType codeType : sourceList) {
            if (codeType == null) {
                continue;
            }

            if (values.contains(codeType.getValue())) {
                return false;
            }

            values.add(codeType.getValue());
        }

        return true;
    }

    public boolean isListEmptyOrAllListIdsUnique(List<CodeType> sourceList) {
        if (isEmpty(sourceList)) {
            return true;
        }

        List<String> listIds = new ArrayList<>();
        for (CodeType codeType : sourceList) {
            if (codeType == null) {
                continue;
            }

            if (listIds.contains(codeType.getListId())) {
                return false;
            }

            listIds.add(codeType.getListId());
        }

        return true;
    }

    public boolean isListEmptyOrValuesMatchPassedArguments(List<CodeType> sourceList, String... valuesToMatch) {
        if (isEmpty(sourceList)) {
            return true;
        }

        List<String> matchList = Arrays.asList(valuesToMatch);
        for (CodeType codeType : sourceList) {
            if (codeType == null) {
                return false;
            }

            if (!matchList.contains(codeType.getValue())) {
                return false;
            }
        }

        return true;
    }

    public int getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf('.');
        return index < 0 ? 0 : string.length() - index - 1;
    }

    public boolean isBigDecimalBetween(BigDecimal value, BigDecimal lowBound, BigDecimal upperBound) {
        return value.compareTo(lowBound) > 0 && value.compareTo(upperBound) < 0;
    }

    public enum FORMATS {
        // TODO : ICCAT and CFR have Territory characters reppresented [a-zA-Z]{3} which is not correct, cause it is matching not existing combinations also (Like ABC
        // TODO : which is not an existing country code). This happens with ICCAT -second sequence- and CFR -first sequence-!

        UUID("[a-fA-F0-9]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"),
        EXT_MARK("[a-zA-Z0-9]{1,14}"),
        IRCS("[a-zA-Z0-9]{1,7}"),
        CFR("[a-zA-Z]{3}[a-zA-Z0-9]{9}"),
        UVI("[a-zA-Z0-9]{7}"),
        ICCAT("AT[a-zA-Z0-9]{3}[a-zA-Z0-9]{3}[a-zA-Z0-9]{5}"),
        GFCM("[a-zA-Z0-9]{1,13}"),
        //EU_TRIP_ID("[a-zA-Z]{3}-TRP-[a-zA-Z0-9]{0,20}"),
        EU_SALES_ID_COMMON("[A-Z]{3}-(SN|TOD|TRD|SN+TOD)-.*"),
        EU_SALES_ID_SPECIFIC(".*-.*-[A-Za-z0-9\\-]{1,20}"),
        EU_SALES_TAKE_OVER_DOCUMENT_ID("[A-Z]{3}-TOD-[A-Za-z0-9\\-]{1,20}"),
        EU_SALES_SALES_NOTE_ID("[A-Z]{3}-SN-[A-Za-z0-9\\-]{1,20}"),
        EU_TRIP_ID("[A-Z]{3}-TRP-[A-Za-z0-9\\-]{1,20}"),
        FLUX_SALES_TYPE("(SN\\+TOD|SN|TOD|TRD)"),
        FLUX_SALES_QUERY_PARAM("(VESSEL|FLAG|ROLE|PLACE|SALES_ID|TRIP_ID)"),
        FLUX_GP_VALIDATION_LEVEL("(L00|L01|L02|L03)"),
        FLUX_GP_VALIDATION_TYPE("(ERR|WAR|IGN|OK)"),
        FLUX_GP_RESPONSE("(OK|NOK|WOK)"),
        ISO_8601_WITH_OPT_MILLIS("\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[1-2]\\d|3[0-1])T(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d([\\.]\\d{3})?Z");

        String formatStr;

        FORMATS(String someFormat) {
            setFormatStr(someFormat);
        }

        public String getFormatStr() {
            return formatStr;
        }

        void setFormatStr(String formatStr) {
            this.formatStr = formatStr;
        }
    }

    /**
     * Check if value passed is present in the MDR list speified
     *
     * @param listName  - MDR list name to be ckecked against
     * @param codeValue - This value will be checked in MDR list
     * @return True-> if value is present in MDR list   False-> if value is not present in MDR list
     */
    public boolean isPresentInMDRList(String listName, String codeValue) {
        MDRAcronymType anEnum = EnumUtils.getEnum(MDRAcronymType.class, listName);
        if (anEnum == null) {
            log.error(THE_LIST + listName + DOESN_T_EXIST_IN_MDR_MODULE);
            return false;
        }
        List<String> values = MDRCacheHolder.getInstance().getList(anEnum);
        if (CollectionUtils.isNotEmpty(values)) {
            return values.contains(codeValue);
        }
        return false;
    }

    /**
     * This function checks that all the CodeType values passed to the function exist in MDR code list or not
     *
     * @param listName      - Values passed would be checked agaist this MDR list
     * @param valuesToMatch - CodeType list--Values from each instance will be checked agaist ListName
     * @return True -> if all values are found in MDR list specified. False -> If even one value is not matching with MDR list
     */
    public boolean isCodeTypePresentInMDRList(String listName, List<CodeType> valuesToMatch) {

        MDRAcronymType anEnum = EnumUtils.getEnum(MDRAcronymType.class, listName);
        if (anEnum == null) {
            log.error(THE_LIST + listName + DOESN_T_EXIST_IN_MDR_MODULE);
            return false;
        }
        List<String> codeListValues = MDRCacheHolder.getInstance().getList(anEnum);

        if (CollectionUtils.isEmpty(valuesToMatch) || CollectionUtils.isEmpty(codeListValues)) {
            return false;
        }

        for (CodeType codeType : valuesToMatch) {
            if (!codeListValues.contains(codeType.getValue()))
                return false;
        }

        return true;
    }

    public boolean isCodeTypeListIdPresentInMDRList(String listName, List<CodeType> valuesToMatch) {

        MDRAcronymType anEnum = EnumUtils.getEnum(MDRAcronymType.class, listName);
        if (anEnum == null) {
            log.error(THE_LIST + listName + DOESN_T_EXIST_IN_MDR_MODULE);
            return false;
        }
        List<String> codeListValues = MDRCacheHolder.getInstance().getList(anEnum);

        if (CollectionUtils.isEmpty(valuesToMatch) || CollectionUtils.isEmpty(codeListValues)) {
            return false;
        }

        for (CodeType codeType : valuesToMatch) {
            if (!codeListValues.contains(codeType.getListId()))
                return false;
        }

        return true;
    }


    /**
     * This function checks that all the IdType values passed to the function exist in MDR code list or not
     *
     * @param listName      - Values passed would be checked agaist this MDR list
     * @param valuesToMatch - IdType list--Values from each instance will be checked agaist ListName
     * @return True -> if all values are found in MDR list specified. False -> If even one value is not matching with MDR list
     */
    public boolean isIdTypePresentInMDRList(String listName, List<IdType> valuesToMatch) {

        MDRAcronymType anEnum = EnumUtils.getEnum(MDRAcronymType.class, listName);
        if (anEnum == null) {
            log.error(THE_LIST + listName + DOESN_T_EXIST_IN_MDR_MODULE);
            return false;
        }

        List<String> codeListValues = MDRCacheHolder.getInstance().getList(anEnum);

        if (CollectionUtils.isEmpty(valuesToMatch) || CollectionUtils.isEmpty(codeListValues)) {
            return false;
        }


        for (IdType codeType : valuesToMatch) {
            if (!codeListValues.contains(codeType.getValue()))
                return false;
        }

        return true;
    }

    public boolean isIdTypePresentInMDRList(List<IdType> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }


        for (IdType idType : ids) {
            if (isIdTypePresentInMDRList(idType) == false) {
                return false;
            }
        }

        return true;
    }


    /**
     * This function checks that the IdType exist in MDR code list or not.
     * The MDR list is defined by the property schemeId from the IdType
     *
     * @param id - IdType that will be checked against ListName
     * @return true when it exists
     */
    public boolean isIdTypePresentInMDRList(IdType id) {
        if (id == null) {
            return false;
        }

        String schemeId = id.getSchemeId();
        String value = id.getValue();

        MDRAcronymType anEnum = EnumUtils.getEnum(MDRAcronymType.class, schemeId);
        if (anEnum == null) {
            log.error(THE_LIST + schemeId + DOESN_T_EXIST_IN_MDR_MODULE);
            return false;
        }

        List<String> codeListValues = MDRCacheHolder.getInstance().getList(anEnum);
        return codeListValues.contains(value);
    }

    public boolean isAllSchemeIdsPresentInMDRList(String listName, List<IdType> idTypes) {
        if (StringUtils.isBlank(listName) || isEmpty(idTypes)) {
            return false;
        }

        for (IdType idType : idTypes) {
            if (!isSchemeIdPresentInMDRList(listName, idType)) {
                return false;
            }
        }

        return true;
    }

    public boolean isSchemeIdPresentInMDRList(String listName, IdType idType) {
        if (idType == null || StringUtils.isBlank(idType.getSchemeId())) {
            return false;
        }

        return isPresentInMDRList(listName, idType.getSchemeId());
    }

    public boolean matchWithFluxTLExceptParties(List<IdType> idTypes, String... parties) {
        if (isEmpty(idTypes) || ArrayUtils.isEmpty(parties)) {
            return false;
        }

        List<String> partiesAllowedToSend = Arrays.asList(parties);

        for (IdType idType : idTypes) {
            String[] idTypeValueArray = getIdTypeValueArray(idType, ":");

            if (idTypeValueArray != null) {
                if (partiesAllowedToSend.contains(idTypeValueArray[0]) || matchWithFluxTL(idType)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean matchWithFluxTL(List<IdType> idTypes) {
        boolean match = false;
        for (IdType idType : idTypes) {
            match = matchWithFluxTL(idType);
            if (match) {
                break;
            }
        }
        return match;
    }

    public boolean matchWithFluxTL(IdType idType) {
        boolean match = false;
        if (idType != null) {
            String[] idValueArray = getIdTypeValueArray(idType, ":");

            if (idValueArray != null) {
                match = StringUtils.equals(idValueArray[0], senderOrReceiver);
            }
        }

        return match;
    }

    public String[] getIdTypeValueArray(IdType idType, String separator) {
        if (StringUtils.isBlank(separator)) {
            return null;
        }

        String[] idValueArray = null;

        if (idType != null) {
            try {
                idValueArray = idType.getValue().split(separator);
            } catch (NullPointerException | PatternSyntaxException ex) {
                log.error("Error splitting IdType's value to array!", ex);
                return null;
            }
        }

        return idValueArray;
    }

    public boolean vesselIdsMatch(List<IdType> vesselIds, IdType vesselCountryId, List<IdTypeWithFlagState> additionalObjectList) {
        if (CollectionUtils.isEmpty(additionalObjectList)) {
            return false;
        }
        List<IdTypeWithFlagState> listToBeMatched = new ArrayList<>();
        for (IdType idType : vesselIds) {
            listToBeMatched.add(new IdTypeWithFlagState(idType.getSchemeId(), idType.getValue(), vesselCountryId.getValue()));
        }

        for (IdTypeWithFlagState elemFromListToBeMatched : listToBeMatched) {
            if (!additionalObjectList.contains(elemFromListToBeMatched)) {
                return false;
            }
        }

        return true;
    }

    public boolean isTypeCodeValuePresentInList(String listName, CodeType typeCode) {
        return isTypeCodeValuePresentInList(listName, Arrays.asList(typeCode));
    }

    public boolean isTypeCodeValuePresentInList(String listName, List<CodeType> typeCodes) {
        String typeCodeValue = getValueForListId(listName, typeCodes);

        if (typeCodeValue == null) {
            return false;
        }

        return isPresentInMDRList(listName, typeCodeValue);
    }

    public String getValueForListId(String listId, List<CodeType> typeCodes) {
        if (StringUtils.isBlank(listId) || CollectionUtils.isEmpty(typeCodes)) {
            return null;
        }

        for (CodeType typeCode : typeCodes) {
            String typeCodeListId = typeCode.getListId();

            if (StringUtils.isNotBlank(typeCodeListId) && typeCodeListId.equals(listId)) {
                return typeCode.getValue();
            }
        }

        return null;
    }

    public String getValueForSchemeId(String schemeId, List<IdType> ids) {
        if (StringUtils.isBlank(schemeId) || CollectionUtils.isEmpty(ids)) {
            return null;
        }

        for (IdType id : ids) {
            String idsSchemeId = id.getSchemeId();

            if (StringUtils.isNotBlank(idsSchemeId) && idsSchemeId.equals(schemeId)) {
                return id.getValue();
            }
        }

        return null;
    }

    public boolean anyFluxLocationTypeCodeContainsValue(List<FLUXLocation> fluxLocations, String value) {
        if (CollectionUtils.isEmpty(fluxLocations) || StringUtils.isBlank(value)) {
            return false;
        }

        for (FLUXLocation fluxLocation : fluxLocations) {
            un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fluxLocation.getTypeCode();

            if (typeCode != null && value.equals(typeCode.getValue())) {
                return true;
            }
        }

        return false;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    /**
     * This method gets value from DataType Column of MDR list for the matching record. Record will be matched with CODE column
     *
     * @param listName  MDR list name to be matched with
     * @param codeValue value for CODE column to be matched with
     * @return DATATYPE column value for the matching record
     */
    public String getDataTypeForMDRList(String listName, String codeValue) {
        MDRAcronymType anEnum = EnumUtils.getEnum(MDRAcronymType.class, listName);
        if (anEnum == null || codeValue == null) {
            log.error(THE_LIST + listName + DOESN_T_EXIST_IN_MDR_MODULE);
            return StringUtils.EMPTY;
        }


        List<ObjectRepresentation> representations = MDRCacheHolder.getInstance().getObjectRepresentationList(anEnum);
        boolean valueFound = false;
        if (CollectionUtils.isNotEmpty(representations)) {
            for (ObjectRepresentation representation : representations) {

                List<ColumnDataType> columnDataTypes = representation.getFields();
                if (CollectionUtils.isEmpty(columnDataTypes)) {
                    continue;
                }
                for (ColumnDataType columnDataType : columnDataTypes) {
                    if ("code".equals(columnDataType.getColumnName()) && columnDataType.getColumnValue().equals(codeValue)) {
                        valueFound = true;
                        break;
                    }
                }
                if (valueFound) {
                    for (ColumnDataType columnDataType : columnDataTypes) {
                        if ("dataType".equals(columnDataType.getColumnName())) {
                            return columnDataType.getColumnValue();
                        }
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public boolean containsMoreThenOneDeclarationPerTrip(List<IdType> specifiedFishingTripIds, Map<String, List<FishingActivityWithIdentifiers>> faTypesPerTrip) {
        boolean isMoreTheOneDeclaration = false;
        if (MapUtils.isEmpty(faTypesPerTrip) || CollectionUtils.isEmpty(specifiedFishingTripIds)) {
            return isMoreTheOneDeclaration;
        }
        for (IdType idType : specifiedFishingTripIds) {
            List<FishingActivityWithIdentifiers> fishingActivityWithIdentifiers = faTypesPerTrip.get(idType.getValue());
            if (CollectionUtils.isEmpty(fishingActivityWithIdentifiers)) {
                continue;
            }
            int declarationCounter = 0;
            for (FishingActivityWithIdentifiers fishTrpWIdent : fishingActivityWithIdentifiers) {
                if (FishingActivityType.DEPARTURE.name().equals(fishTrpWIdent.getFaType())) {
                    declarationCounter++;
                }
            }
            if (declarationCounter > 1) {
                isMoreTheOneDeclaration = true;
                break;
            }
        }
        return isMoreTheOneDeclaration;
    }

    /**
     * This method checks if atleast one FACatch from specifiedFACatches has matching speciesCode and typeCode value
     *
     * @param specifiedFACatches FACatches from this list would be matched against
     * @param speciesCode        FACatch speciesCode value to be matched
     * @param typeCode           FACatch typeCode value to be matched
     * @return TRUE : Atleast one FACatch with matching criteria found
     * FALSE :  No FACatch with matching criteria found
     */
    public boolean containsAnyFaCatch(List<FACatch> specifiedFACatches, String speciesCode, String typeCode) {
        if (CollectionUtils.isEmpty(specifiedFACatches) || speciesCode == null || typeCode == null) {
            return false;
        }


        for (FACatch faCatch : specifiedFACatches) {
            if (faCatch.getSpeciesCode() != null && faCatch.getTypeCode() != null && speciesCode.equals(faCatch.getSpeciesCode().getValue()) && typeCode.equals(faCatch.getTypeCode().getValue())) {
                return true;
            }
        }

        return false;
    }


    public String getSenderOrReceiver() {
        return senderOrReceiver;
    }

    public void setSenderOrReceiver(String senderOrReceiver) {
        this.senderOrReceiver = senderOrReceiver;
    }

    public void initDomainModel() {
        try {
            InitialContext context = (InitialContext) getInitialContext();
            rulesDomainModel = (RulesDomainModel) context.lookup(ServiceConstants.DB_ACCESS_RULES_DOMAIN_MODEL);
            setDomainModel(rulesDomainModel);
        } catch (NamingException e) {
            log.error("Error while retrieving RulesDomainModel", e);
        }
    }

    public Context getInitialContext() {
        InitialContext initialContext = null;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            log.error("Failed to get InitialContext", e);
        }

        return initialContext;
    }

    public void setDomainModel(RulesDomainModel rulesDomainModel) {
        this.rulesDomainModel = rulesDomainModel;
    }
}
