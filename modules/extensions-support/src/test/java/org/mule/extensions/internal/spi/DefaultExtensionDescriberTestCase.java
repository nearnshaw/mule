/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.internal.spi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mule.extensions.HeisenbergModule.EXTENSION_DESCRIPTION;
import static org.mule.extensions.HeisenbergModule.EXTENSION_NAME;
import static org.mule.extensions.HeisenbergModule.EXTENSION_VERSION;
import static org.mule.extensions.HeisenbergModule.HEISENBERG;
import org.mule.extensions.HeisenbergModule;
import org.mule.extensions.internal.DefaultExtensionBuilder;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionOperation;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.extensions.introspection.api.MuleExtensionType;
import org.mule.extensions.introspection.spi.ExtensionBuilder;
import org.mule.extensions.introspection.spi.ExtensionDescriber;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public class DefaultExtensionDescriberTestCase extends AbstractMuleTestCase
{

    private static final String SAY_MY_NAME_OPERATION = "sayMyName";
    private static final String GET_ENEMY_OPERATION = "getEnemy";
    private static final String KILL_OPERATION = "kill";
    private static final String KILL_CUSTOM_OPERATION = "killWithCustomMessage";
    private static final String HIDE_METH_IN_EVENT_OPERATION = "hideMethInEvent";
    private static final String HIDE_METH_IN_MESSAGE_OPERATION = "hideMethInMessage";

    private ExtensionBuilder builder;

    @Before
    public void setUp()
    {
        builder = DefaultExtensionBuilder.newBuilder();
    }

    @Test
    public void describeTestModule() throws Exception
    {
        ExtensionDescriber describer = new DefaultExtensionDescriber(HeisenbergModule.class);
        describer.describe(builder);

        Extension extension = builder.build();
        assertNotNull(extension);

        assertEquals(EXTENSION_NAME, extension.getName());
        assertEquals(EXTENSION_DESCRIPTION, extension.getDescription());
        assertEquals(EXTENSION_VERSION, extension.getVersion());
        assertEquals(Extension.MIN_MULE_VERSION, extension.getMinMuleVersion());
        assertEquals(MuleExtensionType.MODULE, extension.getExtensionType());

        assertTestModuleConfiguration(extension);
        assertTestModuleOperations(extension);
    }

    private void assertTestModuleConfiguration(Extension extension) throws Exception
    {
        assertEquals(1, extension.getConfigurations().size());
        ExtensionConfiguration conf = extension.getConfigurations().get(0);
        assertSame(conf, extension.getConfiguration(ExtensionConfiguration.DEFAULT_NAME));

        List<ExtensionParameter> parameters = conf.getParameters();
        assertEquals(2, parameters.size());

        assertParameter(parameters.get(0), "name", "", String.class, false, true, HEISENBERG);
        assertParameter(parameters.get(1), "enemies", "", List.class, true, true, null);
    }

    private void assertTestModuleOperations(Extension extension) throws Exception
    {
        assertEquals(6, extension.getOperations().size());
        assertOperation(extension, SAY_MY_NAME_OPERATION, "", new Class<?>[] {Object.class}, new Class<?>[] {String.class}, ExtensionConfiguration.DEFAULT_NAME);
        assertOperation(extension, GET_ENEMY_OPERATION, "", new Class<?>[] {Object.class}, new Class<?>[] {String.class}, ExtensionConfiguration.DEFAULT_NAME);
        assertOperation(extension, KILL_OPERATION, "", new Class<?>[] {Object.class}, new Class<?>[] {String.class}, ExtensionConfiguration.DEFAULT_NAME);
        assertOperation(extension, KILL_CUSTOM_OPERATION, "", new Class<?>[] {Object.class}, new Class<?>[] {String.class}, ExtensionConfiguration.DEFAULT_NAME);
        assertOperation(extension, HIDE_METH_IN_EVENT_OPERATION, "", new Class<?>[] {Object.class}, new Class<?>[] {void.class}, ExtensionConfiguration.DEFAULT_NAME);
        assertOperation(extension, HIDE_METH_IN_MESSAGE_OPERATION, "", new Class<?>[] {Object.class}, new Class<?>[] {void.class}, ExtensionConfiguration.DEFAULT_NAME);

        ExtensionOperation operation = extension.getOperation(SAY_MY_NAME_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());

        operation = extension.getOperation(GET_ENEMY_OPERATION);
        assertNotNull(operation);
        assertEquals(1, operation.getParameters().size());
        assertParameter(operation.getParameters().get(0), "index", "", int.class, true, true, null);

        operation = extension.getOperation(KILL_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());

        operation = extension.getOperation(KILL_CUSTOM_OPERATION);
        assertNotNull(operation);
        assertEquals(1, operation.getParameters().size());
        assertParameter(operation.getParameters().get(0), "goodbyeMessage", "", String.class, false, true, "#[payload]");

        operation = extension.getOperation(HIDE_METH_IN_EVENT_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());

        operation = extension.getOperation(HIDE_METH_IN_MESSAGE_OPERATION);
        assertNotNull(operation);
        assertTrue(operation.getParameters().isEmpty());
    }

    private void assertOperation(Extension extension,
                                 String operationName,
                                 String operationDescription,
                                 Class<?>[] inputTypes,
                                 Class<?>[] outputTypes,
                                 String... availableConfs) throws Exception
    {

        ExtensionOperation operation = extension.getOperation(operationName);

        assertEquals(operationName, operation.getName());
        assertEquals(operationDescription, operation.getDescription());
        match(operation.getInputTypes(), inputTypes);
        match(operation.getOutputTypes(), outputTypes);

        if (availableConfs != null)
        {
            for (String conf : availableConfs)
            {
                assertTrue(operation.isAvailableFor(extension.getConfiguration(conf)));
            }
        }


    }

    private void assertParameter(ExtensionParameter param,
                                 String name,
                                 String description,
                                 Class<?> type,
                                 boolean required,
                                 boolean dynamic,
                                 Object defaultValue)
    {

        assertEquals(name, param.getName());
        assertEquals(description, param.getDescription());
        assertEquals(type, param.getType());
        assertEquals(required, param.isRequired());
        assertEquals(dynamic, param.isDynamic());
        assertEquals(defaultValue, param.getDefaultValue());
    }

    private <T> void match(List<T> list, T[] array)
    {
        assertEquals(list.size(), array.length);

        for (int i = 0; i < array.length; i++)
        {
            assertEquals(list.get(i), array[i]);
        }
    }

}
