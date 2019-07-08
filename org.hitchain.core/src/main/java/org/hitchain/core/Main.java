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
            "hit cfg gas        add name deployGas deployGwei writeGas writeGwei\n" +
            "hit cfg [account|rsa|storage|repository|chain|chainapi|gas|gwei] remove name\n" +
            "hit cfg [account|rsa|storage|repository|chain|chainapi|gas|gwei] set    name";
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
            "hit contract deploy                   \n" +
            "hit contract init                     ownerAddress repositoryName\n" +
            "hit contract initWithDelegator        ownerAddress repositoryName delegatorAddress\n" +
            "hit contract updateRepositoryName     newRepositoryName \n" +
            "hit contract updateRepositoryAddress  newRepositoryAddress\n" +
            "hit contract updatePullRequestAddress pullRequestContractAddress\n" +
            "hit contract addTeamMember            memberAddress\n" +
            "hit contract removeTeamMember         memberAddress\n" +
            "hit contract changeOwner              memberAddress\n" +
            "hit contract delegateTo               delegatorAddress";
    public static final String HELP_TOKEN = "" +
            "hit token help\n" +
            "hit token readToken        [accountAddress]\n" +
            "hit token readHitToken     [accountAddress]\n" +
            "hit token readContactToken contractAddress functionName [accountAddress]\n" +
            "hit token requestTestToken [accountAddress]";
    public static final String HELP_PULLREQUEST = "" +
            "hit pullRequest help\n" +
            "hit pullRequest create -m 'comment' [startBranch] [endBranch]\n" +
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
            "hit pullRequest merge id" +
            "hit pullRequest fetch gitUrl" +
            "hit pullRequest enable [-f]\n" +
            "hit pullRequest changeOwner          ownerAddress\n" +
            "hit pullRequest delegateTo           delegatorAddress\n" +
            "hit pullRequest addPullRequest       pullRequest\n" +
            "hit pullRequest addAuthedAccount     accountAddress\n" +
            "hit pullRequest removeAuthedAccount  accountAddress\n";
    public static final String HELP_MIGRATE = "" +
            "hit migrate gitUrl\n";

    public static void main(String[] args) throws Exception {
//        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/mergepr/hellopr");
//        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/mergepr/migrate");
//        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/migratetest/jfinal");
//        args = new String[]{"pullRequest", "create", "5000000", "10", "-m", "test pull request"};
//        args = new String[]{"pr", "listAuthedPR"};
//        args = new String[]{"pr", "merge", "05c6a12b7f6bd6a16ad57cbbefa8fff56cf330c4"};
//        args = new String[]{"pr", "fetch", "https://github.com/ethereum/ethereumj.git"};
//        args = new String[]{"pr", "fetch", "https://gitee.com/jfinal/jfinal.git"};
//        args = new String[]{"migrate", "https://gitee.com/jfinal/jfinal.git"};
        System.out.println("ARG:" + Arrays.toString(args));
        {
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client.protocol.ResponseProcessCookies", "fatal");
        }
        //
        //this just pre-input password.
        {
            Map<String, Set<String>> needPasswords = MapHelper.toMap(
                    "cfg", new HashSet<>(Arrays.asList("account", "rsa")),
                    "repo", new HashSet<>(Arrays.asList("keypair", "member")),
                    "contract", new HashSet<>(Arrays.asList("deploy", "init", "initWithDelegator", "updateRepositoryName", "updateRepositoryAddress", "addTeamMember", "removeTeamMember", "changeOwner", "delegateTo")),
                    "pullRequest", new HashSet<>(),
                    "push", new HashSet<>(),
                    "fetch", new HashSet<>(),
                    "migrate", new HashSet<>()
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
            System.out.println(HELP_MIGRATE);
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
            String v4 = list.poll();// [priKey] [pubKey] password, url, null
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
                    if (HitHelper.chainApiAdd(name, v1)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.chainApiRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.chainApiSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.chainApiInfo(name);
                return;
            }
            if (HitHelper.TYPE_gas.equals(type)) {
                //hit cfg gas        add name deployGas deployGwei writeGas writeGwei
                if (HitHelper.ACTION_add.equals(operation)) {
                    if (HitHelper.gasAdd(name, v1, v2, v3, v4)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_remove.equals(operation)) {
                    if (HitHelper.gasRemove(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                if (HitHelper.ACTION_set.equals(operation)) {
                    if (HitHelper.gasSet(name)) {
                        HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                    }
                    return;
                }
                HitHelper.gasInfo(name);
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
            String operation = list.poll();// read, write, deploy, help
            String p1 = list.poll(), p2 = list.poll(), p3 = list.poll(), p4 = list.poll();
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
                boolean result = api.readTeamMember(fromAddress, contractAddress, p1);
                System.out.println(result);
                return;
            }
            if ("teamMemberList".equals(operation)) {
                String result = api.readTeamMemberList(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
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
            //
            long gasDeploy = HitHelper.getGasDeploy();
            long gasDeployGwei = HitHelper.getGasDeployGwei();
            long gasWrite = HitHelper.getGasWrite();
            long gasWriteGwei = HitHelper.getGasWriteGwei();
            //
            if ("deploy".equals(operation)) {
                String result = api.deployContract(HitHelper.getAccountPriKeyWithPasswordInput(), gasDeploy, gasDeployGwei);
                System.out.println(result);
                return;
            }
            if ("init".equals(operation)) {
                //hit contract init ownerAddress repositoryName
                String result = api.writeInit(p1, p2, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("initWithDelegator".equals(operation)) {
                //hit contract initWithDelegator   ownerAddress repositoryName delegatorAddress
                String result = api.writeInitWithDelegator(p1, p2, p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("updateRepositoryName".equals(operation)) {
                //hit contract updateRepositoryName   newRepositoryName
                String result = api.writeUpdateRepositoryName(p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("updateRepositoryAddress".equals(operation)) {
                //hit contract updateRepositoryAddress  newRepositoryAddress
                String result = api.writeUpdateRepositoryAddress(api.readRepositoryAddress(fromAddress, contractAddress), p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("updatePullRequestAddress".equals(operation)) {
                //hit contract updatePullRequestAddress pullRequestContractAddress
                String result = api.writeUpdatePullRequestAddress(p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("addTeamMember".equals(operation)) {
                //hit contract addTeamMember  memberAddress
                String result = api.writeAddTeamMember(p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("removeTeamMember".equals(operation)) {
                //hit contract removeTeamMember  memberAddress
                String result = api.writeRemoveTeamMember(p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("changeOwner".equals(operation)) {
                //hit contract changeOwner  memberAddress
                String result = api.writeChangeOwner(p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("delegateTo".equals(operation)) {
                //hit contract delegateTo  delegatorAddress
                String result = api.writeDelegateTo(p1, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
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
            String p1 = list.poll(), p2 = list.poll(), p3 = list.poll(), p4 = list.poll();
            if (HitHelper.TYPE_help.equals(operation)) {
                System.out.println(HELP_PULLREQUEST);
                return;
            }
            //
            ProjectInfoFile projectInfoFile = HitHelper.getProjectInfoFile(projectDir);
            PullRequestContractEthereumApi api = PullRequestContractEthereumService.getApi();
            RepositoryContractEthereumApi repoApi = RepositoryContractEthereumService.getApi();
            String fromAddress = HitHelper.getAccountAddress();
            String repoContractAddress = projectInfoFile.getRepoAddress();
            String contractAddress = repoApi.readPullRequestAddress(fromAddress, repoContractAddress);//maybe 0x00...000
            boolean hasPrContract = StringUtils.isNotBlank(contractAddress) && contractAddress.startsWith("0x") && !Numeric.toBigInt(contractAddress).equals(BigInteger.ZERO);
            contractAddress = hasPrContract ? contractAddress : null;
            ///
            if ("owner".equals(operation)) {
                //hit pullRequest owner
                String result = api.readOwner(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("delegator".equals(operation)) {
                //hit pullRequest delegator
                String result = api.readDelegator(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("communityPullRequest".equals(operation)) {
                //hit pullRequest communityPullRequest  index
                String result = api.readCommunityPullRequest(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
                System.out.println(result);
                return;
            }
            if ("communityPullRequestCount".equals(operation)) {
                //hit pullRequest communityPullRequestCount
                int result = api.readCommunityPullRequestCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("authedPullRequest".equals(operation)) {
                //hit pullRequest authedPullRequest  index
                String result = api.readCommunityPullRequest(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
                System.out.println(result);
                return;
            }
            if ("authedPullRequestCount".equals(operation)) {
                //hit pullRequest authedPullRequestCount
                int result = api.readAuthedPullRequestCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("authedAccount".equals(operation)) {
                //hit pullRequest authedAccount accountAddress
                boolean result = api.readAuthedAccount(fromAddress, contractAddress, p1);
                System.out.println(result);
                return;
            }
            if ("authedAccountList".equals(operation)) {
                //hit pullRequest authedAccountList  index
                String result = api.readAuthedAccountList(fromAddress, contractAddress, NumberHelper.getInt(p1, 0));
                System.out.println(result);
                return;
            }
            if ("authedAccountCount".equals(operation)) {
                //hit pullRequest authedAccountCount
                int result = api.readAuthedAccountCount(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("listCommunityPR".equals(operation)) {
                //hit pullRequest listCommunityPR
                String result = api.listCommunityPR(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("listAuthedPR".equals(operation)) {
                //hit pullRequest listAuthedPR
                String result = api.listAuthedPR(fromAddress, contractAddress);
                System.out.println(result);
                return;
            }
            if ("merge".equals(operation)) {
                //hit pullRequest merge id
                if (StringUtils.isBlank(p1)) {
                    System.err.println("Pull request id is required!");
                    return;
                }
                Map<String, Object> prForMerge = null;
                List<Map<String, Object>> prs = PullRequestContractEthereumService.listAuthedPRs(fromAddress, contractAddress);
                for (Map<String, Object> pr : prs) {
                    if (StringUtils.equals(p1, (String) pr.get("id"))) {
                        prForMerge = pr;
                        break;
                    }
                }
                if (prForMerge == null) {
                    prs = PullRequestContractEthereumService.listCommunityPRs(fromAddress, contractAddress);
                    for (Map<String, Object> pr : prs) {
                        if (StringUtils.equals(p1, (String) pr.get("id"))) {
                            prForMerge = pr;
                            break;
                        }
                    }
                }
                if (prForMerge == null) {
                    System.err.println("Pull request for id:" + p1 + " not found!");
                    return;
                }
                boolean result = HitHelper.pullRequestMerge(projectDir, prForMerge);
                if (Boolean.TRUE.equals(result)) {
                    System.out.println("Apply pull request success, you need to merge this branch to the main branch by using git merge command.");
                }
                return;
            }
            if ("fetch".equals(operation)) {
                //hit pullRequest fetch gitUrl
                if (StringUtils.isBlank(p1)) {
                    System.err.println("Git url is required!");
                    return;
                }
                String result = HitHelper.fetchPullRequest(projectDir, p1);
                System.out.println("Fetch pull request success.");
                return;
            }
            //
            long gasDeploy = HitHelper.getGasDeploy();
            long gasDeployGwei = HitHelper.getGasDeployGwei();
            long gasWrite = HitHelper.getGasWrite();
            long gasWriteGwei = HitHelper.getGasWriteGwei();
            //
            if ("create".equals(operation)) {
                //hit pullRequest create -m 'comment' [startBranch] [endBranch]
                String commentCmd = p1;// "-m"
                String comment = p2; // comment content
                String startBranch = p3;
                String endBranch = p4;
                String result = HitHelper.createPullRequestCmd(projectDir, startBranch, endBranch, StringUtils.equals(commentCmd, "-m") ? comment : null);
                System.out.println(result);
                return;
            }
            if ("enable".equals(operation)) {
                //hit pullRequest enable [-f]
                HitHelper.enablePullRequest(projectDir, StringUtils.equals(p1, "-f"));
                return;
            }
            if ("changeOwner".equals(operation)) {
                String result = api.writeChangeOwner(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("delegateTo".equals(operation)) {
                String result = api.writeDelegateTo(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("addPullRequest".equals(operation)) {
                String result = api.writeAddPullRequest(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("addAuthedAccount".equals(operation)) {
                String result = api.writeAddAuthedAccount(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }
            if ("removeAuthedAccount".equals(operation)) {
                String result = api.writeRemoveAuthedAccount(p3, HitHelper.getAccountPriKeyWithPasswordInput(), contractAddress, gasWrite, gasWriteGwei);
                System.out.println(result);
                return;
            }

            System.out.println(HELP_CONTRACT);
            return;
        }
        /*-----------------------------------------------------migrate-----------------------------------------------------*/
        else if (args != null && args.length > 0 && "migrate".equals(args[0])) {
            if (args.length > 1 && StringUtils.isNotBlank(args[1])) {
                String s = HitHelper.migrateWithPullRequest(args[1]);
                System.out.println("Migrate repository success.");
                return;
            }
            System.out.println(HELP_MIGRATE);
            return;
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod(HitHelper.TYPE_main, String[].class).invoke(null, new Object[]{args});
        }
    }
}
