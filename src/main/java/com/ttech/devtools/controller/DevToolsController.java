package com.ttech.devtools.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ttech.devtools.service.DevToolsService;

@RestController
public class DevToolsController {

	@Autowired
	DevToolsService devToolsService;
	
	@GetMapping("/template/{table-name}")
	public Map<String, String> getTemplate(@PathVariable("table-name") String tableName) throws Exception{
		
		return devToolsService.getTemplate(tableName);
	}
}
