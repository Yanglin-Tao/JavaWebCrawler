package webcrawler;

/**
 * 
 * Wrapper class for the months period
 *
 */

public class Period {
	private int startMonths;
    private int endMonths;

    public Period(int startMonths, int endMonths) {
        this.startMonths = startMonths;
        this.endMonths = endMonths;
    }

    public int getStartMonths() {
        return startMonths;
    }

    public int getEndMonths() {
        return endMonths;
    }
}
