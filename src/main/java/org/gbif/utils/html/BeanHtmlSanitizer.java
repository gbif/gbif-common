/***************************************************************************
 * Copyright 2016 Global Biodiversity Information Facility Secretariat
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

package org.gbif.utils.html;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import org.apache.commons.beanutils.PropertyUtils;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple wrapper around OWASP Html Sanitizer to apply it to all String properties of a bean.
 */
public class BeanHtmlSanitizer {

  private static final Logger LOG = LoggerFactory.getLogger(BeanHtmlSanitizer.class);

  /**
   * Apply the {@link PolicyFactory} to all String properties of the provided bean.
   * This function will only apply sanitization to properties that are String and will only
   * be applied to the variable of the provided bean. In other words, it won't be applied to inner-beans.
   *
   * @param obj
   * @param policy
   * @param exclusion set of property that should not be sanitized
   * @param <T>
   * @return same instance of the provided bean with all String properties sanitized by the {@link PolicyFactory}.
   */
  public static <T> T sanitize(T obj, PolicyFactory policy, Set<String> exclusion) {
    Preconditions.checkNotNull(obj, "The provided object can not be null");
    Preconditions.checkNotNull(policy, "The provided policy can not be null");

    try {
      Map<String, Object> properties = PropertyUtils.describe(obj);
      for (String property : properties.keySet()) {
        if ((exclusion == null || !exclusion.contains(property)) &&
                String.class.equals(PropertyUtils.getPropertyType(obj, property))) {
          PropertyUtils.setProperty(obj, property, policy.sanitize((String) properties.get(property)));
        }
      }
      //TODO change for Java 7 multicatch when possible
    } catch (IllegalAccessException e) {
      LOG.error("Issue while applying HTML sanitization", e);
    } catch (InvocationTargetException e) {
      LOG.error("Issue while applying HTML sanitization", e);
    } catch (NoSuchMethodException e) {
      LOG.error("Issue while applying HTML sanitization", e);
    }
    return obj;
  }

  /**
   * Apply the {@link PolicyFactory} to all String properties of the provided bean.
   * This function will only apply sanitization to properties that are String and will only
   * be applied to the variable of the provided bean. In other words, it won't be applied to inner-beans.
   *
   * @return same instance of the provided bean with all String properties sanitized by the {@link PolicyFactory}.
   */
  public static <T> T sanitize(T obj, PolicyFactory policy) {
    return sanitize(obj, policy, null);
  }

}
