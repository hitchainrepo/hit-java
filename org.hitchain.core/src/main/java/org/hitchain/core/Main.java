/*******************************************************************************
 * Copyright (c) 2019-03-22 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.contract.api.PullRequestContractEthereumApi;
import org.hitchain.contract.api.RepositoryContractEthereumApi;
import org.hitchain.contract.api.TokenEthereumApi;
import org.hitchain.contract.ethereum.PullRequestContractEthereumService;
import org.hitchain.contract.ethereum.RepositoryContractEthereumService;
import org.hitchain.contract.ethereum.TokenEthereumService;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.HitHelper;
import org.hitchain.hit.util.WalletHelper;
import org.iff.infra.util.MapHelper;
import org.iff.infra.util.NumberHelper;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

/**
 * Main
 * <pre>
 * ==~/.hit/config==
 * [main]
 *     default=encrypt
 * [account]
 *     default=accountName1
 *     accountName1_address=hex
 *     accountName1_private=encrypt
 *     accountName2_address=hex
 *     accountName2_private=encrypt
 * [rsa]
 *     default=rsaName1
 *     rsaName1_pub=hex
 *     rsaName1_pri=encrypt
 *     rsaName2_pub=hex
 *     rsaName2_pri=encrypt
 * [storage]
 *     default=urlName1
 *     urlName1=url
 *     urlName2=url
 * [repository]
 *     default=urlName1
 *     urlName1=url
 *     urlName2=url
 * ==command==
 * hit cfg help
 * hit cfg create password
 * hit cfg [account|rsa] [add|remove] name [priKey] [pubKey] password
 * hit cfg [storage|repository] [add|remove] name url
 * hit cfg [account|rsa|storage|repository] set name
 * </pre>
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-22
 * auto generate by qdp.
 */
public class Main {
    public static final String HELP_CFG = "" +
            "hit cfg help\n" +
            "hit cfg create\n" +
            "hit cfg recover    password\n" +
            "hit cfg account    add name [priKey]\n" +
            "hit cfg rsa        add name [priKey pubKey]\n" +
            "hit cfg storage    add name url\n" +
            "hit cfg repository add name url\n" +
            "hit cfg chain      add name url(https://ropsten.infura.io, https://mainnet.infura.io/0x7995ab36bB307Afa6A683C24a25d90Dc1Ea83566)\n" +
            "hit cfg chainapi   add name url(http://api-ropsten.etherscan.io/api, https://api.etherscan.io/api)\n" +
            "hit cfg [account|rsa|storage|repository|chain|chainapi] remove name\n" +
            "hit cfg [account|rsa|storage|repository|chain|chainapi] set    name";
    public static final String HELP_REPO = "" +
            "hit repo help\n" +
            "hit repo keypair   add\n" +
            "hit repo keypair   remove\n" +
            "hit repo keypair   renew\n" +
            "hit repo member    add    memberEmail publicRsa eccAddress\n" +
            "hit repo member    remove memberEmail";
    public static final String HELP_CONTRACT = "" +
            "hit contract help\n" +
            "hit contract repositoryName\n" +
            "hit contract repositoryAddress\n" +
            "hit contract pullRequestAddress\n" +
            "hit contract owner\n" +
            "hit contract delegator\n" +
            "hit contract teamMember\n" +
            "hit contract teamMemberList\n" +
            "hit contract teamMemberCount\n" +
            "hit contract historyRepositoryAddress \n" +
            "hit contract deploy                   gasLimit(5000000)  gWei\n" +
            "hit contract init                     gasLimit( 500000)  gWei  ownerAddress repositoryName\n" +
            "hit contract initWithDelegator        gasLimit( 500000)  gWei  ownerAddress repositoryName delegatorAddress\n" +
            "hit contract updateRepositoryName     gasLimit( 500000)  gWei  newRepositoryName \n" +
            "hit contract updateRepositoryAddress  gasLimit( 500000)  gWei  newRepositoryAddress\n" +
            "hit contract updatePullRequestAddress gasLimit( 500000)  gWei  pullRequestContractAddress\n" +
            "hit contract addTeamMember            gasLimit( 500000)  gWei  memberAddress\n" +
            "hit contract removeTeamMember         gasLimit( 500000)  gWei  memberAddress\n" +
            "hit contract changeOwner              gasLimit( 500000)  gWei  memberAddress\n" +
            "hit contract delegateTo               gasLimit( 500000)  gWei  delegatorAddress";
    public static final String HELP_TOKEN = "" +
            "hit token help\n" +
            "hit token readToken        [accountAddress]\n" +
            "hit token readHitToken     [accountAddress]\n" +
            "hit token readContactToken contractAddress functionName [accountAddress]\n" +
            "hit token requestTestToken [accountAddress]";
    public static final String HELP_PULLREQUEST = "" +
            "hit pullRequest help\n" +
            "hit pullRequest create gasLimit(500000) gWei -m 'comment' [startBranch] [endBranch]\n" +
            "hit pullRequest owner\n" +
            "hit pullRequest delegator\n" +
            "hit pullRequest communityPullRequest  index\n" +
            "hit pullRequest communityPullRequestCount\n" +
            "hit pullRequest authedPullRequest  index\n" +
            "hit pullRequest authedPullRequestCount\n" +
            "hit pullRequest authedAccount accountAddress\n" +
            "hit pullRequest authedAccountList  index\n" +
            "hit pullRequest authedAccountCount\n" +
            "hit pullRequest listCommunityPR\n" +
            "hit pullRequest listAuthedPR" +
            "hit pullRequest enable               gasLimit(5000000)  gWei  [gasLimit(500000)]  [gWei] [-f]\n" +
            "hit pullRequest changeOwner          gasLimit(5000000)  gWei  ownerAddress\n" +
            "hit pullRequest delegateTo           gasLimit(5000000)  gWei  delegatorAddress\n" +
            "hit pullRequest addPullRequest       gasLimit(5000000)  gWei  pullRequest\n" +
            "hit pullRequest addAuthedAccount     gasLimit(5000000)  gWei  accountAddress\n" +
            "hit pullRequest removeAuthedAccount  gasLimit(5000000)  gWei  accountAddress\n";

