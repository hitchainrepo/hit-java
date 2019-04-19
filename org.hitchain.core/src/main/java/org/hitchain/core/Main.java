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
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.EthereumHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.HitHelper;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

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
    public static void main(String[] args) throws Exception {
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
                System.out.println("hit cfg help");
                System.out.println("hit cfg create     password");
                System.out.println("hit cfg account    add name [priKey]        password");
                System.out.println("hit cfg rsa        add name [priKey pubKey] password");
                System.out.println("hit cfg storage    add name url");
                System.out.println("hit cfg repository add name url");
                System.out.println("hit cfg [account|rsa|storage|repository] remove name");
                System.out.println("hit cfg [account|rsa|storage|repository] set    name");
                return;
            }
            if (HitHelper.TYPE_create.equals(type)) {
                String password = operation;
                if (HitHelper.createHitConfig(password)) {
                    HitHelper.hitConfigToFile(HitHelper.getHitConfig());
                }
                return;
            }
            if (HitHelper.TYPE_account.equals(type)) {
                if (HitHelper.ACTION_add.equals(operation)) {
                    String pri = StringUtils.isBlank(v2) ? null : v1;
                    String password = pri == null ? v1 : v2;
                    if (HitHelper.accountAdd(name, pri, password)) {
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
                    String password = pri == null ? v1 : v3;
                    if (HitHelper.rsaAdd(name, pri, pub, password)) {
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
        }
        if (args != null && args.length > 0 && "repo".equals(args[0])) {
            File projectDir = null;
            {
                String workDir = System.getProperty("git_work_tree");
                if (workDir == null) {
                    workDir = ".";
                    System.out.println(
                            "System property 'git_work_tree' not specified, using current directory: "
                                    + new File(workDir).getAbsolutePath());
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
                    // list all members
                    return;
                }
                String type = list.poll();//member
                String operation = list.poll();//add, remove
                String member = list.poll();//团队成员Email
                String memberPubKeyRsa = list.poll();//成员的RSA公钥
                String memberAddressEcc = list.poll();//成员的Ethereum地址
                if (HitHelper.TYPE_help.equals(type)) {
                    System.out.println("hit repo help");
                    System.out.println("hit repo keypair   add");
                    System.out.println("hit repo keypair   remove");
                    System.out.println("hit repo keypair   renew");
                    System.out.println("hit repo member    add    memberEmail publicRsa eccAddress");
                    System.out.println("hit repo member    remove memberEmail");
                    return;
                }
                ProjectInfoFile projectInfoFile = getProjectInfoFile(projectDir);
                if (HitHelper.TYPE_member.equals(type)) {
                    if ("add".equals(operation)) {
                        addMember(projectDir, member, memberPubKeyRsa, memberAddressEcc, projectInfoFile);
                        return;
                    }
                    if ("remove".equals(operation)) {
                        if (removeMember(projectDir, member, memberAddressEcc, projectInfoFile)) return;
                        return;
                    }
                    {// print team member info.
                        printMemberInfo(projectInfoFile);
                    }
                    return;
                }
            }
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod(HitHelper.TYPE_main, String[].class).invoke(null, new Object[]{args});
        }
    }

    private static void printMemberInfo(ProjectInfoFile projectInfoFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Team members: \n");
        for (ProjectInfoFile.TeamInfo ti : projectInfoFile.getMembers()) {
            sb.append(StringUtils.rightPad(ti.getMember(), 20)).append(' ')
                    .append(StringUtils.rightPad(ti.getMemberAddressEcc(), 45)).append(' ')
                    .append(ti.getMemberPubKeyRsa()).append('\n');
        }
        System.out.println(sb);
    }

    private static boolean removeMember(File projectDir, String member, String memberAddressEcc, ProjectInfoFile projectInfoFile) {
        ProjectInfoFile.TeamInfo teamInfo = null;
        for (ProjectInfoFile.TeamInfo ti : projectInfoFile.getMembers()) {
            if (StringUtils.equals(ti.getMember(), member)) {
                teamInfo = ti;
                break;
            }
        }
        if (teamInfo == null) {
            System.out.println("No member found.");
            return true;
        }
        projectInfoFile.getMembers().remove(teamInfo);
        String result = EthereumHelper.removeTeamMember(projectInfoFile.getEthereumUrl(), projectInfoFile.getRepoAddress(), memberAddressEcc);
        if (EthereumHelper.isError(result)) {
            System.err.println("Remove member error:" + result);
            System.exit(1);
        }
        if (!GitHelper.updateHitRepositoryProjectInfoFile(projectDir, projectInfoFile)) {
            System.err.println("Update project info file error.");
            System.exit(1);
        }
        System.out.println("Member has removed in the contract.");
        return false;
    }

    private static void addMember(File projectDir, String member, String memberPubKeyRsa, String memberAddressEcc, ProjectInfoFile projectInfoFile) {
        if (StringUtils.isBlank(member) || StringUtils.isBlank(memberPubKeyRsa) || StringUtils.isBlank(memberAddressEcc)) {
            System.err.println("Can't not execute command.");
            System.exit(1);
        }
        for (ProjectInfoFile.TeamInfo ti : projectInfoFile.getMembers()) {
            if (StringUtils.equals(ti.getMember(), member) || StringUtils.equals(ti.getMemberAddressEcc(), memberAddressEcc)) {
                System.out.println("Member is exists.");
                return;
            }
        }
        if (projectInfoFile.isPrivate()) {
            projectInfoFile.addMemberPrivate(member, memberPubKeyRsa, memberAddressEcc, HitHelper.getRsaPriKeyWithPasswordInput());
        } else {
            projectInfoFile.addMemberPublic(member, memberPubKeyRsa, memberAddressEcc);
        }
        if (EthereumHelper.hasTeamMember(projectInfoFile.getEthereumUrl(), projectInfoFile.getRepoAddress(), memberAddressEcc)) {
            System.out.println("Member is exists in the contract.");
            return;
        }
        String result = EthereumHelper.addTeamMember(projectInfoFile.getEthereumUrl(), projectInfoFile.getRepoAddress(), memberAddressEcc);
        if (EthereumHelper.isError(result)) {
            System.err.println("Add member error:" + result);
            System.exit(1);
        }
        if (!GitHelper.updateHitRepositoryProjectInfoFile(projectDir, projectInfoFile)) {
            System.err.println("Update project info file error.");
            System.exit(1);
        }
        System.out.println("Member has added in the contract.");
        return;
    }

    private static ProjectInfoFile getProjectInfoFile(File projectDir) {
        ProjectInfoFile projectInfoFile = null;
        if (GitHelper.existProjectInfoFile(projectDir)) {
            projectInfoFile = GitHelper.readProjectInfoFile(projectDir);
        }
        if (projectInfoFile == null) {
            System.err.println(projectDir.getAbsolutePath() + " is invalid hit repository.");
            System.exit(1);
        }
        if (!StringUtils.equals(HitHelper.getAccountAddress(), projectInfoFile.getOwnerAddressEcc())) {
            System.err.println("Only owner can change this settings.");
            System.exit(1);
        }
        if (!projectInfoFile.verify(null)) {
            System.err.println("Only owner can change this settings.");
            System.exit(1);
        }
        return projectInfoFile;
    }
}
