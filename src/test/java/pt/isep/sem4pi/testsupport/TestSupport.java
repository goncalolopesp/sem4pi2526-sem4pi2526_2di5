package pt.isep.sem4pi.testsupport;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public final class TestSupport {

    private TestSupport() {
        // utility
    }

    public static void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to inject test double into " + target.getClass().getName(), e);
        }
    }

    public static InputStream consoleInput(final String... lines) {
        final String content = String.join(System.lineSeparator(), lines) + System.lineSeparator();
        final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        // Return an InputStream that repeats the provided input several times.
        // Some Console helpers re-prompt on EOF which can cause tests to loop. Repeating
        // the input a limited number of times avoids an immediate EOF and prevents hangs.
        final int maxCycles = 100;

        return new InputStream() {
            private final byte[] buf = bytes;
            private int pos = 0;
            private int cycles = 0;

            @Override
            public synchronized int read() {
                if (pos >= buf.length) {
                    cycles++;
                    if (cycles >= maxCycles) {
                        return -1;
                    }
                    pos = 0;
                }
                return buf[pos++] & 0xFF;
            }

            @Override
            public synchronized int available() {
                return Math.max(0, buf.length - pos);
            }
        };
    }

    public static <T extends Throwable> T newThrowable(final Class<T> type) {
        for (final Constructor<?> constructor : type.getDeclaredConstructors()) {
            try {
                constructor.setAccessible(true);
                final Object[] args = new Object[constructor.getParameterCount()];
                final Class<?>[] parameterTypes = constructor.getParameterTypes();

                for (int i = 0; i < parameterTypes.length; i++) {
                    args[i] = defaultValue(parameterTypes[i]);
                }

                final Object instance = constructor.newInstance(args);
                return type.cast(instance);
            } catch (final ReflectiveOperationException | ClassCastException ignored) {
                // try next constructor
            }
        }

        throw new IllegalStateException("Unable to instantiate throwable: " + type.getName());
    }

    private static Object defaultValue(final Class<?> type) {
        if (!type.isPrimitive()) {
            if (type == String.class) {
                return "test";
            }
            return null;
        }

        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0f;
        }
        if (type == double.class) {
            return 0d;
        }
        if (type == char.class) {
            return '\0';
        }

        return null;
    }
}