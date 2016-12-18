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

import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.exceptions.UnableToSaveException;
import com.microsoft.tfs.core.clients.workitem.fields.Field;
import com.microsoft.tfs.core.clients.workitem.fields.FieldStatus;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TFS-related utility stuff
 *
 * @author Andrei Varabyeu
 */
class TfsUtils {

	static final Predicate<Field> FAILED_FIELDS = field -> field.getStatus() != FieldStatus.VALID;
	private static final Function<Field, String> ERROR_MESSAGE_TRANSFORMER = input -> input.getStatus().getInvalidMessage(input);

	private TfsUtils() {
		// statics only
	}

	static void saveWorkItem(WorkItem workItem) {
		try {
			checkFields(workItem);
			workItem.save();
		} catch (UnableToSaveException e) {
			throw new TfsException("Unable to save work item " + e.getLocalizedMessage(), e);
		}
	}

	private static void checkFields(WorkItem workItem) {
		List<Field> failed = StreamSupport.stream(workItem.getFields().spliterator(), false).filter(FAILED_FIELDS)
				.collect(Collectors.toList());
		if (!failed.isEmpty()) {
			throw new TfsException("Unable to save work item '" + workItem.getTitle() + "'"
					+ failed.stream().map(ERROR_MESSAGE_TRANSFORMER).collect(Collectors.joining(",")));
		}
	}

	static URI asUri(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new TfsException(String.format("Cannot convert '%s' to URI", url), e);
		}
	}

	static String getExtension(String contentType){
		try {
			return MimeTypes.getDefaultMimeTypes().forName(contentType).getExtension();
		} catch (MimeTypeException e) {
			//TODO find another exception
			throw new RuntimeException(String.format("Unable to resolve content type '%s'", contentType), e);
		}
	}
}