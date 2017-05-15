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

package eu.europa.ec.fisheries.uvms.rules.service;

import eu.europa.ec.fisheries.schema.rules.exchange.v1.PluginType;
import eu.europa.ec.fisheries.uvms.rules.model.dto.ValidationResultDto;
import eu.europa.ec.fisheries.uvms.rules.model.exception.RulesModelMarshallException;
import eu.europa.ec.fisheries.uvms.rules.service.exception.RulesServiceException;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.IDType;

import javax.ejb.Local;
import java.util.List;

/**
 * Created by padhyad on 5/9/2017.
 */
@Local
public interface MessageService {

    void setFLUXFAReportMessageReceived(String fluxFAReportMessage, PluginType pluginType, String username) throws RulesServiceException, RulesModelMarshallException;

    FLUXResponseMessage generateFluxResponseMessage(ValidationResultDto faReportValidationResult, List<IDType> idTypes);

    void sendResponseToExchange(FLUXResponseMessage fluxResponseMessageType, String username) throws RulesServiceException;

    void mapAndSendFLUXMdrRequestToExchange(String request);

    void mapAndSendFLUXMdrResponseToMdrModule(String request);

    void receiveSalesQueryRequest(String request);

    void receiveSalesReportRequest(String request);

    void sendSalesReportRequest(String request);

    void receiveSalesResponseRequest(String request);

    void sendSalesResponseRequest(String request);


}
