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

import com.epam.reportportal.commons.template.TemplateEngine;
import com.epam.reportportal.extension.bugtracking.InternalTicket;
import com.google.common.base.Strings;
import com.microsoft.tfs.core.clients.workitem.NonCoreFieldsReferenceNames;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.fields.Field;
import com.microsoft.tfs.core.clients.workitem.files.Attachment;
import com.microsoft.tfs.core.clients.workitem.internal.files.AttachmentImpl;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds description for TFS
 *
 * @author Andrei Varabyeu
 */
class DescriptionBuilder implements InputFieldBuilder<String> {

	private static final String TFS_BUG_TEMPLATE_PATH = "bug_template.ftl";

	private TemplateEngine templateEngine;

	DescriptionBuilder(TemplateEngine templateEngine) {
		this.templateEngine = com.google.common.base.Preconditions.checkNotNull(templateEngine);
	}

	@Override
	public boolean supports(Field f) {
		return NonCoreFieldsReferenceNames.REPRO_STEPS.equals(f.getReferenceName());
	}

	@Override
	public String build(WorkItem workItem, InternalTicket ticket, Collection<String> values) {
		Map<String, Object> templateContext = new HashMap<>();

		/* What is description? */
		templateContext.put("description", values.stream().collect(Collectors.joining("<br>")));

		if ((null != ticket.getComments()) && (!ticket.getComments().trim().isEmpty())) {
			templateContext.put("comments", ticket.getComments());
		}

		if (null != ticket.getBackLinks()) {
			templateContext.put("backLinks", ticket.getBackLinks());
		}

		if (null != ticket.getLogs()) {
			/* Order of log items is important */
			List<HtmlLogEntry> logData = new LinkedList<>();
			for (InternalTicket.LogEntry log : ticket.getLogs()) {
				String attachmentUrl = null;
				if (null != log.getAttachment()) {
					Attachment attachment = new AttachmentImpl(createFile(log), log.getLog().getLogMsg());
					workItem.getAttachments().add(attachment);
					/* need to save to obtain attachment URL */
					TfsUtils.saveWorkItem(workItem);
					attachmentUrl = attachment.getURL().toString();
				}
				String message = log.getIncludeLogs() ? log.getLog().getLogMsg() : "";
				String screen = Strings.isNullOrEmpty(attachmentUrl) ? null : attachmentUrl;
				boolean isLogs = log.getIncludeLogs();
				HtmlLogEntry entry = new HtmlLogEntry(message, screen, isLogs);
				logData.add(entry);
			}
			templateContext.put("logs", logData);
		}
		return templateEngine.merge(TFS_BUG_TEMPLATE_PATH, templateContext);
	}

	private File createFile(InternalTicket.LogEntry log) {
		try {
			File data = File.createTempFile("tfs-" + log.getLog().getId(), TfsUtils.getExtension(log.getAttachment().getContentType()));
			FileUtils.copyInputStreamToFile(log.getAttachment().getInputStream(), data);
			return data;
		} catch (IOException e) {
			throw new TfsException("Cannot create temp file for log '" + log.getLog().getId() + "'", e);
		}
	}

	/**
	 * Public class for velocity template access<br>
	 * <b>UPDATE CAREFULLY cause part of logic in VM file!</b>
	 *
	 * @author Andrei_Ramanchuk
	 */
	public static class HtmlLogEntry {
		/* Log message string */
		private String logMsg;
		/* Screen */
		private String screen;
		/* Flag - should be log-message shown on html page or not */
		private boolean isMessageVisible;

		HtmlLogEntry(String message, String link, boolean isLog) {
			this.logMsg = message;
			this.screen = link;
			this.isMessageVisible = isLog;
		}

		@Override
		public String toString() {
			return "HtmlLogEntry [logMsg=" + logMsg + ", screen=" + screen + ", isMessageVisible=" + isMessageVisible + "]";
		}
	}
}
