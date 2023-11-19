package de.marketmaker.itools.gwtutil.client.util.date;

/**
 * @author Felix Hoffmann
 */

@SuppressWarnings({"UnusedDeclaration"})
public class MmJsDateUtil {
    public static MmJsDate plus(MmJsDate d, String period) {
        try {
            return applyPeriod(d, period, true);
        }
        catch (Exception e) {
// TODO: Errorhandling
            e.printStackTrace();
            return null;
        }
    }

    public static MmJsDate minus(MmJsDate d, String period) {
        try {
            return applyPeriod(d, period, false);
        }
        catch (Exception e) {
// TODO: Errorhandling
            e.printStackTrace();
            return null;
        }
    }

    private static MmJsDate applyPeriod(final MmJsDate date, final String period, boolean add) throws Exception {
        boolean dateMode = true;
        final StringBuilder sb = new StringBuilder();
        final char[] cp = period.toCharArray();
// period has to start with "p" / "P"
        if(!period.startsWith("p") && !period.startsWith("P")) {
            throw new Exception("period has to start with \"p\" or \"P\"");
        }
        for (char c : cp) {
            switch (c) {
                case 'P':
                case 'p':
                    dateMode = true; // date mode
                    break;
                case 'Y':
                case 'y':
                    date.addYears(parseInt(sb, add));
                    break;
                case 'M':
                case 'm':
                    if(dateMode) {
                        date.addMonths(parseInt(sb, add));
                    } else {
                        date.addMinutes(parseInt(sb, add));
                    }
                    break;
                case 'D':
                case 'd':
                    date.addDays(parseInt(sb, add));
                    break;
                case 'W':
                case 'w':
                    date.addDays(7 * parseInt(sb, add));
                    break;

// TODO: nach "t" darf nurnoch "h","m","s" kommen
                case 'T':
                case 't':
                    dateMode = false; // time mode
                    break;

                case 'H':
                case 'h':
                    date.addHours(parseInt(sb, add));
                    break;
                // case 'M': see above
                case 'S':
                case 's':
                    date.addSeconds(parseInt(sb, add));
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    sb.append(c);
                    break;
                default:
                    throw new Exception("incorrect period");
            }
        }
        return date;
    }

    private static int parseInt(final StringBuilder sb, boolean add) {
        if(sb.length()!=0) {
        int result = add ? Integer.parseInt(sb.toString()) : - Integer.parseInt(sb.toString());
        sb.setLength(0);
        return result;
        }
        return 0;
    }

}
