package com.example.task02;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Task02MainTest {

    private static final int HOURS = 0;
    private static final int MINUTES = 1;
    private static final int SECONDS = 2;
    private static final String[] FIELD_NAMES = {"часов", "минут", "секунд"};

    private static Class<?> getTestedClass() {
        try {
            return Class.forName("com.example.task02.TimeSpan");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Не найден класс TimeSpan");
        }
    }

    private static Constructor<?> getConstructor() {
        return Arrays.stream(getTestedClass().getDeclaredConstructors())
                .filter(c -> c.getParameterCount() == 3)
                .findAny()
                .orElseThrow(() -> new AssertionError("Не найден конструктор с тремя аргументами"));
    }

    private static Object createTimeSpan(int hours, int minutes, int seconds) {
        Constructor<?> constructor = getConstructor();
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        try {
            return constructor.newInstance(hours, minutes, seconds);
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new AssertionError(String.format("Невозможно создать TimeSpan(%d, %d, %d)", hours, minutes, seconds), e);
        }
    }

    private static List<Field> getStateFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : getTestedClass().getDeclaredFields()) {
            if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                fields.add(field);
            }
        }

        return fields;
    }

    private static int readField(Field field, Object timeSpan) {
        try {
            Object value = field.get(timeSpan);
            if (!(value instanceof Number)) {
                throw new AssertionError(String.format("Поле %s должно хранить число", field.getName()));
            }

            return ((Number) value).intValue();
        } catch (IllegalAccessException e) {
            throw new AssertionError(String.format("Не могу получить значение поля %s", field.getName()), e);
        }
    }

    /**
     * Определяет, какое поле класса за что отвечает: интервал создается с заведомо разными значениями,
     * поэтому роль поля однозначно определяется его значением. Так тесты не зависят от выбранных студентом имен полей
     */
    private static Field[] resolveFields() {
        List<Field> fields = getStateFields();
        Assertions.assertEquals(3, fields.size(), "Класс должен хранить часы, минуты и секунды в трех отдельных полях");

        Object timeSpan = createTimeSpan(3, 5, 7);
        Field[] resolved = new Field[3];
        int[] expected = {3, 5, 7};
        for (Field field : fields) {
            int value = readField(field, timeSpan);
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] == value && resolved[i] == null) {
                    resolved[i] = field;
                    break;
                }
            }
        }
        for (int i = 0; i < resolved.length; i++) {
            Assertions.assertNotNull(
                resolved[i],
                String.format("После создания TimeSpan(3, 5, 7) ни одно поле не хранит количество %s (%d). "
                            + "Конструктор должен принимать часы, минуты и секунды именно в этом порядке", FIELD_NAMES[i], expected[i]));
        }

        return resolved;
    }

    private static Method getMethod(String name, Class<?>... parameterTypes) {
        try {
            return getTestedClass().getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(String.format("Не найден метод %s", name), e);
        }
    }

    private static void invoke(Method method, Object target, Object... args) {
        try {
            method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(String.format("Ошибка при вызове метода %s", method.getName()), e);
        }
    }

    private static void assertState(String message, Object timeSpan, int hours, int minutes, int seconds) {
        Field[] fields = resolveFields();
        int[] expected = {hours, minutes, seconds};
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertEquals(
                expected[i],
                readField(fields[i], timeSpan),
                String.format("%s: количество %s не равно ожидаемому", message, FIELD_NAMES[i]));
        }
    }

    @Test
    public void testFields() {
        for (Field f : getStateFields()) {
            Assertions.assertTrue(
                Modifier.isPrivate(f.getModifiers()),
                String.format("Поле %s должно быть private", f.getName()));
        }
    }

    @Test
    public void testConstructor() {
        assertState("Конструктор", createTimeSpan(3, 5, 7), 3, 5, 7);
    }

    /**
     * Для каждого поля должен существовать public-метод, возвращающий его значение.
     * Метод ищется по возвращаемому значению, поэтому имена методов могут быть любыми
     */
    @Test
    public void testGetters() {
        Field[] fields = resolveFields();
        Object timeSpan = createTimeSpan(3, 5, 7);
        int[] expected = {3, 5, 7};
        for (int i = 0; i < fields.length; i++) {
            final int value = expected[i];
            boolean found = false;
            for (Method method : getTestedClass().getDeclaredMethods()) {
                if (method.getParameterCount() != 0 || !Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (!Number.class.isAssignableFrom(wrap(method.getReturnType()))) {
                    continue;
                }
                try {
                    Object result = method.invoke(timeSpan);
                    if (result instanceof Number && ((Number) result).intValue() == value) {
                        found = true;
                        break;
                    }
                } catch (ReflectiveOperationException e) {
                    // метод не подошел, проверяем следующий
                }
            }
            Assertions.assertTrue(
                found,
                String.format("Не найден public-метод, возвращающий количество %s", FIELD_NAMES[i]));
        }
    }

    /**
     * Для каждого поля должен существовать public-метод, изменяющий его значение.
     * Метод ищется по производимому эффекту, поэтому имена методов могут быть любыми
     */
    @Test
    public void testSetters() {
        Field[] fields = resolveFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            boolean found = false;
            for (Method method : getTestedClass().getDeclaredMethods()) {
                if (method.getParameterCount() != 1 || !Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (!Number.class.isAssignableFrom(wrap(method.getParameterTypes()[0]))) {
                    continue;
                }
                Object timeSpan = createTimeSpan(3, 5, 7);
                try {
                    method.invoke(timeSpan, 11);
                } catch (ReflectiveOperationException | IllegalArgumentException e) {
                    continue;
                }
                if (readField(field, timeSpan) == 11) {
                    found = true;
                    break;
                }
            }
            Assertions.assertTrue(
                found,
                String.format("Не найден public-метод, устанавливающий количество %s", FIELD_NAMES[i]));
        }
    }

    @Test
    public void testAddMethod() {
        Class<?> clazz = getTestedClass();
        Method method = getMethod("add", clazz);
        Assertions.assertEquals(void.class, method.getReturnType(), "Метод add должен возвращать void");

        Object timeSpan = createTimeSpan(1, 20, 15);
        invoke(method, timeSpan, createTimeSpan(2, 30, 40));
        assertState("1:20:15 + 2:30:40", timeSpan, 3, 50, 55);
    }

    /**
     * При сложении двух интервалов по 30 минут должен получаться 1 час, а не 60 минут
     */
    @Test
    public void testAddMethodNormalization() {
        Class<?> clazz = getTestedClass();
        Method method = getMethod("add", clazz);

        Object timeSpan = createTimeSpan(0, 30, 0);
        invoke(method, timeSpan, createTimeSpan(0, 30, 0));
        assertState("0:30:00 + 0:30:00", timeSpan, 1, 0, 0);

        Object other = createTimeSpan(1, 45, 50);
        invoke(method, other, createTimeSpan(2, 20, 30));
        assertState("1:45:50 + 2:20:30", other, 4, 6, 20);
    }

    /**
     * Метод add не должен изменять переданный в качестве аргумента интервал
     */
    @Test
    public void testAddMethodDoesNotModifyArgument() {
        Class<?> clazz = getTestedClass();
        Method method = getMethod("add", clazz);

        Object argument = createTimeSpan(2, 30, 40);
        invoke(method, createTimeSpan(1, 20, 15), argument);
        assertState("Аргумент метода add", argument, 2, 30, 40);
    }

    @Test
    public void testSubtractMethod() {
        Class<?> clazz = getTestedClass();
        Method method = getMethod("subtract", clazz);
        Assertions.assertEquals(void.class, method.getReturnType(), "Метод subtract должен возвращать void");

        Object timeSpan = createTimeSpan(3, 50, 55);
        invoke(method, timeSpan, createTimeSpan(2, 30, 40));
        assertState("3:50:55 - 2:30:40", timeSpan, 1, 20, 15);
    }

    /**
     * При вычитании должны корректно выполняться "займы" из старших разрядов
     */
    @Test
    public void testSubtractMethodNormalization() {
        Class<?> clazz = getTestedClass();
        Method method = getMethod("subtract", clazz);

        Object timeSpan = createTimeSpan(2, 0, 0);
        invoke(method, timeSpan, createTimeSpan(0, 0, 1));
        assertState("2:00:00 - 0:00:01", timeSpan, 1, 59, 59);

        Object other = createTimeSpan(1, 10, 20);
        invoke(method, other, createTimeSpan(0, 40, 50));
        assertState("1:10:20 - 0:40:50", other, 0, 29, 30);
    }

    /**
     * Метод subtract не должен изменять переданный в качестве аргумента интервал
     */
    @Test
    public void testSubtractMethodDoesNotModifyArgument() {
        Class<?> clazz = getTestedClass();
        Method method = getMethod("subtract", clazz);

        Object argument = createTimeSpan(2, 30, 40);
        invoke(method, createTimeSpan(3, 50, 55), argument);
        assertState("Аргумент метода subtract", argument, 2, 30, 40);
    }

    @Test
    public void testToString() {
        Method toString = getMethod("toString");
        int modifiers = toString.getModifiers();
        Assertions.assertTrue(Modifier.isPublic(modifiers), "Метод toString должен быть public");
        Assertions.assertFalse(Modifier.isStatic(modifiers), "Метод toString не должен быть static");
        Assertions.assertEquals(String.class, toString.getReturnType(), "Метод toString должен возвращать String");

        Object result = createTimeSpan(3, 5, 7).toString();
        Assertions.assertNotNull(result, "Метод toString не должен возвращать null");
        String value = (String) result;
        Assertions.assertTrue(value.contains("3"), "Строковое представление интервала должно содержать количество часов");
        Assertions.assertTrue(value.contains("5"), "Строковое представление интервала должно содержать количество минут");
        Assertions.assertTrue(value.contains("7"), "Строковое представление интервала должно содержать количество секунд");
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }

        return type;
    }
}
