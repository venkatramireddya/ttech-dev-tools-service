package com.ttech.devtools.service;

import java.util.Map;

public interface DevToolsService {

	Map<String, String> getTemplate(String tableName) throws Exception;
}
