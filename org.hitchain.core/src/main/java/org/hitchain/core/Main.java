/*******************************************************************************
 * Copyright (c) 2019-03-22 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hitchain.hit.util.Tuple;
import org.hitchain.hit.util.WalletHelper;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

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
    public static final String TYPE_main = "main";
    public static final String TYPE_account = "account";
    public static final String TYPE_rsa = "rsa";
    public static final String TYPE_storage = "storage";
    public static final String TYPE_repository = "repository";
    public static final String TYPE_help = "help";
    public static final String TYPE_create = "create";
    public static final String ACTION_add = "add";
    public static final String ACTION_remove = "remove";
    public static final String ACTION_set = "set";
    public static final String NAME_default = "default";
    public static final String FILE_HIT_CONFIG = StringUtils.defaultString(System.getProperty("user.home"), ".") + "/.hit/config";
    private static Map<String/*section*/, Map<String/*name*/, String/*value*/>> hitConfig = null;

    public static void main(String[] args) throws Exception {
        hitConfig = hitConfig();
        if (args != null && args.length > 0 && "cfg".equals(args[0])) {
            LinkedList<String> list = new LinkedList<>(Arrays.asList(args));
            list.poll();
            if (list.isEmpty()) {
                hitConfigInfo(null);
                return;
            }
            String type = list.poll();
            String operation = list.poll();//account, rsa, storage, repository
            String name = list.poll();
            String v1 = list.poll();// [priKey] [pubKey] password, url, null
            String v2 = list.poll();// [priKey] [pubKey] password, url, null
            String v3 = list.poll();// [priKey] [pubKey] password, url, null
            if (TYPE_help.equals(type)) {
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
            if (TYPE_create.equals(type)) {
                String password = operation;
                if (createHitConfig(password)) {
                    hitConfigToFile(hitConfig);
                }
                return;
            }
            if (TYPE_account.equals(type)) {
                if (ACTION_add.equals(operation)) {
                    String pri = StringUtils.isBlank(v2) ? null : v1;
                    String password = pri == null ? v1 : v2;
                    if (accountAdd(name, pri, password)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_remove.equals(operation)) {
                    if (accountRemove(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_set.equals(operation)) {
                    if (accountSet(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                accountInfo(name);
                return;
            }
            if (TYPE_rsa.equals(type)) {
                if (ACTION_add.equals(operation)) {
                    String pri = StringUtils.isBlank(v2) ? null : v1;
                    String pub = StringUtils.isBlank(v2) ? null : v2;
                    String password = pri == null ? v1 : v3;
                    if (rsaAdd(name, pri, pub, password)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_remove.equals(operation)) {
                    if (rsaRemove(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_set.equals(operation)) {
                    if (rsaSet(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                rsaInfo(name);
                return;
            }
            if (TYPE_storage.equals(type)) {
                if (ACTION_add.equals(operation)) {
                    if (storageAdd(name, v1)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_remove.equals(operation)) {
                    if (storageRemove(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_set.equals(operation)) {
                    if (storageSet(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                storageInfo(name);
                return;
            }
            if (TYPE_repository.equals(type)) {
                if (ACTION_add.equals(operation)) {
                    if (repositoryAdd(name, v1)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_remove.equals(operation)) {
                    if (repositoryRemove(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                if (ACTION_set.equals(operation)) {
                    if (repositorySet(name)) {
                        hitConfigToFile(hitConfig);
                    }
                    return;
                }
                repositoryInfo(name);
                return;
            }
        } else {
            Class<?> main = Class.forName("org.eclipse.jgit.pgm.Main");
            main.getMethod(TYPE_main, String[].class).invoke(null, args);
        }
    }

    public static Map<String/*section*/, Map<String/*name*/, String/*value*/>> hitConfig() {
        Map<String/*section*/, Map<String/*name*/, String/*value*/>> map = new LinkedHashMap<>();
        System.getProperties().put("HitCfg", map);
        String content = null;
        try {
            File file = new File(FILE_HIT_CONFIG);
            if (!file.exists()) {
                return map;
            }
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            System.out.println("Can not read the hit config file.");
        }
        if (StringUtils.isBlank(content)) {
            return map;
        }
        String[] lines = StringUtils.split(content, '\n');
        String type = null;
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            line = line.trim();
            if (line.startsWith("[")) {
                type = line.substring(1, line.length() - 1);
                continue;
            }
            int indexOf = line.indexOf('=');
            if (indexOf < 1 || StringUtils.isBlank(type)) {
                throw new RuntimeException("Hit config has wrong format!");
            }
            String key = line.substring(0, indexOf).trim();
            String value = line.substring(indexOf + 1, line.length()).trim();
            Map<String, String> kv = map.get(type);
            if (kv == null) {
                map.put(type, kv = new LinkedHashMap<>());
            }
            kv.put(key, value);
        }
        return map;
    }

    public static void hitConfigToFile(Map<String/*section*/, Map<String/*name*/, String/*value*/>> config) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String/*section*/, Map<String/*name*/, String/*value*/>> entry : config.entrySet()) {
            sb.append('[').append(entry.getKey()).append("]\n");
            for (Map.Entry<String, String> kv : entry.getValue().entrySet()) {
                sb.append("    ").append(kv.getKey()).append('=').append(kv.getValue()).append('\n');
            }
        }
        try {
            File file = new File(FILE_HIT_CONFIG);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileUtils.writeStringToFile(file, sb.toString());
            System.out.println("Hit config is updated.");
        } catch (Exception e) {
            System.out.println("Can not read the hit config file!");
        }
    }

    public static Tuple.Two<String, String, String> getByName(Map<String/*section*/, Map<String/*name*/, String/*value*/>> config, String section, String name) {
        if (config.isEmpty()) {
            return null;
        }
        Map<String, String> kv = config.get(section);
        if (kv == null || kv.isEmpty()) {
            return null;
        }
        if (TYPE_account.equals(section) || TYPE_rsa.equals(section)) {
            String pub = name + "_pub";
            String pri = name + "_pri";
            if (!(kv.containsKey(pub) && kv.containsKey(pri))) {
                return null;
            }
            Tuple.Two<String, String, String> two = new Tuple.Two<>(kv.get(pub), kv.get(pri));
            two.result(kv.get(NAME_default));
            return two;
        }
        if (TYPE_storage.equals(section) || TYPE_repository.equals(section) || TYPE_main.equals(section)) {
            if (!kv.containsKey(name)) {
                return null;
            }
            Tuple.Two<String, String, String> two = new Tuple.Two<>(kv.get(name), null);
            two.result(kv.get(NAME_default));
            return two;
        }
        return null;
    }

    public static boolean addByName(Map<String/*section*/, Map<String/*name*/, String/*value*/>> config, String section, String name, String value1, String value2) {
        if (config.isEmpty()) {
            System.out.println("Hit config file is empty, create first!");
            return false;
        }
        Map<String, String> kv = config.get(section);
        if (kv == null) {
            config.put(section, kv = new LinkedHashMap<>());
        }
        if (!kv.containsKey(NAME_default)) {
            kv.put(NAME_default, "");
        }
        if (TYPE_account.equals(section) || TYPE_rsa.equals(section)) {
            if (NAME_default.equals(name)) {
                kv.put(name, value1);
                return true;
            }
            String pub = name + "_pub";
            String pri = name + "_pri";
            if (kv.containsKey(pub) || kv.containsKey(pri)) {
                System.out.println("Hit config " + section + " " + name + " is exists!");
                return false;
            }
            if (kv.isEmpty()) {
                kv.put(NAME_default, name);
            }
            kv.put(pub, value1);
            kv.put(pri, value2);
            return true;
        }
        if (TYPE_storage.equals(section) || TYPE_repository.equals(section) || TYPE_main.equals(section)) {
            if (NAME_default.equals(name)) {
                kv.put(name, value1);
                return true;
            }
            if (kv.containsKey(name)) {
                System.out.println("Hit config " + section + " " + name + " is exists!");
                return false;
            }
            if (kv.isEmpty()) {
                kv.put(NAME_default, name);
            }
            kv.put(name, value1);
            return true;
        }
        System.out.println("Hit config can not find the " + section + " " + name + "!");
        return false;
    }

    public static boolean removeByName(Map<String/*section*/, Map<String/*name*/, String/*value*/>> config, String section, String name) {
        if (config.isEmpty()) {
            System.out.println("Hit config file is empty, create first!");
            return false;
        }
        Map<String, String> kv = config.get(section);
        if (kv == null || kv.isEmpty()) {
            System.out.println("Hit config " + section + " is empty.");
            return true;
        }
        if (TYPE_account.equals(section) || TYPE_rsa.equals(section)) {
            String pub = name + "_pub";
            String pri = name + "_pri";
            kv.remove(pub);
            kv.remove(pri);
            if (name.equals(StringUtils.trim(kv.get(NAME_default)))) {
                kv.put(NAME_default, "");
            }
            return true;
        }
        if (TYPE_storage.equals(section) || TYPE_repository.equals(section) || TYPE_main.equals(section)) {
            kv.remove(name);
            if (name.equals(StringUtils.trim(kv.get(NAME_default)))) {
                kv.put(NAME_default, "");
            }
            return true;
        }
        System.out.println("Hit config can not find the " + section + " " + name + "!");
        return false;
    }

    public static boolean testPassword(String password) {
        Pattern compile = Pattern.compile("^[a-zA-Z0-9~`!@#\\$%\\^&\\*\\(\\)-_\\+=\\{\\}\\[\\]|\\\\:;\"'<,>\\.\\?/]{6,15}$");
        boolean matches = compile.matcher(password).matches();
        if (!matches) {
            System.out.println("Password should use the the number, character and special chars and must has 6 to 15 chars!");
        }
        return matches;
    }

    public static boolean createHitConfig(String password) {
        if (!(hitConfig.isEmpty() || getByName(hitConfig, TYPE_main, NAME_default) == null)) {
            System.out.println("Hit config file is exists!");
            return false;
        }
        if (!testPassword(password)) {
            return false;
        }
        String mnemonic = WalletHelper.stringToMnemonic(password);
        System.out.println(StringUtils.repeat('!', 50));
        System.out.println("Please write down the mnemonic words(for recover your password)!!! : \n" + mnemonic);
        System.out.println(StringUtils.repeat('!', 50));
        String sign = WalletHelper.sign(password);
        Map<String, String> kv = hitConfig.get(TYPE_main);
        if (kv == null) {
            hitConfig.put(TYPE_main, kv = new LinkedHashMap<>());
        }
        kv.put(NAME_default, sign);
        return true;
    }

    public static boolean testHitConfigPassword(String password, boolean createNew) {
        if (!testPassword(password)) {
            return false;
        }
        if (hitConfig.isEmpty()) {
            if (createNew) {
                System.out.println("Hit config is empty, try to create new.");
                createHitConfig(password);
                hitConfig = hitConfig();
                return true;
            } else {
                System.out.println("Hit config is empty!");
                return false;
            }
        }
        Tuple.Two<String, String, String> two = getByName(hitConfig, TYPE_main, NAME_default);
        if (!WalletHelper.verifySign(two.first(), password)) {
            System.out.println("Password is not correct!");
            return false;
        }
        return true;
    }

    public static void hitConfigInfo(String section) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String/*section*/, Map<String/*name*/, String/*value*/>> entry : hitConfig.entrySet()) {
            if (section != null && !entry.getKey().equals(section)) {
                continue;
            }
            sb.append('[').append(entry.getKey()).append("]\n");
            for (Map.Entry<String, String> kv : entry.getValue().entrySet()) {
                sb.append("    ").append(kv.getKey()).append('=').append(kv.getValue()).append('\n');
            }
        }
        System.out.println(sb);
    }

    public static void accountInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_account);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(hitConfig, TYPE_account, name);
        if (two == null) {
            System.out.println("Can not find the account name: " + name);
            return;
        }
        System.out.println("The information by account's name: " + name);
        System.out.println("public  key :" + two.first());
        System.out.println("private key :" + two.second());
        System.out.println("default name:" + two.result());
    }

    public static boolean accountAdd(String name, String priKey, String password) {
        if (!isValidName(name)) {
            return false;
        }
        if (!isValidPassword(password)) {
            return false;
        }
        if (!testHitConfigPassword(password, true)) {
            return false;
        }
        if (StringUtils.isBlank(priKey)) {
            System.out.println("Create new account.");
            Tuple.Two<Object, String, String> two = WalletHelper.createAccount(password);
            addByName(hitConfig, TYPE_account, name, two.first(), two.second());
            return true;
        }
        Tuple.Two<Object, String, String> two = WalletHelper.createExistsAccount(password, priKey);
        addByName(hitConfig, TYPE_account, name, two.first(), two.second());
        return true;
    }

    public static boolean accountRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(hitConfig, TYPE_account, name);
    }

    public static boolean accountSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(hitConfig, TYPE_account, name) == null || !addByName(hitConfig, TYPE_account, NAME_default, name, null)) {
            System.out.println("Can not find the account " + name + " config!");
            return false;
        }
        return true;
    }

    public static void rsaInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_rsa);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(hitConfig, TYPE_rsa, name);
        if (two == null) {
            System.out.println("Can not find the rsa name: " + name);
            return;
        }
        System.out.println("The information by rsa's name: " + name);
        System.out.println("public  key :" + two.first());
        System.out.println("private key :" + two.second());
        System.out.println("default name:" + two.result());
    }

    public static boolean rsaAdd(String name, String priKey, String pubKey, String password) {
        if (!isValidName(name)) {
            return false;
        }
        if (!isValidPassword(password)) {
            return false;
        }
        if (!testHitConfigPassword(password, true)) {
            return false;
        }
        if (StringUtils.isBlank(priKey)) {
            System.out.println("Create new rsa.");
            Tuple.Two<Object, String, String> two = WalletHelper.createRsa(password);
            addByName(hitConfig, TYPE_rsa, name, two.first(), two.second());
            return true;
        }
        Tuple.Two<Object, String, String> two = null;
        try {
            two = WalletHelper.createExistsRsa(password, priKey, pubKey);
        } catch (Exception e) {
            two = null;
        }
        if (two == null) {
            System.out.println("The rsa key pair is invalid!");
            return false;
        }
        addByName(hitConfig, TYPE_rsa, name, two.first(), two.second());
        return true;
    }

    public static boolean rsaRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(hitConfig, TYPE_rsa, name);
    }

    public static boolean rsaSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(hitConfig, TYPE_rsa, name) == null || !addByName(hitConfig, TYPE_rsa, NAME_default, name, null)) {
            System.out.println("Can not find the rsa " + name + " config!");
            return false;
        }
        return true;
    }

    public static void storageInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_storage);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(hitConfig, TYPE_storage, name);
        if (two == null) {
            System.out.println("Can not find the storage name: " + name);
            return;
        }
        System.out.println("The information by storage's name: " + name);
        System.out.println("storage url :" + two.first());
        System.out.println("default name:" + two.result());
    }

    public static boolean storageAdd(String name, String url) {
        if (!isValidName(name)) {
            return false;
        }
        if (!isValidUrl(url)) {
            return false;
        }
        addByName(hitConfig, TYPE_storage, name, url, null);
        return true;
    }

    public static boolean storageRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(hitConfig, TYPE_storage, name);
    }

    public static boolean storageSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(hitConfig, TYPE_storage, name) == null || !addByName(hitConfig, TYPE_storage, NAME_default, name, null)) {
            System.out.println("Can not find the storage " + name + " config!");
            return false;
        }
        return true;
    }

    public static void repositoryInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_repository);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(hitConfig, TYPE_repository, name);
        if (two == null) {
            System.out.println("Can not find the repository name: " + name);
            return;
        }
        System.out.println("The information by repository's name: " + name);
        System.out.println("repository url :" + two.first());
        System.out.println("default    name:" + two.result());
    }

    public static boolean repositoryAdd(String name, String url) {
        if (!isValidName(name)) {
            return false;
        }
        if (!isValidUrl(url)) {
            return false;
        }
        addByName(hitConfig, TYPE_repository, name, url, null);
        return true;
    }

    public static boolean repositoryRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(hitConfig, TYPE_repository, name);
    }

    public static boolean repositorySet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(hitConfig, TYPE_repository, name) == null || !addByName(hitConfig, TYPE_repository, NAME_default, name, null)) {
            System.out.println("Can not find the repository " + name + " config!");
            return false;
        }
        return true;
    }

    private static boolean isValidPassword(String password) {
        if (StringUtils.isBlank(password)) {
            System.out.println("Password is required!");
            return false;
        }
        return true;
    }

    private static boolean isValidName(String name) {
        if (StringUtils.isBlank(name) || !name.matches("^[0-9a-zA-Z_]+$")) {
            System.out.println("Name is required and matches 0-9, a-z, A-Z and _!");
            return false;
        }
        return true;
    }


    private static boolean isValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            System.out.println("Url is required!");
            return false;
        }
        return true;
    }
}
