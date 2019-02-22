/*******************************************************************************
 * Copyright (c) 2019-02-21 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.util;

import java.io.File;

/**
 * GitHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-02-21
 * auto generate by qdp.
 */
public class GitHelper {

    public static void onClone(File projectDir) {

    }

    public static void onPull(File projectDir) {

    }

    /**
     * <pre>
     * #1.Get or create ProjectInfoFile.
     * #2.Get GitFileIndex from ProjectInfoFile contract address.
     * #3.List all current files.
     * #4.Compare current files and GitFileIndex and get the changed files.
     * #5.Write changed files to ipfs.
     * #6.Gen the new GitFileIndex.
     * #7.Write the new GitFileIndex to disk and ipfs.
     * #8.Call contract and update project hash(GitFileIndex hash).
     * </pre>
     * @param projectDir
     */
    public static void onPush(File projectDir) {

    }
}
