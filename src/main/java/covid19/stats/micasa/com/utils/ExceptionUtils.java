package covid19.stats.micasa.com.utils;

public class ExceptionUtils {

    public static Throwable getRoot(Throwable throwable) {
        Throwable root;
        for(root = throwable; root.getCause() != null; root = root.getCause()) { }
        return root;
    }

    public static String getMessage(Throwable throwable) {
        Throwable root = getRoot(throwable);
        return root.getMessage() != null && !root.getMessage().isEmpty() ? root.getMessage() : root.getClass().getName();
    }

}
