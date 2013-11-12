package com.legacytojava.message.dao.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.legacytojava.message.vo.ReloadFlagsVo;

@Component("reloadFlagsDao")
public class ReloadFlagsJdbcDao implements ReloadFlagsDao {
	protected static final Logger logger = Logger.getLogger(ReloadFlagsJdbcDao.class);
	
	@Autowired
	private DataSource mysqlDataSource;
	private JdbcTemplate jdbcTemplate;
	
	private JdbcTemplate getJdbcTemplate() {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(mysqlDataSource);
		}
		return jdbcTemplate;
	}

	private static final class ReloadFlagsMapper implements RowMapper<ReloadFlagsVo> {
		
		public ReloadFlagsVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			ReloadFlagsVo vo = new ReloadFlagsVo();
			
			vo.setClients(rs.getInt("Clients"));
			vo.setRules(rs.getInt("Rules"));
			vo.setActions(rs.getInt("Actions"));
			vo.setTemplates(rs.getInt("Templates"));
			vo.setSchedules(rs.getInt("Schedules"));
			
			return vo;
		}
	}
	
	public ReloadFlagsVo select() {
		return selectWithRepair(0);
	}
	
	private ReloadFlagsVo selectWithRepair(int retry) {
		String sql = "select * from ReloadFlags ";
		List<?> list = (List<?>)getJdbcTemplate().query(sql, new ReloadFlagsMapper());
		if (list.size()>0)
			return (ReloadFlagsVo)list.get(0);
		else if (retry < 1) {
			repair();
			return selectWithRepair(++retry);
		}
		else {
			throw new RuntimeException("Internal error, contact programming.");
		}
	}
	
	public int update(ReloadFlagsVo vo) {
		String sql = "update ReloadFlags set " +
			"Clients=?," +
			"Rules=?," +
			"Actions=?," +
			"Templates=?," +
			"Schedules=?";
		ArrayList<Object> fields = new ArrayList<Object>();
		fields.add(vo.getClients());
		fields.add(vo.getRules());
		fields.add(vo.getActions());
		fields.add(vo.getTemplates());
		fields.add(vo.getSchedules());

		int rowsUpdated = getJdbcTemplate().update(sql, fields.toArray());
		return rowsUpdated;
	}
	
	public int updateClientReloadFlag() {
		String sql = "update ReloadFlags set " +
			"Clients=Clients + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateRuleReloadFlag() {
		String sql = "update ReloadFlags set " +
			"Rules=Rules + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateActionReloadFlag() {
		String sql = "update ReloadFlags set " +
			"Actions=Actions + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateTemplateReloadFlag() {
		String sql = "update ReloadFlags set " +
			"Templates=Templates + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	public int updateScheduleReloadFlag() {
		String sql = "update ReloadFlags set " +
			"Schedules=Schedules + 1";
		int rows = getJdbcTemplate().update(sql);
		return rows;
	}

	private int repair() {
		String sql = "insert into ReloadFlags (" +
				"Clients," +
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
