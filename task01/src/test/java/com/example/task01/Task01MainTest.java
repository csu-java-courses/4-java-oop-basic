package com.example.task01;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Task01MainTest {

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
        Assertions.assertEquals(10, getValue(p, "x"), "x не равен ожидаемому значению");
        Assertions.assertEquals(20, getValue(p, "y"), "y не равен ожидаемому значению");
    }

    @Test
    public void testFlipMethod() {
        Method flip = getMethod("flip");
        Assertions.assertEquals(void.class, flip.getReturnType(), "Метод flip должен возвращать void");
        Assertions.assertEquals(0, flip.getParameterCount(), "Метод flip не должен принимать аргументы");
        Assertions.assertFalse(Modifier.isStatic(flip.getModifiers()), "Метод flip не должен быть static");

        Point p = createPoint(10, 20);
        invoke(flip, p);
        Assertions.assertEquals(-20, getValue(p, "x"), "x не равен ожидаемому значению");
        Assertions.assertEquals(-10, getValue(p, "y"), "y не равен ожидаемому значению");
    }

    /**
     * Пример из условия задачи: (5, -7) переходит в (7, -5)
     */
    @Test
    public void testFlipMethodWithNegativeCoordinate() {
        Method flip = getMethod("flip");
        Point p = createPoint(5, -7);
        invoke(flip, p);
        Assertions.assertEquals(7, getValue(p, "x"), "x не равен ожидаемому значению");
        Assertions.assertEquals(-5, getValue(p, "y"), "y не равен ожидаемому значению");
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
        Assertions.assertEquals(3, getValue(p, "x"), "x не равен ожидаемому значению");
        Assertions.assertEquals(8, getValue(p, "y"), "y не равен ожидаемому значению");
    }

    @Test
    public void testDistanceMethod() {
        Method distance = getMethod("distance", Point.class);
        Assertions.assertEquals(double.class, distance.getReturnType(), "Метод distance должен возвращать double");
        Assertions.assertFalse(Modifier.isStatic(distance.getModifiers()), "Метод distance не должен быть static");

        Point p = createPoint(27, 31);
        Point p2 = createPoint(30, 27);
        Assertions.assertEquals(5d, (double) invoke(distance, p, p2), DELTA, "расстояние не равно ожидаемому значению");
        Assertions.assertEquals(5d, (double) invoke(distance, p2, p), DELTA, "расстояние не равно ожидаемому значению");
    }

    /**
     * Расстояние должно считаться корректно и для отрицательных координат, а не как разность модулей
     */
    @Test
    public void testDistanceMethodWithNegativeCoordinates() {
        Method distance = getMethod("distance", Point.class);
        Point p = createPoint(-3, -4);
        Point p2 = createPoint(3, 4);
        Assertions.assertEquals(10d, (double) invoke(distance, p, p2), DELTA, "расстояние не равно ожидаемому значению");
    }

    /**
     * Расстояние от точки до самой себя равно нулю, а сами точки при подсчете изменяться не должны
     */
    @Test
    public void testDistanceMethodDoesNotModifyPoints() {
        Method distance = getMethod("distance", Point.class);
        Point p = createPoint(15, -40);
        Point p2 = createPoint(-25, 60);
        Assertions.assertEquals(0d, (double) invoke(distance, p, p), DELTA, "расстояние до самой себя должно быть нулевым");

        invoke(distance, p, p2);
        Assertions.assertEquals(15, getValue(p, "x"), "x не равен ожидаемому значению");
        Assertions.assertEquals(-40, getValue(p, "y"), "y не равен ожидаемому значению");
        Assertions.assertEquals(-25, getValue(p2, "x"), "x не равен ожидаемому значению");
        Assertions.assertEquals(60, getValue(p2, "y"), "y не равен ожидаемому значению");
    }

    @Test
    public void testToString() {
        Method toString = getMethod("toString");
        int modifiers = toString.getModifiers();
        Assertions.assertTrue(Modifier.isPublic(modifiers), "Метод toString должен быть public");
        Assertions.assertFalse(Modifier.isStatic(modifiers), "Метод toString не должен быть static");
        Assertions.assertEquals(String.class, toString.getReturnType(), "Метод toString должен возвращать String");

        Point p = createPoint(10, -20);
        Object result = invoke(toString, p);
        Assertions.assertNotNull(result, "Метод toString не должен возвращать null");
        String value = (String) result;
        Assertions.assertTrue(value.contains("10"), "Строковое представление точки должно содержать координату x");
        Assertions.assertTrue(value.contains("-20"), "Строковое представление точки должно содержать координату y");
    }
}
