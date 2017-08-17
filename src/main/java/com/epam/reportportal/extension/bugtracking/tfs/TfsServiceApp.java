/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-tfs
 * 
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */ 
/*
 * This file is part of Report Portal.
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.extension.bugtracking.tfs;

import com.epam.reportportal.commons.template.TemplateEngineProvider;
import com.epam.reportportal.extension.bugtracking.BugTrackingApp;
import com.epam.reportportal.extension.bugtracking.ExternalSystemStrategy;
import com.epam.reportportal.extension.bugtracking.InternalTicketAssembler;
import com.google.common.collect.ImmutableList;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author Andrei Varabyeu
 */
public class TfsServiceApp extends BugTrackingApp {

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	private static final String ENCRYPTOR_DEFAULT_PASS = "reportportal";


	@Autowired
	private ApplicationContext context;

	@Value("${rp.tfs.native}")
	private String nativePath;


	@Bean(name = "tfsFieldBuildersMap")
	public List<InputFieldBuilder<?>> tfsFieldBuilders() {
		//@formatter:off
		return ImmutableList.<InputFieldBuilder<?>> builder()
				.add(new DescriptionBuilder(new TemplateEngineProvider().get()))
				.add(new DateFieldBuilder(DATE_FORMAT))
				.add(new DefaultFieldBuilder()).build();
		//@formatter:on
	}

	@Override
	public ExternalSystemStrategy externalSystemStrategy() {
		System.setProperty("com.microsoft.tfs.jni.native.base-directory", nativePath);

		BasicTextEncryptor encyptor = new BasicTextEncryptor();
		encyptor.setPassword(ENCRYPTOR_DEFAULT_PASS);
		return new TfsSdkStrategy(context.getBean(InternalTicketAssembler.class),
				tfsFieldBuilders(), encyptor);
	}

	public static void main(String[] args) {
		SpringApplication.run(TfsServiceApp.class);
	}
}
