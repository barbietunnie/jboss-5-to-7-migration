package com.es.dao.sender;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.es.dao.abst.AbstractDao;
import com.es.vo.comm.ReloadFlagsVo;

@Component("reloadFlagsDao")
public class ReloadFlagsDao extends AbstractDao {
	protected static final Logger logger = Logger.getLogger(ReloadFlagsDao.class);
	
	public ReloadFlagsVo select() {
		return selectWithRepair(0);
	}
	
	private ReloadFlagsVo selectWithRepair(int retry) {
		String sql = "select * from Reload_Flags ";
		List<ReloadFlagsVo> list = getJdbcTemplate().query(sql, new BeanPropertyRowMapper<ReloadFlagsVo>(ReloadFlagsVo.class));
		if (list.size()>0) {
			return (ReloadFlagsVo)list.get(0);
		}
		else if (retry < 1) {
			repair();
			return selectWithRepair(++retry);
		}
		else {
			throw new RuntimeException("Internal error, contact programming.");
		}
	}
	
	public int update(ReloadFlagsVo vo) {
		String sql = "update Reload_Flags set " +
			"Senders=?," +
			"Rules=?," +
			"Actions=?," +
			"Templates=?," +
			"Schedules=?";
		List<Object> fields = new ArrayList<Object>();
		fields.add(vo.getSenders());
		fields.add(vo.getRules());
		fields.add(vo.getActions());
		fields.add(vo.getTemplates());
		fields.add(vo.getSchedules());

		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}
	
	public int updateSenderReloadFlag() {
		String sql = "update Reload_Flags set " +
			"Senders=Senders + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateRuleReloadFlag() {
		String sql = "update Reload_Flags set " +
			"Rules=Rules + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateActionReloadFlag() {
		String sql = "update Reload_Flags set " +
			"Actions=Actions + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateTemplateReloadFlag() {
		String sql = "update Reload_Flags set " +
			"Templates=Templates + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateScheduleReloadFlag() {
		String sql = "update Reload_Flags set " +
			"Schedules=Schedules + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	private int repair() {
		String sql = "insert into Reload_Flags (" +
				"Senders," +
				"Rules," +
				"Actions," +
				"Templates," +
				"Schedules) " +
				" values (" +
				"0,0,0,0,0)";
		int rowsInserted = getJdbcTemplate().update(sql);
		return rowsInserted;
	}
}
