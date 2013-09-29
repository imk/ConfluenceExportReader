package com.martinkurz.confluence2wiki.beans;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JaxbDateSerializer extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date date) throws Exception {
        return createDateFormat().format(date);
    }

    @Override
    public Date unmarshal(String date) throws Exception {
        return createDateFormat().parse(date);
    }

    private DateFormat createDateFormat() {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }
}
