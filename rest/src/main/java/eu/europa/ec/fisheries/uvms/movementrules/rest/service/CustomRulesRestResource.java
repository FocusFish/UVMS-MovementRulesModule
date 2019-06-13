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
package eu.europa.ec.fisheries.uvms.movementrules.rest.service;

import eu.europa.ec.fisheries.schema.movementrules.customrule.v1.CustomRuleType;
import eu.europa.ec.fisheries.schema.movementrules.customrule.v1.UpdateSubscriptionType;
import eu.europa.ec.fisheries.schema.movementrules.module.v1.GetCustomRuleListByQueryResponse;
import eu.europa.ec.fisheries.schema.movementrules.search.v1.CustomRuleQuery;
import eu.europa.ec.fisheries.uvms.movementrules.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movementrules.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movementrules.rest.error.ErrorHandler;
import eu.europa.ec.fisheries.uvms.movementrules.service.bean.RulesServiceBean;
import eu.europa.ec.fisheries.uvms.movementrules.service.dto.CustomRuleListResponseDto;
import eu.europa.ec.fisheries.uvms.movementrules.service.entity.CustomRule;
import eu.europa.ec.fisheries.uvms.movementrules.service.mapper.CustomRuleMapper;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Path("/customrules")
@Stateless
@Consumes(value = { MediaType.APPLICATION_JSON })
@Produces(value = { MediaType.APPLICATION_JSON })
public class CustomRulesRestResource {

    private final static Logger LOG = LoggerFactory.getLogger(CustomRulesRestResource.class);

    @EJB
    private RulesServiceBean rulesService;
    @Context
    private ServletContext servletContext;
    @Context
    private HttpServletRequest request;

    @POST
    @RequiresFeature(UnionVMSFeature.manageAlarmRules)
    public ResponseDto createCustomRule(final CustomRuleType customRule) {
        LOG.info("Create invoked in rest layer");
        if(rulesService.isValid(customRule)) {
            try {
                CustomRule entity = CustomRuleMapper.toCustomRuleEntity(customRule);
                CustomRule created = rulesService.createCustomRule(entity, UnionVMSFeature.manageGlobalAlarmsRules.name(),
                        rulesService.getApplicationName(servletContext));
                CustomRuleType response = CustomRuleMapper.toCustomRuleType(created);
                return new ResponseDto<>(response, ResponseCode.OK);
            } catch (AccessDeniedException e) {
                LOG.error("[ User has no right to create global alarm rules ] {} ", e);
                return ErrorHandler.getFault(e);
            } catch (Exception e) {
                LOG.error("[ Error when creating. ] {} ", e);
                return ErrorHandler.getFault(e);
            }
        } else {
            return new ResponseDto<>("Custom rule data is not correct", ResponseCode.INPUT_ERROR);
        }
    }

    @GET
    @Path(value = "listAll/{userName}")
    @RequiresFeature(UnionVMSFeature.viewAlarmRules)
    public ResponseDto getCustomRulesByUser(@PathParam(value = "userName") final String userName) {
        LOG.info("Get all custom rules invoked in rest layer");
        try {
            List<CustomRule> customRulesByUser = rulesService.getCustomRulesByUser(userName);
            return new ResponseDto<>(CustomRuleMapper.toCustomRuleTypeList(customRulesByUser), ResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting all custom rules. ] {} ", ex);
            return ErrorHandler.getFault(ex);
        }
    }

    @POST
    @Path("listByQuery")
    @RequiresFeature(UnionVMSFeature.viewAlarmRules)
    public ResponseDto getCustomRulesByQuery(CustomRuleQuery query) {
        LOG.info("Get custom rules by query invoked in rest layer");
        try {
            CustomRuleListResponseDto customRulesListDto = rulesService.getCustomRulesByQuery(query);
            GetCustomRuleListByQueryResponse response = new GetCustomRuleListByQueryResponse();
            response.setTotalNumberOfPages(customRulesListDto.getTotalNumberOfPages());
            response.setCurrentPage(customRulesListDto.getCurrentPage());
            response.getCustomRules().addAll(CustomRuleMapper.toCustomRuleTypeList(customRulesListDto.getCustomRuleList()));
            return new ResponseDto<>(response, ResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting custom rules by query. ] {} ", ex);
            return ErrorHandler.getFault(ex);
        }
    }

