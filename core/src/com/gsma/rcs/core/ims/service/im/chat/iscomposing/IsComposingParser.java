/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 * Copyright (C) 2015 Sony Mobile Communications Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTE: This file has been modified by Sony Mobile Communications Inc.
 * Modifications are licensed under the License.
 ******************************************************************************/

package com.gsma.rcs.core.ims.service.im.chat.iscomposing;

import com.gsma.rcs.core.ParseFailureException;
import com.gsma.rcs.utils.logger.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Is composing event parser (RFC3994)
 */
public class IsComposingParser extends DefaultHandler {
    /*
     * IsComposing SAMPLE: <?xml version="1.0" encoding="UTF-8"?> <isComposing
     * xmlns="urn:ietf:params:xml:ns:im-iscomposing"
     * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xsi:schemaLocation="urn:ietf:params:xml:ns:im-composing iscomposing.xsd"> <state>idle</state>
     * <lastactive>2003-01-27T10:43:00Z</lastactive> <contenttype>audio</contenttype> </isComposing>
     */
    /**
     * Rate to convert from seconds to milliseconds
     */
    private static final long SECONDS_TO_MILLISECONDS_CONVERSION_RATE = 1000;

    private StringBuffer accumulator = null;

    private IsComposingInfo isComposingInfo = null;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private final InputSource mInputSource;

    /**
     * Constructor
     * 
     * @param inputSource Input source
     */
    public IsComposingParser(InputSource inputSource) {
        mInputSource = inputSource;
    }

    /**
     * Parse the is composing input
     * 
     * @throws IsComposingParser
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws ParseFailureException
     */
    public IsComposingParser parse() throws ParserConfigurationException, SAXException,
            ParseFailureException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(mInputSource, this);
            return this;

        } catch (IOException e) {
            throw new ParseFailureException("Failed to parse input source!", e);
        }
    }

    public void startDocument() {
        if (logger.isActivated()) {
            logger.debug("Start document");
        }
        accumulator = new StringBuffer();
    }

    public void characters(char buffer[], int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public void startElement(String namespaceURL, String localName, String qname, Attributes attr) {
        accumulator.setLength(0);

        if (localName.equals("isComposing")) {
            isComposingInfo = new IsComposingInfo();
        }

    }

    public void endElement(String namespaceURL, String localName, String qname) {
        if (localName.equals("state")) {
            if (isComposingInfo != null) {
                isComposingInfo.setState(accumulator.toString());
            }
        } else if (localName.equals("lastactive")) {
            if (isComposingInfo != null) {
                isComposingInfo.setLastActiveDate(accumulator.toString());
            }
        } else if (localName.equals("contenttype")) {
            if (isComposingInfo != null) {
                isComposingInfo.setContentType(accumulator.toString());
            }
        } else if (localName.equals("refresh")) {
            if (isComposingInfo != null) {
                long time = Long.parseLong(accumulator.toString())
                        * SECONDS_TO_MILLISECONDS_CONVERSION_RATE;
                isComposingInfo.setRefreshTime(time);
            }
        } else if (localName.equals("isComposing")) {
            if (logger.isActivated()) {
                logger.debug("Watcher document is complete");
            }
        }
    }

    public void endDocument() {
        if (logger.isActivated()) {
            logger.debug("End document");
        }
    }

    public void warning(SAXParseException exception) {
        if (logger.isActivated()) {
            logger.error("Warning: line " + exception.getLineNumber() + ": "
                    + exception.getMessage());
        }
    }

    public void error(SAXParseException exception) {
        if (logger.isActivated()) {
            logger.error("Error: line " + exception.getLineNumber() + ": " + exception.getMessage());
        }
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        if (logger.isActivated()) {
            logger.error("Fatal: line " + exception.getLineNumber() + ": " + exception.getMessage());
        }
        throw exception;
    }

    public IsComposingInfo getIsComposingInfo() {
        return isComposingInfo;
    }
}
