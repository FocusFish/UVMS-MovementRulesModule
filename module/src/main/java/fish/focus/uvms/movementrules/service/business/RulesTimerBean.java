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
package fish.focus.uvms.movementrules.service.business;

import fish.focus.uvms.asset.client.AssetClient;
import fish.focus.uvms.config.service.ParameterService;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.bean.ValidationServiceBean;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Startup
@Singleton
@DependsOn({"RulesValidator"})
public class RulesTimerBean {

    private static final Logger LOG = LoggerFactory.getLogger(RulesTimerBean.class);

    @EJB
    private RulesServiceBean rulesService;

    @EJB
    private ValidationServiceBean validationService;

    @EJB
    private RulesValidator rulesValidator;

    @EJB
    private ParameterService parameterService;

    @Inject
    private AssetClient assetClient;

    @EJB
    private RulesDao rulesDao;

    private ScheduledFuture<?> comm;

    private ScheduledFuture<?> changes;

    @PostConstruct
    public void postConstruct() {
        LOG.debug("RulesTimerBean init");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
        CheckCommunicationTask checkCommunicationTask = new CheckCommunicationTask(rulesService, parameterService, assetClient, rulesDao);
        comm = executorService.scheduleWithFixedDelay(checkCommunicationTask, 10, 10, TimeUnit.MINUTES);
        CheckRulesChangesTask checkRulesChangesTask = new CheckRulesChangesTask(validationService, rulesValidator, rulesService);
        changes = executorService.scheduleWithFixedDelay(checkRulesChangesTask, 10, 10, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void preDestroy() {
        if (comm != null) {
            comm.cancel(true);
        }
        if (changes != null) {
            changes.cancel(true);
        }
    }
}
