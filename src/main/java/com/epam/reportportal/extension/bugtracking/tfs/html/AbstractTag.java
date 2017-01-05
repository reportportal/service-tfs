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

import com.google.common.base.Joiner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract Tag containing name and attributes
 *
 * @author Andrei Varabyeu
 */
public abstract class AbstractTag implements Tag {

	private String name;

	private Map<String, String> attributes;

	public AbstractTag(String name) {
		this.name = name;
		this.attributes = new LinkedHashMap<>();
	}

	public AbstractTag attribute(String name, String value) {
		this.attributes.put(name, "\"" + value + "\"");
		return this;
	}

	protected String getName() {
		return name;
	}

	protected Map<String, String> getAttributes() {
		return attributes;
	}

	protected void writeStart(StringBuilder builder, String name, Map<String, String> attributes) {
		builder.append("<").append(name);
		if (null != attributes && !attributes.isEmpty()) {
			builder.append(" ");
			Joiner.on(' ').withKeyValueSeparator("=").appendTo(builder, attributes);
		}
		builder.append(">");
	}

	protected void writeEnd(StringBuilder builder, String name) {
		builder.append("</").append(name).append(">");
	}

}