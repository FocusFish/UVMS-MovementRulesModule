package eu.europa.ec.fisheries.uvms.movementrules.service;

import eu.europa.ec.fisheries.uvms.movementrules.service.config.MovementRulesConfigHelper;
import eu.europa.ec.fisheries.uvms.movementrules.service.message.bean.RulesEventMessageConsumerBean;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Arrays;

@ArquillianSuiteDeployment
public abstract class BuildRulesServiceDeployment {

    final static Logger LOG = LoggerFactory.getLogger(BuildRulesServiceDeployment.class);
    
    @Deployment(name = "normal", order = 2)
    public static Archive<?> createDeployment() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war");

        File[] files = Maven.resolver().loadPomFromFile("pom.xml").importRuntimeAndTestDependencies().resolve()
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);

        Arrays.stream(files).sorted((f1, f2) -> f1.getName().compareTo(f2.getName())).forEach(f -> LOG.info(f.getName()));
        
        testWar.addAsResource(new File("src/main/resources/templates/CustomRulesTemplate.drt"),"/templates/CustomRulesTemplate.drt");
        testWar.addAsResource(new File("src/main/resources/templates/SanityRulesTemplate.drt"),"/templates/SanityRulesTemplate.drt");

        testWar.addPackages(true, "eu.europa.ec.fisheries.uvms.movementrules.service");
        
        testWar.addAsResource("persistence-integration.xml", "META-INF/persistence.xml");


        return testWar;
    }

    @Deployment(name = "uvms", order = 1)
    public static Archive<?> createAssetRestMock() {

        WebArchive testWar = ShrinkWrap.create(WebArchive.class, "UnionVMS.war");

        File[] files = Maven.configureResolver().loadPomFromFile("pom.xml")
                .resolve("eu.europa.ec.fisheries.uvms.asset:asset-model",
                        "eu.europa.ec.fisheries.uvms.asset:asset-client",
                        "eu.europa.ec.fisheries.uvms.commons:uvms-commons-message")
                .withTransitivity().asFile();
        testWar.addAsLibraries(files);


        testWar.addClass(UnionVMSRestMock.class);
        testWar.addClass(AssetMTRestMock.class);


        return testWar;
    }

}
