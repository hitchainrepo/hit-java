/*******************************************************************************
 * Copyright (c) 2019-03-22 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core;

import java.util.Arrays;

/**
 * Main
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-22
 * auto generate by qdp.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.toString(args));
        if (args != null && args.length > 0 && "hitcfg".equals(args[0])) {
            System.out.println(Arrays.toString(args));
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod("main", String[].class).invoke(null, args);
        }
    }
}
