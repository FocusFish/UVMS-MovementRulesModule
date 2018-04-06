package eu.europa.ec.fisheries.uvms.rules.service.bean.sales;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import eu.europa.ec.fisheries.schema.sales.*;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FishingActivitySummary;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.FishingTripResponse;
import eu.europa.ec.fisheries.uvms.rules.service.ActivityService;
import eu.europa.ec.fisheries.uvms.rules.service.SalesRulesService;
import eu.europa.ec.fisheries.uvms.rules.service.SalesService;
import eu.europa.ec.fisheries.uvms.rules.service.bean.MDRCacheRuleService;
import eu.europa.ec.fisheries.uvms.rules.service.business.FactWithReferencedId;
import eu.europa.ec.fisheries.uvms.rules.service.business.fact.CodeType;
import eu.europa.ec.fisheries.uvms.rules.service.business.fact.*;
import eu.europa.ec.fisheries.uvms.rules.service.business.helper.ObjectRepresentationHelper;
import eu.europa.ec.fisheries.uvms.rules.service.constants.MDRAcronymType;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.joda.time.DateTime;
import un.unece.uncefact.data.standard.mdr.communication.ObjectRepresentation;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Singleton
@Slf4j
public class SalesRulesServiceBean implements SalesRulesService {

    @EJB
    SalesService salesService;

    @EJB
    private MDRCacheRuleService mdrService;

    @EJB
    ActivityService activityService;

    @Inject
    MapperFacade mapper;

    @Override
    public boolean isCorrectionAndIsItemTypeTheSameAsInTheOriginal(SalesFLUXSalesReportMessageFact fluxSalesReportMessageFact) {
        if (fluxSalesReportMessageFact == null) {
            return false;
        }

        FLUXSalesReportMessage fluxSalesReportMessage = mapper.map(fluxSalesReportMessageFact, FLUXSalesReportMessage.class);
        return salesService.isCorrectionAndIsItemTypeTheSameAsInTheOriginal(fluxSalesReportMessage);
    }

    @Override
    public boolean doesReportNotExistWithId(SalesFLUXReportDocumentFact fact) {
        if (fact == null || isEmpty(fact.getIDS()) || fact.getIDS().get(0) == null || isBlank(fact.getIDS().get(0).getValue())) {
            return false;
        }

        return !salesService.isIdNotUnique(fact.getIDS().get(0).getValue(), SalesMessageIdType.SALES_REPORT);
    }

    @Override
    public boolean isReceptionDate48hAfterSaleDate(SalesFLUXSalesReportMessageFact fact) {
        if (fact == null ||
                isEmpty(fact.getSalesReports()) ||
                isEmpty(fact.getSalesReports().get(0).getIncludedSalesDocuments()) ||
                fact.getFLUXReportDocument() == null ||
                fact.getFLUXReportDocument().getCreationDateTime() == null ||
                fact.getFLUXReportDocument().getCreationDateTime().getDateTime() == null) {
            // Can't let this continue, will cause nullpointers
            return false;
        }

        DateTime receptionDate = fact.getFLUXReportDocument().getCreationDateTime().getDateTime();

        List<SalesDocumentType> includedSalesDocuments = fact.getSalesReports().get(0).getIncludedSalesDocuments();
        for (SalesDocumentType includedSalesDocument : includedSalesDocuments) {
            for (SalesEventType salesEventType : includedSalesDocument.getSpecifiedSalesEvents()) {
                if (salesEventType.getOccurrenceDateTime() != null && salesEventType.getOccurrenceDateTime().getDateTime() != null
                        && isMoreThan48HoursLater(receptionDate, salesEventType.getOccurrenceDateTime().getDateTime())) {
                    return true;
                }
            }
        }


        return false;
    }

    private boolean isMoreThan48HoursLater(DateTime receptionDate, DateTime saleDate) {
        // Add 48 hours and 1 minute to salesDate to determine the latest acceptable date.
        // Adding 48 hours isn't enough, a report sent exactly 48h after the event is still valid.
        // This is why there's a buffer of 1 minute. Not sure if this is enough: time will tell (pun intended)
        DateTime latestAcceptableDate = saleDate.plusHours(48).plusMinutes(1);

        // If receptionDate is after latestAcceptableDate, return true
        return receptionDate.isAfter(latestAcceptableDate);
    }

