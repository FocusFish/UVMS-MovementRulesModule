package fish.focus.uvms.movementrules.service.business;

import fish.focus.uvms.movementrules.service.RulesTestHelper;
import fish.focus.uvms.movementrules.service.TransactionalTests;
import fish.focus.uvms.movementrules.service.bean.RulesServiceBean;
import fish.focus.uvms.movementrules.service.dao.RulesDao;
import fish.focus.uvms.movementrules.service.entity.CustomRule;
import fish.focus.uvms.movementrules.service.entity.Interval;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Instant;

@RunWith(Arquillian.class)
public class CheckRulesChangesTaskTest extends TransactionalTests {

    @Inject
    RulesDao rulesDao;

    @Inject
    RulesValidator rulesValidator;

    @Inject
    RulesServiceBean rulesService;

    @Test
    @OperateOnDeployment("normal")
    public void checkRulesChangesTaskTest() {
        CustomRule customRule = RulesTestHelper.createCompleteCustomRule();
        Interval interval = new Interval();
        interval.setCustomRule(customRule);
        interval.setStart(Instant.ofEpochMilli(System.currentTimeMillis() - 20000));
        interval.setEnd(Instant.ofEpochMilli(System.currentTimeMillis() - 10000));
        customRule.getIntervals().add(interval);
        customRule.setUpdated(Instant.now());
        customRule.setUpdatedBy("TestUser");

        customRule = rulesDao.createCustomRule(customRule);
        Assert.assertTrue(customRule.getActive());

        CheckRulesChangesTask checkRulesChangesTask = new CheckRulesChangesTask(rulesValidator, rulesService);
        checkRulesChangesTask.clearCustomRules();

        CustomRule createdCustomRule = rulesDao.getCustomRuleByGuid(customRule.getGuid());

        Assert.assertEquals(customRule.getGuid(), createdCustomRule.getGuid());
        Assert.assertFalse(createdCustomRule.getActive());
        Assert.assertFalse(createdCustomRule.getArchived());
    }
}
