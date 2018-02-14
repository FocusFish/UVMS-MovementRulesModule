/*
 *
 * Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries © European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package eu.europa.ec.fisheries.uvms.rules.service.business.fact;

import eu.europa.ec.fisheries.schema.rules.template.v1.FactType;
import eu.europa.ec.fisheries.uvms.rules.service.business.AbstractFact;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FACatch;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXCharacteristic;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXLocation;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.VesselTransportMeans;

/**
 * @autor padhyad
 * @author Gregory Rinaldi
 */
@Slf4j
public class FaTranshipmentFact extends AbstractFact {

    private CodeType fishingActivityTypeCode;
    private CodeType faReportDocumentTypeCode;
    private List<FLUXLocation> relatedFLUXLocations;
    private List<VesselTransportMeans> relatedVesselTransportMeans;
    private List<FACatch> specifiedFACatches;
    private List<CodeType> faCatchTypeCodes ;
    private List<CodeType> fluxLocationTypeCodes;
    private List<CodeType> vesselTransportMeansRoleCodes;
    private List<FLUXLocation> faCtchSpecifiedFLUXLocations;
    private List<CodeType> faCtchSpecifiedFLUXLocationsTypeCodes;
    private List<CodeType> fluxCharacteristicTypeCodes;
    private List<CodeType> facatchSpeciesCode;
    private List<FLUXCharacteristic> specifiedFLUXCharacteristics;
    private List<IdType> specifiedFlCharSpecifiedLocatIDs;

    public FaTranshipmentFact() {
        setFactType();
    }

    /**
     * This method checks if there are FLUXLocations present for every FaCatch. If not, it will return false.
     * @param specifiedFACatches
     * @return
     */
    public boolean checkFLUXLocationForFaCatch(List<FACatch> specifiedFACatches){
        if(CollectionUtils.isEmpty(specifiedFACatches)) {
            return false;
        }
        for(FACatch faCatch : specifiedFACatches){
            if(CollectionUtils.isEmpty(faCatch.getSpecifiedFLUXLocations())){
                return false;
            }
        }
        return true;
    }

