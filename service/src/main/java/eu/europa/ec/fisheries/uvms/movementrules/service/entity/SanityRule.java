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
package eu.europa.ec.fisheries.uvms.movementrules.service.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

//@formatter:off
@Entity
@Table(name = "sanityrule")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = SanityRule.FIND_ALL_SANITY_RULES, query = "SELECT r FROM SanityRule r") // for rule engine
})
//@formatter:on
public class SanityRule implements Serializable {

    public static final String FIND_ALL_SANITY_RULES = "SanityRule.findAll";
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sanityrule_id")
    private Long id;        //internal DB id

    @NotNull
    @Column(name = "sanityrule_name")
    private String name;    //exists in Type, same name

    @NotNull
    @Column(name = "sanityrule_guid")
    private String guid;    //exists in Type, same name

    @Column(name = "sanityrule_description")
    private String description; //exists in Type, same name

    @Column(name = "sanityrule_expression")
    private String expression;  //exists in Type, same name

    @Column(name = "sanityrule_updattim")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;       //exists in Type, same name

    @Column(name = "sanityrule_upuser")
    @NotNull
    private String updatedBy;   //exists in Type, same name

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}