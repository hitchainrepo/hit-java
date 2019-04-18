/*******************************************************************************
 * Copyright (c) 2019-03-22 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core;

import org.apache.commons.lang3.StringUtils;
import org.hitchain.hit.util.HitHelper;

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
        args = new String[]{"cfg", "account", "add", "main", "123456"};
        if (args != null && args.length > 0 && "cfg".equals(args[0])) {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                HitHelper.hitConfigInfo(null);
                return;
            }
            String type = list.poll();
            String operation = list.poll();//account, rsa, storage, repository
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
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod(HitHelper.TYPE_main, String[].class).invoke(null, args);
        }
    }
}
