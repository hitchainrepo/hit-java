/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import org.apache.commons.lang3.StringUtils;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.HitRepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.HitRepositoryContractEthereumService;
import org.hitchain.core.HitURIish;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.HitHelper;
import org.hitchain.hit.util.Tuple;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * AmCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class RepositoryNameCommand implements Callable<String> {
    protected String uri;

    public String uri() {
        return uri;
    }

    public RepositoryNameCommand uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String call() throws Exception {
        HitURIish uriIsh = new HitURIish(uri());
        if (!"hit".equals(uriIsh.getScheme())) {
            return null;
        }
        String host = uriIsh.getHost();
        String path = uriIsh.getRawPath();
        if (StringUtils.isBlank(path)) {
            path = host;
            host = HitHelper.getRepository();
        } else {
            host = "https://" + host;
        }
        String repoAddress = StringUtils.removeEnd(StringUtils.removeStart(path, "/"), ".git");
        String contractAddress = StringUtils.substringBefore(repoAddress, "-");
        int repoId = repoAddress.indexOf('-') > 0 ? Integer.valueOf(StringUtils.substringAfter(repoAddress, "-")) : -1;
        if (repoId < 0) {
            return null;
        }
        HitRepositoryContractEthereumApi api = HitRepositoryContractEthereumService.getApi();
        String repoUrl = api.readUrlById(HitHelper.getAccountAddress(), contractAddress, repoId);
        if (ContractApi.isError(repoUrl)) {
            return null;
        }
        {// is valid contract address and have content.
            String gitFileIndexHash = repoUrl;
            IPFS ipfs = GitHelper.getIpfs();
            Map<String/* filename */, Tuple.Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileIndex = GitHelper.readGitFileIndexFromIpfs(ipfs, gitFileIndexHash);
            {
                gitFileIndex.put(GitHelper.HIT_GITFILE_IDX, new Tuple.Two<>(gitFileIndexHash, ""));
            }
            Tuple.Two<Object, String, String> ipfsHashAndSha1 = gitFileIndex.get(GitHelper.HIT_PROJECT_INFO);
            if (StringUtils.isBlank(ipfsHashAndSha1.first())) {
                return null;
            }
            byte[] cat = ipfs.cat(Multihash.fromBase58(ipfsHashAndSha1.first()));
            ProjectInfoFile projectInfoFile = ProjectInfoFile.fromFile(
                    new HashedFile.FileWrapper(GitHelper.HIT_PROJECT_INFO,
                            new HashedFile.ByteArrayInputStreamCallback(cat)));
            return projectInfoFile.getRepoName();
        }
    }
}
