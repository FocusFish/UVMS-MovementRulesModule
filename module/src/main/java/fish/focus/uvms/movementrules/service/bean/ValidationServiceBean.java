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
package fish.focus.uvms.movementrules.service.bean;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fish.focus.schema.exchange.module.v1.SetCommandRequest;
import fish.focus.schema.exchange.movement.v1.MovementType;
import fish.focus.schema.exchange.movement.v1.MovementTypeType;
import fish.focus.schema.exchange.plugin.types.v1.EmailType;
import fish.focus.schema.movementrules.customrule.v1.ActionType;
import fish.focus.schema.movementrules.customrule.v1.SubscriptionTypeType;
import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.uvms.asset.client.AssetClient;
import fish.focus.uvms.asset.client.model.PollType;
import fish.focus.uvms.commons.notifications.NotificationMessage;
import fish.focus.uvms.exchange.client.ExchangeRestClient;
import fish.focus.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import fish.focus.wsdl.user.module.GetContactDetailResponse;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.boundary.AuditServiceBean;
import fish.focus.uvms.movementrules.service.boundary.ExchangeServiceBean;
import fish.focus.uvms.movementrules.service.boundary.UserServiceBean;
import fish.focus.uvms.movementrules.service.constants.AuditObjectTypeEnum;
import fish.focus.uvms.movementrules.service.constants.AuditOperationEnum;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.dto.EventTicket;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.RuleSubscription;
import fish.focus.uvms.movementrules.service.entity.Ticket;
import fish.focus.uvms.movementrules.service.event.TicketCountEvent;
import fish.focus.uvms.movementrules.service.event.TicketEvent;
import fish.focus.uvms.movementrules.service.mapper.EmailMapper;
import fish.focus.uvms.movementrules.service.mapper.ExchangeMovementMapper;
import fish.focus.uvms.movementrules.service.message.producer.bean.IncidentProducer;

