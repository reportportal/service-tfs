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

import com.epam.reportportal.extension.bugtracking.CommonPredicates;
import com.epam.reportportal.extension.bugtracking.ExternalSystemStrategy;
import com.epam.reportportal.extension.bugtracking.InternalTicket;
import com.epam.reportportal.extension.bugtracking.InternalTicketAssembler;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.workitem.CoreFieldReferenceNames;
import com.microsoft.tfs.core.clients.workitem.NonCoreFieldsReferenceNames;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.WorkItemClient;
import com.microsoft.tfs.core.clients.workitem.exceptions.UnableToSaveException;
import com.microsoft.tfs.core.clients.workitem.fields.Field;
import com.microsoft.tfs.core.clients.workitem.fields.FieldDefinition;
import com.microsoft.tfs.core.clients.workitem.project.Project;
import com.microsoft.tfs.core.clients.workitem.wittype.WorkItemType;
import com.microsoft.tfs.core.config.ConnectionInstanceData;
import com.microsoft.tfs.core.config.DefaultConnectionAdvisor;
import com.microsoft.tfs.core.config.httpclient.DefaultHTTPClientFactory;
import com.microsoft.tfs.core.config.httpclient.HTTPClientFactory;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Realization of TFS Strategy
 *
 * @author Andrei Varabyeu
 */
public class TfsSdkStrategy implements ExternalSystemStrategy {
	private static final Logger LOGGER = LoggerFactory.getLogger(TfsSdkStrategy.class);

	/*
	 * static { String file = ; System.out.println(
	 * "TFS Native Libs are placed in " + file); }
	 */

	private static final WorkItemConverter WIT_CONVERTER = new WorkItemConverter();

	private List<InputFieldBuilder<?>> fieldBuilders;

	private InternalTicketAssembler ticketAssembler;

	private BasicTextEncryptor encryptor;

	TfsSdkStrategy(InternalTicketAssembler ticketAssembler, List<InputFieldBuilder<?>> fieldBuilders, BasicTextEncryptor encryptor) {
		this.ticketAssembler = ticketAssembler;
		this.fieldBuilders = fieldBuilders;
		this.encryptor = encryptor;
	}

	@Override
	public boolean checkConnection(ExternalSystem externalSystem) {
		TFSTeamProjectCollection connection = null;
		try {
			connection = getWitClient(externalSystem, false).getConnection();
			connection.authenticate();
			return connection.hasAuthenticated();
		} catch (Exception e) {
			LOGGER.error("Unable to connect to TFS", e);
			return false;
		} finally {
			if (null != connection) {
				connection.close();
			}
		}
	}

	@Override
	public Optional<Ticket> getTicket(final String id, ExternalSystem system) {
		return doWithClient(system, client -> {
			WorkItem workItem = client.getWorkItemByID(Integer.parseInt(id));
			return Optional.ofNullable(WIT_CONVERTER.apply(workItem));
		});
	}

	@Override
	public Ticket submitTicket(PostTicketRQ ticketRQ, ExternalSystem externalSystemDetails) {
		boolean encrypted = true;
		if (!Strings.isNullOrEmpty(ticketRQ.getUsername())) {
			externalSystemDetails.setUsername(ticketRQ.getUsername());
			externalSystemDetails.setPassword(ticketRQ.getPassword());
			externalSystemDetails.setDomain(ticketRQ.getDomain());
			encrypted = false;
		}
		return submitTicket(ticketAssembler.apply(ticketRQ), externalSystemDetails, encrypted);
	}

	@Override
	public List<PostFormField> getTicketFields(String issueType, final ExternalSystem externalSystemDetails) {
		return doWithClient(externalSystemDetails, witClient -> {
			/*
			 * do not save work item. Created just to understand which fields
			 * are required
			 */
			Project project = witClient.getProjects().get(externalSystemDetails.getProject());
			BusinessRule.expect(project, Predicates.notNull()).verify(ErrorType.UNABLE_POST_TICKET,
					MessageFormatter.format("Project '{}' doesn't exist", externalSystemDetails.getProject()));

			WorkItem workItem = witClient.newWorkItem(project.getWorkItemTypes().get(Constants.BUG_ITEM_TYPE));

			List<String> requiredFields = new LinkedList<>();
			List<FieldDefinition> fieldDefinitions = new ArrayList<>(workItem.getFields().size());
			for (Field f : workItem.getFields()) {
				if (TfsUtils.FAILED_FIELDS.test(f)) {
					requiredFields.add(f.getReferenceName());
				}
				if (f.isEditable() && null == f.getValue() && !Constants.SKIPPED_FIELDS.contains(f.getReferenceName())) {
					fieldDefinitions.add(f.getFieldDefinition());
				}
			}
			List<PostFormField> result = fieldDefinitions.stream().map(new WitFieldDefinitionConverter(requiredFields))
					.collect(Collectors.toList());
			/* Put required fields on first place */
			Collections.sort(result);
			return result;
		});
	}

