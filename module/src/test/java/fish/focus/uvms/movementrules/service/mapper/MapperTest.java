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

import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.uvms.commons.date.DateUtils;
import fish.focus.uvms.movementrules.service.dao.MockData;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class MapperTest {

    @InjectMocks
    private CustomRuleMapper mapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @OperateOnDeployment("normal")
    public void testEntityToModel() throws Exception {
        Integer id = 1;
        CustomRule entity = MockData.getCustomRuleEntity(id);

        CustomRuleType result = mapper.toCustomRuleType(entity);

        // Base
        assertSame(entity.getName(), result.getName());
        assertSame(entity.getAvailability(), result.getAvailability().value());
        assertSame(entity.getDescription(), result.getDescription());
        assertEquals(entity.getGuid().toString(), result.getGuid());
        assertSame(entity.getName(), result.getName());
        assertEquals(DateUtils.dateToEpochMilliseconds(entity.getLastTriggered()), result.getLastTriggered());
        assertEquals(DateUtils.dateToEpochMilliseconds(entity.getUpdated()), result.getUpdated());
        assertSame(entity.getUpdatedBy(), result.getUpdatedBy());

        // Rule segments
        List<CustomRuleSegmentType> segments = result.getDefinitions();
        assertSame(3, segments.size());

        for (CustomRuleSegmentType segment : segments) {
            switch (segment.getOrder()) {
                case "0":
                    assertSame("(", segment.getStartOperator());
                    assertSame(CriteriaType.ASSET, segment.getCriteria());
                    assertSame(SubCriteriaType.ASSET_CFR, segment.getSubCriteria());
                    assertSame(ConditionType.EQ, segment.getCondition());
                    assertSame("SWE111222", segment.getValue());
                    assertSame("", segment.getEndOperator());
                    assertSame(LogicOperatorType.OR, segment.getLogicBoolOperator());
                    break;
                case "1":
                    assertSame("", segment.getStartOperator());
                    assertSame(CriteriaType.ASSET, segment.getCriteria());
                    assertSame(SubCriteriaType.ASSET_CFR, segment.getSubCriteria());
                    assertSame(ConditionType.EQ, segment.getCondition());
                    assertSame("SWE111333", segment.getValue());
                    assertSame(")", segment.getEndOperator());
                    assertSame(LogicOperatorType.AND, segment.getLogicBoolOperator());
                    break;
                case "2":
                    assertSame("", segment.getStartOperator());
                    assertSame(CriteriaType.MOBILE_TERMINAL, segment.getCriteria());
                    assertSame(SubCriteriaType.MT_MEMBER_ID, segment.getSubCriteria());
                    assertSame(ConditionType.EQ, segment.getCondition());
                    assertSame("ABC99", segment.getValue());
                    assertSame("", segment.getEndOperator());
                    assertSame(LogicOperatorType.NONE, segment.getLogicBoolOperator());
                    break;
                default:
                    Assert.fail();
                    break;
            }

        }

        // Intervals
        List<CustomRuleIntervalType> intervals = result.getTimeIntervals();
        assertSame(2, intervals.size());

        assertEquals(DateUtils.dateToEpochMilliseconds(entity.getIntervals().get(0).getStart()), intervals.get(0).getStart());
        assertEquals(DateUtils.dateToEpochMilliseconds(entity.getIntervals().get(0).getEnd()), intervals.get(0).getEnd());

        assertEquals(DateUtils.dateToEpochMilliseconds(entity.getIntervals().get(1).getStart()), intervals.get(1).getStart());
        assertEquals(DateUtils.dateToEpochMilliseconds(entity.getIntervals().get(1).getEnd()), intervals.get(1).getEnd());

        // Actions
        List<CustomRuleActionType> actions = result.getActions();
        assertSame(2, actions.size());

        for (CustomRuleActionType action : actions) {
            switch (action.getOrder()) {
                case "0":
                    assertSame(ActionType.SEND_REPORT, action.getAction());
                    assertSame("FLUX", action.getTarget());
                    assertSame("value1", action.getValue());
                    break;
                case "1":
                    assertSame(ActionType.EMAIL, action.getAction());
                    assertSame("value2", action.getValue());
                    break;
                case "2":
                    assertSame(ActionType.SEND_REPORT, action.getAction());
                    assertSame("NAF", action.getTarget());
                    assertSame("value3", action.getValue());
                    break;
                default:
                    Assert.fail();
                    break;
            }
        }

    }

    @Test
    @OperateOnDeployment("normal")
    public void testModelToEntity() {
        Integer id = 1;
        CustomRuleType model = MockData.getModel(id);

        CustomRule result = mapper.toCustomRuleEntity(model);

        assertSame(model.getName(), result.getName());
        assertSame(model.getAvailability().value(), result.getAvailability());
        assertSame(model.getDescription(), result.getDescription());
        assertSame(model.getName(), result.getName());
        assertSame(model.getUpdatedBy(), result.getUpdatedBy());

        // TODO:
        // Rule segments
        // Intervals
        // Actions

    }

    // TODO:
    @Test
    @OperateOnDeployment("normal")
    public void testEntityAndModelToEntity() {
        int id = 1;
        CustomRule entity = MockData.getCustomRuleEntity(id);
        CustomRuleType model = MockData.getModel(1);

        CustomRule result = mapper.toCustomRuleEntity(entity, model);

        assertSame(result.getName(), model.getName());
    }

    // TODO:
    @Test
    @OperateOnDeployment("normal")
    public void testEntityAndModelToModel() {
        int id = 1;
        CustomRule entity = MockData.getCustomRuleEntity(id);
        CustomRuleType model = MockData.getModel(id);

        CustomRuleType result = mapper.toCustomRuleType(model, entity);

        assertSame(result.getName(), entity.getName());
    }
}