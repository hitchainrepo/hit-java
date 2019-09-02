/*******************************************************************************
 * Copyright (c) 2018-11-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.api;

import io.ipfs.api.NamedStreamable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.hitchain.hit.api.ProjectInfoFile.TeamInfo;
import org.hitchain.hit.util.ByteHelper;
import org.hitchain.hit.util.ECCHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.RSAHelper;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * MyFileWrapper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2018-11-29 auto generate by qdp.
 */
public class DecryptableFileWrapper implements NamedStreamable {
    private final HashedFile source;
    private final ProjectInfoFile projectInfoFile;
    private final String member;
    private final String priKeyRsa;

    public DecryptableFileWrapper(HashedFile source, ProjectInfoFile projectInfoFile, String member, String priKeyRsa) {
        if (source == null) {
            throw new IllegalStateException("DecryptableFileWrapper HashedFile does not exist: " + source);
        } else {
            this.source = source;
        }
        if (projectInfoFile == null) {
            throw new IllegalStateException(
                    "DecryptableFileWrapper ProjectInfoFile does not exist: " + projectInfoFile);
        } else {
            this.projectInfoFile = projectInfoFile;
        }
        if (projectInfoFile.isPrivate() && StringUtils.isBlank(member)) {
            throw new IllegalStateException("DecryptableFileWrapper member does not exist: " + member);
        } else {
            this.member = member;
        }
        if (projectInfoFile.isPrivate() && StringUtils.isBlank(priKeyRsa)) {
            throw new IllegalStateException("DecryptableFileWrapper privateKey does not exist: " + priKeyRsa);
        } else {
            this.priKeyRsa = priKeyRsa;
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
        String encryptedRepositoryPriKey = null;
        {// get the encrypted repository private key, if is owner return repository private key, if is member return member's repository private key.
            if (StringUtils.equalsAny(member, projectInfoFile.getOwner(), projectInfoFile.getOwnerPubKeyRsa(),
                    projectInfoFile.getOwnerAddressEcc())) {// #if is owner, get the repository encrypted private key.
                encryptedRepositoryPriKey = projectInfoFile.getRepoPriKey();
            } else {
                for (TeamInfo ti : projectInfoFile.getMembers()) {
                    if (StringUtils.equalsAny(member, ti.getMember(), ti.getMemberPubKeyRsa(),
                            ti.getMemberAddressEcc())) {
                        encryptedRepositoryPriKey = ti.getMemberRepoPriKey();
                        break;
                    }
                }
            }
        }
        if (encryptedRepositoryPriKey == null) {
            return new ByteArrayInputStream(bs);
        }
        try {
            // Encrypt: private key -(hex decode)-> private key bytes -(encrypt with rsa public key)->  encrypt bytes -(hex encode)-> hex encrypt
            // Decrypt: hex encrypt -(hex decode)-> encrypt bytes     -(decrypt with rsa private key)-> private key bytes -(hex encode) private key
            String repositoryPriKey = Hex.toHexString(
                    RSAHelper.decrypt(
                            Hex.decode(encryptedRepositoryPriKey),
                            RSAHelper.getPrivateKeyFromHex(priKeyRsa)
                    ));
            byte[] bytes = ECCHelper.privateDecrypt(bs, ECCHelper.getPrivateKeyFromEthereumHex(repositoryPriKey));
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
                list.add(new DecryptableFileWrapper(hf, projectInfoFile, member, priKeyRsa));
            }
            return list;
        }
        return Collections.emptyList();
    }

    public Optional<String> getName() {
        try {
            return Optional.of(URLEncoder.encode(this.source.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException(var2);
        }
    }
}
