package com.legacytojava.message.ejb.rule;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.logging.Logger;

import com.legacytojava.jbatch.SpringUtil;
import com.legacytojava.message.bo.rule.RulesDataBo;
import com.legacytojava.message.vo.rule.RuleVo;

/**
 * Session Bean implementation class RulesData
 */
@Stateless(mappedName = "ejb/RulesData")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(mappedName = "java:jboss/MessageDS", 
	name = "jdbc/msgdb_pool", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(RulesDataRemote.class)
@Local(RulesDataLocal.class)
public class RulesData implements RulesDataRemote, RulesDataLocal {
	protected static final Logger logger = Logger.getLogger(RulesData.class);
	@Resource
	SessionContext context;
	private RulesDataBo rulesDataBo;
    /**
     * Default constructor. 
     */
    public RulesData() {
    	rulesDataBo = (RulesDataBo)SpringUtil.getAppContext().getBean("rulesDataBo");
    }

	public List<RuleVo> getCurrentRules() {
		List<RuleVo> rules = rulesDataBo.getCurrentRules();
		return rules;
	}

	public RuleVo getRuleByPrimaryKey(String key) {
		RuleVo ruleVo = rulesDataBo.getRuleByPrimaryKey(key);
		return ruleVo;
	}
}
