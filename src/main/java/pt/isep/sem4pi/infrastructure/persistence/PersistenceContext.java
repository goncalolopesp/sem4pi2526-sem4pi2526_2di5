/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isep.sem4pi.application.Application;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * provides easy access to the persistence layer. works as a factory of
 * factories
 *
 * @author Paulo Gandra Sousa
 */
@Utility
public final class PersistenceContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceContext.class);

    private static RepositoryFactory theFactory;

    private PersistenceContext() {
        // ensure utility
    }

    public static RepositoryFactory repositories() {
        if (theFactory == null) {
            final String factoryClassName = Application.settings().getRepositoryFactory();
            try {
                theFactory = (RepositoryFactory) Class.forName(factoryClassName).getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
                LOGGER.error("Unable to dynamically load the Repository Factory", ex);
                throw new IllegalStateException(
                        "Unable to dynamically load the Repository Factory: " + factoryClassName, ex);
            }
        }

        return theFactory;
    }

    private static RepositoryFactory buildRepositoryFactory() {
        // 1) Allow override from JVM/system properties (useful for tests)
        final String overrideFactoryClassName = System.getProperty("persistence.repositoryFactory");
        if (overrideFactoryClassName != null && !overrideFactoryClassName.trim().isEmpty()) {
            try {
                final Class<?> factoryClass = Class.forName(overrideFactoryClassName.trim());
                return (RepositoryFactory) factoryClass.getDeclaredConstructor().newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to load repository factory from system property", e);
            }
        }

        // 2) application.properties (default)
        final Properties properties = new Properties();

        try (InputStream input = PersistenceContext.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input != null) {
                properties.load(input);
            }

            final String factoryClassName = properties.getProperty(
                    "persistence.repositoryFactory",
                    InMemoryRepositoryFactory.class.getName()
            );

            final Class<?> factoryClass = Class.forName(factoryClassName);
            return (RepositoryFactory) factoryClass.getDeclaredConstructor().newInstance();

        } catch (final IOException | ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to load repository factory", e);
        }
    }
}