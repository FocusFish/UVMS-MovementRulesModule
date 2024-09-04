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

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

//@formatter:off
@Entity
@Table(name = "action" , indexes = {
        @Index(columnList = "action_rule_id", name = "action_rule_fk_inx", unique = false)})
@XmlRootElement
//@formatter:on
public class RuleAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "action_id")
    private UUID id;

    @Column(name = "action_action")
    private String action;

    @Column(name = "action_target")
    private String target;

    @Column(name = "action_value")
    private String value;

    @Column(name = "action_order")
    private Integer order;

    @JoinColumn(name = "action_rule_id", referencedColumnName = "rule_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomRule customRule;

    public RuleAction copy(CustomRule newCustomRule) {
        RuleAction copy = new RuleAction();
        copy.setAction(action);
        copy.setTarget(target);
        copy.setValue(value);
        copy.setOrder(order);
        copy.setCustomRule(newCustomRule);

        return copy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public CustomRule getCustomRule() {
        return customRule;
    }

    public void setCustomRule(CustomRule customRule) {
        this.customRule = customRule;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, action, target, value, order, customRule);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuleAction) {
            RuleAction other = (RuleAction) obj;

            if (action != null && !action.equals(other.action)) {
                return false;

            } else if (action == null && other.action != null) {
                return false;
            }

            if (target != null && !target.equals(other.target)) {
                return false;
            } else if (target == null && other.target != null) {
                return false;
            }

            if (value != null && !value.equals(other.value)) {
                return false;
            } else if (value == null && other.value != null) {
                return false;
            }

            if (order != null && !order.equals(other.order)) {
                return false;
            } else if (order == null && other.order != null) {
                return false;
            }

            return true;
        }

        return false;
    }
}