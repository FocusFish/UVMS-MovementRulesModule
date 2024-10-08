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

import fish.focus.schema.movementrules.customrule.v1.*;
import fish.focus.schema.movementrules.module.v1.GetTicketsAndRulesByMovementsResponse;
import fish.focus.schema.movementrules.search.v1.CustomRuleQuery;
import fish.focus.schema.movementrules.search.v1.TicketQuery;
import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.schema.movementrules.ticket.v1.TicketType;
import fish.focus.schema.movementrules.ticketrule.v1.TicketAndRuleType;
import fish.focus.uvms.commons.notifications.NotificationMessage;
import fish.focus.uvms.movementrules.model.dto.MovementDetails;
import fish.focus.uvms.movementrules.service.boundary.AuditServiceBean;
import fish.focus.uvms.movementrules.service.boundary.UserServiceBean;
import fish.focus.uvms.movementrules.service.business.CustomRuleValidator;
import fish.focus.uvms.movementrules.service.business.RulesValidator;
import fish.focus.uvms.movementrules.service.constants.AuditObjectTypeEnum;
import fish.focus.uvms.movementrules.service.constants.AuditOperationEnum;
import fish.focus.uvms.movementrules.service.constants.ServiceConstants;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.dto.CustomRuleListResponseDto;
import fish.focus.uvms.movementrules.service.dto.EventTicket;
import fish.focus.uvms.movementrules.service.dto.TicketListResponseDto;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.PreviousReport;
import fish.focus.uvms.movementrules.service.entity.RuleSubscription;
import fish.focus.uvms.movementrules.service.entity.Ticket;
import fish.focus.uvms.movementrules.service.event.TicketCountEvent;
import fish.focus.uvms.movementrules.service.event.TicketEvent;
import fish.focus.uvms.movementrules.service.event.TicketUpdateEvent;
import fish.focus.uvms.movementrules.service.mapper.CustomRuleMapper;
import fish.focus.uvms.movementrules.service.mapper.TicketMapper;
import fish.focus.uvms.movementrules.service.mapper.search.CustomRuleSearchFieldMapper;
import fish.focus.uvms.movementrules.service.mapper.search.CustomRuleSearchValue;
import fish.focus.uvms.movementrules.service.mapper.search.TicketSearchFieldMapper;
import fish.focus.uvms.movementrules.service.mapper.search.TicketSearchValue;
import fish.focus.uvms.movementrules.service.message.producer.bean.IncidentProducer;
import fish.focus.uvms.user.model.exception.ModelMarshallException;
import fish.focus.wsdl.user.types.Feature;
import fish.focus.wsdl.user.types.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.persistence.NoResultException;
import javax.servlet.ServletContext;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Stateless
public class RulesServiceBean {

    private static final Logger LOG = LoggerFactory.getLogger(RulesServiceBean.class);

    @Inject
    private RulesValidator rulesValidator;

    @Inject
    private UserServiceBean userService;

    @Inject
    private RulesDao rulesDao;

    @Inject
    private AuditServiceBean auditService;

    @Inject
    private ValidationServiceBean validationServiceBean;

    @Inject
    private IncidentProducer incidentProducer;

    @Inject
    @TicketUpdateEvent
    private Event<EventTicket> ticketUpdateEvent;

    @Inject
    @TicketEvent
    private Event<EventTicket> ticketEvent;

    @Inject
    @TicketCountEvent
    private Event<NotificationMessage> ticketCountEvent;