    /**
     * This method will check Rule for SpecifiedFLUXCharacteristic. Rule will be checked for passed fluxLocationSchemeId
     * @param specifiedFLUXCharacteristics
     * @param fluxLocationSchemeId
     * @return
     * @throws Exception
     */
    public boolean checkRuleForSpecifiedFLUXCharacteristic(List<FLUXCharacteristic> specifiedFLUXCharacteristics,String fluxLocationSchemeId) throws Exception {
        if(fluxLocationSchemeId ==null){
            log.error("Please provide value for fluxLocationSchemeId");
            throw new Exception("Please provide value for fluxLocationSchemeId");
        }
        if(CollectionUtils.isEmpty(specifiedFLUXCharacteristics)) {
            return false;
        }
        for(FLUXCharacteristic fluxCharacteristic : specifiedFLUXCharacteristics){
            if(fluxCharacteristic.getTypeCode()!=null && fluxCharacteristic.getTypeCode().getValue().equals("DESTINATION_LOCATION")){
                List<FLUXLocation> fluxLocations=  fluxCharacteristic.getSpecifiedFLUXLocations();
                if(CollectionUtils.isNotEmpty(fluxLocations)){
                    for(FLUXLocation fluxLocation : fluxLocations){
                        if(fluxLocation.getTypeCode() !=null && "LOCATION".equals(fluxLocation.getTypeCode().getValue()) && fluxLocation.getID()!=null &&
                                fluxLocationSchemeId.equals(fluxLocation.getID().getSchemeID()) && isPresentInMDRList(fluxLocationSchemeId,fluxLocation.getID().getValue())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setFactType() {
        this.factType = FactType.FA_TRANSHIPMENT;
    }


    public CodeType getFishingActivityTypeCode() {
        return fishingActivityTypeCode;
    }
    public void setFishingActivityTypeCode(CodeType fishingActivityTypeCode) {
        this.fishingActivityTypeCode = fishingActivityTypeCode;
    }
    public CodeType getFaReportDocumentTypeCode() {
        return faReportDocumentTypeCode;
    }
    public void setFaReportDocumentTypeCode(CodeType faReportDocumentTypeCode) {
        this.faReportDocumentTypeCode = faReportDocumentTypeCode;
    }
    public List<FLUXLocation> getRelatedFLUXLocations() {
        return relatedFLUXLocations;
    }
    public void setRelatedFLUXLocations(List<FLUXLocation> relatedFLUXLocations) {
        this.relatedFLUXLocations = relatedFLUXLocations;
    }
    public List<VesselTransportMeans> getRelatedVesselTransportMeans() {
        return relatedVesselTransportMeans;
    }

    public void setRelatedVesselTransportMeans(List<VesselTransportMeans> relatedVesselTransportMeans) {
        this.relatedVesselTransportMeans = relatedVesselTransportMeans;
    }
    public List<FACatch> getSpecifiedFACatches() {
        return specifiedFACatches;
    }
    public void setSpecifiedFACatches(List<FACatch> specifiedFACatches) {
        this.specifiedFACatches = specifiedFACatches;
    }
    public List<CodeType> getFaCatchTypeCodes() {
        return faCatchTypeCodes;
    }
    public void setFaCatchTypeCodes(List<CodeType> faCatchTypeCodes) {
        this.faCatchTypeCodes = faCatchTypeCodes;
    }
    public List<CodeType> getFluxLocationTypeCodes() {
        return fluxLocationTypeCodes;
    }
    public void setFluxLocationTypeCodes(List<CodeType> fluxLocationTypeCodes) {
        this.fluxLocationTypeCodes = fluxLocationTypeCodes;
    }
    public List<CodeType> getVesselTransportMeansRoleCodes() {
        return vesselTransportMeansRoleCodes;
    }
    public void setVesselTransportMeansRoleCodes(List<CodeType> vesselTransportMeansRoleCodes) {
        this.vesselTransportMeansRoleCodes = vesselTransportMeansRoleCodes;
    }
    public List<FLUXLocation> getFaCtchSpecifiedFLUXLocations() {
        return faCtchSpecifiedFLUXLocations;
    }
    public void setFaCtchSpecifiedFLUXLocations(List<FLUXLocation> faCtchSpecifiedFLUXLocations) {
        this.faCtchSpecifiedFLUXLocations = faCtchSpecifiedFLUXLocations;
    }
    public List<CodeType> getFaCtchSpecifiedFLUXLocationsTypeCodes() {
        return faCtchSpecifiedFLUXLocationsTypeCodes;
    }
    public void setFaCtchSpecifiedFLUXLocationsTypeCodes(List<CodeType> faCtchSpecifiedFLUXLocationsTypeCodes) {
        this.faCtchSpecifiedFLUXLocationsTypeCodes = faCtchSpecifiedFLUXLocationsTypeCodes;
    }
    public List<CodeType> getFluxCharacteristicTypeCodes() {
        return fluxCharacteristicTypeCodes;
    }
    public void setFluxCharacteristicTypeCodes(List<CodeType> fluxCharacteristicTypeCodes) {
        this.fluxCharacteristicTypeCodes = fluxCharacteristicTypeCodes;
    }
    public List<FLUXCharacteristic> getSpecifiedFLUXCharacteristics() {
        return specifiedFLUXCharacteristics;
    }
    public void setSpecifiedFLUXCharacteristics(List<FLUXCharacteristic> specifiedFLUXCharacteristics) {
        this.specifiedFLUXCharacteristics = specifiedFLUXCharacteristics;
    }
    public List<CodeType> getFaCatchSpeciesCodes() {
        return facatchSpeciesCode;
    }
    public void setFaCatchSpeciesCodes(List<CodeType> facatchSpeciesCode) {
        this.facatchSpeciesCode = facatchSpeciesCode;
    }
    public List<CodeType> getFacatchSpeciesCode() {
        return facatchSpeciesCode;
    }
    public void setFacatchSpeciesCode(List<CodeType> facatchSpeciesCode) {
        this.facatchSpeciesCode = facatchSpeciesCode;
    }
    public List<IdType> getSpecifiedFlCharSpecifiedLocatIDs() {
        return specifiedFlCharSpecifiedLocatIDs;
    }
    public void setSpecifiedFlCharSpecifiedLocatIDs(List<IdType> specifiedFlCharSpecifiedLocatIDs) {
        this.specifiedFlCharSpecifiedLocatIDs = specifiedFlCharSpecifiedLocatIDs;
    }
}
