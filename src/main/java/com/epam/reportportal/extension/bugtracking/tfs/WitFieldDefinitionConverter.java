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

import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.microsoft.tfs.core.clients.workitem.fields.FieldDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Converts TFS's SDK model class into internal domain model representation
 *
 * @author Andrei Varabyeu
 */
class WitFieldDefinitionConverter implements Function<FieldDefinition, PostFormField> {

    private List<String> requiredFields;

    WitFieldDefinitionConverter(List<String> requiredFields) {
        Preconditions.checkNotNull(requiredFields, "Required list shouldn't be null");
        this.requiredFields = ImmutableList.<String>builder().addAll(requiredFields).build();
    }

    @Override
    public PostFormField apply(FieldDefinition input) {
        if (null == input) {
            return null;
        }

        PostFormField formField = new PostFormField();
        formField.setId(input.getReferenceName());
        formField.setFieldName(input.getName());

        // formField.setIsRequired() ???
        formField.setFieldType(input.getFieldType().getDisplayName());

        if (null != input.getAllowedValues()) {
            List<AllowedValue> values = new ArrayList<>(input.getAllowedValues().size());
            for (String allowedValue : input.getAllowedValues()) {
                values.add(new AllowedValue(allowedValue, allowedValue));
            }
            formField.setDefinedValues(values);
        }
        formField.setIsRequired(requiredFields.contains(input.getReferenceName()));

        return formField;
    }
}