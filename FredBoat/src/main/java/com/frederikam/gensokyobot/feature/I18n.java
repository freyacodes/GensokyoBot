/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.frederikam.gensokyobot.feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18n {

    private static final Logger log = LoggerFactory.getLogger(I18n.class);

    public static FredBoatLocale DEFAULT = new FredBoatLocale(new Locale("en","US"), "en_US", "English");
    public static final HashMap<String, FredBoatLocale> LANGS = new HashMap<>();

    public static void start() {
        LANGS.put("en_US", DEFAULT);

        log.info("Loaded " + LANGS.size() + " languages: " + LANGS);
    }

    public static ResourceBundle get() {
        return DEFAULT.getProps();
    }

    public static class FredBoatLocale {

        private final ResourceBundle props;
        private final String nativeName;

        FredBoatLocale(Locale locale, String code, String nativeName) throws MissingResourceException {
            props = ResourceBundle.getBundle("lang." + code, locale);
            this.nativeName = nativeName;
        }

        public ResourceBundle getProps() {
            return props;
        }

        @Override
        public String toString() {
            return "[" + nativeName + ']';
        }
    }
}
