package com.example.simarv.toll;

import com.example.simarv.toll.db.TollHistory;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TollHistoryTest {
    @Test
    public void groupDates() throws Exception {
        List<Date> dates = new ArrayList<>();
        dates.add(getDate("Mon May 23 23:01:01 CEST 2016"));
        dates.add(getDate("Mon May 23 23:01:01 CEST 2016"));
        dates.add(getDate("Mon May 21 23:01:01 CEST 2016"));
        dates.add(getDate("Mon May 21 23:01:01 CEST 2016"));
        dates.add(getDate("Mon May 20 23:01:01 CEST 2016"));
        dates.add(getDate("Mon May 20 23:01:01 CEST 2016"));

        Map<Date, List<Date>> r = TollHistory.getData(dates);
        assertEquals(3, r.keySet().size());

        List<Date> values = r.values().iterator().next();
        assertEquals(2, values.size());
    }


    private Date getDate(String string) throws ParseException {
        Calendar cal = GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        cal.setTime(sdf.parse(string));
        return cal.getTime();
    }
}