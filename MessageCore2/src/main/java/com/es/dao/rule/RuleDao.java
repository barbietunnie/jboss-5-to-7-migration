package com.es.dao.rule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.rule.RuleElementVo;
import com.es.vo.rule.RuleLogicVo;
import com.es.vo.rule.RuleSubRuleMapVo;
import com.es.vo.rule.RuleVo;

@Component("ruleDao")
public class RuleDao extends AbstractDao {
	
	@Autowired
	private RuleLogicDao ruleLogicDao;
	@Autowired
	private RuleElementDao ruleElementDao;
	@Autowired
	private RuleSubRuleMapDao ruleSubRuleMapDao;
	
	// TODO
	private static final class RuleMapper implements RowMapper<RuleVo> {
		
		public RuleVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RuleVo ruleVo = new RuleVo();
			
			ruleVo.setRuleName(rs.getString("RuleName"));
			
			RuleLogicVo ruleLogicVo = new RuleLogicVo();
			ruleLogicVo.setRuleName(rs.getString("RuleName"));
			ruleLogicVo.setRuleSeq(rs.getInt("RuleSeq"));
			ruleLogicVo.setRuleType(rs.getString("RuleType"));
			ruleLogicVo.setStatusId(rs.getString("StatusId"));
			ruleLogicVo.setStartTime(rs.getTimestamp("StartTime"));
			ruleLogicVo.setMailType(rs.getString("MailType"));
			ruleLogicVo.setRuleCategory(rs.getString("RuleCategory"));
			ruleLogicVo.setIsSubRule(rs.getString("IsSubRule"));
			
			RuleElementVo ruleElementVo = new RuleElementVo();
			ruleElementVo.setRuleName(rs.getString("RuleName"));
			ruleElementVo.setElementSeq(rs.getInt("ElementSeq"));
			ruleElementVo.setDataName(rs.getString("DataName"));
			ruleElementVo.setHeaderName(rs.getString("HeaderName"));
			ruleElementVo.setCriteria(rs.getString("Criteria"));
			ruleElementVo.setCaseSensitive(rs.getString("CaseSensitive"));
			ruleElementVo.setTargetText(rs.getString("TargetText"));
			ruleElementVo.setExclusions(rs.getString("Exclusions"));
			ruleElementVo.setExclListProc(rs.getString("ExclListProc"));
			ruleElementVo.setDelimiter(rs.getString("Delimiter"));
			
			ruleVo.setRuleLogicVo(ruleLogicVo);
			ruleVo.getRuleElementVos().add(ruleElementVo);
			return ruleVo;
		}
	}

	public RuleVo getByPrimaryKey(String ruleName) {
		String sql = 
			"select " +
			"  logic.*, element.* " +
			" from Rule_Logic as logic " +
			"  join Rule_Element as element on logic.ruleName=element.ruleName "+
			" where logic.ruleName=? " +
			" order by element.elementSeq ";
		
		Object[] parms = new Object[] {ruleName};
		
		List<RuleVo> list = getJdbcTemplate().query(sql, parms, new RuleMapper());
		if (list == null || list.size() == 0) {
			return null;
		}

		RuleVo vo = null;
		for (int i = 0; i < list.size(); i++) {
			RuleVo ruleVo = list.get(i);
			if (vo == null) {
				vo = ruleVo;
			}
			else {
				vo.getRuleElementVos().addAll(ruleVo.getRuleElementVos());
			}
		}
		List<RuleSubRuleMapVo> subRules = getRuleSubRuleMapDao().getByRuleName(vo.getRuleName());
		vo.getRuleSubRuleVos().addAll(subRules);
		return vo;
	}
	
	/**
	 * returns a list of RuleVo's
	 */
	public List<RuleVo> getActiveRules() {
		List<RuleLogicVo> ruleLogics = getRuleLogicDao().getActiveRules();
		List<RuleVo> ruleVos = new ArrayList<RuleVo>();
		for (int i = 0; i < ruleLogics.size(); i++) {
			RuleLogicVo ruleLogicVo = ruleLogics.get(i);
			RuleVo ruleVo = new RuleVo();
			ruleVo.setRuleName(ruleLogicVo.getRuleName());
			ruleVo.setRuleLogicVo(ruleLogicVo);
			List<RuleElementVo> elements = getRuleElementDao().getByRuleName(
					ruleLogicVo.getRuleName());
			ruleVo.getRuleElementVos().addAll(elements);
			List<RuleSubRuleMapVo> subRules = getRuleSubRuleMapDao().getByRuleName(
					ruleLogicVo.getRuleName());
			ruleVo.getRuleSubRuleVos().addAll(subRules);
			ruleVos.add(ruleVo);
		}
		return ruleVos;
	}
	
	RuleSubRuleMapDao getRuleSubRuleMapDao() {
		return ruleSubRuleMapDao;
	}

	RuleElementDao getRuleElementDao() {
		return ruleElementDao;
	}

	RuleLogicDao getRuleLogicDao() {
		return ruleLogicDao;
	}

}
