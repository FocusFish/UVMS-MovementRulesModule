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
package fish.focus.uvms.movementrules.service.mapper;

import fish.focus.schema.movementrules.customrule.v1.ConditionType;
import fish.focus.schema.movementrules.customrule.v1.CriteriaType;
import fish.focus.schema.movementrules.customrule.v1.LogicOperatorType;
import fish.focus.schema.movementrules.customrule.v1.SubCriteriaType;
import fish.focus.uvms.commons.date.DateUtils;
import fish.focus.uvms.movementrules.service.business.CustomRuleDto;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.Interval;
import fish.focus.uvms.movementrules.service.entity.RuleAction;
import fish.focus.uvms.movementrules.service.entity.RuleSegment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CustomRuleParser {

    private CustomRuleParser() {
    }

    public static List<CustomRuleDto> parseRules(List<CustomRule> rawRules) {
        List<CustomRuleDto> rules = new ArrayList<>();

        for (CustomRule rawRule : rawRules) {
            CustomRuleDto rulesDto = new CustomRuleDto();

            rulesDto.setRuleName(rawRule.getName());
            rulesDto.setRuleGuid(rawRule.getGuid().toString());

            List<RuleSegment> segments = rawRule.getRuleSegmentList();
            segments.sort(Comparator.comparing(RuleSegment::getOrder));

            StringBuilder sb = new StringBuilder();

            for (RuleSegment segment : segments) {
                if (segment.getStartOperator() != null) {
                    sb.append(segment.getStartOperator());
                }

                // All subcriteria
                String value = segment.getValue().replace("\"", "");

                if (segment.getSubCriteria() != null) {
                    switch (SubCriteriaType.valueOf(segment.getSubCriteria())) {


                        // ACTIVITY
                        case ACTIVITY_CALLBACK:
                            sb.append("activityCallback");
                            break;
                        case ACTIVITY_MESSAGE_ID:
                            sb.append("activityMessageId");
                            break;
                        case ACTIVITY_MESSAGE_TYPE:
                            sb.append("activityMessageType");
                            break;

                        // AREA
                        case AREA_CODE:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("areaCodes");
                            break;
                        case AREA_TYPE:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("areaTypes");
                            break;
                        case AREA_CODE_ENT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("entAreaCodes");
                            break;
                        case AREA_TYPE_ENT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("entAreaTypes");
                            break;
                        case AREA_CODE_EXT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("extAreaCodes");
                            break;
                        case AREA_TYPE_EXT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("extAreaTypes");
                            break;
                        case AREA_CODE_VMS_ENT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("vmsEntAreaCodes");
                            break;
                        case AREA_TYPE_VMS_ENT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("vmsEntAreaTypes");
                            break;
                        case AREA_CODE_VMS_EXT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("vmsExtAreaCodes");
                            break;
                        case AREA_TYPE_VMS_EXT:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("vmsExtAreaTypes");
                            break;

                        // ASSET
                        case ASSET_FILTER:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("assetFilters");
                            break;
                        case ASSET_ID_GEAR_TYPE:
                            sb.append("assetIdGearType");
                            break;
                        case EXTERNAL_MARKING:
                            sb.append("externalMarking");
                            break;
                        case FLAG_STATE:
                            sb.append("flagState");
                            break;
                        case ASSET_CFR:
                            sb.append("cfr");
                            break;
                        case ASSET_IRCS:
                            sb.append("ircs");
                            break;
                        case ASSET_NAME:
                            sb.append("assetName");
                            break;
                        case ASSET_STATUS:
                            sb.append("assetStatus");
                            break;
                        case ASSET_TYPE:
                            sb.append("assetType");
                            break;
                        case ASSET_PARKED:
                            sb.append("parked");
                            break;

                        // MOBILE_TERMINAL
                        case COMCHANNEL_TYPE:
                            sb.append("comChannelType");
                            break;
                        case MT_TYPE:
                            sb.append("mobileTerminalType");
                            break;
                        case MT_DNID:
                            sb.append("mobileTerminalDnid");
                            break;
                        case MT_MEMBER_ID:
                            sb.append("mobileTerminalMemberNumber");
                            break;
                        case MT_SERIAL_NO:
                            sb.append("mobileTerminalSerialNumber");
                            break;
                        case MT_STATUS:
                            sb.append("mobileTerminalStatus");
                            break;

                        // POSITION
                        case ALTITUDE:
                            sb.append("altitude");
                            break;
                        case LATITUDE:
                            sb.append("latitude");
                            break;
                        case LONGITUDE:
                            sb.append("longitude");
                            break;
                        case CALCULATED_COURSE:
                            sb.append("calculatedCourse");
                            break;
                        case CALCULATED_SPEED:
                            sb.append("calculatedSpeed");
                            break;
                        case MOVEMENT_TYPE:
                            sb.append("movementType");
                            break;
                        case POSITION_REPORT_TIME:      //some problems require really creative solutions.......
                            sb.append("DateUtils.stringToDate(\"");
                            sb.append(value);
                            sb.append("\")");
                            break;
                        case REPORTED_COURSE:
                            sb.append("reportedCourse");
                            break;
                        case REPORTED_SPEED:
                            sb.append("reportedSpeed");
                            break;
                        case SEGMENT_TYPE:
                            sb.append("segmentType");
                            break;
                        case SOURCE:
                            sb.append("source");
                            break;
                        case STATUS_CODE:
                            sb.append("statusCode");
                            break;
                        case VICINITY_OF:
                            // If list and NE
                            if (segment.getCondition().equals(ConditionType.NE.value())) {
                                sb.append("!");
                            }
                            sb.append("vicinityOf");
                            break;
                        case VICINITY_DISTANCE_OF:
                            sb.append("vicinityDistance");
                            break;
                        case CLOSEST_COUNTRY_CODE:
                            sb.append("closestCountryCode");
                            break;
                        case CLOSEST_PORT_CODE:
                            sb.append("closestPortCode");
                            break;

                        // REPORT
                        case SUM_POSITION_REPORT:
                            sb.append("sumPositionReport");
                            break;
                        case TIME_DIFF_POSITION_REPORT:
                            sb.append("timeDiffPositionReport");
                            break;

                        default:
                            break;
                    }
                }
                switch (ConditionType.valueOf(segment.getCondition())) {
                    case EQ:
                        // Different EQ if a list
                        if (isListCriteria(segment.getSubCriteria())) {
                            sb.append(".contains(");
                        } else {
                            sb.append(" == ");
                        }
                        break;
                    case NE:
                        // Different NE if a list
                        if (isListCriteria(segment.getSubCriteria())) {
                            sb.append(".contains(");
                        } else {
                            sb.append(" != ");
                        }
                        break;
                    case GT:
                        if (SubCriteriaType.POSITION_REPORT_TIME.value().equals(segment.getSubCriteria())) {
                            sb.append(" < ");
                        } else {
                            sb.append(" > ");
                        }
                        break;
                    case GE:
                        if (SubCriteriaType.POSITION_REPORT_TIME.value().equals(segment.getSubCriteria())) {
                            sb.append(" <= ");
                        } else {
                            sb.append(" >= ");
                        }
                        break;
                    case LT:
                        if (SubCriteriaType.POSITION_REPORT_TIME.value().equals(segment.getSubCriteria())) {
                            sb.append(" > ");
                        } else {
                            sb.append(" < ");
                        }
                        break;
                    case LE:
                        if (SubCriteriaType.POSITION_REPORT_TIME.value().equals(segment.getSubCriteria())) {
                            sb.append(" >= ");
                        } else {
                            sb.append(" <= ");
                        }
                        break;
                    default: // undefined
                        break;

                }
                // Remove quotations (event though there shouldn't be any) from the value, since it totally messes up the rule engine
                if (segment.getSubCriteria().equals(SubCriteriaType.POSITION_REPORT_TIME.value())) {
                    sb.append("positionTime");
                } else {
                    sb.append("\"");
                    sb.append(value);
                    sb.append("\"");
                }

                // If list, end "contains" with parenthesis
                if ((segment.getCriteria().equals(CriteriaType.AREA.value()) || segment.getCriteria().equals(CriteriaType.ASSET_FILTER.value()) || segment.getSubCriteria().equals(SubCriteriaType.VICINITY_OF.value()))
                        && (segment.getCondition().equals(ConditionType.EQ.value()) || segment.getCondition().equals(ConditionType.NE.value()))) {
                    sb.append(")");
                }

                if (segment.getEndOperator() != null) {
                    sb.append(segment.getEndOperator());
                }

                switch (LogicOperatorType.valueOf(segment.getLogicOperator())) {
                    case AND:
                        sb.append(" && ");
                        break;
                    case OR:
                        sb.append(" || ");
                        break;
                    case NONE:
                        break;
                    default: // undefined
                        break;
                }
            }

            // Add time intervals
            List<Interval> intervals = rawRule.getIntervals();

            for (int i = 0; i < intervals.size(); i++) {
                // If first
                if (i == 0) {
                    sb.append(" && (");
                }

                sb.append(createInterval(intervals.get(i)));

                // If last
                if (i == intervals.size() - 1) {
                    sb.append(")");
                } else {
                    sb.append(" || ");
                }
            }

            rulesDto.setExpression(sb.toString());

            // Format: "ACTION,TARGET,VALUE;ACTION,TARGET,VALUE;ACTION,TARGET,VALUE;"
            List<RuleAction> actions = rawRule.getRuleActionList();

            sb = new StringBuilder();
            if (actions.isEmpty()) {
                sb.append(";");
            } else {
                for (RuleAction action : actions) {
                    sb.append(action.getAction());
                    sb.append(",");
                    sb.append(action.getTarget() != null ? action.getTarget() : "");
                    sb.append(",");
                    sb.append(action.getValue());
                    sb.append(";");
                }
            }

            rulesDto.setAction(sb.toString());

            rules.add(rulesDto);
        }

        return rules;
    }

    private static boolean isListCriteria(String subcriteria) {
        return SubCriteriaType.AREA_CODE.value().equals(subcriteria) ||             //this does not contain VICINITY_DISTANCE_OF since that needs < > = !
                SubCriteriaType.AREA_TYPE.value().equals(subcriteria) ||
                SubCriteriaType.AREA_CODE_ENT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_TYPE_ENT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_CODE_EXT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_TYPE_EXT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_CODE_VMS_ENT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_TYPE_VMS_ENT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_CODE_VMS_EXT.value().equals(subcriteria) ||
                SubCriteriaType.AREA_TYPE_VMS_EXT.value().equals(subcriteria) ||
                SubCriteriaType.ASSET_FILTER.value().equals(subcriteria) ||
                //SubCriteriaType.VICINITY_DISTANCE_OF.value().equals(subcriteria) ||
                SubCriteriaType.VICINITY_OF.value().equals(subcriteria);
    }

    private static String createInterval(Interval interval) {
        StringBuilder sb = new StringBuilder();
        if (interval.getStart() != null) {
            // TODO find a better solution than date to string to date conversion
            String start = DateUtils.dateToEpochMilliseconds(interval.getStart());
            sb.append("DateUtils.stringToDate(\"");
            sb.append(start);
            sb.append("\")");
            sb.append(" <= positionTime");
        }

        if (interval.getStart() != null && interval.getEnd() != null) {
            sb.append(" && ");
        }

        if (interval.getEnd() != null) {
            String end = DateUtils.dateToEpochMilliseconds(interval.getEnd());
            sb.append("DateUtils.stringToDate(\"");
            sb.append(end);
            sb.append("\")");
            sb.append(">= positionTime");

        }
        return sb.toString();
    }

}