    public CustomRule createCustomRule(CustomRule customRule, String featureName, String applicationName) throws AccessDeniedException, ModelMarshallException, JMSException {
        // Get organisation of user
        String organisationName = userService.getOrganisationName(customRule.getUpdatedBy());
        if (organisationName != null) {
            customRule.setOrganisation(organisationName);
        } else {
            LOG.warn("User {} is not connected to any organisation!", customRule.getUpdatedBy());
        }
        if (customRule.getAvailability().equals(AvailabilityType.GLOBAL.value())) {
            UserContext userContext = userService.getFullUserContext(customRule.getUpdatedBy(), applicationName);
            if (!hasFeature(userContext, featureName)) {
                throw new AccessDeniedException("Forbidden access");
            }
        }

        List<RuleSubscription> subscriptionEntities = new ArrayList<>();
        RuleSubscription creatorSubscription = new RuleSubscription();
        creatorSubscription.setCustomRule(customRule);
        creatorSubscription.setOwner(customRule.getUpdatedBy());
        creatorSubscription.setType(SubscriptionTypeType.TICKET.value());
        subscriptionEntities.add(creatorSubscription);
        customRule.getRuleSubscriptionList().addAll(subscriptionEntities);


        customRule.setUpdated(Instant.now());
        customRule.setStartDate(Instant.now());


        rulesDao.createCustomRule(customRule);

        // TODO: Rewrite so rules are loaded when changed
        rulesValidator.updateCustomRules();
        auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE, AuditOperationEnum.CREATE, customRule.getGuid().toString(), null, customRule.getUpdatedBy());
        return customRule;
    }

    public CustomRule getCustomRuleByGuid(UUID guid) {
        try {
            return rulesDao.getCustomRuleByGuid(guid);
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CustomRule> getCustomRulesByUser(String userName) {
        List<CustomRule> customRules = rulesDao.getCustomRulesByUser(userName);
        return customRules;
    }

    public List<CustomRule> getRunnableCustomRules() {
        return rulesDao.getRunnableCustomRuleList();
    }

    public CustomRuleListResponseDto getCustomRulesByQuery(CustomRuleQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Custom rule list query is null");
        }
        if (query.getPagination() == null) {
            throw new IllegalArgumentException("Pagination in custom rule list query is null");
        }

        Integer page = query.getPagination().getPage();
        Integer listSize = query.getPagination().getListSize();

        List<CustomRuleSearchValue> searchKeyValues = CustomRuleSearchFieldMapper.mapSearchField(query.getCustomRuleSearchCriteria());

        String sql = CustomRuleSearchFieldMapper.createSelectSearchSql(searchKeyValues, query.isDynamic());

        List<CustomRule> customRuleEntityList = rulesDao.getCustomRuleListPaginated(page, listSize, sql);
        Integer numberMatches = customRuleEntityList.size();

        int numberOfPages = (int) (numberMatches / listSize);
        if (numberMatches % listSize != 0) {
            numberOfPages += 1;
        }

        CustomRuleListResponseDto customRuleListByQuery = new CustomRuleListResponseDto();
        customRuleListByQuery.setTotalNumberOfPages(numberOfPages);
        customRuleListByQuery.setCurrentPage(query.getPagination().getPage());
        customRuleListByQuery.setCustomRuleList(customRuleEntityList);
        return customRuleListByQuery;
    }

    public CustomRule getCustomRuleOrAssetNotSendingRule(String guid) {
        if (ServiceConstants.ASSET_NOT_SENDING_RULE.equals(guid)) {
            return ServiceConstants.ASSET_NOT_SENDING_CUSTOMRULE;
        }
        try {
            return rulesDao.getCustomRuleByGuid(UUID.fromString(guid));
        } catch (Exception e) {
            return null;
        }
    }

    public CustomRule updateCustomRule(CustomRule oldCustomRule, String featureName, String applicationName) throws ModelMarshallException, JMSException, AccessDeniedException {
        // Get organisation of user
        String organisationName = userService.getOrganisationName(oldCustomRule.getUpdatedBy());
        if (organisationName != null) {
            oldCustomRule.setOrganisation(organisationName);
        } else {
            LOG.warn("User {} is not connected to any organisation!", oldCustomRule.getUpdatedBy());
        }

        if (oldCustomRule.getAvailability().equals(AvailabilityType.GLOBAL.value())) {
            UserContext userContext = userService.getFullUserContext(oldCustomRule.getUpdatedBy(), applicationName);
            if (!hasFeature(userContext, featureName)) {
                throw new AccessDeniedException("Forbidden access");
            }
        }

        CustomRule customRule = internalUpdateCustomRule(oldCustomRule);
        rulesValidator.updateCustomRules();
        auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE, AuditOperationEnum.UPDATE, customRule.getGuid().toString(), null, oldCustomRule.getUpdatedBy());
        return customRule;

    }

    private CustomRule internalUpdateCustomRule(CustomRule newEntity) {
        if (newEntity == null) {
            throw new IllegalArgumentException("Custom Rule is null");
        }

        if (newEntity.getGuid() == null) {
            throw new IllegalArgumentException("GUID of Custom Rule is null");
        }

        CustomRule oldEntity = getCustomRuleByGuid(newEntity.getGuid());
        if (oldEntity == null) {
            throw new IllegalArgumentException("Could not find rule with id: " + newEntity.getGuid());
        }

        CustomRule copiedNewEntity = newEntity.copy();


        // Close old version
        oldEntity.setArchived(true);
        oldEntity.setActive(false);
        oldEntity.setEndDate(Instant.now());
        // Copy subscription list (ignore if provided)
        // Copy to new array to avoid concurrent modification exception
        List<RuleSubscription> subscriptions = new ArrayList<>(oldEntity.getRuleSubscriptionList());
        for (RuleSubscription subscription : subscriptions) {
            rulesDao.detachSubscription(subscription);
            copiedNewEntity.getRuleSubscriptionList().add(subscription);
            subscription.setCustomRule(copiedNewEntity);
        }

        copiedNewEntity.setUpdated(Instant.now());
        copiedNewEntity.setStartDate(Instant.now());
        copiedNewEntity.setGuid(null);
        copiedNewEntity = rulesDao.createCustomRule(copiedNewEntity);
        return copiedNewEntity;
    }

    public CustomRule updateCustomRule(CustomRule oldCustomRule) {
        CustomRule updatedCustomRule = internalUpdateCustomRule(oldCustomRule);
        auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE, AuditOperationEnum.UPDATE, updatedCustomRule.getGuid().toString(), null, oldCustomRule.getUpdatedBy());
        return updatedCustomRule;

    }

    public CustomRule updateSubscription(UpdateSubscriptionType updateSubscriptionType, String username) {
        if (updateSubscriptionType == null) {
            throw new IllegalArgumentException("Subscription is null");
        }

        boolean validRequest = updateSubscriptionType.getSubscription().getType() != null && updateSubscriptionType.getSubscription().getOwner() != null;
        if (!validRequest) {
            throw new IllegalArgumentException("Not a valid subscription!");
        }

        if (updateSubscriptionType.getRuleGuid() == null) {
            throw new IllegalArgumentException("Custom Rule GUID for Subscription is null");
        }

        CustomRule customRuleEntity = rulesDao.getCustomRuleByGuid(UUID.fromString(updateSubscriptionType.getRuleGuid()));

        if (SubscritionOperationType.ADD.equals(updateSubscriptionType.getOperation())) {
            RuleSubscription ruleSubscription = new RuleSubscription();
            ruleSubscription.setOwner(updateSubscriptionType.getSubscription().getOwner());
            if (updateSubscriptionType.getSubscription().getType() != null) {
                ruleSubscription.setType(updateSubscriptionType.getSubscription().getType().name());
            }
            customRuleEntity.getRuleSubscriptionList().add(ruleSubscription);
            ruleSubscription.setCustomRule(customRuleEntity);

            // TODO: Don't log rule guid, log subscription guid?
            auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE_SUBSCRIPTION, AuditOperationEnum.CREATE, updateSubscriptionType.getRuleGuid(), updateSubscriptionType.getSubscription().getOwner() + "/" + updateSubscriptionType.getSubscription().getType(), username);
        } else if (SubscritionOperationType.REMOVE.equals(updateSubscriptionType.getOperation())) {
            List<RuleSubscription> subscriptions = customRuleEntity.getRuleSubscriptionList();
            for (RuleSubscription subscription : subscriptions) {
                if (subscription.getOwner().equals(updateSubscriptionType.getSubscription().getOwner()) && subscription.getType().equals(updateSubscriptionType.getSubscription().getType().name())) {
                    customRuleEntity.getRuleSubscriptionList().remove(subscription);
                    rulesDao.removeSubscription(subscription);

                    // TODO: Don't log rule guid, log subscription guid?
                    auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE_SUBSCRIPTION, AuditOperationEnum.DELETE, updateSubscriptionType.getRuleGuid(), updateSubscriptionType.getSubscription().getOwner() + "/" + updateSubscriptionType.getSubscription().getType(), username);
                    break;
                }
            }
        }

        return customRuleEntity;
    }

    public CustomRule deleteCustomRule(String guidString, String username, String featureName, String applicationName) throws AccessDeniedException {
        LOG.info("[INFO] Deleting custom rule by guid: {}.", guidString);
        if (guidString == null) {
            throw new IllegalArgumentException("No custom rule to remove");
        }

        UUID guid = UUID.fromString(guidString);
        CustomRule customRuleFromDb = getCustomRuleByGuid(guid);
        if (customRuleFromDb == null) {
            throw new IllegalArgumentException("Could not find rule with id: " + guidString);
        }
        if (customRuleFromDb.getAvailability().equals(AvailabilityType.GLOBAL.value())) {
            UserContext userContext = userService.getFullUserContext(username, applicationName);
            if (!hasFeature(userContext, featureName)) {
                throw new AccessDeniedException("Forbidden access");
            }
        }

        customRuleFromDb.setArchived(true);
        customRuleFromDb.setActive(false);
        customRuleFromDb.setEndDate(Instant.now());

        rulesValidator.updateCustomRules();
        auditService.sendAuditMessage(AuditObjectTypeEnum.CUSTOM_RULE, AuditOperationEnum.DELETE, customRuleFromDb.getGuid().toString(), null, username);
        return customRuleFromDb;

    }

    public TicketListResponseDto getTicketList(String loggedInUser, TicketQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Ticket list query is null");
        }
        if (query.getPagination() == null) {
            throw new IllegalArgumentException("Pagination in ticket list query is null");
        }

        Integer listSize = query.getPagination().getListSize();
        List<TicketSearchValue> searchKeyValues = TicketSearchFieldMapper.mapSearchField(query.getTicketSearchCriteria());
        List<UUID> validRuleGuids = rulesDao.getCustomRulesForTicketsByUser(loggedInUser);
        List<String> validRuleStrings = validRuleGuids.stream().map(guid -> guid.toString()).collect(Collectors.toList());

        String sql = TicketSearchFieldMapper.createSelectSearchSql(searchKeyValues, validRuleStrings, true);
        List<Ticket> ticketEntityList = rulesDao.getTicketListPaginated(query.getPagination().getPage(), listSize, sql);
        Integer numberMatches = ticketEntityList.size();

        int numberOfPages = (int) (numberMatches / listSize);
        if (numberMatches % listSize != 0) {
            numberOfPages += 1;
        }

        TicketListResponseDto ticketListDto = new TicketListResponseDto();
        ticketListDto.setCurrentPage(query.getPagination().getPage());
        ticketListDto.setTotalNumberOfPages(numberOfPages);
        ticketListDto.setTicketList(ticketEntityList);
        return ticketListDto;
    }

    public Instant getLastTriggeredForRule(UUID ruleGuid) {
        Ticket ticket = rulesDao.getLatestTicketForRule(ruleGuid);
        Instant retVal = null;
        if (ticket != null) {
            retVal = ticket.getUpdated();
        }
        return retVal;
    }

    public List<Ticket> getTicketsByMovements(List<String> movements) {
        if (movements == null) {
            throw new IllegalArgumentException("Movements list is null");
        }
        if (movements.isEmpty()) {
            throw new IllegalArgumentException("Movements list is empty");
        }

        return rulesDao.getTicketsByMovements(movements);
    }

    public GetTicketsAndRulesByMovementsResponse getTicketsAndRulesByMovements(List<String> movementGuidList) {
        List<TicketAndRuleType> ticketsAndRules = new ArrayList<>();
        // TODO: This can be done more efficiently with some join stuff
        List<Ticket> tickets = rulesDao.getTicketsByMovements(movementGuidList);
        for (Ticket ticket : tickets) {
            CustomRule rule = rulesDao.getCustomRuleByGuid(UUID.fromString(ticket.getRuleGuid()));
            TicketType ticketType = TicketMapper.toTicketType(ticket);
            CustomRuleType ruleType = CustomRuleMapper.toCustomRuleType(rule);
            TicketAndRuleType ticketsAndRule = new TicketAndRuleType();
            ticketsAndRule.setTicket(ticketType);
            ticketsAndRule.setRule(ruleType);
            ticketsAndRules.add(ticketsAndRule);
        }

        GetTicketsAndRulesByMovementsResponse response = new GetTicketsAndRulesByMovementsResponse();
        response.getTicketsAndRules().addAll(ticketsAndRules);
        return response;
    }

    public long countTicketsByMovements(List<String> movements) {
        if (movements == null) {
            throw new IllegalArgumentException("Movements list is null");
        }
        if (movements.isEmpty()) {
            throw new IllegalArgumentException("Movements list is empty");
        }

        return rulesDao.countTicketListByMovements(movements);
    }

    public Ticket updateTicketStatus(Ticket ticket) {
        if (ticket == null || ticket.getGuid() == null) {
            throw new IllegalArgumentException("Ticket is null");
        }
        Ticket entity = rulesDao.getTicketByGuid(ticket.getGuid());

        entity.setStatus(ticket.getStatus());
        entity.setUpdated(Instant.now());
        entity.setUpdatedBy(ticket.getUpdatedBy());

        rulesDao.updateTicket(entity);

        CustomRule customRule = getCustomRuleOrAssetNotSendingRule(ticket.getRuleGuid());

        // Notify long-polling clients of the update
        ticketUpdateEvent.fire(new EventTicket(entity, customRule));
        // Notify long-polling clients of the change (no value since FE will need to fetch it)
        ticketCountEvent.fire(new NotificationMessage("ticketCount", null));
        auditService.sendAuditMessage(AuditObjectTypeEnum.TICKET, AuditOperationEnum.UPDATE, entity.getGuid().toString(), "", ticket.getUpdatedBy());
        return entity;

    }

    public List<Ticket> updateTicketStatusByQuery(String loggedInUser, TicketQuery query, TicketStatusType status) {
        if (loggedInUser == null) {
            throw new IllegalArgumentException("LoggedInUser is null, can not update status");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is null, can not update status");
        }
        if (query == null) {
            throw new IllegalArgumentException("Status is null, can not update status");
        }
        List<TicketSearchValue> searchKeyValues = TicketSearchFieldMapper.mapSearchField(query.getTicketSearchCriteria());
        List<UUID> validRuleGuids = rulesDao.getCustomRulesForTicketsByUser(loggedInUser);
        List<String> validRuleStrings = new ArrayList<>();
        for (UUID uuid : validRuleGuids) {
            validRuleStrings.add(uuid.toString());
        }
        String sql = TicketSearchFieldMapper.createSelectSearchSql(searchKeyValues, validRuleStrings, true);
        List<Ticket> tickets = rulesDao.getTicketList(sql);
        for (Ticket ticket : tickets) {
            ticket.setStatus(status);
            ticket.setUpdated(Instant.now());
            ticket.setUpdatedBy(loggedInUser);

            rulesDao.updateTicket(ticket);

            CustomRule customRule = getCustomRuleOrAssetNotSendingRule(ticket.getRuleGuid());

            // Notify long-polling clients of the update
            ticketUpdateEvent.fire(new EventTicket(ticket, customRule));
            auditService.sendAuditMessage(AuditObjectTypeEnum.TICKET, AuditOperationEnum.UPDATE, ticket.getGuid().toString(), null, loggedInUser);
        }

        // Notify long-polling clients of the change (no value since FE will need to fetch it)
        ticketCountEvent.fire(new NotificationMessage("ticketCount", null));
        return tickets;
    }

    public long getNumberOfAssetsNotSending() {
        return rulesDao.getNumberOfTicketsForRule(ServiceConstants.ASSET_NOT_SENDING_RULE);
    }


    // Triggered by CheckCommunicationTask
    public List<PreviousReport> getPreviousMovementReports() {
        return rulesDao.getPreviousReportList();
    }


    // Triggered by CheckCommunicationTask
    public void timerRuleTriggered(String ruleName, PreviousReport previousReport) {
        LOG.info("Timer rule triggered for asset: {}", previousReport.getAssetGuid());

        Ticket ticketEntity = createAssetNotSendingDummyTicket(ruleName, previousReport, null);
        incidentProducer.updatedTicket(new EventTicket(ticketEntity, ServiceConstants.ASSET_NOT_SENDING_CUSTOMRULE));
    }

    public Ticket createAssetNotSendingDummyTicket(String ruleName, PreviousReport previousReport, String pollId) {
        Ticket ticket = new Ticket();
        ticket.setAssetGuid(previousReport.getAssetGuid());
        if (previousReport.getMovementGuid() != null)
            ticket.setMovementGuid(previousReport.getMovementGuid().toString());
        if (previousReport.getMobTermGuid() != null)
            ticket.setMobileTerminalGuid(previousReport.getMobTermGuid().toString());
        Instant now = Instant.now();
        ticket.setCreatedDate(now);
        ticket.setRuleName(ruleName);
        ticket.setRuleGuid(ruleName);
        ticket.setUpdatedBy("UVMS");
        ticket.setUpdated(now);
        ticket.setStatus(TicketStatusType.POLL_PENDING);
        ticket.setTicketCount(1L);

        return ticket;
    }

    public Ticket createDummyTicket(MovementDetails movementDetails) {
        Ticket ticket = new Ticket();
        ticket.setAssetGuid(movementDetails.getAssetGuid());
        if (movementDetails.getMovementGuid() != null)
            ticket.setMovementGuid(movementDetails.getMovementGuid());
        if (movementDetails.getMobileTerminalGuid() != null)
            ticket.setMobileTerminalGuid(movementDetails.getMobileTerminalGuid());
        Instant now = Instant.now();
        ticket.setCreatedDate(now);
        ticket.setRuleName("Dummy rule name");
        ticket.setRuleGuid("Dummy rule guid");
        ticket.setUpdatedBy("UVMS");
        ticket.setUpdated(now);
        ticket.setStatus(TicketStatusType.OPEN);
        ticket.setTicketCount(1L);

        return ticket;
    }

    public Ticket updateTicketCount(Ticket ticket) {
        if (ticket == null || ticket.getGuid() == null) {
            throw new IllegalArgumentException("Ticket is null, can not upate status");
        }
        ticket.setUpdated(Instant.now());

        CustomRule customRule = getCustomRuleOrAssetNotSendingRule(ticket.getRuleGuid());

        // Notify long-polling clients of the update
        ticketUpdateEvent.fire(new EventTicket(ticket, customRule));
        // Notify long-polling clients of the change (no value since FE will need to fetch it)
        ticketCountEvent.fire(new NotificationMessage("ticketCount", null));
        auditService.sendAuditMessage(AuditObjectTypeEnum.TICKET, AuditOperationEnum.UPDATE, ticket.getGuid().toString(), null, ticket.getUpdatedBy());
        return ticket;
    }


    public Ticket getTicketByGuid(UUID guid) {
        return rulesDao.getTicketByGuid(guid);
    }

    private boolean hasFeature(UserContext userContext, String featureName) {
        for (fish.focus.wsdl.user.types.Context c : userContext.getContextSet().getContexts()) {
            for (Feature f : c.getRole().getFeature()) {
                if (featureName.equals(f.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValid(CustomRuleType customRule) {
        return CustomRuleValidator.isCustomRuleValid(customRule);
    }

    public String getApplicationName(ServletContext servletContext) {
        String cfgName = servletContext.getInitParameter("usmApplication");
        if (cfgName == null) {
            cfgName = "Union-VMS";
        }
        return cfgName;
    }
}
