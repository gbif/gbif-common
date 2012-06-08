/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.utils.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class EmailUtils {

  public static class EmailWithName {

    public String email;
    public String name;
  }

  private static final Pattern EMAIL_PATTERN =
    Pattern.compile("(?:(.+)(?: +| *<))?([^@ ]+(?:@| at )[^@ ]+\\.[a-zA-Z0-9_-]{2,4})(?: *>)?");

  public static EmailWithName parseEmail(String x) {
    if (StringUtils.isBlank(x)) {
      return null;
    }
    EmailWithName n = new EmailWithName();
    Matcher m = EMAIL_PATTERN.matcher(x);
    if (m.find()) {
      //      int all = m.groupCount();
      //      int idx = 0;
      //      while (idx<=all){
      //        System.out.println(m.group(idx));
      //        idx++;
      //      }
      n.name = StringUtils.trimToNull(m.group(1));
      n.email = StringUtils.trimToNull(m.group(2));
    } else {
      n.name = StringUtils.trimToNull(x);
    }
    return n;
  }
}