	// TODO make a part of interface
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Ticket submitTicket(final InternalTicket ticket, final ExternalSystem externalSystemDetails, final boolean encrypted) {
		return doWithClient(externalSystemDetails, encrypted, witClient -> {
			WorkItemType bugWorkItemType = witClient.getProjects().get(externalSystemDetails.getProject()).getWorkItemTypes()
					.get(Constants.BUG_ITEM_TYPE);
			WorkItem workItem = witClient.newWorkItem(bugWorkItemType);

			/* Make sure it's present */
			workItem.setTitle(ticket.getFields().get(CoreFieldReferenceNames.TITLE).iterator().next());

			/*
			 * Make sure required HTML field for RP data is present. It should
			 * be always exists.
			 */
			if (!ticket.getFields().containsKey(NonCoreFieldsReferenceNames.REPRO_STEPS)) {
				ticket.getFields().put(NonCoreFieldsReferenceNames.REPRO_STEPS, "");
			}
			/*
			 * FIXME dirty-hack! Else put it in the end to avoid required fields
			 * save operation problems
			 */
			else {
				String storedValue = ticket.getFields().get(NonCoreFieldsReferenceNames.REPRO_STEPS).iterator().next();
				ticket.getFields().removeAll(NonCoreFieldsReferenceNames.REPRO_STEPS);
				Multimap<String, String> newFields = ticket.getFields();
				newFields.put(NonCoreFieldsReferenceNames.REPRO_STEPS, storedValue);
				ticket.setFields(newFields);
			}

			for (Map.Entry<String, Collection<String>> field : ticket.getFields().asMap().entrySet()) {
				if (CommonPredicates.IS_EMPTY.test(field.getValue())) {
					continue;
				}

				for (InputFieldBuilder builder : fieldBuilders) {
					if (builder.supports(workItem.getFields().getField(field.getKey()))) {
						workItem.getFields().getField(field.getKey()).setValue(builder.build(workItem, ticket, field.getValue()));
						/* If builder found - stop cycling */
						break;
					}
				}
			}

			try {
				TfsUtils.saveWorkItem(workItem);
			} catch (UnableToSaveException e) {
				throw new TfsException("Unable to save work item " + e.getLocalizedMessage(), e);
			}

			return WIT_CONVERTER.apply(workItem);
		});
	}

	private synchronized WorkItemClient getWitClient(ExternalSystem details, boolean descryptPass) {
		String nativeLibsPath = System.getProperty("com.microsoft.tfs.jni.native.base-directory");
		if (!Strings.isNullOrEmpty(nativeLibsPath)) {
			LOGGER.warn("TFS native libs are placed in {}. Dir present: {}", nativeLibsPath, new File(nativeLibsPath).exists());
		}


		String username = Strings.isNullOrEmpty(details.getDomain()) ?
				details.getUsername() :
				details.getDomain() + "\\" + details.getUsername();
		String passw = descryptPass ? encryptor.decrypt(details.getPassword()) : details.getPassword();
		TFSTeamProjectCollection tfsTeamProjectCollection = new TFSTeamProjectCollection(TfsUtils.asUri(details.getUrl()),
				new UsernamePasswordCredentials(username, passw), new DefaultConnectionAdvisor(Locale.getDefault(), TimeZone.getDefault()) {
			@Override
			public HTTPClientFactory getHTTPClientFactory(ConnectionInstanceData instanceData) {
				return new DefaultHTTPClientFactory(instanceData) {
					@Override
					protected boolean shouldAcceptUntrustedCertificates(ConnectionInstanceData connectionInstanceData) {
						return true;
					}
				};
			}
		});
		return tfsTeamProjectCollection.getWorkItemClient();
	}

	private <T> T doWithClient(ExternalSystem externalSystem, WorkItemAction<T> action) {
		return doWithClient(externalSystem, true, action);
	}

	private <T> T doWithClient(ExternalSystem externalSystem, boolean encrypted, WorkItemAction<T> action) {
		WorkItemClient witClient = getWitClient(externalSystem, encrypted);
		try {
			return action.perform(witClient);
		} finally {
			if (null != witClient) {
				witClient.close();
			}
		}
	}

	private interface WorkItemAction<T> {
		T perform(WorkItemClient client);
	}
}