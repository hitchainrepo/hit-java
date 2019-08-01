/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.apache.commons.lang3.StringUtils;
import org.hitchain.contract.api.PullRequestContractEthereumApi;
import org.hitchain.contract.api.RepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.PullRequestContractEthereumService;
import org.hitchain.contract.ethereum.RepositoryContractEthereumService;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.HitHelper;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * AmCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class AmCommand implements Callable<Hit> {
    protected Hit hit;
    protected String patchId;
    //    boolean ignoreSpaceChange = StringUtils.equalsAny("--ignore-space-change", p1, p2, p3, p4);
//    boolean ignoreWhitespace = StringUtils.equalsAny("--ignore-white-sapce", p1, p2, p3, p4);
//    boolean forceMergeLine = StringUtils.equalsAny("--force-merge", p1, p2, p3, p4);
//    boolean noCommit = StringUtils.equalsAny("--no-commit", p1, p2, p3, p4);
    protected boolean ignoreSpaceChange;
    protected boolean ignoreWhitespace;
    protected boolean forceMergeLine;
    protected boolean noCommit;

    @Override
    public Hit call() throws Exception {
        ProjectInfoFile projectInfoFile = hit().projectInfoFile();

        PullRequestContractEthereumApi api = PullRequestContractEthereumService.getApi();
        RepositoryContractEthereumApi repoApi = RepositoryContractEthereumService.getApi();
        String fromAddress = HitHelper.getAccountAddress();
        String repoContractAddress = projectInfoFile.getRepoAddress();
        String contractAddress = repoApi.readPullRequestAddress(fromAddress, repoContractAddress);//maybe 0x00...000
        boolean hasPrContract = StringUtils.isNotBlank(contractAddress) && contractAddress.startsWith("0x") && !Numeric.toBigInt(contractAddress).equals(BigInteger.ZERO);
        contractAddress = hasPrContract ? contractAddress : null;

        Map<String, Object> prForMerge = null;
        List<Map<String, Object>> prs = PullRequestContractEthereumService.listAuthedPRs(fromAddress, contractAddress);
        for (Map<String, Object> pr : prs) {
            if (StringUtils.equals(patchId(), (String) pr.get("id"))) {
                prForMerge = pr;
                break;
            }
        }
        if (prForMerge == null) {
            prs = PullRequestContractEthereumService.listCommunityPRs(fromAddress, contractAddress);
            for (Map<String, Object> pr : prs) {
                if (StringUtils.equals(patchId(), (String) pr.get("id"))) {
                    prForMerge = pr;
                    break;
                }
            }
        }
        if (prForMerge == null) {
            System.err.println("Pull request for id:" + patchId() + " not found!");
            return hit();
        }
        boolean result = HitHelper.pullRequestMerge(hit().getRepository().getDirectory(), prForMerge, ignoreSpaceChange, ignoreWhitespace, forceMergeLine, noCommit);
        if (Boolean.TRUE.equals(result)) {
            System.out.println("Apply pull request success, you need to merge this branch to the main branch by using git merge command.");
        }
        return hit();
    }

    public Hit hit() {
        return hit;
    }

    public AmCommand hit(Hit hit) {
        this.hit = hit;
        return this;
    }

    public boolean noCommit() {
        return noCommit;
    }

    public AmCommand noCommit(boolean noCommit) {
        this.noCommit = noCommit;
        return this;
    }

    public boolean forceMergeLine() {
        return forceMergeLine;
    }

    public AmCommand forceMergeLine(boolean forceMergeLine) {
        this.forceMergeLine = forceMergeLine;
        return this;
    }

    public boolean ignoreWhitespace() {
        return ignoreWhitespace;
    }

    public AmCommand ignoreWhitespace(boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
        return this;
    }

    public boolean ignoreSpaceChange() {
        return ignoreSpaceChange;
    }

    public AmCommand ignoreSpaceChange(boolean ignoreSpaceChange) {
        this.ignoreSpaceChange = ignoreSpaceChange;
        return this;
    }

    public String patchId() {
        return patchId;
    }

    public AmCommand patchId(String patchId) {
        this.patchId = patchId;
        return this;
    }
}