@Stateless
public class ValidationServiceBean  {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceBean.class);

    @EJB
    private RulesDao rulesDao;
    
    @Inject
    private UserServiceBean userService;
    
    @Inject
    private ExchangeServiceBean exchangeService;
    
    @Inject
    private AuditServiceBean auditService;

    @Inject
    private IncidentProducer incidentProducer;

    @Inject
    @TicketEvent
    private Event<EventTicket> ticketEvent;

    @Inject
    @TicketCountEvent
    private Event<NotificationMessage> ticketCountEvent;

    @Inject
    private AssetClient assetClient;

    // Triggered by rule engine
    public void customRuleTriggered(String ruleName, String ruleGuid, MovementDetails movementDetails, String actions) {
        LOG.debug("Performing actions on triggered user rules, rule: {}", ruleName);

        CustomRule triggeredRule = getCustomRule(ruleGuid);
        triggeredRule.setLastTriggered(Instant.now());
        
        Instant auditTimestamp = Instant.now();

        auditTimestamp = auditLog("Time to create/update ticket:", auditTimestamp);

        sendMailToSubscribers(triggeredRule, movementDetails);
        auditTimestamp = auditLog("Time to send email to subscribers:", auditTimestamp);

        // Actions list format:
        // ACTION,TARGET,VALUE;ACTION,TARGET,VALUE;
        // N.B! The .drl rule file gives the string "null" when (for instance)
        // value is null.
        String[] parsedActionKeyValueList = actions.split(";");
        for (String keyValue : parsedActionKeyValueList) {
            String[] keyValueList = keyValue.split(",");
            String action = keyValueList[0];
            String target = "";
            String value = "";
            if (keyValueList.length == 3) {
                target = keyValueList[1];
                value = keyValueList[2];
            }
            switch (ActionType.valueOf(action)) {
                case EMAIL:
                    // Value=address.
                    sendToEmail(value, ruleName, movementDetails);
                    auditTimestamp = auditLog("Time to send (action) email:", auditTimestamp);
                    break;
                case SEND_REPORT:
                    sendToEndpoint(ruleName, movementDetails, value, target, ActionType.SEND_REPORT);
                    auditTimestamp = auditLog("Time to send to endpoint:", auditTimestamp);
                    break;
                case SEND_ENTRY_REPORT:
                    sendToEndpoint(ruleName, movementDetails, value, target, ActionType.SEND_ENTRY_REPORT);
                    break;
                case SEND_EXIT_REPORT:
                    sendToEndpoint(ruleName, movementDetails, value, target, ActionType.SEND_EXIT_REPORT);
                    break;
                case MANUAL_POLL:
                    createPollInternal(movementDetails, ruleName);
                    auditTimestamp = auditLog("Time to send poll:", auditTimestamp);
                    break;
                case CREATE_INCIDENT:
                    Ticket ticket = upsertTicket(triggeredRule, movementDetails);
                    incidentProducer.createdTicket(new EventTicket(ticket, triggeredRule));
                    break;
                case CREATE_TICKET:
                    upsertTicket(triggeredRule, movementDetails);
                    break;

                    /*
                case ON_HOLD:
                    LOG.info("NOT IMPLEMENTED!");
                    break;
                case TOP_BAR_NOTIFICATION:
                    LOG.info("NOT IMPLEMENTED!");
                    break;
                case SMS:
                    LOG.info("NOT IMPLEMENTED!");
                    break;
                    */
                default:
                    LOG.info("The action '{}' is not defined", action);
                    break;
            }
        }
    }

    private Ticket upsertTicket(CustomRule triggeredRule, MovementDetails movementDetails){
        if (triggeredRule != null && triggeredRule.isAggregateInvocations()) {
            return createTicketOrIncreaseCount(movementDetails, triggeredRule);
        } else {
            return createTicket(triggeredRule, movementDetails);
        }
    }


    private CustomRule getCustomRule(String ruleGuid) {
        try {
            return rulesDao.getCustomRuleByGuid(UUID.fromString(ruleGuid));
        } catch (Exception e) {
            LOG.error("[ Failed to fetch rule when sending email to subscribers due to erro when getting CustomRule by GUID! ] {}", e.getMessage());
            return null;
        }
    }

    private Ticket createTicketOrIncreaseCount(MovementDetails movementDetails, CustomRule triggeredRule) {
        Ticket latestTicketForRule = rulesDao.getLatestTicketForRule(triggeredRule.getGuid());
        if (latestTicketForRule == null) {
            return createTicket(triggeredRule, movementDetails);
        } else {
            latestTicketForRule.setTicketCount(latestTicketForRule.getTicketCount() + 1);
            latestTicketForRule.setUpdated(Instant.now());
            return latestTicketForRule;
        }
    }
    
    private void sendMailToSubscribers(CustomRule customRule, MovementDetails movementDetails) {
        if (customRule == null) {
            return;
        }
        
        List<RuleSubscription> subscriptions = customRule.getRuleSubscriptionList();
        if (subscriptions != null) {
            for (RuleSubscription subscription : subscriptions) {
                if (SubscriptionTypeType.EMAIL.value().equals(subscription.getType())) {
                    try {
                        // Find current email address
                        GetContactDetailResponse userResponse = userService.getContactDetails(subscription.getOwner());
                        String emailAddress = userResponse.getContactDetails().getEMail();
                        sendToEmail(emailAddress, customRule.getName(), movementDetails);
                    } catch (Exception e) {
                        // If a mail attempt fails, proceed with the rest
                        LOG.error("Could not send email to user '{}'", subscription.getOwner());
                    }
                }
            }
        }
    }

    private void sendToEndpoint(String ruleName, MovementDetails movementDetails, String organisationName, String pluginName, ActionType actionType) {
        LOG.debug("Sending to organisation '{}'", organisationName);

        try {
            MovementType exchangeMovement = ExchangeMovementMapper.mapToExchangeMovementType(movementDetails);

            mapTypeOfMessage(exchangeMovement, actionType);

            exchangeService.sendReportToPlugin(pluginName, ruleName, organisationName, exchangeMovement, new ArrayList<>(), movementDetails);

            auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE_ACTION, AuditOperationEnum.SEND_TO_ENDPOINT, null, organisationName, "UVMS");

        } catch (Exception e) {
            LOG.error("[ Failed to send to endpoint! ] {}", e.getMessage());
        }
    }

    private void mapTypeOfMessage(MovementType movement, ActionType actionType) {
        if (actionType == null) {
            return;
        }
        if (ActionType.SEND_ENTRY_REPORT.equals(actionType)) {
            movement.setMovementType(MovementTypeType.ENT);
        } else if (ActionType.SEND_EXIT_REPORT.equals(actionType) ) {
            movement.setMovementType(MovementTypeType.EXI);
        }
    }

    @Inject
    ExchangeRestClient exchangeRestClient;

    private void sendToEmail(String emailAddress, String ruleName, MovementDetails movementDetails) {
        LOG.info("Sending email to '{}'", emailAddress);

        EmailType email = new EmailType();

        email.setSubject(EmailMapper.buildSubject(movementDetails));
        email.setBody(EmailMapper.buildBody(ruleName, movementDetails));
        email.setTo(emailAddress);

        SetCommandRequest sendEmailCommand = ExchangeModuleRequestMapper.createSetCommandSendEmailRequest(null, email, ruleName);
        exchangeRestClient.sendCommandToPlugin(sendEmailCommand);

        auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE_ACTION, AuditOperationEnum.SEND_EMAIL, null, emailAddress, "UVMS");
        LOG.info("No plugin of the correct type found. Nothing was sent.");
    }

    private String createPollInternal(MovementDetails fact, String ruleName){
        try {
            String username = "Triggerd by rule: " + ruleName;
            String comment = "This poll was triggered by rule: " + ruleName + " on: " + Instant.now().toString() + " on Asset: " + fact.getAssetName();

            return assetClient.createPollForAsset(UUID.fromString(fact.getAssetGuid()), username, comment, PollType.AUTOMATIC_POLL);

        } catch (Exception e){
            LOG.error("Error while sending rule-triggered poll: ", e);
            return "NOK " + e.getMessage();
        }
    }

    private Ticket createTicket(CustomRule customRule, MovementDetails fact) {
        try {
            Ticket ticket = new Ticket();

            ticket.setAssetGuid(fact.getAssetGuid());
            ticket.setMobileTerminalGuid(fact.getMobileTerminalGuid());
            ticket.setChannelGuid(fact.getChannelGuid());
            ticket.setCreatedDate(Instant.now());
            ticket.setUpdated(Instant.now());
            ticket.setRuleName(customRule.getName());
            ticket.setRuleGuid(customRule.getGuid().toString());
            ticket.setStatus(TicketStatusType.OPEN);
            ticket.setUpdatedBy("UVMS");
            ticket.setMovementGuid(fact.getMovementGuid());

            for (int i = 0; i < fact.getAreaTypes().size(); i++) {
                if ("EEZ".equals(fact.getAreaTypes().get(i))) {
                    ticket.setRecipient(fact.getAreaCodes().get(i));
                }
            }


            ticket.setTicketCount(1L);
            Ticket createdTicket = rulesDao.createTicket(ticket);

            ticketEvent.fire(new EventTicket(ticket, customRule));

            // Notify long-polling clients of the change (no value since FE will need to fetch it)
            ticketCountEvent.fire(new NotificationMessage("ticketCount", null));

            auditService.sendAuditMessage(AuditObjectTypeEnum.TICKET, AuditOperationEnum.CREATE, createdTicket.getGuid().toString(), null, createdTicket.getUpdatedBy());

            return ticket;
        } catch (Exception e) { //TODO: figure out if we are to have this kind of exception handling here and if we are to catch everything
            LOG.error("[ Failed to create ticket! ] {}", e);
            return null;
        }
    }


    public long getNumberOfOpenTickets(String userName) {
        LOG.info("Counting open tickets for user: {}", userName);
        List<UUID> validRuleGuids = rulesDao.getCustomRulesForTicketsByUser(userName);
        if (!validRuleGuids.isEmpty()) {
            List<String> validRuleStrings = new ArrayList<>();
            for (UUID uuid: validRuleGuids) {
                validRuleStrings.add(uuid.toString());
            }
            return rulesDao.getNumberOfOpenTickets(validRuleStrings);
        }
        return 0;
    }

    private Instant auditLog(String msg, Instant lastTimestamp) {
        Instant newTimestamp = Instant.now();
        long duration = newTimestamp.toEpochMilli() - lastTimestamp.toEpochMilli();
        LOG.debug("--> AUDIT - {} {}ms", msg, duration);
        return newTimestamp;
    }
}
