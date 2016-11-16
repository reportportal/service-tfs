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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.epam.reportportal.extension.bugtracking.InternalTicket;
import com.google.common.base.Preconditions;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.fields.Field;

/**
 * Builder for dates
 *
 * @author Andrei Varabyeu
 */
final class DateFieldBuilder implements InputFieldBuilder<Date> {

	private final String dateFormat;

	DateFieldBuilder(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
	public boolean supports(Field f) {
		return Date.class.equals(f.getFieldDefinition().getSystemType());
	}

	@Override
	public Date build(WorkItem workItem, InternalTicket ticket, Collection<String> values) {
		Preconditions.checkArgument(null != values && 1 == values.size(), "Incorrect size of values. " + "Only one value is supported");
		String value = values.iterator().next();
		try {
			return new SimpleDateFormat(this.dateFormat).parse(value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Unable to parse date from: ");
		}
	}
}