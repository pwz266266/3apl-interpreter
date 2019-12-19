public class Utility {
    public static boolean isInteger(String s) {
        return s.matches("-?(0|[1-9]\\d*)");
    }
    public static boolean isNumeric(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}
