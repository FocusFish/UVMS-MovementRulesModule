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

package fish.focus.uvms.movementrules.service.entity;

import fish.focus.schema.movementrules.ticket.v1.TicketStatusType;
import fish.focus.uvms.movementrules.service.constants.ServiceConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = Ticket.FIND_TICKET_BY_ASSET_AND_RULE, query = "SELECT t FROM Ticket t WHERE t.assetGuid = :assetGuid and t.status <> 'CLOSED' and t.ruleGuid = :ruleGuid"),
        @NamedQuery(name = Ticket.FIND_TICKETS_BY_MOVEMENTS, query = "SELECT t FROM Ticket t where t.movementGuid IN :movements"),
        @NamedQuery(name = Ticket.COUNT_OPEN_TICKETS, query = "SELECT count(t) FROM Ticket t where t.status = 'OPEN' AND t.ruleGuid IN :validRuleGuids"),
        @NamedQuery(name = Ticket.COUNT_TICKETS_BY_MOVEMENTS, query = "SELECT count(t) FROM Ticket t where t.movementGuid IN :movements"),
        @NamedQuery(name = Ticket.COUNT_TICKETS_FOR_RULE, query = "SELECT count(t) FROM Ticket t where t.ruleGuid = :ruleGuid"),
        @NamedQuery(name = Ticket.FIND_LATEST_TICKET_FOR_RULE, query = "SELECT t FROM Ticket t WHERE t.ruleGuid = :ruleGuid ORDER BY t.createdDate DESC"),
        @NamedQuery(name = Ticket.FIND_ALL_ASSET_NOT_SENDING_TICKETS_BETWEEN, query = "SELECT t FROM Ticket t WHERE t.ruleGuid = '" + ServiceConstants.ASSET_NOT_SENDING_RULE + "' AND t.updated BETWEEN :from AND :to ORDER BY t.updated DESC")
})
public class Ticket implements Serializable {

    public static final String FIND_TICKET_BY_ASSET_AND_RULE = "Ticket.findByAssetGuid";
    public static final String FIND_TICKETS_BY_MOVEMENTS = "Ticket.findByMovementGuids";
    public static final String COUNT_OPEN_TICKETS = "Ticket.countOpenTickets";
    public static final String COUNT_TICKETS_BY_MOVEMENTS = "Ticket.countByMovementGuids";
    public static final String COUNT_TICKETS_FOR_RULE = "Ticket.countAssetsNotSending";
    public static final String FIND_LATEST_TICKET_FOR_RULE = "Ticket.findLastTicketForRule";
    public static final String FIND_ALL_ASSET_NOT_SENDING_TICKETS_BETWEEN = "Ticket.findAllAssetNotSendingTicketsBetween";

    private static final long serialVersionUID = -2423644165824696757L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ticket_id")
    private UUID guid;    //exists in Type, same name

    @Column(name = "ticket_assetguid")
    private String assetGuid;   //exists in Type, same name

    @Column(name = "ticket_mobileterminalguid")
    private String mobileTerminalGuid;  //exists in Type, same name

    @Column(name = "ticket_channelguid")
    private String channelGuid;     //exists in Type, same name

    @Column(name = "ticket_ruleguid")
    private String ruleGuid;        //exists in Type, same name

    @Column(name = "ticket_rulename")
    private String ruleName;        //exists in Type, same name

    @Column(name = "ticket_recipient")
    private String recipient;       //exists in Type, same name

    @Column(name = "ticket_movementguid")
    private String movementGuid;    //exists in Type, same name

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status")
    private TicketStatusType status;          //expects values from TicketStatusType, exists in Type, same name

    @Column(name = "ticket_count")
    private Long ticketCount;       //exists in Type, same name

    @Column(name = "ticket_createddate")
    private Instant createdDate;       //exists in Type as openDate

    @Column(name = "ticket_updattim")
    @NotNull
    private Instant updated;       //exists in Type, same name

    @Column(name = "ticket_upuser")
    @NotNull
    private String updatedBy;   //exists in Type, same name

    //TicketType has a variable called comment that is not present in this class

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public String getAssetGuid() {
        return assetGuid;
    }

    public void setAssetGuid(String assetGuid) {
        this.assetGuid = assetGuid;
    }

    public String getMobileTerminalGuid() {
        return mobileTerminalGuid;
    }

    public void setMobileTerminalGuid(String mobileTerminalGuid) {
        this.mobileTerminalGuid = mobileTerminalGuid;
    }

    public String getChannelGuid() {
        return channelGuid;
    }

    public void setChannelGuid(String channelGuid) {
        this.channelGuid = channelGuid;
    }

    public String getRuleGuid() {
        return ruleGuid;
    }

    public void setRuleGuid(String ruleGuid) {
        this.ruleGuid = ruleGuid;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMovementGuid() {
        return movementGuid;
    }

    public void setMovementGuid(String movementGuid) {
        this.movementGuid = movementGuid;
    }

    public TicketStatusType getStatus() {
        return status;
    }

    public void setStatus(TicketStatusType status) {
        this.status = status;
    }

    public Long getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(Long ticketCount) {
        this.ticketCount = ticketCount;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}