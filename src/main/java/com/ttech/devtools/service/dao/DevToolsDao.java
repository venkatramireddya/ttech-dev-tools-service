package com.ttech.devtools.service.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ttech.devtools.model.TableDescribe;

@Repository
public class DevToolsDao {

	
	@Autowired
	@Qualifier("JdbcTemplate")
	JdbcTemplate jdbcTemplate;
	
	public List<TableDescribe> describeTable(String tableName) {
		List<TableDescribe> describeFields = jdbcTemplate.query(
				"select COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE from USER_TAB_COLUMNS where TABLE_NAME= '" + tableName+"'", new BeanPropertyRowMapper<TableDescribe>(TableDescribe.class));
		return describeFields;
	}
}
