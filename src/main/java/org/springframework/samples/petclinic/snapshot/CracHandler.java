package org.springframework.samples.petclinic.snapshot;

import org.crac.Context;
import org.crac.Resource;
import org.crac.Core;
import org.springframework.boot.web.server.context.WebServerApplicationContext;

import org.springframework.boot.tomcat.TomcatWebServer;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class CracHandler implements Resource {

	private final WebServerApplicationContext webContext;

	private final DataSource dataSource;

	public CracHandler(WebServerApplicationContext webContext, DataSource dataSource) {
		this.webContext = webContext;
		this.dataSource = dataSource;
	}

	@Override
	public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
		for (var thread : Thread.getAllStackTraces().keySet()) {
			if (thread.getName().toLowerCase().contains("socket")) {
				System.out.println("Possible socket thread: " + thread);
			}
		}

		System.out.println("Before checkpoint: stopping Tomcat");

		if (webContext.getWebServer() != null) {
			webContext.getWebServer().stop();
		}
		try (Connection conn = dataSource.getConnection()) {
			conn.close();
		}
	}

	@Override
	public void afterRestore(Context<? extends Resource> context) throws Exception {
		System.out.println("After restore: restarting Tomcat");
		if (webContext.getWebServer() != null) {
			webContext.getWebServer().start();
		}
		try (Connection conn = dataSource.getConnection()) {
			conn.isValid(5);
		}
	}

}
