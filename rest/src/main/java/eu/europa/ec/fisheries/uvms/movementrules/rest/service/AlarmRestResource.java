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

import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.schema.movementrules.alarm.v1.AlarmReportType;
import eu.europa.ec.fisheries.schema.movementrules.module.v1.GetAlarmListByQueryResponse;
import eu.europa.ec.fisheries.schema.movementrules.search.v1.AlarmQuery;
import eu.europa.ec.fisheries.uvms.movementrules.rest.dto.ResponseCode;
import eu.europa.ec.fisheries.uvms.movementrules.rest.dto.ResponseDto;
import eu.europa.ec.fisheries.uvms.movementrules.rest.error.ErrorHandler;
import eu.europa.ec.fisheries.uvms.movementrules.service.RulesService;
import eu.europa.ec.fisheries.uvms.movementrules.service.ValidationService;
import eu.europa.ec.fisheries.uvms.movementrules.service.dto.AlarmListResponseDto;
import eu.europa.ec.fisheries.uvms.movementrules.service.mapper.AlarmMapper;
import eu.europa.ec.fisheries.uvms.rest.security.RequiresFeature;
import eu.europa.ec.fisheries.uvms.rest.security.UnionVMSFeature;

@Path("/alarms")
@Stateless
public class AlarmRestResource {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmRestResource.class);

    @EJB
    private RulesService rulesService;

    @EJB
    private ValidationService validationService;

    @Context
    private HttpServletRequest request;

    /**
     *
     * @responseMessage 200 All alarms matching query fetched
     * @responseMessage 500 No alarms fetched
     *
     * @summary Get a list of all alarms by query
     *
     */
    @POST
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/list")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public ResponseDto<AlarmListResponseDto> getAlarmList(AlarmQuery query) {
        LOG.info("Get alarm list invoked in rest layer");
        try {
            AlarmListResponseDto alarmList = rulesService.getAlarmList(query);
            GetAlarmListByQueryResponse response = new GetAlarmListByQueryResponse();
            response.getAlarms().addAll(AlarmMapper.toAlarmReportTypeList(alarmList.getAlarmList()));
            response.setTotalNumberOfPages(alarmList.getTotalNumberOfPages());
            response.setCurrentPage(alarmList.getCurrentPage());
            return new ResponseDto(response, ResponseCode.OK);
        } catch (Exception  e) {
            LOG.error("[ Error when getting alarm list by query. ] {} ", e.getMessage());
            return ErrorHandler.getFault(e);
        }
    }

    /**
     *
     * @responseMessage 200 Selected alarm updated
     * @responseMessage 500 No alarm updated
     *
     * @summary Update an alarm status
     *
     */
    @PUT
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @RequiresFeature(UnionVMSFeature.manageAlarmsHoldingTable)
    public ResponseDto updateAlarmStatus(final AlarmReportType alarmReportType) {
        LOG.info("Update alarm status invoked in rest layer");
        try {
            AlarmReportType response = AlarmMapper.toAlarmReportType(rulesService.updateAlarmStatus(AlarmMapper.toAlarmReportEntity(alarmReportType)));
            return new ResponseDto(response, ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when updating Alarm. ] {} ", e.getMessage());
            return ErrorHandler.getFault(e);
        }
    }

    /**
     *
     * @responseMessage 200 Alarm fetched by GUID
     * @responseMessage 500 No alarm fetched
     *
     * @summary Get an alarm by GUID
     *
     */
    @GET
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/{guid}")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public ResponseDto getAlarmReportByGuid(@PathParam("guid") String guid) {
        try {
            AlarmReportType response = AlarmMapper.toAlarmReportType(rulesService.getAlarmReportByGuid(guid));
            return new ResponseDto(response, ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting alarm by GUID. ] {} ", e.getMessage());
            return ErrorHandler.getFault(e);
        }
    }

    /**
     *
     * @responseMessage 200 Selected alarms processed
     * @responseMessage 500 Reprocessing of alarms failed
     *
     * @summary Reprocess alarms
     *
     */
    @POST
    @Consumes(value = { MediaType.APPLICATION_JSON })
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/reprocess")
    @RequiresFeature(UnionVMSFeature.manageAlarmsHoldingTable)
    public ResponseDto reprocessAlarm(final List<String> alarmGuidList) {
        LOG.info("Reprocess alarm invoked in rest layer");
        try {
            return new ResponseDto(rulesService.reprocessAlarm(alarmGuidList, request.getRemoteUser()), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when reprocessing. ] {} ", e.getMessage());
            return ErrorHandler.getFault(e);
        }
    }

    /**
     *
     * @responseMessage 200 Number of open alarms
     * @responseMessage 500 No result
     *
     * @summary Get number of open alarms
     *
     */
    @GET
    @Produces(value = { MediaType.APPLICATION_JSON })
    @Path("/countopen")
    @RequiresFeature(UnionVMSFeature.viewAlarmsHoldingTable)
    public ResponseDto getNumberOfOpenAlarmReports() {
        //Principal userPrincipal = request.getUserPrincipal();    //what is a Principal?

        try {
            return new ResponseDto(validationService.getNumberOfOpenAlarmReports(), ResponseCode.OK);
        } catch (Exception e) {
            LOG.error("[ Error when getting number of open alarms. ] {} ", e.getMessage());
            return ErrorHandler.getFault(e);
        }
    }

}