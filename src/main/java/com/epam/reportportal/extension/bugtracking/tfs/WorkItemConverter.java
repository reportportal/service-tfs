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

import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.microsoft.tfs.core.clients.workitem.CoreFieldReferenceNames;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.util.TSWAHyperlinkBuilder;

import java.util.function.Function;

/**
 * Converts TFS's SDK model class into internal domain model representation
 *
 * @author Andrei Varabyeu
 */
public class WorkItemConverter implements Function<WorkItem, Ticket> {

	@Override
	public Ticket apply(WorkItem workItem) {
		if (null == workItem) {
			return null;
		}
		Ticket ticket = new Ticket();

		String ticketUrl = new TSWAHyperlinkBuilder(workItem.getClient().getConnection()).getWorkItemEditorURL(workItem.getID()).toString();
		ticket.setTicketUrl(ticketUrl);
		ticket.setSummary(workItem.getTitle());
		ticket.setId(String.valueOf(workItem.getID()));
		ticket.setStatus(workItem.getFields().getField(CoreFieldReferenceNames.STATE).getValue().toString());
		return ticket;
	}
}