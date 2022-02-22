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

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

//@formatter:off
@Entity
@Table(name = "rulesegment", indexes = {
        @Index(columnList = "ruleseg_rule_id", name = "ruleseg_rule_fk_inx", unique = false)})
@XmlRootElement
//@formatter:on
public class RuleSegment implements Serializable {  //Type class is customRuleSegmentType

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ruleseg_id")
    private UUID id;        //internal DB id

    @Column(name = "ruleseg_start_operator")
    private String startOperator;   //exists in type, same name, preferably ( or empty

    @Column(name = "ruleseg_criteria")
    private String criteria;    //Expects a value from the CriteriaType class, exists in type, same name

    @Column(name = "ruleseg_subcriteria")
    private String subCriteria; //Expects a value from the SubCriteriaType class, exists in type, same name

    @Column(name = "ruleseg_condition")
    private String condition;   //Expects a value from the ConditionType class, exists in type, same name

    @Column(name = "ruleseg_value")
    private String value;       //exists in type, same name

    @Column(name = "ruleseg_end_operator")
    private String endOperator; //exists in type, same name, preferably ) or empty

    @Column(name = "ruleseg_logic_operator")
    private String logicOperator;   //Expects a value from the LogicOperatorType class, exists in type, same name

    @Column(name = "ruleseg_order")
    private Integer order;  //exists in type, same name

    @JoinColumn(name = "ruleseg_rule_id", referencedColumnName = "rule_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CustomRule customRule;  //does not exist in type

    public RuleSegment copy (CustomRule newCustomRule){
        RuleSegment rs = new RuleSegment();
        rs.setStartOperator(startOperator);
        rs.setCriteria(criteria);
        rs.setSubCriteria(subCriteria);
        rs.setCondition(condition);
        rs.setValue(value);
        rs.setEndOperator(endOperator);
        rs.setLogicOperator(logicOperator);
        rs.setOrder(order);

        rs.setCustomRule(newCustomRule);
        return rs;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStartOperator() {
        return startOperator;
    }

    public void setStartOperator(String startOperator) {
        this.startOperator = startOperator;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getSubCriteria() {
        return subCriteria;
    }

    public void setSubCriteria(String subCriteria) {
        this.subCriteria = subCriteria;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEndOperator() {
        return endOperator;
    }

    public void setEndOperator(String endOperator) {
        this.endOperator = endOperator;
    }

    public String getLogicOperator() {
        return logicOperator;
    }

    public void setLogicOperator(String logicOperator) {
        this.logicOperator = logicOperator;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public CustomRule getCustomRule() {
        return customRule;
    }

    public void setCustomRule(CustomRule customRule) {
        this.customRule = customRule;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, startOperator, criteria, subCriteria, condition, value, endOperator, logicOperator, order, customRule);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuleSegment) {
            RuleSegment other = (RuleSegment) obj;
            if (startOperator != null && !startOperator.equals(other.startOperator)) {
                return false;
            } else if (startOperator == null && other.startOperator != null) {
                return false;
            }
            if (criteria != null && !criteria.equals(other.criteria)) {
                return false;
            } else if (criteria == null && other.criteria != null) {
                return false;
            }
            if (subCriteria != null && !subCriteria.equals(other.subCriteria)) {
                return false;
            } else if (subCriteria == null && other.subCriteria != null) {
                return false;
            }
            if (condition != null && !condition.equals(other.condition)) {
                return false;
            } else if (condition == null && other.condition != null) {
                return false;
            }
            if (value != null && !value.equals(other.value)) {
                return false;
            } else if (value == null && other.value != null) {
                return false;
            }
            if (endOperator != null && !endOperator.equals(other.endOperator)) {
                return false;
            } else if (endOperator == null && other.endOperator != null) {
                return false;
            }
            if (logicOperator != null && !logicOperator.equals(other.logicOperator)) {
                return false;
            } else if (logicOperator == null && other.logicOperator != null) {
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