    @Override
    public boolean isReceptionDate48hAfterLandingDeclaration(SalesFLUXSalesReportMessageFact fact) {
        try {
            String fishingTripID = fact.getSalesReports().get(0).getIncludedSalesDocuments().get(0).getSpecifiedFishingActivities().get(0).getSpecifiedFishingTrip().getIDS().get(0).getValue();

            Optional<FishingTripResponse> fishingTripResponse = activityService.getFishingTrip(fishingTripID);

            if (!fishingTripResponse.isPresent()) {
                /**
                 * In case the query returns no values
                 * (which means there was no fishing trip found with the ID specified in the sales note),
                 * we don't fire the rule
                 */
                return false;
            }

            FishingTripResponse fishingTrip = fishingTripResponse.get();
            Optional<DateTime> dateTimeFromLandingActivity = findAcceptanceDateOfLanding(fishingTrip);
            if (!dateTimeFromLandingActivity.isPresent()) {
                return false;
            }

            DateTime receptionDate = fact.getFLUXReportDocument().getCreationDateTime().getDateTime();

            return isMoreThan48HoursLater(receptionDate, dateTimeFromLandingActivity.get());
        } catch (NullPointerException | IndexOutOfBoundsException ex) {
            // if anything is not filled in, this rule cannot be evaluated properly
            return false;
        }
    }

    @Override
    public boolean isIdNotUnique(SalesDocumentFact fact) {
        if (fact == null || isEmpty(fact.getIDS()) || fact.getIDS().get(0) == null || isBlank(fact.getIDS().get(0).getValue())) {
            return false;
        }

        return salesService.isIdNotUnique(fact.getIDS().get(0).getValue(), SalesMessageIdType.SALES_DOCUMENT);
    }

    @Override
    public boolean isIdNotUnique(SalesQueryFact fact) {
        if (fact == null || fact.getID() == null || isBlank(fact.getID().getValue())) {
            return false;
        }

        return salesService.isIdNotUnique(fact.getID().getValue(), SalesMessageIdType.SALES_QUERY);
    }

    @Override
    public boolean isIdNotUnique(SalesFLUXResponseDocumentFact fact) {
        if (fact == null || fact.getIDS() == null ||
                isEmpty(fact.getIDS()) || fact.getIDS().get(0) == null
                || isBlank(fact.getIDS().get(0).getValue())) {
            return false;
        }

        return salesService.isIdNotUnique(fact.getIDS().get(0).getValue(), SalesMessageIdType.SALES_RESPONSE);
    }

    @Override
    public boolean doesReferencedIdNotExist(FactWithReferencedId fact) {
        if (fact == null || fact.getReferencedID() == null || isBlank(fact.getReferencedID().getValue())) {
            return false;
        }

        return !salesService.isIdNotUnique(fact.getReferencedID().getValue(), SalesMessageIdType.SALES_REFERENCED_ID);
    }

    @Override
    public boolean doesTakeOverDocumentIdExist(SalesDocumentFact fact) {
        if (fact == null || isEmpty(fact.getTakeoverDocumentIDs())) {
            return true;
        }

        List<String> takeOverDocumentIds = Lists.newArrayList();

        for (IdType takeoverDocumentIDType : fact.getTakeoverDocumentIDs()) {
            if (takeoverDocumentIDType != null && !isBlank(takeoverDocumentIDType.getValue())) {
                takeOverDocumentIds.add(takeoverDocumentIDType.getValue());
            }
        }
        return salesService.areAnyOfTheseIdsNotUnique(takeOverDocumentIds, SalesMessageIdType.SALES_REPORT);
    }

    @Override
    public boolean doesSalesNoteIdExist(SalesDocumentFact fact) {
        if (fact == null || isEmpty(fact.getSalesNoteIDs())) {
            return true;
        }

        List<String> salesNoteIds = Lists.newArrayList();

        for (IdType salesNoteIDType : fact.getSalesNoteIDs()) {
            if (salesNoteIDType != null && !isBlank(salesNoteIDType.getValue())) {
                salesNoteIds.add(salesNoteIDType.getValue());
            }
        }
        return salesService.areAnyOfTheseIdsNotUnique(salesNoteIds, SalesMessageIdType.SALES_REPORT);
    }

    @Override
    public boolean isStartDateMoreThan3YearsAgo(SalesDelimitedPeriodFact fact) {
        if (fact == null || fact.getStartDateTime() == null || fact.getStartDateTime().getDateTime() == null) {
            return false;
        }

        return fact.getStartDateTime().getDateTime().isBefore(DateTime.now().minusYears(3).minusMinutes(1));
    }

