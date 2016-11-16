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

package com.epam.reportportal.extension.bugtracking.tfs;

import java.util.Collection;
import java.util.stream.Collectors;

import com.epam.reportportal.extension.bugtracking.InternalTicket;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.fields.Field;

/**
 * Should be the last in the builders chain
 *
 * @author Andrei Varabyeu
 */
class DefaultFieldBuilder implements InputFieldBuilder<String> {

	@Override
	public boolean supports(Field f) {
		return true;
	}

	@Override
	public String build(WorkItem workItem, InternalTicket ticket, Collection<String> values) {
		return values.stream().collect(Collectors.joining(","));
	}
}