    @GET
    @Path(value = "{guid}")
    @RequiresFeature(UnionVMSFeature.viewAlarmRules)
    public ResponseDto getCustomRuleByGuid(@PathParam(value = "guid") final String guid) {
        LOG.info("Get custom rule by guid invoked in rest layer");
        try {
            CustomRule customRuleByGuid = rulesService.getCustomRuleByGuid(UUID.fromString(guid));
            CustomRuleType response = CustomRuleMapper.toCustomRuleType(customRuleByGuid);
            return new ResponseDto<>(response, ResponseCode.OK);
        } catch (Exception ex) {
            LOG.error("[ Error when getting custom rule by guid. ] {} ", ex);
            return ErrorHandler.getFault(ex);
        }
    }

    @PUT
    @RequiresFeature(UnionVMSFeature.manageAlarmRules)
    public ResponseDto update(final CustomRuleType customRuleType) {
        LOG.info("Update custom rule invoked in rest layer");
        if(rulesService.isValid(customRuleType)) {
            try {
                CustomRule customRule = CustomRuleMapper.toCustomRuleEntity(customRuleType);
                CustomRule updated = rulesService.updateCustomRule(customRule, UnionVMSFeature.manageGlobalAlarmsRules.name(),
                        rulesService.getApplicationName(servletContext));
                CustomRuleType response = CustomRuleMapper.toCustomRuleType(updated);
                return new ResponseDto<>(response, ResponseCode.OK);
            } catch (AccessDeniedException e) {
                LOG.error("Forbidden access", e.getMessage());
                return ErrorHandler.getFault(e);
            } catch (Exception e) {
                LOG.error("[ Error when updating. ] {} ", e);
                return ErrorHandler.getFault(e);
            }
        } else {
            return new ResponseDto<>("Custom rule data is not correct", ResponseCode.INPUT_ERROR);
        }
    }

    @POST
    @Path("/subscription")
    @RequiresFeature(UnionVMSFeature.manageAlarmRules)
    public ResponseDto updateSubscription(UpdateSubscriptionType updateSubscriptionType) {
        LOG.info("Update subscription invoked in rest layer");
        try {
            CustomRule updated = rulesService.updateSubscription(updateSubscriptionType, request.getRemoteUser());
            CustomRuleType response = CustomRuleMapper.toCustomRuleType(updated);
            return new ResponseDto<>(response, ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when updating subscription and custom rule. ] {} ", e);
            return ErrorHandler.getFault(e);
        }
    }

    @DELETE
    @Path("/{guid}")
    @RequiresFeature(UnionVMSFeature.manageAlarmRules)
    public ResponseDto deleteCustomRule(@PathParam(value = "guid") final String guid) {
        LOG.info("Delete custom rule invoked in rest layer");
        try {
            CustomRule deleted = rulesService.deleteCustomRule(guid, request.getRemoteUser(),
                    UnionVMSFeature.manageGlobalAlarmsRules.name(), rulesService.getApplicationName(servletContext));
            CustomRuleType response = CustomRuleMapper.toCustomRuleType(deleted);
            return new ResponseDto<>(response, ResponseCode.OK);
        } catch (AccessDeniedException e) {
            LOG.error("Forbidden access", e.getMessage());
            return ErrorHandler.getFault(e);
        } catch (Exception e) {
            LOG.error("[ Error when deleting custom rule. ] {} ", e);
            return ErrorHandler.getFault(e);
        }
    }
}