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

package com.epam.reportportal.extension.bugtracking.tfs.html;

import com.google.common.base.Strings;

/**
 * Simple Tag containing text body
 *
 * @author Andrei Varabyeu
 */
public class SimpleTag extends AbstractTag {

	private String body;

	public SimpleTag(String name, String body) {
		super(name);
		this.body = body;
	}

	public SimpleTag(String name) {
		this(name, null);
	}

	public String getBody() {
		return body;
	}

	@Override
	public String html() {
		StringBuilder builder = new StringBuilder();
		writeStart(builder, getName(), getAttributes());
		if (!Strings.isNullOrEmpty(body)){
			builder.append(body);
		}
		writeEnd(builder, getName());
		return builder.toString();
	}

}