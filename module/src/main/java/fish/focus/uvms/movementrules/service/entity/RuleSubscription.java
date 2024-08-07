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

import fish.focus.schema.movementrules.customrule.v1.SubscriptionTypeType;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.UUID;

//@formatter:off
@Entity
@Table(name = "rulesubscription", indexes = {
        @Index(columnList = "rulesub_rule_id", name = "rulesub_rule_fk_inx", unique = false)})
@XmlRootElement
//@formatter:on
public class RuleSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rulesub_id")
    private UUID id;        //internal DB id

    @Column(name = "rulesub_owner")
    private String owner;   //exists in Type, same name

    @Column(name = "rulesub_type")
    private String type;    //expects a value from SubscriptionTypeType, exists in Type, same name

    @JoinColumn(name = "rulesub_rule_id", referencedColumnName = "rule_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomRule customRule;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType(SubscriptionTypeType type) {
        this.type = type.value();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public CustomRule getCustomRule() {
        return customRule;
    }

    public void setCustomRule(CustomRule customRule) {
        this.customRule = customRule;
    }

}