    @Override
    public boolean isDateNotInPast(SalesFLUXResponseDocumentFact fact) {
        if (fact == null || fact.getCreationDateTime() == null || fact.getCreationDateTime().getDateTime() == null) {
            return false;
        }

        return fact.getCreationDateTime().getDateTime().isAfter(DateTime.now());
    }


    @Override
    public boolean isDateOfValidationAfterCreationDateOfResponse(SalesFLUXResponseDocumentFact fact) {
        if (fact == null || fact.getCreationDateTime() == null || fact.getCreationDateTime().getDateTime() == null
                || fact.getRelatedValidationResultDocuments() == null || isEmpty(fact.getRelatedValidationResultDocuments())) {
            return false;
        }

        DateTime creationDateOfReport = fact.getCreationDateTime().getDateTime();

        for (ValidationResultDocumentType validationResultDocumentType : fact.getRelatedValidationResultDocuments()) {
            if (validationResultDocumentType.getCreationDateTime() != null && validationResultDocumentType.getCreationDateTime().getDateTime() != null) {
                DateTime creationDateOfValidationResult = validationResultDocumentType.getCreationDateTime().getDateTime();

                if (creationDateOfValidationResult.isAfter(creationDateOfReport)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isSalesQueryParameterValueNotValid(CodeType typeCode, CodeType valueCode){
        if (typeCode == null || valueCode == null){
            return true;
        }

        switch (typeCode.getValue()){
            case "ROLE":
                return !mdrService.isPresentInMDRList("FLUX_SALES_QUERY_PARAM_ROLE", valueCode.getValue());
            case "FLAG":
                return !mdrService.isPresentInMDRList("TERRITORY", valueCode.getValue());
            case "PLACE":
                return !mdrService.isPresentInMDRList("LOCATION", valueCode.getValue());
            default:
                return true;
        }
    }

    @Override
    public boolean isTheUsedCurrencyAnOfficialCurrencyOfTheCountryAtTheDateOfTheSales(SalesDocumentFact fact) {
        Optional<String> currency = fact.getCurrencyCodeIfExists();
        Optional<String> country = fact.getCountryIfExists();
        Optional<DateTime> occurrence = fact.getOccurrenceIfPresent();

        if (currency.isPresent() && country.isPresent() && occurrence.isPresent()) {
            List<ObjectRepresentation> territoriesAndTheirCurrencies = mdrService.getObjectRepresentationList(MDRAcronymType.TERRITORY_CURR);
            return ObjectRepresentationHelper.doesObjectRepresentationExistWithTheGivenCodeAndWithTheGivenValueForTheGivenColumn(currency.get(), "placesCode", country.get(), territoriesAndTheirCurrencies);
            //TODO: take only the MDR lists that were active on the day of the occurrence into account
        } else {
            return false;
        }
    }

    @Override
    public boolean isOriginalAndIsIdNotUnique(SalesFLUXSalesReportMessageFact fact) {
        if (fact == null || isEmpty(fact.getSalesReports()) || fact.getSalesReports().get(0) == null
                || fact.getFLUXReportDocument() == null || fact.getFLUXReportDocument().getPurposeCode() == null
                || isBlank(fact.getFLUXReportDocument().getPurposeCode().getValue())
                || isEmpty(fact.getSalesReports().get(0).getIncludedSalesDocuments())
                || isEmpty(fact.getSalesReports().get(0).getIncludedSalesDocuments().get(0).getIDS())
                || isBlank(fact.getSalesReports().get(0).getIncludedSalesDocuments().get(0).getIDS().get(0).getValue())
                ) {
            return false;
        }

        // If the report is not an original, return false
        if (!fact.getFLUXReportDocument().getPurposeCode().getValue().equals("9")) {
            return false;
        }

        return salesService.isIdNotUnique(fact.getSalesReports().get(0).getIncludedSalesDocuments().get(0).getIDS().get(0).getValue(), SalesMessageIdType.SALES_DOCUMENT);
    }

    private Optional<DateTime> findAcceptanceDateOfLanding(FishingTripResponse fishingTrip) {
        for (FishingActivitySummary activitySummary : fishingTrip.getFishingActivityLists()) {
            if (activitySummary.getActivityType().equals("LANDING")) {
                return Optional.of(new DateTime(activitySummary.getAcceptedDateTime().toGregorianCalendar()));
            }
        }
        return Optional.absent();
    }


}
