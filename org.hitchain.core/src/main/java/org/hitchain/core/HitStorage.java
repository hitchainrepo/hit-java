/*******************************************************************************
 * Copyright (c) 2019-03-01 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.core;

import org.eclipse.jgit.lib.ProgressMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

/**
 * HitStorage
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-01
 * auto generate by qdp.
 */
public class HitStorage {
    public HitStorage(final Properties props) {

    }

    /**
     * List the names of keys available within a bucket.
     * <p>
     * This method is primarily meant for obtaining a "recursive directory
     * listing" rooted under the specified bucket and prefix location.
     *
     * @param prefix common prefix to filter the results by. Must not be null.
     *               Supplying the empty string will list all keys in the bucket.
     *               Supplying a non-empty string will act as though a trailing '/'
     *               appears in prefix, even if it does not.
     * @return list of keys starting with <code>prefix</code>, after removing
     * <code>prefix</code> (or <code>prefix + "/"</code>)from all
     * of them.
     * @throws java.io.IOException sending the request was not possible, or the response XML
     *                             document could not be parsed properly.
     */
    public List<String> list(String prefix) throws IOException {
        if (prefix.length() > 0 && !prefix.endsWith("/")) { //$NON-NLS-1$
            prefix += "/"; //$NON-NLS-1$
        }
        return null;
    }

    /**
     * return the file content.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public byte[] get(String path) throws IOException {
        return null;
    }

    /**
     * delete the file.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public InputStream delete(String path) throws IOException {
        return null;
    }

    /**
     * put file to storage.
     *
     * @param path
     * @param data
     * @return
     */
    public String put(String path, byte[] data) {
        return null;
    }

    /**
     * start put.
     *
     * @param path
     * @param monitor
     * @param monitorTask
     * @return
     * @throws IOException
     */
    public OutputStream beginPut(final String path, final ProgressMonitor monitor, final String monitorTask) throws IOException {
        //return encryption.encrypt(new DigestOutputStream(buffer, md5));
        return null;
    }
}
