/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://bintray.com/epam/reportportal/
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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Complex tag contains inner tag instead of body
 *
 * @author Andrei Varabyeu
 */
public class ComplexTag extends AbstractTag {

	private Deque<Tag> childs = new ArrayDeque<>();

	public ComplexTag(String name) {
		super(name);
	}

	public ComplexTag addChild(Tag tag) {
		this.childs.push(tag);
		return this;
	}

	@Override
	public String html() {
		Deque<Tag> childToRender = new ArrayDeque<>(this.childs);
		StringBuilder builder = new StringBuilder();
		writeStart(builder, getName(), getAttributes());
		while (!childToRender.isEmpty()) {
			builder.append(childToRender.poll().html());
		}

		writeEnd(builder, getName());
		return builder.toString();
	}

	public static ComplexTag root(String name) {
		return new ComplexTag(name);
	}

	public static ComplexTag root(String name, Map<String, String> attributes) {
		ComplexTag tag = new ComplexTag(name);
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			tag.attribute(entry.getKey(), entry.getValue());
		}
		return tag;
	}

}