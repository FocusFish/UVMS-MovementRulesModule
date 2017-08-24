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

import eu.europa.ec.fisheries.remote.RulesDomainModel;
import eu.europa.ec.fisheries.schema.rules.template.v1.FactType;
import eu.europa.ec.fisheries.uvms.rules.model.exception.RulesModelException;
import eu.europa.ec.fisheries.uvms.rules.service.business.AbstractFact;
import eu.europa.ec.fisheries.uvms.rules.service.constants.FactConstants;
import eu.europa.ec.fisheries.uvms.rules.service.constants.ServiceConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.GearCharacteristic;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;

/**
 * Created by padhyad on 4/19/2017.
 */
@Slf4j
public class FishingGearFact extends AbstractFact {

    private RulesDomainModel rulesDomainModel;

    private CodeType typeCode;

    private List<CodeType> roleCodes;

    private List<GearCharacteristic> applicableGearCharacteristics;

    private boolean fishingActivity;

    public FishingGearFact() {
        setFactType();
        initDomainModel();
    }

    @Override
    public void setFactType() {
        this.factType = FactType.FISHING_GEAR;
    }

    public CodeType getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(CodeType typeCode) {
        this.typeCode = typeCode;
    }

    public List<CodeType> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<CodeType> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public List<GearCharacteristic> getApplicableGearCharacteristics() {
        return applicableGearCharacteristics;
    }

    public void setApplicableGearCharacteristics(List<GearCharacteristic> applicableGearCharacteristics) {
        this.applicableGearCharacteristics = applicableGearCharacteristics;
    }

    public boolean isFishingActivity() {
        return fishingActivity;
    }

    public void setFishingActivity(boolean fishingActivity) {
        this.fishingActivity = fishingActivity;
    }

    public void setDomainModel(RulesDomainModel rulesDomainModel) {
        this.rulesDomainModel = rulesDomainModel;
    }

    public boolean isRequiredGearCharacteristicsPresent(CodeType fishingGearTypeCode) {
        if (fishingGearTypeCode == null || StringUtils.isBlank(fishingGearTypeCode.getValue()) || applicableGearCharacteristics == null) {
            return false;
        }

        List<String> requiredFishingGearCharacteristicCodes = null;
        try {
            requiredFishingGearCharacteristicCodes = rulesDomainModel.getFishingGearCharacteristicCodes(fishingGearTypeCode.getValue(), true);
        } catch (RulesModelException e) {
            log.error("Error while retrieving fishing gear characteristic codes", e);
        }

        if (requiredFishingGearCharacteristicCodes.isEmpty()) {
            return true;
        } else if (requiredFishingGearCharacteristicCodes.size() > applicableGearCharacteristics.size()) {
            return false;
        }

        int hits = 0;
        for (GearCharacteristic applicableGearCharacteristic : applicableGearCharacteristics) {
            un.unece.uncefact.data.standard.unqualifieddatatype._20.CodeType applicableGearCharacteristicTypeCode = applicableGearCharacteristic.getTypeCode();

            String fishingGearCharacteristicCode = null;
            try {
                if (!FactConstants.FA_GEAR_CHARACTERISTIC.equals(applicableGearCharacteristicTypeCode.getListID())) {
                    continue;
                }

                fishingGearCharacteristicCode = applicableGearCharacteristic.getTypeCode().getValue();
            } catch (NullPointerException npe) {
                fishingGearCharacteristicCode = null;
            }

            if (StringUtils.isNotBlank(fishingGearCharacteristicCode) && requiredFishingGearCharacteristicCodes.contains(fishingGearCharacteristicCode)) {
                hits++;
                continue;
            }
        }

        return hits == requiredFishingGearCharacteristicCodes.size();
    }

    private void initDomainModel() {
        try {
            InitialContext context = (InitialContext) getInitialContext();
            rulesDomainModel = (RulesDomainModel) context.lookup(ServiceConstants.DB_ACCESS_RULES_DOMAIN_MODEL);
            setDomainModel(rulesDomainModel);
        } catch (NamingException e) {
            log.error("Error while retrieving RulesDomainModel", e);
        }
    }

    private Context getInitialContext() {
        InitialContext initialContext = null;
        try {
            initialContext = new InitialContext();
        } catch (NamingException e) {
            log.error("Failed to get InitialContext", e);
        }

        return initialContext;
    }
}
