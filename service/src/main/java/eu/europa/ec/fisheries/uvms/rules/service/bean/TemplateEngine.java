/*
 *
 * Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries � European Union, 2015-2016.
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import eu.europa.ec.fisheries.remote.RulesDomainModel;
import eu.europa.ec.fisheries.schema.rules.rule.v1.Rule;
import eu.europa.ec.fisheries.schema.rules.template.v1.Template;
import eu.europa.ec.fisheries.uvms.rules.model.exception.RulesModelException;
import eu.europa.ec.fisheries.uvms.rules.service.business.AbstractFact;
import eu.europa.ec.fisheries.uvms.rules.service.lifecycle.RuleLifecycleContainer;

@Stateless
public class TemplateEngine {

    @Inject
    private RulesDomainModel rulesDb;

    @Inject
	private RuleLifecycleContainer ruleLifecycleContainer;
    
    public void evaluate(AbstractFact fact) throws RulesModelException {
    	List<String> rules = generateAllRules();
    	ruleLifecycleContainer.triggerEvaluation(rules, fact);
    }
    
    private List<String> generateAllRules() throws RulesModelException {
        List<Template> templates = rulesDb.getAllTemplates();
        List<String> allRules = new ArrayList<>();
        for (Template template : templates) {
        	TemplateRuleGenerator datasource = ruleLifecycleContainer.getRuleGenerator(template);
            List<Rule> rules = rulesDb.getRuleByType(template.getRuleType());
        	List<String> ruleDefinition  = datasource.computeRules(template, rules);
        	allRules.addAll(ruleDefinition);
        }
    	return allRules;
    }
}
