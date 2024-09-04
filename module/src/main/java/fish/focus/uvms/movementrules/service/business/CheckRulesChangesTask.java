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

import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Singleton
public class CheckRulesChangesTask {

    private static final Logger LOG = LoggerFactory.getLogger(CheckRulesChangesTask.class);

    private RulesValidator rulesValidator;
    private RulesServiceBean rulesService;

    public CheckRulesChangesTask() {
    }

    @Inject
    public CheckRulesChangesTask(RulesValidator rulesValidator, RulesServiceBean rulesService) {
        this.rulesValidator = rulesValidator;
        this.rulesService = rulesService;
    }

    @Schedule(minute = "*/10", hour = "*", persistent = false)
    public void clearCustomRules() {
        LOG.debug("Looking outdated custom rules");
        List<CustomRule> customRules = rulesService.getRunnableCustomRules();
        boolean updateNeeded = false;
        for (CustomRule rule : customRules) {
            // If there are no time intervals, we do not need to check if the rule should be inactivated.
            boolean inactivate = !rule.getIntervals().isEmpty();
            Optional<Instant> latest = rule.getIntervals().stream().map(Interval::getEnd).max(Instant::compareTo);
            if (latest.isPresent()) {
                Instant end = latest.get();
                Instant now = Instant.now();
                if (end.isAfter(now)) {
                    inactivate = false;
                }
            }
            if (inactivate) {
                LOG.debug("Inactivating {}", rule.getName());
                rule.setActive(false);
                rule.setUpdatedBy("UVMS Out of date checker");
                updateNeeded = true;
            }
        }
        if (updateNeeded) {
            LOG.debug("Clear outdated custom rules");
            rulesValidator.updateCustomRules();
        }
    }
}