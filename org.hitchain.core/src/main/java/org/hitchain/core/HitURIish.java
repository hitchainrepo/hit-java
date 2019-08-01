/*******************************************************************************
 * Copyright (c) 2019-03-09 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.core;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Hit;
import org.eclipse.jgit.transport.URIish;

import java.net.URISyntaxException;
import java.net.URL;

/**
 * HitURIish
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-09
 * auto generate by qdp.
 */
public class HitURIish extends URIish {
    private String humanishName = null;

    public HitURIish(String s) throws URISyntaxException {
        super(s);
    }

    public HitURIish(URL u) {
        super(u);
    }

    public HitURIish() {
        super();
    }

    public String getHumanishName() throws IllegalArgumentException {
        if (!"hit".equals(getScheme())) {
            return super.getHumanishName();
        }
        if (humanishName != null) {
            return humanishName;
        }
        try {
            humanishName = Hit.repositoryName().uri(toString()).call();
            if (StringUtils.isBlank(humanishName)) {
                throw new IllegalArgumentException("Not repository found for: " + toString());
            }
            return humanishName;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
