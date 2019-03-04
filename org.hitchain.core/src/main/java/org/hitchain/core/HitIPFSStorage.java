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
 * HitIPFSStorage
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-01
 * auto generate by qdp.
 */
public class HitIPFSStorage {
    public HitIPFSStorage(final Properties props) {

    }

    /**
     * List the path content.
     *
     * @param prefix
     * @return
     * @throws IOException
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
