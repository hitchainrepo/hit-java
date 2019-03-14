/*******************************************************************************
 * Copyright (c) 2019-03-09 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.core;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.URIish;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.EthereumHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.Tuple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

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
            String host = getHost();
            String path = getRawPath();
            if (StringUtils.isBlank(path)) {
                path = host;
                host = GitHelper.URL_ETHER;
            } else {
                host = "https://" + host;
            }
            String contractAddress = StringUtils.removeEnd(StringUtils.removeStart(path, "/"), ".git");
            String projectAddress = EthereumHelper.getProjectAddress(host, contractAddress);
            if (StringUtils.isNotBlank(projectAddress) && !EthereumHelper.isError(projectAddress)) {// is valid contract address and have content.
                String gitFileIndexHash = projectAddress;
                IPFS ipfs = GitHelper.getIpfs();
                Map<String/* filename */, Tuple.Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileIndex = GitHelper.readGitFileIndexFromIpfs(ipfs, gitFileIndexHash);
                Tuple.Two<Object, String, String> ipfsHashAndSha1 = gitFileIndex.get(GitHelper.HIT_PROJECT_INFO);
                if (StringUtils.isNotBlank(ipfsHashAndSha1.first())) {
                    byte[] cat = ipfs.cat(Multihash.fromBase58(ipfsHashAndSha1.first()));
                    ProjectInfoFile projectInfoFile = ProjectInfoFile.fromFile(
                            new HashedFile.FileWrapper(GitHelper.HIT_PROJECT_INFO, new HashedFile.InputStreamCallback() {
                                public InputStream call(HashedFile hashedFile) throws IOException {
                                    return new ByteArrayInputStream(cat);
                                }
                            }));
                    return humanishName = projectInfoFile.getRepoName();
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        throw new IllegalArgumentException("Not repository found for: " + toString());
    }
}
