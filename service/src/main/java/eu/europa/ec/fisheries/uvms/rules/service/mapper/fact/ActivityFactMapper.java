/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.rules.service.mapper.fact;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityTableType;
import eu.europa.ec.fisheries.uvms.rules.service.business.fact.*;
import eu.europa.ec.fisheries.uvms.rules.service.mapper.xpath.util.XPathStringWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.TextType;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.europa.ec.fisheries.uvms.rules.service.constants.XPathConstants.*;

/**
 * Created by kovian on 14/06/2017.
 */
@Slf4j
public class ActivityFactMapper {

    public static final String VALUE_PROP = "value";
    public static final String VALUE_INDICATOR_PROP = "valueIndicator";
    public static final String VALUE_MEASURE_PROP = "valueMeasure";
    public static final String VALUE_CODE_PROP = "valueCode";
    public static final String VALUE_QUANTITY_PROP = "valueQuantity";
    private XPathStringWrapper xPathUtil;

    /**
     * Additional objects - to be set before validation through generator.setAdditionalValidationObject(.., ..){..}
     **/
    private List<IdTypeWithFlagState> assetList;

    private Map<ActivityTableType, List<IdType>> nonUniqueIdsMap = new HashMap<>();

    private static final String TYPE_CODE_PROP = "typeCode";
    public static final String ROLE_CODES_PROP = "roleCodes";
    public static final String APPLICABLE_GEAR_CHARACTERISTICS_PROP = "applicableGearCharacteristics";
    public static final String AFFECTED_QUANTITY_PROP = "affectedQuantity";
    public static final String SPECIFIED_FLUX_LOCATION_PROP = "specifiedFluxLocations";
    public static final String RECOVERY_MEASURE_CODE_PROP = "recoveryMeasureCodes";
    private static final String REASON_CODE_PROP = "reasonCode";
    private static final String FA_REPORT_DOCUMENT_TYPE_CODE_PROP = "faReportDocumentTypeCode";
    private static final String RELATED_FLUX_LOCATIONS_PROP = "relatedFLUXLocations";
    private static final String FISHERY_TYPE_CODE_PROP = "fisheryTypeCode";
    private static final String SPECIES_TARGET_CODE_PROP = "speciesTargetCode";
    private static final String SPECIFIED_FISHING_TRIP_PROP = "specifiedFishingTrip";
    private static final String OCCURRENCE_DATE_TIME_PROP = "occurrenceDateTime";
    private static final String CODE_TYPE_FOR_FACATCH_FLUXLOCATION = "facatchFluxlocationTypeCode";
    private static final String CODE_TYPE_FOR_FACATCH = "facatchTypeCode";
    private static final String SPECIFIED_FA_CATCHES_TYPE_CODE_PROP = "specifiedFACatchesTypeCodes";
    private static final String RELATED_FLUX_LOCATIONS_TYPE_CODE_PROP = "relatedFluxLocationTypeCodes";
    private static final String RELATED_FLUX_LOCATIONS_ID_PROP = "relatedFluxLocationIDs";

    private ActivityFactMapper(){
        super();
    }

    public ActivityFactMapper(XPathStringWrapper strUtil_){
        setxPathUtil(strUtil_);
    }

