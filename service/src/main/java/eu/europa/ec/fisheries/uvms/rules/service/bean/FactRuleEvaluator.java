/*
 *
 * Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package eu.europa.ec.fisheries.uvms.rules.service.bean;

import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.schema.rules.rule.v1.ExternalRuleType;
import eu.europa.ec.fisheries.schema.rules.rule.v1.RuleType;
import eu.europa.ec.fisheries.uvms.rules.model.dto.TemplateRuleMapDto;
import eu.europa.ec.fisheries.uvms.rules.service.SalesRulesService;
import eu.europa.ec.fisheries.uvms.rules.service.business.AbstractFact;
import eu.europa.ec.fisheries.uvms.rules.service.business.RulesValidator;
import eu.europa.ec.fisheries.uvms.rules.service.business.TemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.drools.template.parser.DefaultTemplateContainer;
import org.drools.template.parser.TemplateContainer;
import org.drools.template.parser.TemplateDataListener;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

@Slf4j
@Singleton
@DependsOn({"MDRCacheServiceBean"})
public class FactRuleEvaluator {

    @EJB
    private SalesRulesService salesRulesService;

    @EJB
    private MDRCacheRuleService mdrCacheRuleService;

    @EJB
    private RulesValidator rulesValidator;

    private List<String> failedRules = new ArrayList<>();
    private List<AbstractFact> exceptionsList = new ArrayList<>();

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void initializeRules(Collection<TemplateRuleMapDto> templates) {
        Map<String, String> drlsAndRules = new HashMap<>();
        for (TemplateRuleMapDto template : templates) {
            String templateFile = TemplateFactory.getTemplateFileName(template.getTemplateType().getType());
            String templateName = template.getTemplateType().getTemplateName();
            drlsAndRules.putAll(generateRulesFromTemplate(templateName, templateFile, template.getRules()));
            drlsAndRules.putAll(generateExternalRulesFromTemplate(template.getExternalRules()));
        }
        createAllPackages(drlsAndRules);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private Map<String, String> generateRulesFromTemplate(String templateName, String templateFile, List<RuleType> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return Collections.emptyMap();
        }
        InputStream templateStream = this.getClass().getResourceAsStream(templateFile);
        TemplateContainer tc = new DefaultTemplateContainer(templateStream);
        Map<String, String> drlsAndBrId = new HashMap<>();
        TemplateDataListener listener = new TemplateDataListener(tc);
        int rowNum = 0;
        for (RuleType ruleDto : rules) {
            listener.newRow(rowNum, 0);
            listener.newCell(rowNum, 0, templateName, 0);
            listener.newCell(rowNum, 1, ruleDto.getExpression(), 0);
            listener.newCell(rowNum, 2, ruleDto.getBrId(), 0);
            listener.newCell(rowNum, 3, ruleDto.getMessage(), 0);
            listener.newCell(rowNum, 4, ruleDto.getErrorType().value(), 0);
            listener.newCell(rowNum, 5, ruleDto.getLevel(), 0);
            listener.newCell(rowNum, 6, ruleDto.getPropertyNames(), 0);
            rowNum++;
        }
        listener.finishSheet();
        String drl = listener.renderDRL();
        log.debug(drl);
        drlsAndBrId.put(drl, templateName);
        return drlsAndBrId;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private Map<String, String> generateExternalRulesFromTemplate(List<ExternalRuleType> externalRules) {
        if (CollectionUtils.isEmpty(externalRules)) {
            return Collections.emptyMap();
        }
        Map<String, String> drlsAndBrId = new HashMap<>();
        for (ExternalRuleType extRuleType : externalRules) {
            String drl = extRuleType.getDrl();
            log.debug("DRL for BR Id {} : {} ", extRuleType.getBrId(), drl);
            drlsAndBrId.put(drl, extRuleType.getBrId());
        }
        return drlsAndBrId;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private Collection<KiePackage> createAllPackages(Map<String, String> drlsAndRules) {
        Collection<KiePackage> compiledPackages = new ArrayList<>();
        KieContainer container = null;

        List<String> systemPackagesPaths =  new ArrayList<>();
       // systemPackagesPaths.add("src/main/resources/rules/SanityRules.drl");
        String drl = rulesValidator.getSanityRuleDrlFile();
        KieFileSystem kieFileSystem = KieServices.Factory.get().newKieFileSystem();
        //kieFileSystem.write("src/main/resources/rules/SanityRules.drl", drl);

        String sruletemplateName = "SanityRules";
        drlsAndRules.put(drl, sruletemplateName);
        for (Map.Entry<String, String> ruleEntrySet : drlsAndRules.entrySet()) {
            String rule = ruleEntrySet.getKey();
            String templateName = ruleEntrySet.getValue();
            StringBuilder ruleName = new StringBuilder("src/main/resources/rule/");
            String systemPackage = ruleName.append(templateName).append(".drl").toString();
            systemPackagesPaths.add(systemPackage);
            kieFileSystem.write(systemPackage, rule);
            KieBuilder kieBuilder = KieServices.Factory.get().newKieBuilder(kieFileSystem).buildAll();
            if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
                log.error("Rule failed to build {} ", templateName);
                kieFileSystem.delete(ruleName.toString(), rule);
                failedRules.add(templateName);
            }
        }

        if (drlsAndRules.size() > 0){
            container = KieServices.Factory.get().newKieContainer(KieServices.Factory.get().getRepository().getDefaultReleaseId());
        }

        if (container != null) {
        	compiledPackages = container.getKieBase().getKiePackages();
        }
        return compiledPackages;
    }

    public void validateFact(Collection<AbstractFact> facts) {
        KieSession ksession = null;
        try {
            KieContainer container = KieServices.Factory.get().newKieContainer(KieServices.Factory.get().getRepository().getDefaultReleaseId());
            ksession = container.newKieSession();

            ksession.setGlobal("salesService", salesRulesService);
            ksession.setGlobal("mdrService", mdrCacheRuleService);

            for (AbstractFact fact : facts) { // Insert All the facts
                ksession.insert(fact);
            }

            ksession.fireAllRules();
            ksession.dispose();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Collection<?> objects = null;
            if (ksession != null) {
                objects = ksession.getObjects();
            }
            if (CollectionUtils.isNotEmpty(objects)) {
                Collection<AbstractFact> failedFacts = (Collection<AbstractFact>) objects;
                AbstractFact next = failedFacts.iterator().next();
                String message = e.getMessage();
                String brId = message.substring(message.indexOf('/') + 1, message.indexOf(".drl"));
                next.addWarningOrError("WARNING", message, brId, "L099", StringUtils.EMPTY);
                next.setOk(false);
                facts.remove(next); // remove fact with exception and re-validate the other facts
                exceptionsList.add(next);
                validateFact(facts);
            }
        }
    }

    public List<AbstractFact> getExceptionsList() {
        return exceptionsList;
    }
    public void setExceptionsList(List<AbstractFact> exceptionsList) {
        this.exceptionsList = exceptionsList;
    }
    public List<String> getFailedRules() {
        return failedRules;
    }
}
