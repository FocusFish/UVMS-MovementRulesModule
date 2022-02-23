package fish.focus.uvms.movementrules.rest.service.arquillian;

import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import fish.focus.schema.movementrules.customrule.v1.CustomRuleType;
import fish.focus.uvms.commons.date.JsonBConfigurator;
import fish.focus.uvms.rest.security.UnionVMSFeature;
import fish.focus.uvms.usm.jwt.JwtTokenHandler;
import fish.focus.uvms.movementrules.service.BuildRulesServiceDeployment;

public abstract class BuildRulesRestDeployment extends BuildRulesServiceDeployment {

    @Inject
    private JwtTokenHandler tokenHandler;

    private String token;
   
    protected WebTarget getWebTarget() {
        return ClientBuilder.newClient().register(JsonBConfigurator.class).target("http://localhost:8080/test/rest");
    }

    protected String getToken() {
        if (token == null) {
            token = tokenHandler.createToken("user", 
                    Arrays.asList(UnionVMSFeature.viewAlarmRules.getFeatureId(), 
                            UnionVMSFeature.manageAlarmRules.getFeatureId(),
                            UnionVMSFeature.manageAlarmsOpenTickets.getFeatureId(),
                            UnionVMSFeature.manageGlobalAlarmsRules.getFeatureId(),
                            UnionVMSFeature.viewAlarmsOpenTickets.getFeatureId()));
        }
        return token;
    }
    
    protected String getTokenExternal() {
        if (token == null) {
            token = ClientBuilder.newClient()
                    .target("http://localhost:8080/unionvms/user/rest/user/token")
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);
        }
        return token;
    }
}
