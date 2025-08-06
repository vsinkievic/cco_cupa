package lt.creditco.cupa.remote;

import java.util.Random;

public class TestRandomData {

    private static final Random RANDOM = new Random();

    public static String randomValueFrom(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return values[RANDOM.nextInt(values.length)];
    }

    public static int randomFrom(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    public static String random5DigitNumber() {
        return String.format("%05d", RANDOM.nextInt(100000));
    }
}
