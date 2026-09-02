package com.example.task05;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Task05MainTest {

    /**
     * Тестирование расстояния от точки до точки
     */
    @Test
    public void testPointToPoint() {
        double[] p1 = {1, 1};
        double[] p2 = {2, 2};
        final double p1toP2 = new Point(p1[0], p1[1]).getLength(new Point(p2[0], p2[1]));
        final double expected = Math.sqrt(2);
        testEquals(p1toP2, expected);
    }

    /**
     * Тестирование расстояния от точки до точки №1
     */
    @Test
    public void testPointToPoint2() {
        double[] p1 = {2534.343909, 123.358176};
        double[] p2 = {1093.723481, 453.09511543};
        final double p1toP2 = new Point(p1[0], p1[1]).getLength(new Point(p2[0], p2[1]));
        final double expected = 1477.874712820937;
        testEquals(p1toP2, expected);
    }

    /**
     * Расстояние не должно зависеть от порядка точек и должно быть нулевым для совпадающих точек
     */
    @Test
    public void testPointToPointSymmetry() {
        Point p1 = new Point(-15.5, 40.25);
        Point p2 = new Point(24.5, -19.75);
        testEquals(p1.getLength(p2), p2.getLength(p1));
        testEquals(p1.getLength(new Point(-15.5, 40.25)), 0);
    }

    /**
     * Тестирование геттеров и сеттеров координат
     */
    @Test
    public void testPointAccessors() {
        Point point = new Point(3.5, -7.25);
        testEquals(point.getX(), 3.5);
        testEquals(point.getY(), -7.25);

        point.setX(10.75);
        point.setY(-2.5);
        testEquals(point.getX(), 10.75);
        testEquals(point.getY(), -2.5);
    }

    /**
     * Тестирование модификаторов полей класса Point
     */
    @Test
    public void testFieldsModificators() {
        List<Field> fields = getPointFields();
        if (fields.isEmpty()) {
            throw new AssertionError("Не найдено полей в классе Point");
        }
        for (Field field : fields) {
            boolean isPrivate = Modifier.isPrivate(field.getModifiers());
            boolean isFinal = Modifier.isFinal(field.getModifiers());
            Assertions.assertFalse(
                !isPrivate && !isFinal,
                MessageFormat.format("Поле {0} недостаточно инкапсулировано", field.getName()));
        }
    }

    /**
     * Тестирование длины ломаной линии №1
     */
    @Test
    public void testPolygonalLineLength() {
        Point[] points = {
                new Point(1, 2),
                new Point(4, 6),
                new Point(8, 9),
                new Point(12, 12),
                new Point(15, 16),
        };
        PolygonalLine line = new PolygonalLine();
        line.setPoints(points);
        testEquals(line.getLength(), 20);
    }

    /**
     * Тестирование длины ломаной линии №2
     */
    @Test
    public void testPolygonalLineLength2() {
        Point[] points = {
                new Point(1, 2),
                new Point(4, 6),
                new Point(8, 9),
                new Point(12, 12),
                new Point(15, 16),
        };
        PolygonalLine line = new PolygonalLine();
        for (Point p : points) {
            line.addPoint(p);
        }
        testEquals(line.getLength(), 20);
    }

    /**
     * Тестирование длины ломаной линии №3
     */
    @Test
    public void testPolygonalLineLength3() {
        final int pointsCount = 50;
        Random random = new SecureRandom();
        Point[] points = new Point[pointsCount];
        double[] doubles = random.doubles(pointsCount * 2, -1000, 1000).toArray();
        double expectedLength = 0;
        for (int i = 0; i < pointsCount; i++) {
            double x = doubles[i * 2];
            double y = doubles[i * 2 + 1];
            points[i] = new Point(x, y);
            if (i > 0) {
                double x2 = points[i - 1].getX();
                double y2 = points[i - 1].getY();
                expectedLength += Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
            }
        }
        PolygonalLine line = new PolygonalLine();
        for (int i = 0; i < pointsCount; i++) {
            line.addPoint(points[i]);
        }
        double actualLength = line.getLength();
        testEquals(expectedLength, actualLength);
    }

    /**
     * Тестирование добавления точки по координатам
     */
    @Test
    public void testPolygonalLineAddPointByCoordinates() {
        PolygonalLine line = new PolygonalLine();
        line.addPoint(1, 2);
        line.addPoint(4, 6);
        line.addPoint(8, 9);
        testEquals(line.getLength(), 10);
    }

    /**
     * Повторный вызов setPoints должен заменять точки ломаной, а не добавлять их к уже имеющимся
     */
    @Test
    public void testPolygonalLineSetPointsReplacesPoints() {
        PolygonalLine line = new PolygonalLine();
        line.setPoints(new Point[]{
                new Point(0, 0),
                new Point(30, 40)
        });
        line.setPoints(new Point[]{
                new Point(0, 0),
                new Point(3, 4)
        });
        testEquals(line.getLength(), 5);
    }

    /**
     * Тестирование модификации массива точек.
     * Если этот метод падает, то скорее всего некорректно реализован метод {@link PolygonalLine#setPoints(Point[])}, который
     * заменяет внутренний массив на переданный. Корректная реализация должна копировать элементы массив, не используя переданный
     * массив в качестве хранилища
     */
    @Test
    public void testPolygonalLineArrayModification() {
        Point[] points = {
                new Point(1, 2),
                new Point(4, 6)
        };
        PolygonalLine line = new PolygonalLine();
        line.setPoints(points);
        final double expected = line.getLength();
        points[1] = new Point(10, 20);
        Assertions.assertTrue(Math.abs(expected - line.getLength()) < 1e-8, "Модификация массива точек приводит к модификации ломаной линии");
    }

    /**
     * Тестирование модификации точки, переданной в {@link PolygonalLine#setPoints(Point[])}.
     * Если этот тест падает, то скорее всего некорректно реализован метод {@link PolygonalLine#setPoints(Point[])},
     * который добавляет передаваемые точки в "массив" точек ломаной без копирования.
     * Корректная реализация должна "копировать" не только сам массив, но еще и точку,
     * т.е. создавать новый экземпляр объекта на основе переданного.
     */
    @Test
    public void testPolygonalLinePointModification() {
        Point[] points = {
                new Point(1, 2),
                new Point(4, 6)
        };
        PolygonalLine line = new PolygonalLine();
        line.setPoints(points);
        final double expected = line.getLength();
        movePoint(points[0]);
        Assertions.assertTrue(Math.abs(expected - line.getLength()) < 1e-8, "Модификация точки приводит к модификации ломаной линии");
    }

    /**
     * Тестирование модификации точки, переданной в {@link PolygonalLine#addPoint(Point)}.
     * Если этот тест падает, то скорее всего некорректно реализован метод {@link PolygonalLine#addPoint(Point)},
     * который добавляет точку к ломаной без копирования.
     * Корректная реализация должна "копировать" объект для того, чтобы внутреннее состояние ломаной не зависило от
     * внешней модификации ее составляющих точек.
     */
    @Test
    public void testPolygonalLinePointModification2() {
        Point[] points = {
                new Point(1, 2),
                new Point(4, 6)
        };
        PolygonalLine line = new PolygonalLine();
        for (Point p : points) {
            line.addPoint(p);
        }
        final double expected = line.getLength();
        movePoint(points[0]);
        Assertions.assertTrue(Math.abs(expected - line.getLength()) < 1e-8, "Модификация точки приводит к модификации ломаной линии");
    }

    private static List<Field> getPointFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : Point.class.getDeclaredFields()) {
            if (!field.isSynthetic() && !Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * "Сдвигает" точку через ее публичный интерфейс: внутреннее устройство класса Point тесты не фиксируют
     */
    private static void movePoint(Point point) {
        point.setX(point.getX() + 100);
        point.setY(point.getY() + 100);
    }

    private void testEquals(double value1, double value2) {
        Assertions.assertTrue(
            Math.abs(value1 - value2) < 1e-8,
            MessageFormat.format("Значения {0} и {1} не совпадают", value1, value2));
    }
}
