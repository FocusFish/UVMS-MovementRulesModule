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
package fish.focus.uvms.movementrules.rest;

import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.movementrules.rest.constants.RestConstants;
import fish.focus.uvms.movementrules.rest.filter.MovementRulesRestExceptionMapper;
import fish.focus.uvms.movementrules.rest.service.*;
import fish.focus.uvms.rest.security.UnionVMSFeatureFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath(RestConstants.MODULE_REST)
public class RestActivator extends Application {

    private final static Logger LOG = LoggerFactory.getLogger(RestActivator.class);

    private final Set<Object> singletons = new HashSet<>();
    private final Set<Class<?>> set = new HashSet<>();

    public RestActivator() {
        set.add(CustomRulesRestResource.class);
        set.add(TicketRestResource.class);
        set.add(ConfigResource.class);
        set.add(UnionVMSFeatureFilter.class);
        set.add(InternalRestResources.class);
        set.add(SSEResource.class);
        set.add(PreviousReportRestResource.class);

        set.add(JsonBConfigurator.class);
        set.add(MovementRulesRestExceptionMapper.class);
        LOG.info(RestConstants.MODULE_NAME + " module starting up");
    }

    @Override
    public Set<Class<?>> getClasses() {
        return set;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}