    public FaReportDocumentFact generateFactForFaReportDocument(FAReportDocument faReportDocument) {
        if (faReportDocument == null) {
            xPathUtil.clear();
            return null;
        }

        String partialXpath = xPathUtil.getValue();

        FaReportDocumentFact faReportDocumentFact = new FaReportDocumentFact();

        faReportDocumentFact.setCreationDateTime(getDate(faReportDocumentsRelatedFLUXReportDocumentCreationDateTime(faReportDocument)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, CREATION_DATE_TIME).storeInRepo(faReportDocumentFact, "creationDateTime");

        faReportDocumentFact.setAcceptanceDateTime(getDate(faReportDocument.getAcceptanceDateTime()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(ACCEPTANCE_DATE_TIME).storeInRepo(faReportDocumentFact, "acceptanceDateTime");

        faReportDocumentFact.setIds(mapToIdType(faReportDocumentsRelatedFLUXReportDocumentIDS(faReportDocument)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, ID).storeInRepo(faReportDocumentFact, "ids");

        faReportDocumentFact.setOwnerFluxPartyIds(mapToIdType(faReportDocumentsRelatedFLUXReportDocumentOwnerFLUXPartyIDS(faReportDocument)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, OWNER_FLUX_PARTY, ID).storeInRepo(faReportDocumentFact, "ownerFluxPartyIds");

        faReportDocumentFact.setUniqueIds(getIds(faReportDocument.getRelatedFLUXReportDocument()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, ID).storeInRepo(faReportDocumentFact, "uniqueIds");

        faReportDocumentFact.setRelatedFLUXReportDocumentReferencedID(mapToSingleIdType(faReportDocumentsRelatedFLUXReportDocumentReferencedID(faReportDocument)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, REFERENCED_ID).storeInRepo(faReportDocumentFact, "relatedFLUXReportDocumentReferencedID");

        faReportDocumentFact.setRelatedFLUXReportDocumentIDs(mapToIdType(faReportDocumentsRelatedFLUXReportDocumentIDS_(faReportDocument)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, ID).storeInRepo(faReportDocumentFact, "relatedFLUXReportDocumentIDs");

        faReportDocumentFact.setPurposeCode(mapToCodeType(faReportDocumentsRelatedFLUXReportDocumentPurposeCode(faReportDocument)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, PURPOSE_CODE).storeInRepo(faReportDocumentFact, "purposeCode");

        faReportDocumentFact.setTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faReportDocumentFact, TYPE_CODE_PROP);

        faReportDocumentFact.setRelatedReportIDs(mapToIdType(faReportDocument.getRelatedReportIDs()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_REPORT_ID).storeInRepo(faReportDocumentFact, "relatedReportIDs");

        faReportDocumentFact.setRelatedFLUXReportDocument(faReportDocument.getRelatedFLUXReportDocument());
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT).storeInRepo(faReportDocumentFact, "relatedFLUXReportDocument");

        faReportDocumentFact.setSpecifiedVesselTransportMeans(faReportDocument.getSpecifiedVesselTransportMeans());
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_VESSEL_TRANSPORT_MEANS).storeInRepo(faReportDocumentFact, "specifiedVesselTransportMeans");

        faReportDocumentFact.setReferencedID(referencedIdFromRelatedFLUXReportDocument(faReportDocument));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, REFERENCED_ID).storeInRepo(faReportDocumentFact, "referencedID");

        List<FishingActivity> specifiedFishingActivities = faReportDocument.getSpecifiedFishingActivities();
        if (CollectionUtils.isNotEmpty(specifiedFishingActivities)) {
            faReportDocumentFact.setSpecifiedFishingActivities(new ArrayList<>(specifiedFishingActivities));
            faReportDocumentFact.setSpecifiedFishingActivitiesTypes(mapFishingActivityTypes(specifiedFishingActivities));
            faReportDocumentFact.setSpecifiedAndRealtedFishActOccurrenceDateTimes(mapOccurrenceDateTimesFromFishingActivities(specifiedFishingActivities));
        }
        // Even if specifiedFishingActivities is empty we still need to map the xpath, cause those properties have rules being applied to them,
        // and if the rule fails (ex. cause of the property being empty or null) then we still need to return the xpath to what failed.
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_ACTIVITY).storeInRepo(faReportDocumentFact, "specifiedFishingActivities");
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_ACTIVITY, TYPE_CODE).storeInRepo(faReportDocumentFact, "specifiedFishingActivitiesTypes");
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_ACTIVITY, OCCURRENCE_DATE_TIME).storeInRepo(faReportDocumentFact, "specifiedAndRealtedFishActOccurrenceDateTimes");

        faReportDocumentFact.setNonUniqueIdsList(nonUniqueIdsMap.get(ActivityTableType.RELATED_FLUX_REPORT_DOCUMENT_ENTITY));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_REPORT_DOCUMENT, ID).storeInRepo(faReportDocumentFact, "nonUniqueIdsList");

        return faReportDocumentFact;
    }


    private List<Date> mapOccurrenceDateTimesFromFishingActivities(List<FishingActivity> fishingActivities) {
        if(CollectionUtils.isEmpty(fishingActivities)){
            return Collections.emptyList();
        }
        List<Date> dates = new ArrayList<>();
        for(FishingActivity activity : fishingActivities){;
            dates.add(getDate(activity.getOccurrenceDateTime()));
            if(CollectionUtils.isNotEmpty(activity.getRelatedFishingActivities())){
                dates.addAll(mapOccurrenceDateTimesFromFishingActivities(activity.getRelatedFishingActivities()));
            }
        }
        dates.removeAll(Collections.singleton(null));
        return dates;
    }

    private List<String> mapFishingActivityTypes(List<FishingActivity> specifiedFishingActivities) {
        List<String> actTypesList = new ArrayList<>();
        if(CollectionUtils.isEmpty(specifiedFishingActivities)){
            return actTypesList;
        }
        for(FishingActivity fishAct : specifiedFishingActivities){
            un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fishAct.getTypeCode();
            if(typeCode != null){
                actTypesList.add(typeCode.getValue());
            }
        }
        return actTypesList;
    }

    private IdType referencedIdFromRelatedFLUXReportDocument(FAReportDocument faReportDocument){
        FLUXReportDocument relatedFLUXReportDocument = faReportDocument.getRelatedFLUXReportDocument();
        if(relatedFLUXReportDocument != null && relatedFLUXReportDocument.getReferencedID() != null){
            return mapToSingleIdType(relatedFLUXReportDocument.getReferencedID());
        }
        return null;
    }


    public List<FaReportDocumentFact> generateFactForFaReportDocuments(List<FAReportDocument> faReportDocuments) {
        if (faReportDocuments == null) {
            return Collections.emptyList();
        }
        int index = 1;
        List<FaReportDocumentFact> list = new ArrayList<>();
        for (FAReportDocument fAReportDocument : faReportDocuments) {
            xPathUtil.append(FLUXFA_REPORT_MESSAGE).appendWithIndex(FA_REPORT_DOCUMENT, index);
            list.add(generateFactForFaReportDocument(fAReportDocument));
            index++;
        }

        return list;
    }


    public FishingActivityFact generateFactForFishingActivity(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null) {
            xPathUtil.clear();
            return null;
        }

        String partialXpath = xPathUtil.getValue();

        FishingActivityFact fishingActivityFact = getFishingActivityCoreFact(fishingActivity, partialXpath);

        if (faReportDocument != null) {
            fishingActivityFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FLUX_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(fishingActivityFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return fishingActivityFact;
    }

    public FishingActivityFact generateFactForFishingActivity(FishingActivity fishingActivity, boolean isSubActivity) {
        if (fishingActivity == null) {
            xPathUtil.clear();
            return null;
        }

        String partialXpath = xPathUtil.getValue();

        FishingActivityFact fishingActivityFact = getFishingActivityCoreFact(fishingActivity, partialXpath);
        fishingActivityFact.setIsSubActivity(isSubActivity);

        return fishingActivityFact;
    }

    private FishingActivityFact getFishingActivityCoreFact(FishingActivity fishingActivity, String partialXpath) {
        FishingActivityFact fishingActivityFact = new FishingActivityFact();

        if (fishingActivity.getSpecifiedDelimitedPeriods() != null) {
            fishingActivityFact.setDelimitedPeriods(new ArrayList<>(fishingActivity.getSpecifiedDelimitedPeriods()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_DELIMITED_PERIOD).storeInRepo(fishingActivityFact, "delimitedPeriods");
        }
        fishingActivityFact.setRelatedFishingTrip(mapRelatedFishingTrips(fishingActivity.getRelatedFishingActivities()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FISHING_ACTIVITY, SPECIFIED_FISHING_TRIP).storeInRepo(fishingActivityFact, "relatedFishingTrip");

        fishingActivityFact.setDurationMeasure(mapDurationMeasure(fishingActivity.getSpecifiedDelimitedPeriods()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_DELIMITED_PERIOD, DURATION_MEASURE).storeInRepo(fishingActivityFact, "durationMeasure");

        BigDecimal operatQuantity = fishingActivityOperationsQuantityValue(fishingActivity);
        if (operatQuantity != null) {
            fishingActivityFact.setOperationQuantity(operatQuantity.intValue());
            xPathUtil.appendWithoutWrapping(partialXpath).append(OPERATIONS_QUANTITY).storeInRepo(fishingActivityFact, "operationQuantity");
        }
        fishingActivityFact.setRelatedActivityFluxLocations(getFluxLocations(fishingActivity.getRelatedFishingActivities()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FISHING_ACTIVITY, RELATED_FLUX_LOCATION).storeInRepo(fishingActivityFact, "relatedActivityFluxLocations");

        fishingActivityFact.setIsDateProvided(isDatePresent(fishingActivity.getOccurrenceDateTime()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(OCCURRENCE_DATE_TIME).storeInRepo(fishingActivityFact, "isDateProvided");

        fishingActivityFact.setFluxCharacteristicsTypeCode(getApplicableFLUXCharacteristicsTypeCode(fishingActivity.getSpecifiedFLUXCharacteristics()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FLUX_CHARACTERISTIC, TYPE_CODE).storeInRepo(fishingActivityFact, "fluxCharacteristicsTypeCode");

        fishingActivityFact.setRelatedDelimitedPeriods(getDelimitedPeriod(fishingActivity.getRelatedFishingActivities()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FISHING_ACTIVITY, SPECIFIED_DELIMITED_PERIOD).storeInRepo(fishingActivityFact, "relatedDelimitedPeriods");

        if (fishingActivity.getRelatedFishingActivities() != null) {
            fishingActivityFact.setRelatedFishingActivities(new ArrayList<>(fishingActivity.getRelatedFishingActivities()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FISHING_ACTIVITY).storeInRepo(fishingActivityFact, "relatedFishingActivities");
        }
        if (fishingActivity.getRelatedFLUXLocations() != null) {
            fishingActivityFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(fishingActivityFact, RELATED_FLUX_LOCATIONS_PROP);
        }

        fishingActivityFact.setReasonCode(mapToCodeType(fishingActivity.getReasonCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(REASON_CODE).storeInRepo(fishingActivityFact, REASON_CODE_PROP);

        fishingActivityFact.setFisheryTypeCode(mapToCodeType(fishingActivity.getFisheryTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FISHERY_TYPE_CODE).storeInRepo(fishingActivityFact, FISHERY_TYPE_CODE_PROP);

        fishingActivityFact.setSpeciesTargetCode(mapToCodeType(fishingActivity.getSpeciesTargetCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIES_TARGET_CODE).storeInRepo(fishingActivityFact, SPECIES_TARGET_CODE_PROP);

        fishingActivityFact.setSpecifiedFishingTrip(fishingActivity.getSpecifiedFishingTrip());
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_TRIP).storeInRepo(fishingActivityFact, SPECIFIED_FISHING_TRIP_PROP);

        fishingActivityFact.setTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(fishingActivityFact, TYPE_CODE_PROP);

        fishingActivityFact.setOccurrenceDateTime(getDate(fishingActivity.getOccurrenceDateTime()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(OCCURRENCE_DATE_TIME).storeInRepo(fishingActivityFact, OCCURRENCE_DATE_TIME_PROP);

        fishingActivityFact.setVesselRelatedActivityCode(mapToCodeType(fishingActivity.getVesselRelatedActivityCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(VESSEL_RELATED_ACTIVITY_CODE).storeInRepo(fishingActivityFact, "vesselRelatedActivityCode");

        fishingActivityFact.setRelatedFluxLocationRFMOCodeList(getFLUXLocationRFMOCodes(fishingActivity.getRelatedFLUXLocations()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, REGIONAL_FISHERIES_MANAGEMENT_ORGANIZATION_CODE).storeInRepo(fishingActivityFact, "relatedFluxLocationRFMOCodeList");

        return fishingActivityFact;
    }


    public List<FishingActivityFact> generateFactForFishingActivities(List<FishingActivity> fishingActivities, FAReportDocument faReportDocument) {
        if (fishingActivities == null) {
            return Collections.emptyList();
        }

        List<FishingActivityFact> list = new ArrayList<>();
        for (FishingActivity fishingActivity : fishingActivities) {
            list.add(generateFactForFishingActivity(fishingActivity, faReportDocument));
        }

        return list;
    }


    public FluxFaReportMessageFact generateFactForFluxFaReportMessage(FLUXFAReportMessage fluxfaReportMessage) {
        if (fluxfaReportMessage == null) {
            return null;
        }

        FluxFaReportMessageFact fluxFaReportMessageFact = new FluxFaReportMessageFact();

        String partialXpath = xPathUtil.append(FLUXFA_REPORT_MESSAGE).getValue();

        fluxFaReportMessageFact.setCreationDateTime(getDate(fluxfaReportMessageFLUXReportDocumentCreationDateTime(fluxfaReportMessage)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, CREATION_DATE_TIME).storeInRepo(fluxFaReportMessageFact, "creationDateTime");

        if (fluxfaReportMessage.getFAReportDocuments() != null) {
            fluxFaReportMessageFact.setFaReportDocuments(new ArrayList<>(fluxfaReportMessage.getFAReportDocuments()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(FA_REPORT_DOCUMENT).storeInRepo(fluxFaReportMessageFact, "faReportDocuments");
        }

        fluxFaReportMessageFact.setIds(mapToIdType(fluxfaReportMessageFLUXReportDocumentIDS(fluxfaReportMessage)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, ID).storeInRepo(fluxFaReportMessageFact, "ids");

        fluxFaReportMessageFact.setReferencedID(mapToSingleIdType(fluxfaReportMessageFLUXReportDocumentReferencedID(fluxfaReportMessage)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, REFERENCED_ID).storeInRepo(fluxFaReportMessageFact, "referencedID");

        fluxFaReportMessageFact.setOwnerFluxPartyIds(mapToIdType(fluxfaReportMessageFLUXReportDocumentOwnerFLUXPartyIDS(fluxfaReportMessage)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, OWNER_FLUX_PARTY, ID).storeInRepo(fluxFaReportMessageFact, "ownerFluxPartyIds");

        fluxFaReportMessageFact.setUniqueIds(getIds(fluxfaReportMessage.getFLUXReportDocument()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, ID).storeInRepo(fluxFaReportMessageFact, "uniqueIds");

        fluxFaReportMessageFact.setPurposeCode(mapToCodeType(fluxfaReportMessageFLUXReportDocumentPurposeCode(fluxfaReportMessage)));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, PURPOSE_CODE).storeInRepo(fluxFaReportMessageFact, "purposeCode");

        fluxFaReportMessageFact.setNonUniqueIdsList(nonUniqueIdsMap.get(ActivityTableType.FLUX_REPORT_DOCUMENT_ENTITY));
        xPathUtil.appendWithoutWrapping(partialXpath).append(FLUX_REPORT_DOCUMENT, ID).storeInRepo(fluxFaReportMessageFact, "nonUniqueIdsList");

        return fluxFaReportMessageFact;
    }

    public VesselTransportMeansFact generateFactForVesselTransportMean(VesselTransportMeans vesselTransportMean, boolean isCommingFromFaReportDocument) {
        if (vesselTransportMean == null) {
            xPathUtil.clear();
            return null;
        }
        VesselTransportMeansFact vesselTransportMeansFact = generateFactForVesselTransportMean(vesselTransportMean);
        vesselTransportMeansFact.setIsFromFaReport(isCommingFromFaReportDocument);
        return vesselTransportMeansFact;
    }

    public List<VesselTransportMeansFact> generateFactForVesselTransportMeans(List<VesselTransportMeans> vesselTransportMean) {
        if (vesselTransportMean == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }
        List<VesselTransportMeansFact> list = new ArrayList<>();
        int index = 1;
        String strToAppend = xPathUtil.getValue();
        for (VesselTransportMeans vesselTransportMeans : vesselTransportMean) {
            xPathUtil.appendWithoutWrapping(strToAppend).appendWithIndex(RELATED_VESSEL_TRANSPORT_MEANS, index);
            VesselTransportMeansFact vesselTransportMeansFact = generateFactForVesselTransportMean(vesselTransportMeans);
            list.add(vesselTransportMeansFact);
            index++;
        }
        return list;
    }


    public VesselTransportMeansFact generateFactForVesselTransportMean(VesselTransportMeans vesselTransportMean) {
        if (vesselTransportMean == null) {
            xPathUtil.clear();
            return null;
        }

        // Since every time we get the final value (Eg. when storing in repo) we clean the StringBuffer inside XPathStringWrapper, we need to store and use the initial
        // value which is to be used always (appended as the first string in the buffer).
        String toBeAppendedAlways = xPathUtil.getValue();

        VesselTransportMeansFact vesselTransportMeansFact = new VesselTransportMeansFact();

        List<ContactParty> specifiedContactParties = vesselTransportMean.getSpecifiedContactParties();

        vesselTransportMeansFact.setRegistrationVesselCountryId(mapToSingleIdType(vesselTransportMeanRegistrationVesselCountryID(vesselTransportMean)));
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(REGISTRATION_VESSEL_COUNTRY, ID).storeInRepo(vesselTransportMeansFact, "registrationVesselCountryId");

        vesselTransportMeansFact.setSpecifiedContactPersons(mapToContactPersonList(specifiedContactParties));
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(SPECIFIED_CONTACT_PARTY, SPECIFIED_CONTACT_PERSON).storeInRepo(vesselTransportMeansFact, "specifiedContactPersons");

        vesselTransportMeansFact.setIds(mapToIdType(vesselTransportMean.getIDS()));
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(ID).storeInRepo(vesselTransportMeansFact, "ids");

        vesselTransportMeansFact.setSpecifiedContactPartyRoleCodes(mapFromContactPartyToCodeType(specifiedContactParties));
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(SPECIFIED_CONTACT_PARTY, ROLE_CODE).storeInRepo(vesselTransportMeansFact, "specifiedContactPartyRoleCodes");

        vesselTransportMeansFact.setRoleCode(mapToCodeType(vesselTransportMean.getRoleCode()));
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(ROLE_CODE).storeInRepo(vesselTransportMeansFact, "roleCode");

        if (specifiedContactParties != null) {
            vesselTransportMeansFact.setSpecifiedContactParties(new ArrayList<>(specifiedContactParties));
        }
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(SPECIFIED_CONTACT_PARTY).storeInRepo(vesselTransportMeansFact, "specifiedContactParties");

        vesselTransportMeansFact.setSpecifiedStructuredAddresses(mapSpecifiedStructuredAddresses(specifiedContactParties));
        xPathUtil.appendWithoutWrapping(toBeAppendedAlways).append(SPECIFIED_CONTACT_PARTY, SPECIFIED_STRUCTURED_ADDRESS).storeInRepo(vesselTransportMeansFact, "specifiedStructuredAddresses");

        vesselTransportMeansFact.setAssetList(assetList);

        return vesselTransportMeansFact;
    }

    private List<StructuredAddress> mapSpecifiedStructuredAddresses(List<ContactParty> specifiedContactParties) {
        List<StructuredAddress> structAdrList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(specifiedContactParties)){
            for(ContactParty contParty : specifiedContactParties){
                structAdrList.addAll(contParty.getSpecifiedStructuredAddresses());
            }
        }
        structAdrList.removeAll(Collections.singleton(null));
        return structAdrList;
    }


    public StructuredAddressFact generateFactsForStructureAddress(StructuredAddress structuredAddress) {
        if (structuredAddress == null) {
            xPathUtil.clear();
            return null;
        }

        String partialXpath = xPathUtil.getValue();

        StructuredAddressFact structuredAddressFact = new StructuredAddressFact();

        structuredAddressFact.setPostcodeCode(structuredAddressPostcodeCodeValue(structuredAddress));
        xPathUtil.appendWithoutWrapping(partialXpath).append(POSTCODE_CODE).storeInRepo(structuredAddressFact, "postcodeCode");

        structuredAddressFact.setCountryID(structuredAddressCountryIDValue(structuredAddress));
        xPathUtil.appendWithoutWrapping(partialXpath).append(COUNTRY_ID).storeInRepo(structuredAddressFact, "countryID");

        structuredAddressFact.setCityName(structuredAddressCityNameValue(structuredAddress));
        xPathUtil.appendWithoutWrapping(partialXpath).append(CITY_NAME).storeInRepo(structuredAddressFact, "cityName");

        structuredAddressFact.setStreetName(structuredAddressStreetNameValue(structuredAddress));
        xPathUtil.appendWithoutWrapping(partialXpath).append(STREET_NAME).storeInRepo(structuredAddressFact, "streetName");

        structuredAddressFact.setPlotIdentification(structuredAddressPlotIdentificationValue(structuredAddress));
        xPathUtil.appendWithoutWrapping(partialXpath).append(PLOT_IDENTIFICATION).storeInRepo(structuredAddressFact, "plotIdentification");

        return structuredAddressFact;
    }


    public List<StructuredAddressFact> generateFactsForStructureAddresses(List<StructuredAddress> structuredAddresses, String adressType) {
        if (structuredAddresses == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }
        String partialXpath = xPathUtil.getValue();
        List<StructuredAddressFact> list = new ArrayList<>();
        int index = 1;
        for (StructuredAddress structuredAddress : structuredAddresses) {
            xPathUtil.appendWithoutWrapping(partialXpath).appendWithIndex(adressType, index);
            list.add(generateFactsForStructureAddress(structuredAddress));
            index++;
        }
        return list;
    }


    public FishingGearFact generateFactsForFishingGear(FishingGear fishingGear, String gearType) {
        if (fishingGear == null) {
            xPathUtil.clear();
            return null;
        }

        FishingGearFact fishingGearFact = new FishingGearFact();
        if (SPECIFIED_FISHING_GEAR.equals(gearType)) {
            fishingGearFact.setFishingActivity(true);
        }

        final String partialXpath = xPathUtil.getValue();

        fishingGearFact.setTypeCode(mapToCodeType(fishingGear.getTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(fishingGearFact, TYPE_CODE_PROP);

        fishingGearFact.setRoleCodes(mapToCodeType(fishingGear.getRoleCodes()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(ROLE_CODE).storeInRepo(fishingGearFact, ROLE_CODES_PROP);

        if (fishingGear.getApplicableGearCharacteristics() != null) {
            fishingGearFact.setApplicableGearCharacteristics(new ArrayList<>(fishingGear.getApplicableGearCharacteristics()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(APPLICABLE_GEAR_CHARACTERISTIC).storeInRepo(fishingGearFact, APPLICABLE_GEAR_CHARACTERISTICS_PROP);
        }

        return fishingGearFact;
    }


    public List<FishingGearFact> generateFactsForFishingGears(List<FishingGear> fishingGears, String gearType) {
        if (fishingGears == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }
        final String partialXpath = xPathUtil.getValue();
        List<FishingGearFact> list = new ArrayList<>();
        int index = 1;
        for (FishingGear fishingGear : fishingGears) {
            xPathUtil.appendWithoutWrapping(partialXpath).appendWithIndex(gearType, index);
            list.add(generateFactsForFishingGear(fishingGear, gearType));
            index++;
        }

        return list;
    }


    public GearCharacteristicsFact generateFactsForGearCharacteristic(GearCharacteristic gearCharacteristic) {
        if (gearCharacteristic == null) {
            return null;
        }

        GearCharacteristicsFact gearCharacteristicsFact = new GearCharacteristicsFact();

        gearCharacteristicsFact.setTypeCode(mapToCodeType(gearCharacteristic.getTypeCode()));
        xPathUtil.append(TYPE_CODE).storeInRepo(gearCharacteristicsFact, TYPE_CODE_PROP);

        gearCharacteristicsFact.setValue(gearCharacteristic.getValue());
        xPathUtil.append(VALUE).storeInRepo(gearCharacteristicsFact, VALUE_PROP);

        gearCharacteristicsFact.setValueIndicator(gearCharacteristic.getValueIndicator());
        xPathUtil.append(VALUE_INDICATOR).storeInRepo(gearCharacteristicsFact, VALUE_INDICATOR_PROP);

        gearCharacteristicsFact.setValueMeasure(mapToMeasureType(gearCharacteristic.getValueMeasure()));
        xPathUtil.append(VALUE_MEASURE).storeInRepo(gearCharacteristicsFact, VALUE_MEASURE_PROP);

        gearCharacteristicsFact.setValueCode(mapToCodeType(gearCharacteristic.getValueCode()));
        xPathUtil.append(VALUE_CODE).storeInRepo(gearCharacteristicsFact, VALUE_CODE_PROP);

        gearCharacteristicsFact.setValueQuantity(mapQuantityTypeToMeasureType(gearCharacteristic.getValueQuantity()));
        xPathUtil.append(VALUE_QUANTITY).storeInRepo(gearCharacteristicsFact, VALUE_QUANTITY_PROP);

        return gearCharacteristicsFact;
    }


    public List<GearCharacteristicsFact> generateFactsForGearCharacteristics(List<GearCharacteristic> gearCharacteristics, String gearType) {
        if (gearCharacteristics == null) {
            return Collections.emptyList();
        }

        String partialXpath = xPathUtil.getValue();

        List<GearCharacteristicsFact> list = new ArrayList<>();
        int index = 1;
        for (GearCharacteristic gearCharacteristic : gearCharacteristics) {
            xPathUtil.appendWithoutWrapping(partialXpath).appendWithIndex(gearType, index);
            list.add(generateFactsForGearCharacteristic(gearCharacteristic));
            index++;
        }

        return list;
    }


    public GearProblemFact generateFactsForGearProblem(GearProblem gearProblem) {
        if (gearProblem == null) {
            xPathUtil.clear();
            return null;
        }

        final String partialXpath = xPathUtil.getValue();
        GearProblemFact gearProblemFact = new GearProblemFact();

        gearProblemFact.setTypeCode(mapToCodeType(gearProblem.getTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(gearProblemFact, TYPE_CODE_PROP);

        if (gearProblem.getRecoveryMeasureCodes() != null) {
            gearProblemFact.setRecoveryMeasureCodes(mapToCodeType(gearProblem.getRecoveryMeasureCodes()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RECOVERY_MEASURE_CODE).storeInRepo(gearProblemFact, RECOVERY_MEASURE_CODE_PROP);
        }

        gearProblemFact.setAffectedQuantity(gearProblem.getAffectedQuantity());
        xPathUtil.appendWithoutWrapping(partialXpath).append(AFFECTED_QUANTITY).storeInRepo(gearProblemFact, AFFECTED_QUANTITY_PROP);

        if (gearProblem.getSpecifiedFLUXLocations() != null) {
            gearProblemFact.setSpecifiedFluxLocations(gearProblem.getSpecifiedFLUXLocations());
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FLUX_LOCATION).storeInRepo(gearProblemFact, SPECIFIED_FLUX_LOCATION_PROP);
        }

        return gearProblemFact;
    }


    public List<GearProblemFact> generateFactsForGearProblems(List<GearProblem> gearProblems) {
        if (gearProblems == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }
        final String partialXpath = xPathUtil.getValue();
        List<GearProblemFact> list = new ArrayList<>();
        int index = 1;
        for (GearProblem gearProblem : gearProblems) {
            xPathUtil.appendWithoutWrapping(partialXpath).appendWithIndex(SPECIFIED_GEAR_PROBLEM, index);
            list.add(generateFactsForGearProblem(gearProblem));
            index++;
        }

        return list;
    }

    public List<FaCatchFact> generateFactsForFaCatch(FishingActivity activity) {

        if (activity == null) {
            return Collections.emptyList();
        }

        List<FACatch> faCatches = activity.getSpecifiedFACatches();
        List<FLUXLocation> relatedFLUXLocations = activity.getRelatedFLUXLocations();
        List<FaCatchFact> facts = new ArrayList<>();

        if (CollectionUtils.isEmpty(faCatches) && relatedFLUXLocations == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }

        String partialXPath = xPathUtil.getValue();
        int index = 1;
        for (FACatch faCatch : faCatches) {

            FaCatchFact faCatchFact = new FaCatchFact();

            partialXPath = xPathUtil.appendWithoutWrapping(partialXPath).appendWithIndex(SPECIFIED_FA_CATCH, index).getValue();

            faCatchFact.setResultAAPProduct(getAppliedProcessAAPProducts(faCatch.getAppliedAAPProcesses()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, RESULT_AAP_PRODUCT).storeInRepo(faCatchFact, "resultAAPProduct");

            faCatchFact.setSpeciesCode(mapToCodeType(faCatch.getSpeciesCode()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(SPECIES_CODE).storeInRepo(faCatchFact, "speciesCode");

            faCatchFact.setAppliedAAPProcessConversionFactorNumber(mapAAPProcessList(faCatch.getAppliedAAPProcesses()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, CONVERSION_FACTOR_NUMERIC).storeInRepo(faCatchFact, "appliedAAPProcessConversionFactorNumber");

            faCatchFact.setCategoryCode(mapToCodeType(faCatchSpecifiedSizeDistributionCategoryCode(faCatch)));
            xPathUtil.appendWithoutWrapping(partialXPath).append(SPECIFIED_SIZE_DISTRIBUTION, CATEGORY_CODE).storeInRepo(faCatchFact, "categoryCode");

            faCatchFact.setTypeCode(mapToCodeType(faCatch.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(TYPE_CODE).storeInRepo(faCatchFact, TYPE_CODE_PROP);

            faCatchFact.setSizeDistributionClassCode(mapToCodeType(faCatchesSpecifiedSizeDistributionClassCodes(faCatch)));
            xPathUtil.appendWithoutWrapping(partialXPath).append(SPECIFIED_SIZE_DISTRIBUTION, CLASS_CODE).storeInRepo(faCatchFact, "sizeDistributionClassCode");

            faCatchFact.setUnitQuantity(mapQuantityTypeToMeasureType(faCatch.getUnitQuantity()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(UNIT_QUANTITY).storeInRepo(faCatchFact, "unitQuantity");

            faCatchFact.setWeightMeasure(mapToMeasureType(faCatch.getWeightMeasure()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(WEIGHT_MEASURE).storeInRepo(faCatchFact, "weightMeasure");

            faCatchFact.setWeighingMeansCode(mapToCodeType(faCatch.getWeighingMeansCode()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(WEIGHING_MEANS_CODE).storeInRepo(faCatchFact, "weighingMeansCode");

            faCatchFact.setResultAAPProductWeightMeasure(getMeasureTypeFromAAPProcess(faCatch.getAppliedAAPProcesses(), WEIGHT_MEASURE));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, RESULT_AAP_PRODUCT, WEIGHT_MEASURE).storeInRepo(faCatchFact, "resultAAPProductWeightMeasure");

            faCatchFact.setResultAAPProductUnitQuantity(getMeasureTypeFromAAPProcess(faCatch.getAppliedAAPProcesses(), UNIT_QUANTITY));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, RESULT_AAP_PRODUCT, UNIT_QUANTITY).storeInRepo(faCatchFact, "resultAAPProductUnitQuantity");

            faCatchFact.setAppliedAAPProcessTypeCodes(getAppliedProcessTypeCodes(faCatch.getAppliedAAPProcesses()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, TYPE_CODE).storeInRepo(faCatchFact, "appliedAAPProcessTypeCodes");

            faCatchFact.setResultAAPProductPackagingTypeCode(getAAPProductPackagingTypeCode(faCatch.getAppliedAAPProcesses()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, RESULT_AAP_PRODUCT, PACKAGING_TYPE_CODE).storeInRepo(faCatchFact, "resultAAPProductPackagingTypeCode");

            faCatchFact.setResultAAPProductPackagingUnitQuantity(getMeasureTypeFromAAPProcess(faCatch.getAppliedAAPProcesses(), PACKAGING_UNIT_QUANTITY));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, RESULT_AAP_PRODUCT, PACKAGING_UNIT_QUANTITY).storeInRepo(faCatchFact, "resultAAPProductPackagingUnitQuantity");

            faCatchFact.setResultAAPProductPackagingUnitAverageWeightMeasure(getMeasureTypeFromAAPProcess(faCatch.getAppliedAAPProcesses(), AVERAGE_WEIGHT_MEASURE));
            xPathUtil.appendWithoutWrapping(partialXPath).append(APPLIED_AAP_PROCESS, RESULT_AAP_PRODUCT, PACKAGING_UNIT_AVERAGE_WEIGHT_MEASURE).storeInRepo(faCatchFact, "resultAAPProductPackagingUnitAverageWeightMeasure");

            faCatchFact.setSpecifiedFLUXLocations(faCatch.getSpecifiedFLUXLocations());
            xPathUtil.appendWithoutWrapping(partialXPath).append(SPECIFIED_FLUX_LOCATION).storeInRepo(faCatchFact, "specifiedFLUXLocations");

            faCatchFact.setSpecifiedFluxLocationRFMOCodeList(getFLUXLocationRFMOCodes(faCatch.getSpecifiedFLUXLocations()));
            xPathUtil.appendWithoutWrapping(partialXPath).append(SPECIFIED_FLUX_LOCATION, REGIONAL_FISHERIES_MANAGEMENT_ORGANIZATION_CODE).storeInRepo(faCatchFact, "specifiedFluxLocationRFMOCodeList");

            if (relatedFLUXLocations != null) {
                faCatchFact.setFluxLocationId(mapFLUXLocationList(relatedFLUXLocations));
                xPathUtil.appendWithoutWrapping(partialXPath).append(RELATED_FLUX_LOCATION, ID).storeInRepo(faCatchFact, "fluxLocationId");
            }

            facts.add(faCatchFact);

            index++;
        }
        return facts;
    }


    public VesselStorageCharacteristicsFact generateFactsForVesselStorageCharacteristic(VesselStorageCharacteristic vesselStorageCharacteristic) {
        if (vesselStorageCharacteristic == null) {
            xPathUtil.clear();
            return null;
        }

        VesselStorageCharacteristicsFact vesselStorageCharacteristicsFact = new VesselStorageCharacteristicsFact();

        vesselStorageCharacteristicsFact.setTypeCodes(mapToCodeType(vesselStorageCharacteristic.getTypeCodes()));
        xPathUtil.append(TYPE_CODE).storeInRepo(vesselStorageCharacteristicsFact, "typeCodes");

        return vesselStorageCharacteristicsFact;
    }


    public List<VesselStorageCharacteristicsFact> generateFactsForVesselStorageCharacteristics(List<VesselStorageCharacteristic> vesselStorageCharacteristics) {
        if (vesselStorageCharacteristics == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }

        List<VesselStorageCharacteristicsFact> list = new ArrayList<>();
        for (VesselStorageCharacteristic vesselStorageCharacteristic : vesselStorageCharacteristics) {
            list.add(generateFactsForVesselStorageCharacteristic(vesselStorageCharacteristic));
        }

        return list;
    }


    public FishingTripFact generateFactForFishingTrip(FishingTrip fishingTrip) {
        if (fishingTrip == null) {
            xPathUtil.clear();
            return null;
        }
        FishingTripFact fishingTripFact = new FishingTripFact();

        String partialXpath = xPathUtil.getValue();

        fishingTripFact.setIds(mapToIdType(fishingTrip.getIDS()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(ID).storeInRepo(fishingTripFact, "ids");

        fishingTripFact.setTypeCode(mapToCodeType(fishingTrip.getTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(fishingTripFact, TYPE_CODE_PROP);

        return fishingTripFact;
    }


    public List<FishingTripFact> generateFactForFishingTrips(List<FishingTrip> fishingTrip, String fishingTripType) {
        if (fishingTrip == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }
        List<FishingTripFact> list = new ArrayList<>();
        String partialXpath = xPathUtil.getValue();
        int index = 1;
        for (FishingTrip fishingTrip_ : fishingTrip) {
            xPathUtil.appendWithoutWrapping(partialXpath).appendWithIndex(fishingTripType, index);
            list.add(generateFactForFishingTrip(fishingTrip_));
            index++;
        }
        return list;
    }


    public FluxLocationFact generateFactForFluxLocation(FLUXLocation fluxLocation) {
        if (fluxLocation == null) {
            xPathUtil.clear();
            return null;
        }

        final String partialXpath = xPathUtil.getValue();

        FluxLocationFact fluxLocationFact = new FluxLocationFact();

        fluxLocationFact.setId(mapToSingleIdType(fluxLocation.getID()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(ID).storeInRepo(fluxLocationFact, "id");

        fluxLocationFact.setApplicableFLUXCharacteristicTypeCode(getApplicableFLUXCharacteristicsTypeCode(fluxLocation.getApplicableFLUXCharacteristics()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(APPLICABLE_FLUX_CHARACTERISTIC, TYPE_CODE).storeInRepo(fluxLocationFact, "applicableFLUXCharacteristicTypeCode");

        fluxLocationFact.setCountryID(mapToSingleIdType(fluxLocation.getCountryID()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(COUNTRY_ID).storeInRepo(fluxLocationFact, "countryID");

        fluxLocationFact.setTypeCode(mapToCodeType(fluxLocation.getTypeCode()));
        xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(fluxLocationFact, TYPE_CODE_PROP);

        fluxLocationFact.setSpecifiedPhysicalFLUXGeographicalCoordinate(fluxLocation.getSpecifiedPhysicalFLUXGeographicalCoordinate());
        xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_PHYSICAL_FLUX_GEOGRAPHICAL_COORDINATE).storeInRepo(fluxLocationFact, "specifiedPhysicalFLUXGeographicalCoordinate");

        fluxLocationFact.setPhysicalStructuredAddress(fluxLocation.getPhysicalStructuredAddress());
        xPathUtil.appendWithoutWrapping(partialXpath).append(PHYSICAL_STRUCTURED_ADDRESS).storeInRepo(fluxLocationFact, "physicalStructuredAddress");

        return fluxLocationFact;
    }


    public List<FluxLocationFact> generateFactsForFluxLocations(List<FLUXLocation> fluxLocation) {
        if (fluxLocation == null) {
            xPathUtil.clear();
            return Collections.emptyList();
        }
        final String partialXpath = xPathUtil.getValue();
        List<FluxLocationFact> list = new ArrayList<>();
        int index = 1;
        for (FLUXLocation fLUXLocation : fluxLocation) {
            xPathUtil.appendWithoutWrapping(partialXpath).appendWithIndex(RELATED_FLUX_LOCATION, index);
            list.add(generateFactForFluxLocation(fLUXLocation));
            index++;
        }
        return list;
    }


    public FluxCharacteristicsFact generateFactForFluxCharacteristics(FLUXCharacteristic fluxCharacteristic) {
        if (fluxCharacteristic == null) {
            return null;
        }

        FluxCharacteristicsFact fluxCharacteristicsFact = new FluxCharacteristicsFact();

        fluxCharacteristicsFact.setTypeCode(fluxCharacteristicTypeCodeValue(fluxCharacteristic));

        return fluxCharacteristicsFact;
    }


    public List<FluxCharacteristicsFact> generateFactsForFluxCharacteristics(List<FLUXCharacteristic> fluxCharacteristic) {
        if (fluxCharacteristic == null) {
            return Collections.emptyList();
        }

        List<FluxCharacteristicsFact> list = new ArrayList<>();
        for (FLUXCharacteristic fLUXCharacteristic : fluxCharacteristic) {
            list.add(generateFactForFluxCharacteristics(fLUXCharacteristic));
        }

        return list;
    }


    public FaDepartureFact generateFactsForFaDeparture(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaDepartureFact faDepartureFact = new FaDepartureFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faDepartureFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faDepartureFact, "fishingActivityTypeCode");
            if (fishingActivity.getRelatedFLUXLocations() != null) {
                List<FLUXLocation> relatedFLUXLocations = fishingActivity.getRelatedFLUXLocations();
                faDepartureFact.setRelatedFLUXLocations(relatedFLUXLocations);
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faDepartureFact, RELATED_FLUX_LOCATIONS_PROP);

                List<CodeType> codeTypes = new ArrayList<>();
                for (FLUXLocation fluxLocation : relatedFLUXLocations) {
                    un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fluxLocation.getTypeCode();
                    codeTypes.add(mapToCodeType(typeCode));
                }
                faDepartureFact.setRelatedFLUXLocationTypeCodes(codeTypes);
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE_PROP).storeInRepo(faDepartureFact, RELATED_FLUX_LOCATIONS_TYPE_CODE_PROP);
            }
            if (fishingActivity.getSpecifiedFishingGears() != null) {

                List<FishingGear> specifiedFishingGears = fishingActivity.getSpecifiedFishingGears();
                List<CodeType> roleCodes = new ArrayList<>();
                for (FishingGear fishingGear : specifiedFishingGears) {
                    roleCodes.addAll(mapToCodeType(fishingGear.getRoleCodes()));
                }
                faDepartureFact.setSpecifiedFishingGearRoleCodeTypes(roleCodes);

                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_GEAR, ROLE_CODE).storeInRepo(faDepartureFact, "specifiedFishingGearRoleCodeTypes");
            }
            List<FACatch> specifiedFACatches = fishingActivity.getSpecifiedFACatches();
            if (specifiedFACatches != null) {
                List<CodeType> codeTypeList = new ArrayList<>();

                for (FACatch faCatch : specifiedFACatches) {
                    codeTypeList.add(mapToCodeType(faCatch.getTypeCode()));
                }
                faDepartureFact.setSpecifiedFACatchCodeTypes(codeTypeList);
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, TYPE_CODE).storeInRepo(faDepartureFact, "specifiedFACatchCodeTypes");
            }
            faDepartureFact.setReasonCode(mapToCodeType(fishingActivity.getReasonCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(REASON_CODE).storeInRepo(faDepartureFact, REASON_CODE_PROP);
            faDepartureFact.setSpecifiedFishingTrip(fishingActivity.getSpecifiedFishingTrip());
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_TRIP).storeInRepo(faDepartureFact, SPECIFIED_FISHING_TRIP_PROP);
            faDepartureFact.setOccurrenceDateTime(getDate(fishingActivity.getOccurrenceDateTime()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(OCCURRENCE_DATE_TIME).storeInRepo(faDepartureFact, OCCURRENCE_DATE_TIME_PROP);
        }
        if (faReportDocument != null) {
            faDepartureFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faDepartureFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faDepartureFact;
    }


    public FaEntryToSeaFact generateFactsForEntryIntoSea(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaEntryToSeaFact faEntryToSeaFact = new FaEntryToSeaFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faEntryToSeaFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faEntryToSeaFact, "fishingActivityTypeCode");
            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faEntryToSeaFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faEntryToSeaFact, RELATED_FLUX_LOCATIONS_PROP);

                faEntryToSeaFact.setRelatedFluxLocationTypeCodes(getFLUXLocationTypeCodes(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faEntryToSeaFact, RELATED_FLUX_LOCATIONS_TYPE_CODE_PROP);

                faEntryToSeaFact.setRelatedFluxLocationIDs(mapFLUXLocationList(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, ID).storeInRepo(faEntryToSeaFact, RELATED_FLUX_LOCATIONS_ID_PROP);
            }
            faEntryToSeaFact.setSpeciesTargetCode(mapToCodeType(fishingActivity.getSpeciesTargetCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIES_TARGET_CODE).storeInRepo(faEntryToSeaFact, SPECIES_TARGET_CODE_PROP);
            faEntryToSeaFact.setReasonCode(mapToCodeType(fishingActivity.getReasonCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(REASON_CODE).storeInRepo(faEntryToSeaFact, REASON_CODE_PROP);
            faEntryToSeaFact.setSpecifiedFACatchesTypeCodes(getFACatchesTypeCodes(fishingActivity.getSpecifiedFACatches()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, TYPE_CODE).storeInRepo(faEntryToSeaFact, SPECIFIED_FA_CATCHES_TYPE_CODE_PROP);
        }
        if (faReportDocument != null) {
            faEntryToSeaFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faEntryToSeaFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faEntryToSeaFact;
    }

    public FaExitFromSeaFact generateFactsForExitArea(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaExitFromSeaFact faExitFromSeaFact = new FaExitFromSeaFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faExitFromSeaFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faExitFromSeaFact, "fishingActivityTypeCode");
            faExitFromSeaFact.setSpecifiedFACatchesTypeCodes(getFACatchesTypeCodes(fishingActivity.getSpecifiedFACatches()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, TYPE_CODE).storeInRepo(faExitFromSeaFact, SPECIFIED_FA_CATCHES_TYPE_CODE_PROP);

            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faExitFromSeaFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faExitFromSeaFact, RELATED_FLUX_LOCATIONS_PROP);

                faExitFromSeaFact.setRelatedFluxLocationTypeCodes(getFLUXLocationTypeCodes(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faExitFromSeaFact, RELATED_FLUX_LOCATIONS_TYPE_CODE_PROP);

                faExitFromSeaFact.setRelatedFluxLocationIDs(mapFLUXLocationList(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FISHING_ACTIVITY).storeInRepo(faExitFromSeaFact, "relatedFishingActivities");

            }
        }
        if (faReportDocument != null) {
            faExitFromSeaFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faExitFromSeaFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faExitFromSeaFact;
    }

    public FaFishingOperationFact generateFactsForFishingOperation(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaFishingOperationFact faFishingOperationFact = new FaFishingOperationFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faFishingOperationFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faFishingOperationFact, "fishingActivityTypeCode");

            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faFishingOperationFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faFishingOperationFact, "relatedFLUXLocations");
            }

            BigDecimal operatQuantity = fishingActivityOperationsQuantityValue(fishingActivity);
            if (operatQuantity != null) {
                faFishingOperationFact.setOperationsQuantity(operatQuantity.toString());
                xPathUtil.appendWithoutWrapping(partialXpath).append(OPERATIONS_QUANTITY).storeInRepo(faFishingOperationFact, "operationsQuantity");
            }
            faFishingOperationFact.setVesselRelatedActivityCode(mapToCodeType(fishingActivity.getVesselRelatedActivityCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(VESSEL_RELATED_ACTIVITY_CODE).storeInRepo(faFishingOperationFact, "vesselRelatedActivityCode");

            if(CollectionUtils.isNotEmpty(fishingActivity.getRelatedFishingActivities())){
                faFishingOperationFact.setRelatedFishingActivities(fishingActivity.getRelatedFishingActivities());
                xPathUtil.appendWithoutWrapping(partialXpath).append(VESSEL_RELATED_ACTIVITY_CODE).storeInRepo(faFishingOperationFact, "vesselRelatedActivityCode");
            }
        }
        if (faReportDocument != null) {
            faFishingOperationFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faFishingOperationFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faFishingOperationFact;
    }


    public FaJointFishingOperationFact generateFactsForJointFishingOperation(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaJointFishingOperationFact faJointFishingOperationFact = new FaJointFishingOperationFact();

        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faJointFishingOperationFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faJointFishingOperationFact, "fishingActivityTypeCode");
            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faJointFishingOperationFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faJointFishingOperationFact, RELATED_FLUX_LOCATIONS_PROP);
            }
        }
        if (faReportDocument != null) {
            faJointFishingOperationFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faJointFishingOperationFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faJointFishingOperationFact;
    }


    public FaRelocationFact generateFactsForRelocation(FishingActivity fishingActivity) {
        if (fishingActivity == null) {
            return null;
        }

        FaRelocationFact faRelocationFact = new FaRelocationFact();

        faRelocationFact.setTypeCode(fishingActivityTypeCodeValue(fishingActivity));

        return faRelocationFact;
    }


    public FaDiscardFact generateFactsForDiscard(FishingActivity fishingActivity) {
        if (fishingActivity == null) {
            return null;
        }

        FaDiscardFact faDiscardFact = new FaDiscardFact();

        faDiscardFact.setTypeCode(fishingActivityTypeCodeValue_(fishingActivity));

        return faDiscardFact;
    }

    public List<CodeType> getFACatchesTypeCodes(List<FACatch> faCatches) {

        List<CodeType> codeTypes = null;

        if (faCatches != null) {
            codeTypes = new ArrayList<>();

            for (FACatch faCatch : faCatches) {
                un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = faCatch.getTypeCode();

                if (typeCode != null) {
                    CodeType codeType = new CodeType();
                    codeType.setListId(typeCode.getListID());
                    codeType.setValue(typeCode.getValue());
                    codeTypes.add(codeType);
                }
            }
        }

        return codeTypes;
    }


    public FaNotificationOfArrivalFact generateFactsForPriorNotificationOfArrival(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaNotificationOfArrivalFact faNotificationOfArrivalFact = new FaNotificationOfArrivalFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faNotificationOfArrivalFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faNotificationOfArrivalFact, "fishingActivityTypeCode");
            List<FLUXLocation> relatedFLUXLocations = fishingActivity.getRelatedFLUXLocations();
            if (relatedFLUXLocations != null) {
                faNotificationOfArrivalFact.setRelatedFLUXLocations(new ArrayList<>(relatedFLUXLocations));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faNotificationOfArrivalFact, RELATED_FLUX_LOCATIONS_PROP);
                List<CodeType> codeTypes = new ArrayList<>();
                for (FLUXLocation location : relatedFLUXLocations) {
                    CodeType codeType = mapToCodeType(location.getTypeCode());
                    if (codeType != null) {
                        codeTypes.add(codeType);
                    }
                }

                faNotificationOfArrivalFact.setRelatedFLUXLocationTypeCodes(codeTypes);
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faNotificationOfArrivalFact , RELATED_FLUX_LOCATIONS_TYPE_CODE_PROP);
            }
            List<FACatch> specifiedFACatches = fishingActivity.getSpecifiedFACatches();
            if (specifiedFACatches != null) {
                faNotificationOfArrivalFact.setSpecifiedFACatches(new ArrayList<>(fishingActivity.getSpecifiedFACatches()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH).storeInRepo(faNotificationOfArrivalFact, "specifiedFACatches");
                List<CodeType> codeTypeList = new ArrayList<>();
                for (FACatch faCatch : specifiedFACatches) {
                    codeTypeList.add(mapToCodeType(faCatch.getTypeCode()));
                }
                faNotificationOfArrivalFact.setSpecifiedFACatchTypeCodes(codeTypeList);
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, TYPE_CODE).storeInRepo(faNotificationOfArrivalFact, "specifiedFACatchTypeCodes");
            }
            faNotificationOfArrivalFact.setReasonCode(mapToCodeType(fishingActivity.getReasonCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(REASON_CODE).storeInRepo(faNotificationOfArrivalFact, REASON_CODE_PROP);

            faNotificationOfArrivalFact.setOccurrenceDateTime(getDate(fishingActivity.getOccurrenceDateTime()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(OCCURRENCE_DATE_TIME).storeInRepo(faNotificationOfArrivalFact , OCCURRENCE_DATE_TIME_PROP);

            FishingTrip specifiedFishingTrip = fishingActivity.getSpecifiedFishingTrip();
            if (specifiedFishingTrip != null) {
                List<DelimitedPeriod> specifiedDelimitedPeriods = specifiedFishingTrip.getSpecifiedDelimitedPeriods();
                faNotificationOfArrivalFact.setDelimitedPeriods(specifiedDelimitedPeriods);

                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_TRIP, SPECIFIED_DELIMITED_PERIOD).storeInRepo(faNotificationOfArrivalFact, "delimitedPeriods");
            }

            List<FLUXCharacteristic> specifiedFLUXCharacteristics = fishingActivity.getSpecifiedFLUXCharacteristics();
            if (CollectionUtils.isNotEmpty(specifiedFLUXCharacteristics)) {

                List<CodeType> codeTypes = new ArrayList<>();
                List<Date> dates = new ArrayList<>();

                for (FLUXCharacteristic characteristic : specifiedFLUXCharacteristics) {
                    if (characteristic != null) {
                        codeTypes.add(mapToCodeType(characteristic.getTypeCode()));
                        Date date = getDate(characteristic.getValueDateTime());
                        if (date != null) {
                            dates.add(date);
                        }

                    }
                }

                faNotificationOfArrivalFact.setSpecifiedFLUXCharacteristicValueDateTimes(dates);
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FLUX_CHARACTERISTIC, "ValueDateTime").storeInRepo(faNotificationOfArrivalFact, "specifiedFLUXCharacteristicValueDateTimes");

                faNotificationOfArrivalFact.setSpecifiedFLUXCharacteristicsTypeCodes(codeTypes);
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FLUX_CHARACTERISTIC, TYPE_CODE).storeInRepo(faNotificationOfArrivalFact, "specifiedFLUXCharacteristicsTypeCodes");

            }
        }
        if (faReportDocument != null) {
            faNotificationOfArrivalFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faNotificationOfArrivalFact, "faReportDocumentTypeCode");
        }

        return faNotificationOfArrivalFact;
    }


    public FaTranshipmentFact generateFactsForTranshipment(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaTranshipmentFact faTranshipmentFact = new FaTranshipmentFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faTranshipmentFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faTranshipmentFact, "fishingActivityTypeCode");
            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faTranshipmentFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faTranshipmentFact, RELATED_FLUX_LOCATIONS_PROP);

                faTranshipmentFact.setFluxLocationTypeCodes(getFLUXLocationTypeCodes(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faTranshipmentFact, "fluxLocationTypeCodes");
            }
            if (fishingActivity.getRelatedVesselTransportMeans() != null) {
                faTranshipmentFact.setRelatedVesselTransportMeans(new ArrayList<>(fishingActivity.getRelatedVesselTransportMeans()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_VESSEL_TRANSPORT_MEANS).storeInRepo(faTranshipmentFact,"relatedVesselTransportMeans");

                faTranshipmentFact.setVesselTransportMeansRoleCodes(getVesselTransportMeansRoleCodes(fishingActivity.getRelatedVesselTransportMeans()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_VESSEL_TRANSPORT_MEANS,ROLE_CODE).storeInRepo(faTranshipmentFact,"vesselTransportMeansRoleCodes");
            }
            List<FACatch> specifiedFACatches = fishingActivity.getSpecifiedFACatches();
            if (specifiedFACatches != null) {
                faTranshipmentFact.setSpecifiedFACatches(new ArrayList<>(specifiedFACatches));
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH).storeInRepo(faTranshipmentFact,"specifiedFACatches");

                faTranshipmentFact.setFaCatchTypeCodes(getCodeTypesFromFaCatch(specifiedFACatches,CODE_TYPE_FOR_FACATCH));
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH,TYPE_CODE).storeInRepo(faTranshipmentFact,"faCatchTypeCodes");

                faTranshipmentFact.setFaCtchSpecifiedFLUXLocations(getFluxLocationsFromFaCatch(specifiedFACatches));
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH,SPECIFIED_FLUX_LOCATION).storeInRepo(faTranshipmentFact,"faCtchSpecifiedFLUXLocations");

                faTranshipmentFact.setFaCtchSpecifiedFLUXLocationsTypeCodes(getCodeTypesFromFaCatch(specifiedFACatches,CODE_TYPE_FOR_FACATCH_FLUXLOCATION));
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH,SPECIFIED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faTranshipmentFact,"faCtchSpecifiedFLUXLocationsTypeCodes");
            }
            if (fishingActivity.getSpecifiedFLUXCharacteristics() != null) {
                faTranshipmentFact.setFluxCharacteristicTypeCodes(getApplicableFLUXCharacteristicsTypeCode(fishingActivity.getSpecifiedFLUXCharacteristics()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(APPLICABLE_FLUX_CHARACTERISTIC,TYPE_CODE).storeInRepo(faTranshipmentFact, "fluxCharacteristicTypeCodes");
            }

        }
        if (faReportDocument != null) {
            faTranshipmentFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faTranshipmentFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faTranshipmentFact;
    }

    private List<FLUXLocation> getFluxLocationsFromFaCatch(List<FACatch> specifiedFACatches) {
        List<FLUXLocation> faCatchFLUXLocations = null;

        for(FACatch faCatch : specifiedFACatches){
            List<FLUXLocation> fluxLocations =faCatch.getSpecifiedFLUXLocations();
            if(CollectionUtils.isNotEmpty(fluxLocations)){
                if(faCatchFLUXLocations == null){
                    faCatchFLUXLocations = new ArrayList<>();
                }
                faCatchFLUXLocations.addAll(fluxLocations);
            }
        }
        return faCatchFLUXLocations;
    }


    public FaArrivalFact generateFactsForDeclarationOfArrival(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            xPathUtil.clear();
            return null;
        }

        FaArrivalFact faArrivalFact = new FaArrivalFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faArrivalFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faArrivalFact, "");
            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faArrivalFact.setRelatedFLUXLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faArrivalFact, RELATED_FLUX_LOCATIONS_PROP);
            }

            faArrivalFact.setFishingGearRoleCodes(getFishingGearRoleCodes(fishingActivity.getSpecifiedFishingGears()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_GEAR, ROLE_CODE).storeInRepo(faArrivalFact, "fishingGearRoleCodes");

            faArrivalFact.setFluxLocationTypeCodes(getFLUXLocationTypeCodes(fishingActivity.getRelatedFLUXLocations()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faArrivalFact, "fluxLocationTypeCodes");

            faArrivalFact.setFishingTripIds(mapToIdType(fishingActivitySpecifiedFishingTripIDS(fishingActivity)));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FISHING_TRIP, ID).storeInRepo(faArrivalFact, "fishingTripIds");

            faArrivalFact.setReasonCode(mapToCodeType(fishingActivity.getReasonCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(REASON_CODE).storeInRepo(faArrivalFact, REASON_CODE_PROP);

            faArrivalFact.setOccurrenceDateTime(fishingActivity.getOccurrenceDateTime());
            xPathUtil.appendWithoutWrapping(partialXpath).append(OCCURRENCE_DATE_TIME).storeInRepo(faArrivalFact, OCCURRENCE_DATE_TIME_PROP);
        }
        if (faReportDocument != null) {
            faArrivalFact.setFaReportTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faArrivalFact, "faReportTypeCode");
        }

        return faArrivalFact;
    }


    public FaQueryFact generateFactsForFaQuery(FAQuery faQuery) {
        if (faQuery == null) {
            return null;
        }

        FaQueryFact faQueryFact = new FaQueryFact();

        faQueryFact.setId(mapToSingleIdType(faQuery.getID()));
        faQueryFact.setTypeCode(mapToCodeType(faQuery.getTypeCode()));
        faQueryFact.setSubmittedDateTime(getDate(faQuery.getSubmittedDateTime()));

        return faQueryFact;
    }


    public FaLandingFact generateFactsForLanding(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaLandingFact faLandingFact = new FaLandingFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faLandingFact.setRelatedFluxLocationTypeCodes(getFLUXLocationTypeCodes(fishingActivity.getRelatedFLUXLocations()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faLandingFact, "relatedFluxLocationTypeCodes");
            if (fishingActivity.getSpecifiedFACatches() != null) {
                faLandingFact.setSpecifiedFaCatches(new ArrayList<>(fishingActivity.getSpecifiedFACatches()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH).storeInRepo(faLandingFact, "specifiedFaCatches");
            }
            faLandingFact.setFishingActivityCodeType(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faLandingFact, "fishingActivityCodeType");
            if (fishingActivity.getRelatedFLUXLocations() != null) {
                faLandingFact.setRelatedFluxLocations(new ArrayList<>(fishingActivity.getRelatedFLUXLocations()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION).storeInRepo(faLandingFact, "relatedFluxLocations");
            }

            faLandingFact.setSpecifiedFaCatchFluxLocationTypeCode(getCodeTypesFromFaCatch(fishingActivity.getSpecifiedFACatches(), CODE_TYPE_FOR_FACATCH_FLUXLOCATION));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, SPECIFIED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faLandingFact, "specifiedFaCatchFluxLocationTypeCode");

            faLandingFact.setSpecifiedFaCatchTypeCode(getCodeTypesFromFaCatch(fishingActivity.getSpecifiedFACatches(), CODE_TYPE_FOR_FACATCH));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, TYPE_CODE).storeInRepo(faLandingFact, "specifiedFaCatchTypeCode");
        }
        if (faReportDocument != null) {
            faLandingFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faLandingFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faLandingFact;
    }


    public FaNotificationOfTranshipmentFact generateFactsForNotificationOfTranshipment(FishingActivity fishingActivity, FAReportDocument faReportDocument) {
        if (fishingActivity == null && faReportDocument == null) {
            return null;
        }

        FaNotificationOfTranshipmentFact faNotificationOfTranshipmentFact = new FaNotificationOfTranshipmentFact();
        String partialXpath = xPathUtil.getValue();
        if (fishingActivity != null) {
            faNotificationOfTranshipmentFact.setFishingActivityTypeCode(mapToCodeType(fishingActivity.getTypeCode()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(TYPE_CODE).storeInRepo(faNotificationOfTranshipmentFact, "fishingActivityTypeCode");

            faNotificationOfTranshipmentFact.setVesselTransportMeansRoleCode(getVesselTransportMeansRoleCodes(fishingActivity.getRelatedVesselTransportMeans()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_VESSEL_TRANSPORT_MEANS, ROLE_CODE).storeInRepo(faNotificationOfTranshipmentFact, "vesselTransportMeansRoleCode");

            faNotificationOfTranshipmentFact.setFluxLocationTypeCode(getFLUXLocationTypeCodes(fishingActivity.getRelatedFLUXLocations()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_FLUX_LOCATION, TYPE_CODE).storeInRepo(faNotificationOfTranshipmentFact, "fluxLocationTypeCode");

            faNotificationOfTranshipmentFact.setFluxCharacteristicValueQuantity(getApplicableFLUXCharacteristicsValueQuantity(fishingActivity.getSpecifiedFLUXCharacteristics()));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FLUX_CHARACTERISTIC, VALUE_QUANTITY).storeInRepo(faNotificationOfTranshipmentFact, "fluxCharacteristicValueQuantity");

            if (fishingActivity.getRelatedVesselTransportMeans() != null) {
                faNotificationOfTranshipmentFact.setRelatedVesselTransportMeans(new ArrayList<>(fishingActivity.getRelatedVesselTransportMeans()));
                xPathUtil.appendWithoutWrapping(partialXpath).append(RELATED_VESSEL_TRANSPORT_MEANS).storeInRepo(faNotificationOfTranshipmentFact, "relatedVesselTransportMeans");
            }

            faNotificationOfTranshipmentFact.setFaCatchTypeCode(getCodeTypesFromFaCatch(fishingActivity.getSpecifiedFACatches(), CODE_TYPE_FOR_FACATCH));
            xPathUtil.appendWithoutWrapping(partialXpath).append(SPECIFIED_FA_CATCH, TYPE_CODE).storeInRepo(faNotificationOfTranshipmentFact, "faCatchTypeCode");
        }
        if (faReportDocument != null) {
            faNotificationOfTranshipmentFact.setFaReportDocumentTypeCode(mapToCodeType(faReportDocument.getTypeCode()));
            xPathUtil.append(FLUXFA_REPORT_MESSAGE, FA_REPORT_DOCUMENT, TYPE_CODE).storeInRepo(faNotificationOfTranshipmentFact, FA_REPORT_DOCUMENT_TYPE_CODE_PROP);
        }

        return faNotificationOfTranshipmentFact;
    }


    public FaQueryParameterFact generateFactsForFaQueryParameter(FAQueryParameter faQueryParameter) {
        if (faQueryParameter == null) {
            return null;
        }

        FaQueryParameterFact faQueryParameterFact = new FaQueryParameterFact();

        faQueryParameterFact.setTypeCode(faQueryParameterTypeCodeValue(faQueryParameter));

        return faQueryParameterFact;
    }


    public FaResponseFact generateFactsForFaResponse(FLUXResponseMessage fluxResponseMessage) {
        if (fluxResponseMessage == null) {
            return null;
        }

        FaResponseFact faResponseFact = new FaResponseFact();

        faResponseFact.setReferencedID(fluxResponseMessageFLUXResponseDocumentReferencedIDValue(fluxResponseMessage));
        xPathUtil.append(FLUX_RESPONSE_MESSAGE, FLUX_RESPONSE_DOCUMENT, REFERENCED_ID).storeInRepo(faResponseFact, "referencedID");

        return faResponseFact;
    }


    public eu.europa.ec.fisheries.uvms.rules.service.business.fact.CodeType mapToCodeType(un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType codeType) {
        if (codeType == null) {
            return null;
        }

        eu.europa.ec.fisheries.uvms.rules.service.business.fact.CodeType codeType_ = new eu.europa.ec.fisheries.uvms.rules.service.business.fact.CodeType();

        codeType_.setListId(codeType.getListID());
        codeType_.setValue(codeType.getValue());

        return codeType_;
    }


    public IdType mapToSingleIdType(IDType idType) {
        if (idType == null) {
            return null;
        }

        IdType idType_ = new IdType();

        idType_.setSchemeId(idType.getSchemeID());
        idType_.setValue(idType.getValue());

        return idType_;
    }


    public List<IdType> mapToIdType(List<IDType> idTypes) {
        if (CollectionUtils.isEmpty(idTypes)) {
            return Collections.emptyList();
        }

        List<IdType> list_ = new ArrayList<>();
        for (IDType iDType : idTypes) {
            list_.add(mapToSingleIdType(iDType));
        }

        return list_;
    }


    public List<eu.europa.ec.fisheries.uvms.rules.service.business.fact.CodeType> mapToCodeType(List<un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType> codeTypes) {
        if (codeTypes == null) {
            return Collections.emptyList();
        }

        List<eu.europa.ec.fisheries.uvms.rules.service.business.fact.CodeType> list__ = new ArrayList<>();
        for (un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType codeType : codeTypes) {
            list__.add(mapToCodeType(codeType));
        }

        return list__;
    }


    public eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType mapToMeasureType(un.unece.uncefact.data.standard.unqualifieddatatype._20.MeasureType measureType) {
        if (measureType == null) {
            return null;
        }

        eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType measureType__ = new eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType();

        measureType__.setValue(measureType.getValue());
        measureType__.setUnitCode(measureType.getUnitCode());

        return measureType__;
    }


    public List<eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType> mapToMeasureType(List<un.unece.uncefact.data.standard.unqualifieddatatype._20.MeasureType> measureTypes) {
        if (measureTypes == null) {
            return Collections.emptyList();
        }

        List<eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType> list = new ArrayList<>();
        for (un.unece.uncefact.data.standard.unqualifieddatatype._20.MeasureType measureType : measureTypes) {
            list.add(mapToMeasureType(measureType));
        }

        return list;
    }


    public eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType mapQuantityTypeToMeasureType(QuantityType quantityType) {
        if (quantityType == null) {
            return null;
        }

        eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType measureType_ = new eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType();

        measureType_.setValue(quantityType.getValue());
        measureType_.setUnitCode(quantityType.getUnitCode());

        return measureType_;
    }


    public List<eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType> mapToQuantityTypeToMeasureType(List<QuantityType> quantityTypes) {
        if (quantityTypes == null) {
            return Collections.emptyList();
        }

        List<eu.europa.ec.fisheries.uvms.rules.service.business.fact.MeasureType> list = new ArrayList<>();
        for (QuantityType quantityType : quantityTypes) {
            list.add(mapQuantityTypeToMeasureType(quantityType));
        }

        return list;
    }

    private DateTimeType faReportDocumentsRelatedFLUXReportDocumentCreationDateTime(FAReportDocument fAReportDocument) {

        if (fAReportDocument == null) {
            return null;
        }
        FLUXReportDocument relatedFLUXReportDocument = fAReportDocument.getRelatedFLUXReportDocument();
        if (relatedFLUXReportDocument == null) {
            return null;
        }
        DateTimeType creationDateTime = relatedFLUXReportDocument.getCreationDateTime();
        if (creationDateTime == null) {
            return null;
        }
        return creationDateTime;
    }

    private List<IDType> faReportDocumentsRelatedFLUXReportDocumentIDS(FAReportDocument fAReportDocument) {

        if (fAReportDocument == null) {
            return Collections.emptyList();
        }
        FLUXReportDocument relatedFLUXReportDocument = fAReportDocument.getRelatedFLUXReportDocument();
        if (relatedFLUXReportDocument == null) {
            return Collections.emptyList();
        }
        List<IDType> iDS = relatedFLUXReportDocument.getIDS();
        if (iDS == null) {
            return Collections.emptyList();
        }
        return iDS;
    }

    private List<IDType> faReportDocumentsRelatedFLUXReportDocumentOwnerFLUXPartyIDS(FAReportDocument fAReportDocument) {

        if (fAReportDocument == null) {
            return Collections.emptyList();
        }
        FLUXReportDocument relatedFLUXReportDocument = fAReportDocument.getRelatedFLUXReportDocument();
        if (relatedFLUXReportDocument == null) {
            return Collections.emptyList();
        }
        FLUXParty ownerFLUXParty = relatedFLUXReportDocument.getOwnerFLUXParty();
        if (ownerFLUXParty == null) {
            return Collections.emptyList();
        }
        List<IDType> iDS = ownerFLUXParty.getIDS();
        if (iDS == null) {
            return Collections.emptyList();
        }
        return iDS;
    }

    private IDType faReportDocumentsRelatedFLUXReportDocumentReferencedID(FAReportDocument fAReportDocument) {

        if (fAReportDocument == null) {
            return null;
        }
        FLUXReportDocument relatedFLUXReportDocument = fAReportDocument.getRelatedFLUXReportDocument();
        if (relatedFLUXReportDocument == null) {
            return null;
        }
        IDType referencedID = relatedFLUXReportDocument.getReferencedID();
        if (referencedID == null) {
            return null;
        }
        return referencedID;
    }

    private List<IDType> faReportDocumentsRelatedFLUXReportDocumentIDS_(FAReportDocument fAReportDocument) {

        if (fAReportDocument == null) {
            return Collections.emptyList();
        }
        FLUXReportDocument relatedFLUXReportDocument = fAReportDocument.getRelatedFLUXReportDocument();
        if (relatedFLUXReportDocument == null) {
            return Collections.emptyList();
        }
        List<IDType> iDS = relatedFLUXReportDocument.getIDS();
        if (iDS == null) {
            return Collections.emptyList();
        }
        return iDS;
    }

    private un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType faReportDocumentsRelatedFLUXReportDocumentPurposeCode(FAReportDocument fAReportDocument) {

        if (fAReportDocument == null) {
            return null;
        }
        FLUXReportDocument relatedFLUXReportDocument = fAReportDocument.getRelatedFLUXReportDocument();
        if (relatedFLUXReportDocument == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType purposeCode = relatedFLUXReportDocument.getPurposeCode();
        if (purposeCode == null) {
            return null;
        }
        return purposeCode;
    }

    private DateTimeType fluxfaReportMessageFLUXReportDocumentCreationDateTime(FLUXFAReportMessage fLUXFAReportMessage) {

        if (fLUXFAReportMessage == null) {
            return null;
        }
        FLUXReportDocument fLUXReportDocument = fLUXFAReportMessage.getFLUXReportDocument();
        if (fLUXReportDocument == null) {
            return null;
        }
        DateTimeType creationDateTime = fLUXReportDocument.getCreationDateTime();
        if (creationDateTime == null) {
            return null;
        }
        return creationDateTime;
    }

    private List<IDType> fluxfaReportMessageFLUXReportDocumentIDS(FLUXFAReportMessage fLUXFAReportMessage) {

        if (fLUXFAReportMessage == null) {
            return Collections.emptyList();
        }
        FLUXReportDocument fLUXReportDocument = fLUXFAReportMessage.getFLUXReportDocument();
        if (fLUXReportDocument == null) {
            return Collections.emptyList();
        }
        List<IDType> iDS = fLUXReportDocument.getIDS();
        if (iDS == null) {
            return Collections.emptyList();
        }
        return iDS;
    }

    private IDType fluxfaReportMessageFLUXReportDocumentReferencedID(FLUXFAReportMessage fLUXFAReportMessage) {

        if (fLUXFAReportMessage == null) {
            return null;
        }
        FLUXReportDocument fLUXReportDocument = fLUXFAReportMessage.getFLUXReportDocument();
        if (fLUXReportDocument == null) {
            return null;
        }
        IDType referencedID = fLUXReportDocument.getReferencedID();
        if (referencedID == null) {
            return null;
        }
        return referencedID;
    }

    private List<IDType> fluxfaReportMessageFLUXReportDocumentOwnerFLUXPartyIDS(FLUXFAReportMessage fLUXFAReportMessage) {

        if (fLUXFAReportMessage == null) {
            return Collections.emptyList();
        }
        FLUXReportDocument fLUXReportDocument = fLUXFAReportMessage.getFLUXReportDocument();
        if (fLUXReportDocument == null) {
            return Collections.emptyList();
        }
        FLUXParty ownerFLUXParty = fLUXReportDocument.getOwnerFLUXParty();
        if (ownerFLUXParty == null) {
            return Collections.emptyList();
        }
        List<IDType> iDS = ownerFLUXParty.getIDS();
        if (iDS == null) {
            return Collections.emptyList();
        }
        return iDS;
    }

    private un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType fluxfaReportMessageFLUXReportDocumentPurposeCode(FLUXFAReportMessage fLUXFAReportMessage) {

        if (fLUXFAReportMessage == null) {
            return null;
        }
        FLUXReportDocument fLUXReportDocument = fLUXFAReportMessage.getFLUXReportDocument();
        if (fLUXReportDocument == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType purposeCode = fLUXReportDocument.getPurposeCode();
        if (purposeCode == null) {
            return null;
        }
        return purposeCode;
    }

    public IDType vesselTransportMeanRegistrationVesselCountryID(VesselTransportMeans vesselTransportMeans) {
        if (vesselTransportMeans == null) {
            return null;
        }
        VesselCountry registrationVesselCountry = vesselTransportMeans.getRegistrationVesselCountry();
        if (registrationVesselCountry == null) {
            return null;
        }
        IDType iD = registrationVesselCountry.getID();
        if (iD == null) {
            return null;
        }
        return iD;
    }

    public String structuredAddressPostcodeCodeValue(StructuredAddress structuredAddress) {

        if (structuredAddress == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType postcodeCode = structuredAddress.getPostcodeCode();
        if (postcodeCode == null) {
            return null;
        }
        String value = postcodeCode.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private IdType structuredAddressCountryIDValue(StructuredAddress structuredAddress) {

        if (structuredAddress == null) {
            return null;
        }
        IDType countryID = structuredAddress.getCountryID();

        if (countryID == null) {
            return null;
        }

        String value = countryID.getValue();
        String schemeID = countryID.getSchemeID();

        if (schemeID == null && value == null) {
            return null;
        }

        IdType idType = new IdType();
        idType.setSchemeId(schemeID);
        idType.setValue(value);

        return idType;
    }

    private String structuredAddressCityNameValue(StructuredAddress structuredAddress) {

        if (structuredAddress == null) {
            return null;
        }
        TextType cityName = structuredAddress.getCityName();
        if (cityName == null) {
            return null;
        }
        String value = cityName.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private String structuredAddressStreetNameValue(StructuredAddress structuredAddress) {

        if (structuredAddress == null) {
            return null;
        }
        TextType streetName = structuredAddress.getStreetName();
        if (streetName == null) {
            return null;
        }
        String value = streetName.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private String structuredAddressPlotIdentificationValue(StructuredAddress structuredAddress) {

        if (structuredAddress == null) {
            return null;
        }
        TextType plotIdentification = structuredAddress.getPlotIdentification();
        if (plotIdentification == null) {
            return null;
        }
        String value = plotIdentification.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private String gearProblemTypeCodeValue(GearProblem gearProblem) {

        if (gearProblem == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = gearProblem.getTypeCode();
        if (typeCode == null) {
            return null;
        }
        String value = typeCode.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private List<un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType> faCatchesSpecifiedSizeDistributionClassCodes(FACatch fACatch) {

        if (fACatch == null) {
            return Collections.emptyList();
        }
        SizeDistribution specifiedSizeDistribution = fACatch.getSpecifiedSizeDistribution();
        if (specifiedSizeDistribution == null) {
            return Collections.emptyList();
        }
        List<un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType> classCodes = specifiedSizeDistribution.getClassCodes();
        if (classCodes == null) {
            return Collections.emptyList();
        }
        return classCodes;
    }

    private String fluxCharacteristicTypeCodeValue(FLUXCharacteristic fLUXCharacteristic) {

        if (fLUXCharacteristic == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fLUXCharacteristic.getTypeCode();
        if (typeCode == null) {
            return null;
        }
        String value = typeCode.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private BigDecimal fishingActivityOperationsQuantityValue(FishingActivity fishingActivity) {

        if (fishingActivity == null) {
            return null;
        }
        QuantityType operationsQuantity = fishingActivity.getOperationsQuantity();
        if (operationsQuantity == null) {
            return null;
        }
        BigDecimal value = operationsQuantity.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private String fishingActivityTypeCodeValue(FishingActivity fishingActivity) {

        if (fishingActivity == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fishingActivity.getTypeCode();
        if (typeCode == null) {
            return null;
        }
        String value = typeCode.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private String fishingActivityTypeCodeValue_(FishingActivity fishingActivity) {

        if (fishingActivity == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fishingActivity.getTypeCode();
        if (typeCode == null) {
            return null;
        }
        String value = typeCode.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private List<IDType> fishingActivitySpecifiedFishingTripIDS(FishingActivity fishingActivity) {

        if (fishingActivity == null) {
            return Collections.emptyList();
        }
        FishingTrip specifiedFishingTrip = fishingActivity.getSpecifiedFishingTrip();
        if (specifiedFishingTrip == null) {
            return Collections.emptyList();
        }
        List<IDType> iDS = specifiedFishingTrip.getIDS();
        if (iDS == null) {
            return Collections.emptyList();
        }
        return iDS;
    }

    public String faQueryParameterTypeCodeValue(FAQueryParameter fAQueryParameter) {

        if (fAQueryParameter == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType typeCode = fAQueryParameter.getTypeCode();
        if (typeCode == null) {
            return null;
        }
        String value = typeCode.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private String fluxResponseMessageFLUXResponseDocumentReferencedIDValue(FLUXResponseMessage fLUXResponseMessage) {

        if (fLUXResponseMessage == null) {
            return null;
        }
        FLUXResponseDocument fLUXResponseDocument = fLUXResponseMessage.getFLUXResponseDocument();
        if (fLUXResponseDocument == null) {
            return null;
        }
        IDType referencedID = fLUXResponseDocument.getReferencedID();
        if (referencedID == null) {
            return null;
        }
        String value = referencedID.getValue();
        if (value == null) {
            return null;
        }
        return value;
    }

    private un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType faCatchSpecifiedSizeDistributionCategoryCode(FACatch fACatch) {

        if (fACatch == null) {
            return null;
        }
        SizeDistribution specifiedSizeDistribution = fACatch.getSpecifiedSizeDistribution();
        if (specifiedSizeDistribution == null) {
            return null;
        }
        un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType categoryCode = specifiedSizeDistribution.getCategoryCode();
        if (categoryCode == null) {
            return null;
        }
        return categoryCode;
    }


    public static List<NumericType> mapAAPProcessList(List<AAPProcess> aapProcesses) {
        List<NumericType> numericTypeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aapProcesses)) {
            for (AAPProcess aapProcess : aapProcesses) {
                un.unece.uncefact.data.standard.unqualifieddatatype._20.NumericType conversionFactorNumeric = aapProcess.getConversionFactorNumeric();
                if (conversionFactorNumeric != null) {
                    NumericType numericType = new NumericType();
                    numericType.setValue(conversionFactorNumeric.getValue());
                    numericType.setFormat(conversionFactorNumeric.getFormat());
                    numericTypeList.add(numericType);
                }
            }
        }
        return numericTypeList;
    }

    public static List<IdType> mapFLUXLocationList(List<FLUXLocation> fluxLocations) {
        List<IdType> idTypeList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fluxLocations)) {
            for (FLUXLocation fluxLocation : fluxLocations) {
                IDType id = fluxLocation.getID();
                if (id != null) {
                    IdType idType = new IdType();
                    idType.setSchemeId(fluxLocation.getID().getSchemeID());
                    idType.setValue(fluxLocation.getID().getValue());
                    idTypeList.add(idType);
                }
            }
        }
        return idTypeList;
    }


    public static boolean isDatePresent(DateTimeType dateTimeType) {
        return (dateTimeType != null);
    }

    public List<MeasureType> mapDurationMeasure(List<DelimitedPeriod> delimitedPeriods) {
        List<MeasureType> measureTypes = null;
        if (CollectionUtils.isNotEmpty(delimitedPeriods)) {
            measureTypes = new ArrayList<>();
            for (DelimitedPeriod delimitedPeriod : delimitedPeriods) {
                if (delimitedPeriod.getDurationMeasure() != null) {
                    measureTypes.add(mapToMeasureType(delimitedPeriod.getDurationMeasure()));
                }
            }
        }
        return measureTypes;
    }

    public static List<FishingTrip> mapRelatedFishingTrips(List<FishingActivity> relatedFishingActivities) {
        List<FishingTrip> fishingTrips = null;
        if (CollectionUtils.isNotEmpty(relatedFishingActivities)) {
            fishingTrips = new ArrayList<>();
            for (FishingActivity fishingActivity : relatedFishingActivities) {
                if (fishingActivity.getSpecifiedFishingTrip() != null) {
                    fishingTrips.add(fishingActivity.getSpecifiedFishingTrip());
                }
            }
        }
        return fishingTrips;
    }

    public static List<FLUXLocation> getFluxLocations(List<FishingActivity> relatedFishingActivities) {
        List<FLUXLocation> fluxLocations = null;
        if (CollectionUtils.isNotEmpty(relatedFishingActivities)) {
            fluxLocations = new ArrayList<>();
            for (FishingActivity activity : relatedFishingActivities) {
                if (activity.getRelatedFLUXLocations() != null) {
                    fluxLocations.addAll(activity.getRelatedFLUXLocations());
                }
            }
        }
        return fluxLocations;
    }

    public static List<DelimitedPeriod> getDelimitedPeriod(List<FishingActivity> relatedFishingActivities) {
        List<DelimitedPeriod> delimitedPeriod = null;
        if (CollectionUtils.isNotEmpty(relatedFishingActivities)) {
            delimitedPeriod = new ArrayList<>();
            for (FishingActivity activity : relatedFishingActivities) {
                if (activity.getSpecifiedDelimitedPeriods() != null) {
                    delimitedPeriod.addAll(activity.getSpecifiedDelimitedPeriods());
                }
            }
        }
        return delimitedPeriod;
    }

    public List<CodeType> mapFromContactPartyToCodeType(List<ContactParty> contactPartyList) {
        List<CodeType> codeTypes = null;

        if (!CollectionUtils.isEmpty(contactPartyList)) {

            codeTypes = org.mapstruct.ap.internal.util.Collections.newArrayList();

            for (ContactParty contactParty : contactPartyList) {
                codeTypes.addAll(mapToCodeType(contactParty.getRoleCodes()));
            }
        }

        return codeTypes;
    }

    public static String getUUID(List<IDType> ids) {
        if (ids != null) {
            for (IDType idType : ids) {
                if (idType.getSchemeID().equalsIgnoreCase("UUID")) {
                    return idType.getValue();
                }
            }
        }
        return null;
    }

    public static List<ContactPerson> mapToContactPersonList(List<ContactParty> contactPartyList) {
        List<ContactPerson> contactPersonList = null;

        if (CollectionUtils.isNotEmpty(contactPartyList)) {

            contactPersonList = org.mapstruct.ap.internal.util.Collections.newArrayList();

            for (ContactParty contactParty : contactPartyList) {
                contactPersonList.addAll(contactParty.getSpecifiedContactPersons());
            }
        }

        return contactPersonList;
    }

    public static Date getDate(DateTimeType dateTimeType) {
        Date date = null;
        if (dateTimeType != null) {
            try {
                if (dateTimeType.getDateTime() != null) {
                    date = dateTimeType.getDateTime().toGregorianCalendar().getTime();
                } else {
                    String format = dateTimeType.getDateTimeString().getFormat();
                    String value = dateTimeType.getDateTimeString().getValue();
                    date = new SimpleDateFormat(format).parse(value);
                }
            } catch (Exception e) {
                log.debug("Error while trying to parse dateTimeType", e);
            }
        }

        return date;
    }

    public static List<AAPProduct> getAppliedProcessAAPProducts(List<AAPProcess> appliedAAPProcesses) {
        if (CollectionUtils.isEmpty(appliedAAPProcesses)) {
            return java.util.Collections.emptyList();
        }
        List<AAPProduct> aapProducts = new ArrayList<>();

        for (AAPProcess aapProcess : appliedAAPProcesses) {
            if (CollectionUtils.isNotEmpty(aapProcess.getResultAAPProducts())) {
                aapProducts.addAll(aapProcess.getResultAAPProducts());
            }
        }
        return aapProducts;
    }

    /**
     * Extract List<MeasureType> from AAPProduct. List will be created from different attributes of AAPProduct based on parameter methodToChoose.
     *
     * @param appliedAAPProcesses
     * @param methodToChoose
     * @return
     */
    public List<MeasureType> getMeasureTypeFromAAPProcess(List<AAPProcess> appliedAAPProcesses, String methodToChoose) {
        if (CollectionUtils.isEmpty(appliedAAPProcesses)) {
            return java.util.Collections.emptyList();
        }
        List<MeasureType> measureTypes = new ArrayList<>();
        for (AAPProcess aapProcess : appliedAAPProcesses) {
            if (CollectionUtils.isNotEmpty(aapProcess.getResultAAPProducts())) {
                mapAapProductToMeasureType(methodToChoose, measureTypes, aapProcess);
            }
        }
        return measureTypes;
    }

    private void mapAapProductToMeasureType(String methodToChoose, List<MeasureType> measureTypes, AAPProcess aapProcess) {
        for (AAPProduct aapProduct : aapProcess.getResultAAPProducts()) {
            switch (methodToChoose) {
                case PACKAGING_UNIT_QUANTITY:
                    if (aapProduct.getPackagingUnitQuantity() != null) {
                        measureTypes.add(mapQuantityTypeToMeasureType(aapProduct.getPackagingUnitQuantity()));
                    }
                    break;
                case AVERAGE_WEIGHT_MEASURE:
                    if (aapProduct.getPackagingUnitAverageWeightMeasure() != null) {
                        measureTypes.add(mapToMeasureType(aapProduct.getPackagingUnitAverageWeightMeasure()));
                    }
                    break;
                case WEIGHT_MEASURE:
                    if (aapProduct.getWeightMeasure() != null) {
                        measureTypes.add(mapToMeasureType(aapProduct.getWeightMeasure()));
                    }
                    break;
                case UNIT_QUANTITY:
                    if (aapProduct.getUnitQuantity() != null) {
                        measureTypes.add(mapQuantityTypeToMeasureType(aapProduct.getUnitQuantity()));
                    }
                    break;
            }
        }
    }

    public List<CodeType> getAAPProductPackagingTypeCode(List<AAPProcess> appliedAAPProcesses) {
        if (CollectionUtils.isEmpty(appliedAAPProcesses)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (AAPProcess aapProcess : appliedAAPProcesses) {
            if (CollectionUtils.isNotEmpty(aapProcess.getResultAAPProducts())) {
                for (AAPProduct aapProduct : aapProcess.getResultAAPProducts()) {
                    if (aapProduct.getPackagingTypeCode() != null) {
                        codeTypes.add(mapToCodeType(aapProduct.getPackagingTypeCode()));
                    }
                }
            }
        }
        return codeTypes;
    }

    public List<CodeType> getAppliedProcessTypeCodes(List<AAPProcess> appliedAAPProcesses) {
        if (CollectionUtils.isEmpty(appliedAAPProcesses)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();

        for (AAPProcess aapProcess : appliedAAPProcesses) {
            if (CollectionUtils.isNotEmpty(aapProcess.getTypeCodes())) {
                codeTypes.addAll(mapToCodeType(aapProcess.getTypeCodes()));
            }
        }
        return codeTypes;
    }

    public List<CodeType> getApplicableFLUXCharacteristicsTypeCode(List<FLUXCharacteristic> fluxCharacteristics) {
        if (CollectionUtils.isEmpty(fluxCharacteristics)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (FLUXCharacteristic fluxCharacteristic : fluxCharacteristics) {
            if (fluxCharacteristic.getTypeCode() != null) {
                codeTypes.add(mapToCodeType(fluxCharacteristic.getTypeCode()));
            }
        }
        return codeTypes;
    }

    public List<MeasureType> getApplicableFLUXCharacteristicsValueQuantity(List<FLUXCharacteristic> fluxCharacteristics) {
        if (CollectionUtils.isEmpty(fluxCharacteristics)) {
            return java.util.Collections.emptyList();
        }
        List<MeasureType> measureTypes = new ArrayList<>();
        for (FLUXCharacteristic fluxCharacteristic : fluxCharacteristics) {
            if (fluxCharacteristic.getValueQuantity() != null) {
                measureTypes.add(mapQuantityTypeToMeasureType(fluxCharacteristic.getValueQuantity()));
            }
        }
        return measureTypes;
    }

    public List<CodeType> getFLUXLocationTypeCodes(List<FLUXLocation> fluxLocations) {
        if (CollectionUtils.isEmpty(fluxLocations)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (FLUXLocation fluxLocation : fluxLocations) {
            if (fluxLocation.getTypeCode() != null) {
                codeTypes.add(mapToCodeType(fluxLocation.getTypeCode()));
            }
        }
        return codeTypes;
    }

    public List<CodeType> getFLUXLocationRFMOCodes(List<FLUXLocation> fluxLocations) {
        if (CollectionUtils.isEmpty(fluxLocations)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (FLUXLocation fluxLocation : fluxLocations) {
            if (fluxLocation.getRegionalFisheriesManagementOrganizationCode() != null) {
                codeTypes.add(mapToCodeType(fluxLocation.getRegionalFisheriesManagementOrganizationCode()));
            }
        }
        return codeTypes;
    }

    public List<CodeType> getFishingGearRoleCodes(List<FishingGear> fishingGears) {
        if (CollectionUtils.isEmpty(fishingGears)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (FishingGear fishingGear : fishingGears) {

            if (CollectionUtils.isNotEmpty(fishingGear.getRoleCodes())) {
                codeTypes.addAll(mapToCodeType(fishingGear.getRoleCodes()));
            }
        }
        return codeTypes;
    }

    public List<CodeType> getVesselTransportMeansRoleCodes(List<VesselTransportMeans> vesselTransportMeanses) {
        if (CollectionUtils.isEmpty(vesselTransportMeanses)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (VesselTransportMeans vesselTransportMeans : vesselTransportMeanses) {

            if (vesselTransportMeans.getRoleCode() != null) {
                codeTypes.add(mapToCodeType(vesselTransportMeans.getRoleCode()));
            }
        }
        return codeTypes;
    }

    /**
     * Fetch List<CodeType> from FACatch. CodeType List will be created from FACatch based on parameter methodToChoose
     * i.e code type for FACatch or code type for specified fluxlocation
     *
     * @param faCatch // * @param methodToChoose
     * @return
     */
    public List<CodeType> getCodeTypesFromFaCatch(List<FACatch> faCatch, String methodToChoose) {
        if (CollectionUtils.isEmpty(faCatch)) {
            return java.util.Collections.emptyList();
        }
        List<CodeType> codeTypes = new ArrayList<>();
        for (FACatch faCatches : faCatch) {
            if (CODE_TYPE_FOR_FACATCH.equals(methodToChoose)) {
                if (faCatches.getTypeCode() != null) {
                    codeTypes.add(mapToCodeType(faCatches.getTypeCode()));
                }
            } else if (CODE_TYPE_FOR_FACATCH_FLUXLOCATION.equals(methodToChoose)) {
                mapSpecifiedFluxLocationsCodeTypeList(codeTypes, faCatches);
            }
        }
        return codeTypes;
    }

    private void mapSpecifiedFluxLocationsCodeTypeList(List<CodeType> codeTypes, FACatch faCatches) {
        if (CollectionUtils.isNotEmpty(faCatches.getSpecifiedFLUXLocations())) {
            for (FLUXLocation specifiedFluxLocation : faCatches.getSpecifiedFLUXLocations()) {
                if (specifiedFluxLocation.getTypeCode() != null) {
                    codeTypes.add(mapToCodeType(specifiedFluxLocation.getTypeCode()));
                }
            }
        }
    }

    public static List<String> getIds(FLUXReportDocument fluxReportDocument) {
        if (fluxReportDocument == null) {
            return java.util.Collections.emptyList();
        }
        List<IDType> idTypes = fluxReportDocument.getIDS();
        List<String> ids = new ArrayList<>();
        for (IDType idType : idTypes) {
            ids.add(idType.getValue().concat("_").concat(idType.getSchemeID()));
        }
        return ids;
    }

    public void setxPathUtil(XPathStringWrapper xPathUtil_) {
        this.xPathUtil = xPathUtil_;
    }
    public void setAssetList(List<IdTypeWithFlagState> assetList) {
        this.assetList = assetList;
    }
    public void setNonUniqueIdsMap(Map<ActivityTableType, List<IdType>> nonUniqueIdsMap) {
        this.nonUniqueIdsMap = nonUniqueIdsMap;
    }

    public Map<ActivityTableType, List<IdType>> getNonUniqueIdsMap() {
        return nonUniqueIdsMap;
    }
}