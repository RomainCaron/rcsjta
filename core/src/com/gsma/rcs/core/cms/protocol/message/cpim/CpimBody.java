/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010-2016 Orange.
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
 ******************************************************************************/

package com.gsma.rcs.core.cms.protocol.message.cpim;

import com.gsma.rcs.core.cms.Constants;
import com.gsma.rcs.core.cms.protocol.message.HeaderPart;

public abstract class CpimBody {

    protected final HeaderPart mHeaders;

    protected CpimBody() {
        mHeaders = new HeaderPart();
    }

    protected CpimBody(String contentType) {
        this();
        mHeaders.addHeader(Constants.HEADER_CONTENT_TYPE, contentType);
    }

    public String getContentType() {
        return mHeaders.getHeaderValue(Constants.HEADER_CONTENT_TYPE);
    }

    protected abstract void parseBody(String body);

    protected abstract String toPayload();
}