    public static void main(String[] args) throws Exception {
        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/hellopr");
        args = new String[]{"pullRequest", "create", "5000000", "10", "-m", "test pull request"};
        System.out.println("ARG:" + Arrays.toString(args));
        //
        //this just pre-input password.
        {
            Map<String, Set<String>> needPasswords = MapHelper.toMap(
                    "cfg", new HashSet<>(Arrays.asList("account", "rsa")),
                    "repo", new HashSet<>(Arrays.asList("keypair", "member")),
                    "contract", new HashSet<>(Arrays.asList("deploy", "init", "initWithDelegator", "updateRepositoryName", "updateRepositoryAddress", "addTeamMember", "removeTeamMember", "changeOwner", "delegateTo")),
                    "pullRequest", new HashSet<>(),
                    "push", new HashSet<>(),
                    "fetch", new HashSet<>()
            );
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            if (!HitHelper.getHitConfig().isEmpty() && args.length > 0) {
                Set<String> strings = needPasswords.get(list.poll());
                if (strings != null && (strings.isEmpty() || strings.contains(list.poll()))) {
                    HitHelper.getAccountPriKeyWithPasswordInput();
                }
            }
        }
        //
        if (args != null && args.length > 0 && "help".equals(args[0])) {
            System.out.println(HELP_CFG);
            System.out.println(HELP_REPO);
            System.out.println(HELP_CONTRACT);
            System.out.println(HELP_TOKEN);
            System.out.println(HELP_PULLREQUEST);
        }
        //
        if (args != null && args.length > 0 && "cfg".equals(args[0])) {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                HitHelper.hitConfigInfo(null);
                return;
            }
            String type = list.poll();//account, rsa, storage, repository
            String operation = list.poll();//add, remove, set
            String name = list.poll();
            String v1 = list.poll();// [priKey] [pubKey] password, url, null
            String v2 = list.poll();// [priKey] [pubKey] password, url, null
            String v3 = list.poll();// [priKey] [pubKey] password, url, null
            if (HitHelper.TYPE_help.equals(type)) {
                System.out.println(HELP_CFG);
                return;
            }
            if (HitHelper.TYPE_create.equals(type)) {
                if (HitHelper.createHitConfig()) {
                    HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                }
                return;
            }
            if (HitHelper.TYPE_account.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    String pri = StringUtils.isBlank(v2) ? null : v1;
                    if (HitHelper.accountAdd(name, pri)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.accountRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.accountSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.accountInfo(name);
                return;
            }
            if (HitHelper.TYPE_rsa.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    String pri = StringUtils.isBlank(v2) ? null : v1;
                    String pub = StringUtils.isBlank(v2) ? null : v2;
                    if (HitHelper.rsaAdd(name, pri, pub)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.rsaRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.rsaSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.rsaInfo(name);
                return;
            }
            if (HitHelper.TYPE_storage.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    if (HitHelper.storageAdd(name, v1)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.storageRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.storageSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.storageInfo(name);
                return;
            }
            if (HitHelper.TYPE_repository.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    if (HitHelper.repositoryAdd(name, v1)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.repositoryRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.repositorySet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.repositoryInfo(name);
                return;
            }
            if (HitHelper.TYPE_recover.equals(type)) {
                if ("password".equals(operation)) {
                    System.err.println("Input the mnemonic words:");
                    String input = HitHelper.readFromSystemInput();
                    String password = WalletHelper.mnemonicToString(input);
                    System.out.println("The recover value:" + password);
                    return;
                }
                return;
            }
            if (HitHelper.TYPE_chain.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    if (HitHelper.chainAdd(name, v1)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.chainRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.chainSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.chainInfo(name);
                return;
            }
            if (HitHelper.TYPE_chainapi.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    if (HitHelper.chainAdd(name, v1)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.chainRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.chainSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.chainInfo(name);
                return;
            }

            System.out.println(HELP_CFG);
            return;
        }
        /*-----------------------------------------------------repo-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "repo".equals(args[0])) {
            File projectDir = null;
            {
                String workDir = System.getProperty("git_work_tree");
                if (workDir == null) {
                    workDir = ".";
                }
                projectDir = new File(workDir + "/.git");
            }
            if (projectDir == null || !projectDir.exists()) {
                System.err.println(projectDir.getAbsolutePath() + " is invalid git dir.");
                System.exit(1);
            }
            try (Repository db = new FileRepository(projectDir)) {
                LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
                list.poll();
                if (list.isEmpty()) {
                    list.add(HitHelper.TYPE_help);
                }
                String type = list.poll();//member, keypair
                String operation = list.poll();//add, remove
                String member = list.poll();//团队成员Email
                String memberPubKeyRsa = list.poll();//成员的RSA公钥
                String memberAddressEcc = list.poll();//成员的Ethereum地址
                if (HitHelper.TYPE_help.equals(type)) {
                    System.out.println(HELP_REPO);
                    return;
                }
                ProjectInfoFile projectInfoFile = HitHelper.getProjectInfoFile(projectDir);
                if (HitHelper.TYPE_member.equals(type)) {
                    if (HitHelper.ACTION_add.equals(operation)) {
                        HitHelper.addMember(projectDir, member, memberPubKeyRsa, memberAddressEcc, projectInfoFile);
                        return;
                    }
                    if (HitHelper.ACTION_remove.equals(operation)) {
                        if (HitHelper.removeMember(projectDir, member, memberAddressEcc, projectInfoFile)) return;
                        return;
                    }
                    {// print team member info.
                        HitHelper.printMemberInfo(projectInfoFile);
                    }
                    return;
                }
                if (HitHelper.TYPE_keypair.equals(type)) {
                    if (HitHelper.ACTION_add.equals(operation)) {
                        if (projectInfoFile.isPrivate()) {
                            System.out.println("Repository already has keypair.");
                            return;
                        }
                        HitHelper.addKeyPair(projectDir, projectInfoFile);
                        System.out.println("Repository keypair has updated.");
                        return;
                    }
                    if (HitHelper.ACTION_remove.equals(operation)) {
                        if (!projectInfoFile.isPrivate()) {
                            System.out.println("Repository is public without keypair.");
                            return;
                        }
                        HitHelper.removeKeyPair(projectDir, projectInfoFile);
                        System.out.println("Repository keypair has removed.");
                        return;
                    }
                    if (HitHelper.ACTION_renew.equals(operation)) {
                        HitHelper.addKeyPair(projectDir, projectInfoFile);
                        System.out.println("Repository keypair has renewed.");
                        return;
                    }
                    {// print team member info.
                        HitHelper.printKeyPairInfo(projectInfoFile);
                    }
                    return;
                }
            }

            System.out.println(HELP_REPO);
            return;
        }
        /*-----------------------------------------------------contract-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "contract".equals(args[0])) {
            File projectDir = null;
            {
                String workDir = System.getProperty("git_work_tree");
                if (workDir == null) {
                    workDir = ".";
                }
                projectDir = new File(workDir + "/.git");
            }
            if (projectDir == null || !projectDir.exists()) {
                System.err.println(projectDir.getAbsolutePath() + " is invalid git dir.");
                System.exit(1);
            }
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            // read  : Operation=repositoryName|repositoryAddress|owner|delegator|authedAccounts|authedAccountList|authedAccountSize|hasTeamMember|teamMemberAtIndex
            // write : Operation=init|initWithDelegator|updateRepositoryName|updateRepositoryAddress|addTeamMember|removeTeamMember|changeOwner|delegateTo
            // deploy: Operation=deploy
            String operation = list.poll();// read, write, deploy, help
            String gasLimitStr = list.poll();// as arg0 for read
            String gweiStr = list.poll();//
            String arg0 = list.poll();
            String arg1 = list.poll();
            String arg2 = list.poll();
            if (HitHelper.TYPE_help.equals(operation)) {
                System.out.println(HELP_CONTRACT);
                return;
            }
            ProjectInfoFile projectInfoFile = HitHelper.getProjectInfoFile(projectDir);
            RepositoryContractEthereumApi api = RepositoryContractEthereumService.getApi();
            String fromAddress = HitHelper.getAccountAddress();
            String contractAddress = projectInfoFile.getRepoAddress();
            if ("repositoryName".equals(operation)) {
                String result = api.readRepositoryName(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("repositoryAddress".equals(operation)) {
                String result = api.readRepositoryAddress(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("pullRequestAddress".equals(operation)) {
                String result = api.readPullRequestAddress(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("owner".equals(operation)) {
                String result = api.readOwner(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("delegator".equals(operation)) {
                String result = api.readDelegator(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("teamMember".equals(operation)) {
                boolean result = api.readTeamMember(fromAddress, contractAddress, arg0 = gasLimitStr);
                System.out.println(result);
                return;
            }
            if ("teamMemberList".equals(operation)) {
                String result = api.readTeamMemberList(fromAddress, contractAddress, NumberHelper.getInt(arg0 = gasLimitStr, 0));
                System.out.println(result);
                return;
            }
            if ("teamMemberCount".equals(operation)) {
                int result = api.readTeamMemberCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("historyRepositoryAddress".equals(operation)) {
                String result = api.readHistoryRepositoryAddress(contractAddress);
                System.out.println(result);
                return;
            }
            //
            long gasLimit = NumberHelper.getLong(gasLimitStr, 0);
            long gWei = NumberHelper.getLong(gweiStr, 0);
            if ("deploy".equals(operation)) {
                String result = api.deployContract(HitHelper.getAccountPriKeyWithPasswordInput(), gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("init".equals(operation)) {
                String result = api.writeInit(arg0, arg1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("initWithDelegator".equals(operation)) {
                String result = api.writeInitWithDelegator(arg0, arg1, arg2, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("updateRepositoryName".equals(operation)) {
                String result = api.writeUpdateRepositoryName(arg0, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("updateRepositoryAddress".equals(operation)) {
                String result = api.writeUpdateRepositoryAddress(api.readRepositoryAddress(fromAddress, contractAddress), arg1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("addTeamMember".equals(operation)) {
                String result = api.writeAddTeamMember(arg0, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("removeTeamMember".equals(operation)) {
                String result = api.writeRemoveTeamMember(arg0, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("changeOwner".equals(operation)) {
                String result = api.writeChangeOwner(arg0, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("delegateTo".equals(operation)) {
                String result = api.writeDelegateTo(arg0, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }

            System.out.println(HELP_CONTRACT);
            return;
        }
        /*-----------------------------------------------------token-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "token".equals(args[0])) {
            File projectDir = null;
            {
                String workDir = System.getProperty("git_work_tree");
                if (workDir == null) {
                    workDir = ".";
                }
                projectDir = new File(workDir + "/.git");
            }
            if (projectDir == null || !projectDir.exists()) {
                System.err.println(projectDir.getAbsolutePath() + " is invalid git dir.");
                System.exit(1);
            }
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            String operation = list.poll();// readToken, readHitToken, readContactToken, requestTestToken
            String accoutAddress = list.poll();
            String contractAddress = list.size() > 0 ? (accoutAddress + (accoutAddress = "")) : list.poll();
            String functionName = list.poll();
            if (list.size() > 0) {
                accoutAddress = list.poll();
            }
            if (HitHelper.TYPE_help.equals(operation)) {
                System.out.println(HELP_TOKEN);
                return;
            }
            TokenEthereumApi api = TokenEthereumService.getApi();
            accoutAddress = StringUtils.defaultString(accoutAddress, HitHelper.getAccountAddress());
            if ("readToken".equals(operation)) {
                String result = api.readToken(accoutAddress);
                System.out.println(result);
                return;
            }
            if ("readHitToken".equals(operation)) {
                String result = api.readHitToken(accoutAddress);
                System.out.println(result);
                return;
            }
            if ("readContactToken".equals(operation)) {
                String result = api.readContactToken(accoutAddress, contractAddress, functionName);
                System.out.println(result);
                return;
            }
            if ("requestTestToken".equals(operation)) {
                String result = api.requestTestToken(accoutAddress);
                System.out.println(result);
                return;
            }
            System.out.println(HELP_TOKEN);
            return;
        }
        /*-----------------------------------------------------pullRequest-----------------------------------------------------*/
        else if (args != null && args.length > 0 && ("pullRequest".equals(args[0]) || "pr".equals(args[0]))) {
            File projectDir = null;
            {
                String workDir = System.getProperty("git_work_tree");
                if (workDir == null) {
                    workDir = ".";
                }
                projectDir = new File(workDir + "/.git");
            }
            if (projectDir == null || !projectDir.exists()) {
                System.err.println(projectDir.getAbsolutePath() + " is invalid git dir.");
                System.exit(1);
            }
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                list.add(HitHelper.TYPE_help);
            }
            String operation = list.poll();// Operation, enable, help
            String p1 = list.poll(), p2 = list.poll(), p3 = list.poll(), p4 = list.poll(), p5 = list.poll(), p6 = list.poll();
            String gasLimitStr = p1, gweiStr = p2;
            if (HitHelper.TYPE_help.equals(operation)) {
                System.out.println(HELP_PULLREQUEST);
                return;
            }
            //
            ProjectInfoFile projectInfoFile = HitHelper.getProjectInfoFile(projectDir);
            PullRequestContractEthereumApi api = PullRequestContractEthereumService.getApi();
            RepositoryContractEthereumApi repoApi = RepositoryContractEthereumService.getApi();
            String fromAddress = HitHelper.getAccountAddress();
            String contractAddress = projectInfoFile.getRepoAddress();
            ///
            if ("owner".equals(operation)) {
                String result = api.readOwner(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("delegator".equals(operation)) {
                String result = api.readDelegator(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("communityPullRequest".equals(operation)) {
                String result = api.readCommunityPullRequest(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
                System.out.println(result);
                return;
            }
            if ("communityPullRequestCount".equals(operation)) {
                int result = api.readCommunityPullRequestCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("authedPullRequest".equals(operation)) {
                String result = api.readCommunityPullRequest(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
                System.out.println(result);
                return;
            }
            if ("authedPullRequestCount".equals(operation)) {
                int result = api.readCommunityPullRequestCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("authedAccount".equals(operation)) {
                boolean result = api.readAuthedAccount(fromAddress, contractAddress, p1);
                System.out.println(result);
                return;
            }
            if ("authedAccountList".equals(operation)) {
                String result = api.readAuthedAccountList(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
                System.out.println(result);
                return;
            }
            if ("authedAccountCount".equals(operation)) {
                int result = api.readAuthedAccountCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("listCommunityPR".equals(operation)) {
                String result = api.listCommunityPR();
                System.out.println(result);
                return;
            }
            if ("listAuthedPR".equals(operation)) {
                String result = api.listAuthedPR();
                System.out.println(result);
                return;
            }
            //
            long gasLimit = NumberHelper.getLong(gasLimitStr, 0);
            long gWei = NumberHelper.getLong(gweiStr, 0);
            if ("create".equals(operation)) {
                //hit pullRequest create gasLimit(500000) gWei -m 'comment' [startBranch] [endBranch]
                String commentCmd = p3;// "-m"
                String comment = p4; // comment content
                String startBranch = p5;
                String endBranch = p6;
                // setting system property for transport.
                System.setProperty("GIT_CMD", "pullRequest");
                // commit is required.
                if (!StringUtils.equals(commentCmd, "-m") || StringUtils.isBlank(comment)) {
                    System.err.println("PullRequest comment is required, cmd: hit pullRequest create -m 'comment' [startBranch] [endBranch].");
                    return;
                }
                // check if the repository has enabled the pull request.
                String pullRequestAddress = repoApi.readPullRequestAddress(fromAddress, contractAddress);
                if (StringUtils.isBlank(pullRequestAddress)) {
                    System.err.println("The repository is not enable the pull request!");
                    return;
                }
                String pullRequest = HitHelper.createPullRequest(projectDir, startBranch, endBranch);
                if (StringUtils.isBlank(pullRequest)) {
                    System.err.println("Can not create pull request!");
                    return;
                }
                String prInfo = HitHelper.createPullRequestInfo("test", comment, new String[]{pullRequest});
                if (StringUtils.isBlank(prInfo)) {
                    System.err.println("Can not create pull request info!");
                    return;
                }
                //
                String result = api.writeAddPullRequest(prInfo, HitHelper.getAccountPriKeyWithPasswordInput(), pullRequestAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("enable".equals(operation)) {
                //hit pullRequest enable               gasLimit(5000000)  gWei  [gasLimit(500000)]  [gWei] [-f]
                // test privilege.
                if (!StringUtils.equalsIgnoreCase(projectInfoFile.getOwnerAddressEcc(), HitHelper.getAccountAddress())) {
                    if (!StringUtils.equalsIgnoreCase(repoApi.readDelegator(fromAddress, contractAddress), HitHelper.getAccountAddress())) {
                        System.err.println("Only repository owner or delegator can enable pull request!");
                        return;
                    }
                }
                // check is has pull request.
                String prAddress = repoApi.readPullRequestAddress(fromAddress, contractAddress);// 0x00000
                if (StringUtils.isNotBlank(prAddress) && prAddress.startsWith("0x") && !Numeric.toBigInt(prAddress).equals(BigInteger.ZERO)) {
                    String force = p6;
                    if (!StringUtils.equals(force, "-f")) {
                        System.err.println("This repository has enabled pull request, if you want to change pull request address add -f options!");
                        return;
                    }
                }
                // deploy the pull request contract.
                String result = api.deployContract(HitHelper.getAccountPriKeyWithPasswordInput(), gasLimit, gWei);
                if (ContractApi.isError(result)) {
                    System.out.println("Deploy pull request contract failed, error: " + result + "!");
                    return;
                }
                System.out.println("Pull request contract deploy success, contract address: " + result);
                String result2 = RepositoryContractEthereumService.getApi().writeUpdatePullRequestAddress(result, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, NumberHelper.getLong(p4, 500000), NumberHelper.getLong(p5, 10));
                if (ContractApi.isError(result2)) {
                    System.out.println("Update pull request contract failed, error: " + result2 + "!");
                } else {
                    System.out.println("Pull request enable success.");
                }
                return;
            }
            if ("changeOwner".equals(operation)) {
                String result = api.writeChangeOwner(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("delegateTo".equals(operation)) {
                String result = api.writeDelegateTo(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("addPullRequest".equals(operation)) {
                String result = api.writeAddPullRequest(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("addAuthedAccount".equals(operation)) {
                String result = api.writeAddAuthedAccount(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }
            if ("removeAuthedAccount".equals(operation)) {
                String result = api.writeRemoveAuthedAccount(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasLimit, gWei);
                System.out.println(result);
                return;
            }

            System.out.println(HELP_CONTRACT);
            return;
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod(HitHelper.TYPE_main, String[].class).invoke(null, new Object[]{args});
        }
    }
}
