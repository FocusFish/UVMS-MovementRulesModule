package fish.focus.uvms.movementrules.service;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Before;

public class TransactionalTests extends BuildRulesServiceDeployment {

    @Inject
    protected UserTransaction userTransaction;

    @Before
    public void before() throws SystemException, NotSupportedException {
        userTransaction.begin();
    }

    @After
    public void cleanupTransaction() throws SystemException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        userTransaction.rollback();
        //userTransaction.commit();
    }

}

