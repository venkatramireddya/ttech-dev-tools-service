package com.ttech.devtools.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.ttech.devtools.model.ColumnField;
import com.ttech.devtools.model.TableDescribe;
import com.ttech.devtools.service.DevToolsService;
import com.ttech.devtools.service.dao.DevToolsDao;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class DevToolsServiceImpl implements DevToolsService {

	@Autowired
	DevToolsDao devToolsDao;

	public Map<String, String> getTemplate(String tableName) throws Exception {
		Map<String, String> template = getParsedTemplates( tableName);
		System.out.println(template);
		return template;
	}

	
	public Map<String, String> getParsedTemplates(String tableName) throws Exception {
		Map<String, String> templates = new LinkedHashMap<>();
		Map<Object, Object> map;
		try {
			map = extractFromSQL(tableName);
			Set<String> fileNames = new LinkedHashSet<>();
			fileNames.add("scaffolding/1-Controller");
			fileNames.add("scaffolding/2-Service");
			fileNames.add("scaffolding/3-ServiceImpl");
			fileNames.add("scaffolding/4-Model");
			fileNames.add("scaffolding/5-SQL");
			fileNames.add("scaffolding/8-GenericDao");
			for(String fileName : fileNames) {
				Resource resource = new ClassPathResource(fileName);
			    byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
			    String data = new String(bdata, StandardCharsets.UTF_8);
				templates.put(fileName.split("-")[1], parseTemplate(map, data));
			}
		} catch (Exception e) {
			log.error("Exception : ", e);
			throw e;
		} 
		return templates;
	}

	
	private Map<Object, Object> extractFromSQL(String tableName)
			throws FileNotFoundException, IOException {

		List<TableDescribe> listTableDesc = devToolsDao.describeTable(tableName);
		List<ColumnField> fields = new ArrayList<>();
		ColumnField primaryField = null;
		for (TableDescribe def : listTableDesc) {
			ColumnField field = new ColumnField();
			field.setName(def.getColumnName());
			field.setNameCamelCase(toFieldName(toCamelCase(def.getColumnName())));
			String dbType = def.getDataType();
			String javaType = "";
			String dbDataType = "";
			if (StringUtils.containsIgnoreCase(dbType, "varchar") || StringUtils.containsIgnoreCase(dbType, "char")
					|| StringUtils.containsIgnoreCase(dbType, "LONGVARCHAR")
					|| StringUtils.containsIgnoreCase(dbType, "varchar2")) {
				javaType = "String";
				dbDataType = "VARCHAR";
			} else if (StringUtils.containsIgnoreCase(dbType, "NUMBER")
					|| StringUtils.containsIgnoreCase(dbType, "integer")) {
				/*
				 * // extract number from type int size =
				 * Integer.parseInt(dbType.replaceAll("\\D+", ""));
				 */
				if (def.getDataLength() < 5) {
					javaType = "int";
					dbDataType = "NUMBER ";
				} else {
					
					javaType = "long";
					dbDataType = "INTEGER";
				}
			} else if (StringUtils.containsIgnoreCase(dbType, "bigint")) {
				javaType = "long";
				dbDataType = "LONG";
			} else if (StringUtils.containsIgnoreCase(dbType, "date")
					|| StringUtils.containsIgnoreCase(dbType, "TIMESTAMP")
					|| StringUtils.containsIgnoreCase(dbType, "DATETIME")) {
				javaType = "Date";
				dbDataType = "DATE";
			} else if (StringUtils.containsIgnoreCase(dbType, "decimal")
					|| StringUtils.containsIgnoreCase(dbType, "numeric")) {
				javaType = "double";
				dbDataType = "DOUBLE";
			}else if (StringUtils.containsIgnoreCase(dbType, "float")) {
				javaType = "float ";
				dbDataType = "FLOAT";
			}

			field.setType(javaType);
			field.setDbDataType(dbDataType);
			/*
			 * if (def.getKey().contains("PRI") || def.getKey().contains("MUL")) {
			 * field.setPrimary(1); primaryField = field; }
			 */
			fields.add(field);
		}
		if (primaryField == null) {
			primaryField = fields.get(0);
		}

		Map<Object, Object> map1 = new TreeMap<Object, Object>();
		map1.put("fields", fields);
		map1.put("primaryField", primaryField);
		map1.put("TableName", tableName);
		map1.put("ClassName", toCamelCase(tableName));
		map1.put("ClassVariableName", toFieldName(toCamelCase(tableName)));
		map1.put("pagename", toCamelCase(tableName));
		map1.put("ModuleName", getModuleName(""));
		return map1;
	}

	private static String toCamelCase(String s) {
		String[] parts = s.split("_");
		String camelCaseString = "";
		for (String part : parts) {
			if (part.length() > 0) {
				camelCaseString = camelCaseString + toProperCase(part);
			}
		}
		return camelCaseString;
	}

	private static String toProperCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	private static String toFieldName(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}
	
	private String parseTemplate(Map<Object, Object> map, String content) throws Exception {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		stringLoader.putTemplate("nameTemplate", content);
		cfg.setTemplateLoader(stringLoader);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		Template template1 = cfg.getTemplate("nameTemplate");
		StringWriter stringWriter = new StringWriter();
		template1.process(map, stringWriter);
		//return HtmlUtils.htmlEscape(stringWriter.toString());
		return stringWriter.toString();
	}
	
	
	private String getModuleName(String templateReq) {
		if(StringUtils.isNotEmpty(templateReq)) {
			return templateReq.toUpperCase();
		} else {
			if(templateReq.contains("Test_")) {
				return "test";
			} else if (templateReq.contains("TEST_")) {
				return "test";
			}
		}
		return "TTECH";
	}
}
