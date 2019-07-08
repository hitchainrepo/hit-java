/*******************************************************************************
 * Copyright (c) 2019-04-25 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.pgm;

/**
 * HitMain
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-04-25
 * auto generate by qdp.
 */
public class HitMain {
    public static void main(String[] args) throws Exception {
        Class<?> main = Class.forName("org.hitchain.core.Main");
        main.getDeclaredMethod("main", String[].class).invoke(null, new Object[]{args});
    }
}
