/*******************************************************************************
 * Copyright (c) 2018-11-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.api;

import org.apache.commons.io.IOUtils;
import org.hitchain.hit.util.ECCHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.ipfs.NamedStreamable;

import java.io.*;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * MyFileWrapper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-11-29
 * auto generate by qdp.
 */
public class EncryptableFileWrapper implements NamedStreamable {
    private final HashedFile source;
    private final ProjectInfoFile projectInfoFile;

    public EncryptableFileWrapper(HashedFile source, ProjectInfoFile projectInfoFile) {
        if (source == null) {
            throw new IllegalStateException("EncryptableFileWrapper HashedFile does not exist: " + source);
        } else {
            this.source = source;
        }
        if (projectInfoFile == null) {
            throw new IllegalStateException(
                    "EncryptableFileWrapper ProjectInfoFile does not exist: " + projectInfoFile);
        } else {
            this.projectInfoFile = projectInfoFile;
        }
    }

    public byte[] getContents() throws IOException {
        return IOUtils.toByteArray(getInputStream());
    }

    public InputStream getInputStream() throws IOException {
        byte[] bs = source.getContents();
        if (GitHelper.HIT_PROJECT_INFO.equals(source.getName()) || GitHelper.HIT_GITFILE_IDX.equals(source.getName()) || !projectInfoFile.isPrivate()) {
            return new ByteArrayInputStream(bs);
        }
        try {
            // Encrypt: use repository public key to encrypt the file.
            // Decrypt: decrypt repository private key by user rsa private key and then decrypt the file.
            PublicKey publicKey = ECCHelper.getPublicKeyFromEthereumPublicKeyHex(projectInfoFile.getRepoPubKey());
            byte[] bytes = ECCHelper.publicEncrypt(bs, publicKey);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

    public boolean isDirectory() {
        return this.source.isDirectory();
    }

    public List<NamedStreamable> getChildren() {
        if (source.isDirectory()) {
            List<HashedFile> children = source.getChildren();
            List<NamedStreamable> list = new ArrayList<>();
            for (HashedFile hf : children) {
                list.add(new EncryptableFileWrapper(hf, projectInfoFile));
            }
            return list;
        }
        return Collections.emptyList();
    }

    public Optional<String> getName() {
        try {
            return Optional.of(URLEncoder.encode(new File(this.source.getName()).getName(), "UTF-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException(var2);
        }
    }
}
