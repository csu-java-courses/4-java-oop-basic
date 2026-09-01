package com.example.task01;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Tests {

    private static final double DELTA = 1e-8;

    private Constructor<Point> getConstructor() {
        try {
            return Point.class.getDeclaredConstructor(int.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Не найден требуемый конструктор", e);
        }
    }

    private Point createPoint(int x, int y) {
        try {
            return getConstructor().newInstance(x, y);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("Невозможно создать Point", e);
        }
    }

    private static int getValue(Point p, String field) {
        try {
            Field f = Point.class.getDeclaredField(field);
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            return (int) f.get(p);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(String.format("Не могу получить значение \"%s\" для Point", field), e);
        }
    }

    private static Method getMethod(String name, Class<?>... parameterTypes) {
        try {
            return Point.class.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(String.format("Не найден метод %s", name), e);
        }
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(String.format("Ошибка при вызове метода %s", method.getName()), e);
        }
    }

    @Test
    public void testConstructor() {
        getConstructor();
    }

    @Test
    public void testCreatePoint() {
        Point p = createPoint(10, 20);
        Assert.assertEquals("x не равен ожидаемому значению", 10, getValue(p, "x"));
        Assert.assertEquals("y не равен ожидаемому значению", 20, getValue(p, "y"));
    }

    @Test
    public void testFlipMethod() {
        Method flip = getMethod("flip");
        Assert.assertEquals("Метод flip должен возвращать void", void.class, flip.getReturnType());
        Assert.assertEquals("Метод flip не должен принимать аргументы", 0, flip.getParameterCount());
        Assert.assertFalse("Метод flip не должен быть static", Modifier.isStatic(flip.getModifiers()));

        Point p = createPoint(10, 20);
        invoke(flip, p);
        Assert.assertEquals("x не равен ожидаемому значению", -20, getValue(p, "x"));
        Assert.assertEquals("y не равен ожидаемому значению", -10, getValue(p, "y"));
    }

    /**
     * Пример из условия задачи: (5, -7) переходит в (7, -5)
     */
    @Test
    public void testFlipMethodWithNegativeCoordinate() {
        Method flip = getMethod("flip");
        Point p = createPoint(5, -7);
        invoke(flip, p);
        Assert.assertEquals("x не равен ожидаемому значению", 7, getValue(p, "x"));
        Assert.assertEquals("y не равен ожидаемому значению", -5, getValue(p, "y"));
    }

    /**
     * Двойной вызов flip должен возвращать точку в исходное состояние
     */
    @Test
    public void testDoubleFlipMethod() {
        Method flip = getMethod("flip");
        Point p = createPoint(3, 8);
        invoke(flip, p);
        invoke(flip, p);
        Assert.assertEquals("x не равен ожидаемому значению", 3, getValue(p, "x"));
        Assert.assertEquals("y не равен ожидаемому значению", 8, getValue(p, "y"));
    }

    @Test
    public void testDistanceMethod() {
        Method distance = getMethod("distance", Point.class);
        Assert.assertEquals("Метод distance должен возвращать double", double.class, distance.getReturnType());
        Assert.assertFalse("Метод distance не должен быть static", Modifier.isStatic(distance.getModifiers()));

        Point p = createPoint(27, 31);
        Point p2 = createPoint(30, 27);
        Assert.assertEquals("расстояние не равно ожидаемому значению", 5d, (double) invoke(distance, p, p2), DELTA);
        Assert.assertEquals("расстояние не равно ожидаемому значению", 5d, (double) invoke(distance, p2, p), DELTA);
    }

    /**
     * Расстояние должно считаться корректно и для отрицательных координат, а не как разность модулей
     */
    @Test
    public void testDistanceMethodWithNegativeCoordinates() {
        Method distance = getMethod("distance", Point.class);
        Point p = createPoint(-3, -4);
        Point p2 = createPoint(3, 4);
        Assert.assertEquals("расстояние не равно ожидаемому значению", 10d, (double) invoke(distance, p, p2), DELTA);
    }

    /**
     * Расстояние от точки до самой себя равно нулю, а сами точки при подсчете изменяться не должны
     */
    @Test
    public void testDistanceMethodDoesNotModifyPoints() {
        Method distance = getMethod("distance", Point.class);
        Point p = createPoint(15, -40);
        Point p2 = createPoint(-25, 60);
        Assert.assertEquals("расстояние до самой себя должно быть нулевым", 0d, (double) invoke(distance, p, p), DELTA);

        invoke(distance, p, p2);
        Assert.assertEquals("x не равен ожидаемому значению", 15, getValue(p, "x"));
        Assert.assertEquals("y не равен ожидаемому значению", -40, getValue(p, "y"));
        Assert.assertEquals("x не равен ожидаемому значению", -25, getValue(p2, "x"));
        Assert.assertEquals("y не равен ожидаемому значению", 60, getValue(p2, "y"));
    }

    @Test
    public void testToString() {
        Method toString = getMethod("toString");
        int modifiers = toString.getModifiers();
        Assert.assertTrue("Метод toString должен быть public", Modifier.isPublic(modifiers));
        Assert.assertFalse("Метод toString не должен быть static", Modifier.isStatic(modifiers));
        Assert.assertEquals("Метод toString должен возвращать String", String.class, toString.getReturnType());

        Point p = createPoint(10, -20);
        Object result = invoke(toString, p);
        Assert.assertNotNull("Метод toString не должен возвращать null", result);
        String value = (String) result;
        Assert.assertTrue("Строковое представление точки должно содержать координату x", value.contains("10"));
        Assert.assertTrue("Строковое представление точки должно содержать координату y", value.contains("-20"));
    }
}
