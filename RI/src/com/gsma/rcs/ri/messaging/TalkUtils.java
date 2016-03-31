/*******************************************************************************
 * Software Name : RCS IMS Stack
 * <p/>
 * Copyright (C) 2010-2016 Orange.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.gsma.rcs.ri.messaging;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TalkUtils {

    private final static String TIME_FORMAT = "kk:mm";
    private final static String DAY_TIME_FORMAT = "EEE, dd MMM yyyy";
    private final static String DATE_FORMAT = "dd MMM yyyy\nkk:mm";

    private final static SimpleDateFormat sSimpleDateFormatTime = new SimpleDateFormat(TIME_FORMAT,
            Locale.getDefault());

    private final static SimpleDateFormat sSimpleDateFormatDayTime = new SimpleDateFormat(
            DAY_TIME_FORMAT, Locale.getDefault());

    private final static SimpleDateFormat sSimpleDateFormatDate = new SimpleDateFormat(DATE_FORMAT,
            Locale.getDefault());

    public static String formatDateAsTime(long date) {
        return sSimpleDateFormatTime.format(date);
    }

    public static String formatDateAsDaytime(long date) {
        return sSimpleDateFormatDayTime.format(date);
    }

    public static String formatDate(long date) {
        return sSimpleDateFormatDate.format(date);
    }

    public static boolean compareDaytime(long date1, long date2) {
        return formatDateAsDaytime(date1).equals(formatDateAsDaytime(date2));
    }

}
