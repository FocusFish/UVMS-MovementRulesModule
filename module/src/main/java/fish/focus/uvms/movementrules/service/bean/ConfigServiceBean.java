package fish.focus.uvms.movementrules.service.bean;

import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.uvms.movementrules.service.dto.MainCriteria;
import fish.focus.uvms.movementrules.service.dto.SubCriteria;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Stateless
public class ConfigServiceBean {

    public Map<String, HashMap<String, ArrayList<String>>> getCriterias() {
        Map<String, HashMap<String, ArrayList<String>>> map = new HashMap<>();
        MainCriteria[] mainCriterias = MainCriteria.values();
        for (MainCriteria mainCriteria : mainCriterias) {
            HashMap<String, ArrayList<String>> subResult = new HashMap<>();
            SubCriteria[] subCriterias = SubCriteria.values();
            for (SubCriteria subCriteria : subCriterias) {
                if (subCriteria.getMainCriteria().equals(mainCriteria)) {
                    subResult.put(subCriteria.toString(), getConditionsByCriteria(subCriteria));
                }
                if (!mainCriteria.equals(MainCriteria.ROOT)) {
                    map.put(mainCriteria.name(), subResult);
                }
            }
        }
        return map;
    }

    public LogicOperatorType[] getLogicOperatorType() {
        return LogicOperatorType.values();
    }

    public AvailabilityType[] getAvailability() {
        return AvailabilityType.values();
    }

    public MobileTerminalStatus[] getMobileTerminalStatuses() {
        return MobileTerminalStatus.values();
    }

    public AssetStatus[] getAssetStatuses() {
        return AssetStatus.values();
    }


    public Map<ActionType, Boolean> getActions() {
        return Arrays.stream(ActionType.values())
                .collect(Collectors.toMap(Function.identity(),
                        a -> !(a.equals(ActionType.MANUAL_POLL) ||
                                a.equals(ActionType.CREATE_TICKET) ||
                                a.equals(ActionType.CREATE_INCIDENT))));
    }

    private ArrayList<String> getConditionsByCriteria(SubCriteria subCriteria) {
        ArrayList<String> conditions = new ArrayList<>();
        switch (subCriteria) {
            case ACTIVITY_CALLBACK:
            case ACTIVITY_MESSAGE_ID:
            case ACTIVITY_MESSAGE_TYPE:
            case AREA_CODE:
            case AREA_TYPE:
            case ASSET_ID_GEAR_TYPE:
            case EXTERNAL_MARKING:
            case ASSET_NAME:
            case COMCHANNEL_TYPE:
            case MT_TYPE:
            case FLAG_STATE:
            case MOVEMENT_TYPE:
            case SEGMENT_TYPE:
            case SOURCE:
            case CLOSEST_COUNTRY_CODE:
            case CLOSEST_PORT_CODE:
            case ASSET_FILTER:
            case ASSET_STATUS:
            case MT_STATUS:
            case AREA_CODE_ENT:
            case AREA_CODE_VMS_ENT:
            case AREA_TYPE_ENT:
            case AREA_TYPE_VMS_ENT:
            case AREA_CODE_EXT:
            case AREA_CODE_VMS_EXT:
            case AREA_TYPE_EXT:
            case AREA_TYPE_VMS_EXT:
            case ASSET_TYPE:
            case ASSET_CFR:
            case ASSET_IRCS:
            case VICINITY_OF:
                conditions.add(ConditionType.EQ.name());
                conditions.add(ConditionType.NE.name());
                break;


            case MT_DNID:
            case MT_MEMBER_ID:
            case MT_SERIAL_NO:
            case ALTITUDE:
            case LATITUDE:
            case LONGITUDE:
            case POSITION_REPORT_TIME:
            case STATUS_CODE:
            case REPORTED_COURSE:
            case REPORTED_SPEED:
            case CALCULATED_COURSE:
            case CALCULATED_SPEED:
            case TIME_DIFF_POSITION_REPORT:
            case SUM_POSITION_REPORT:
            default:
                conditions.add(ConditionType.EQ.name());
                conditions.add(ConditionType.NE.name());
                conditions.add(ConditionType.LT.name());
                conditions.add(ConditionType.GT.name());
                conditions.add(ConditionType.LE.name());
                conditions.add(ConditionType.GE.name());
                break;
        }
        return conditions;
    }

}
