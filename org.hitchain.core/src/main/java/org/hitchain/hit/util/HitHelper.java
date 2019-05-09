package org.hitchain.hit.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Hit utils.
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
 * @author zhaochen
 */
public class HitHelper {
    public static final String TYPE_main = "main";
    public static final String TYPE_account = "account";
    public static final String TYPE_rsa = "rsa";
    public static final String TYPE_storage = "storage";
    public static final String TYPE_repository = "repository";
    public static final String TYPE_help = "help";
    public static final String TYPE_create = "create";
    //
    public static final String TYPE_member = "member";
    public static final String TYPE_keypair = "keypair";
    //
    public static final String ACTION_add = "add";
    public static final String ACTION_remove = "remove";
    public static final String ACTION_set = "set";
    //
    public static final String ACTION_renew = "renew";

    public static final String NAME_default = "default";
    public static final String FILE_HIT_CONFIG = StringUtils.defaultString(System.getProperty("user.home"), ".") + "/.hit/config";
    private static Map<String/*section*/, Map<String/*name*/, String/*value*/>> hitConfig = null;
    /**
     * cache the encrypted password by first input.
     */
    private static String passwordCache = null;
    private static String passwordEncrypt = UUID.randomUUID().toString();

    public static Map<String/*section*/, Map<String/*name*/, String/*value*/>> hitConfig() {
        Map<String/*section*/, Map<String/*name*/, String/*value*/>> map = new LinkedHashMap<>();
        System.getProperties().put("HitCfg", map);
        String content = null;
        try {
            File file = new File(FILE_HIT_CONFIG);
            if (!file.exists()) {
                return (hitConfig = map);
            }
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (Exception e) {
            System.out.println("Can not read the hit config file.");
        }
        if (StringUtils.isBlank(content)) {
            return (hitConfig = map);
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
        return (hitConfig = map);
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
            if (NAME_default.equals(name)) {
                Tuple.Two<String, String, String> two = new Tuple.Two<>(kv.get(name), null);
                two.result(kv.get(NAME_default));
                return two;
            }
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
            if (StringUtils.isBlank(kv.get(NAME_default))) {
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
            if (StringUtils.isBlank(kv.get(NAME_default))) {
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
        if (!(getHitConfig().isEmpty() || getByName(getHitConfig(), TYPE_main, NAME_default) == null)) {
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
        Map<String, String> kv = getHitConfig().get(TYPE_main);
        if (kv == null) {
            getHitConfig().put(TYPE_main, kv = new LinkedHashMap<>());
        }
        kv.put(NAME_default, sign);
        return true;
    }

    public static boolean testHitConfigPassword(String password, boolean createNew) {
        if (!testPassword(password)) {
            return false;
        }
        if (getHitConfig().isEmpty()) {
            if (createNew) {
                System.out.println("Hit config is empty, try to create new.");
                createHitConfig(password);
                hitConfig();
                return true;
            } else {
                System.out.println("Hit config is empty!");
                return false;
            }
        }
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_main, NAME_default);
        if (!WalletHelper.verifySign(two.first(), password)) {
            System.out.println("Password is not correct!");
            return false;
        }
        return true;
    }

    public static void hitConfigInfo(String section) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String/*section*/, Map<String/*name*/, String/*value*/>> entry : getHitConfig().entrySet()) {
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
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_account, name);
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
            addByName(getHitConfig(), TYPE_account, name, two.first(), two.second());
            return true;
        }
        Tuple.Two<Object, String, String> two = WalletHelper.createExistsAccount(password, priKey);
        addByName(getHitConfig(), TYPE_account, name, two.first(), two.second());
        return true;
    }

    public static boolean accountRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_account, name);
    }

    public static boolean accountSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_account, name) == null || !addByName(getHitConfig(), TYPE_account, NAME_default, name, null)) {
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
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_rsa, name);
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
            addByName(getHitConfig(), TYPE_rsa, name, two.first(), two.second());
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
        addByName(getHitConfig(), TYPE_rsa, name, two.first(), two.second());
        return true;
    }

    public static boolean rsaRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_rsa, name);
    }

    public static boolean rsaSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_rsa, name) == null || !addByName(getHitConfig(), TYPE_rsa, NAME_default, name, null)) {
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
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_storage, name);
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
        addByName(getHitConfig(), TYPE_storage, name, url, null);
        return true;
    }

    public static boolean storageRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_storage, name);
    }

    public static boolean storageSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_storage, name) == null || !addByName(getHitConfig(), TYPE_storage, NAME_default, name, null)) {
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
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_repository, name);
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
        addByName(getHitConfig(), TYPE_repository, name, url, null);
        return true;
    }

    public static boolean repositoryRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_repository, name);
    }

    public static boolean repositorySet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_repository, name) == null || !addByName(getHitConfig(), TYPE_repository, NAME_default, name, null)) {
            System.out.println("Can not find the repository " + name + " config!");
            return false;
        }
        return true;
    }

    public static Map<String, Map<String, String>> getHitConfig() {
        return hitConfig == null ? hitConfig() : hitConfig;
    }

    public static Tuple.Two<String, String, String> getDefaultValue(String section) {
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), section, NAME_default);
        if (two == null || StringUtils.isBlank(two.first())) {
            return null;
        }
        return getByName(getHitConfig(), section, two.first());
    }

    public static Tuple.Two<String, String, String> getAccount() {
        return getDefaultValue(TYPE_account);
    }

    public static Tuple.Two<String, String, String> getAccount(String password) {
        Tuple.Two<String, String, String> two = getAccount();
        String pri = two == null ? null : two.second();
        if (StringUtils.isNotBlank(pri) && testHitConfigPassword(password, false)) {
            Tuple.Two<String, String, String> value = new Tuple.Two<>(two.first(), WalletHelper.decryptWithPasswordHex(pri, password));
            value.result(two.result());
            return value;
        }
        System.out.println("Private key or password is invalid!");
        return null;
    }

    public static Tuple.Two<String, String, String> getAccountWithPasswordInput() {
        Tuple.Two<String, String, String> two = getAccount();
        if (two == null) {
            return null;
        }
        String password = readPassword();
        if (StringUtils.isBlank(password)) {
            return null;
        }
        Tuple.Two<String, String, String> value = new Tuple.Two<>(two.first(), WalletHelper.decryptWithPasswordHex(two.second(), password));
        value.result(two.result());
        return value;
    }

    public static Tuple.Two<String, String, String> getRsa() {
        return getDefaultValue(TYPE_rsa);
    }

    public static Tuple.Two<String, String, String> getRsa(String password) {
        Tuple.Two<String, String, String> two = getRsa();
        String pri = two == null ? null : two.second();
        if (StringUtils.isNotBlank(pri) && testHitConfigPassword(password, false)) {
            Tuple.Two<String, String, String> value = new Tuple.Two<>(two.first(), WalletHelper.decryptWithPasswordHex(pri, password));
            value.result(two.result());
            return value;
        }
        System.out.println("Private key or password is invalid!");
        return null;
    }

    public static Tuple.Two<String, String, String> getRsaWithPasswordInput() {
        Tuple.Two<String, String, String> two = getRsa();
        if (two == null) {
            return null;
        }
        String password = readPassword();
        if (StringUtils.isBlank(password)) {
            return null;
        }
        Tuple.Two<String, String, String> value = new Tuple.Two<>(two.first(), WalletHelper.decryptWithPasswordHex(two.second(), password));
        value.result(two.result());
        return value;
    }

    public static String getStorage() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_storage);
        return two == null ? null : two.first();
    }

    public static String getRepository() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_repository);
        return two == null ? null : two.first();
    }

    public static String getAccountPubKey() {
        Tuple.Two<String, String, String> two = getAccount();
        return two == null ? null : two.first();
    }

    public static String getAccountAddress() {
        return getAccountPubKey();
    }

    public static String getAccountPriKey(String password) {
        Tuple.Two<String, String, String> two = getAccount(password);
        return two == null ? null : two.second();
    }

    public static String getAccountPriKeyWithPasswordInput() {
        Tuple.Two<String, String, String> two = getAccountWithPasswordInput();
        return two == null ? null : two.second();
    }

    public static String getRsaPubKey() {
        Tuple.Two<String, String, String> two = getRsa();
        return two == null ? null : two.first();
    }

    public static String getRsaPriKey(String password) {
        Tuple.Two<String, String, String> two = getRsa(password);
        return two == null ? null : two.second();
    }

    public static String getRsaPriKeyWithPasswordInput() {
        Tuple.Two<String, String, String> two = getRsaWithPasswordInput();
        return two == null ? null : two.second();
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

    public static String readFromSystemInput() {
        try {
            // System.in是一个很原始、很简陋的输入流对象，通常不直接使用它来读取用户的输入。
            // 一般会在外面封装过滤流：
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            // 然后调用br.readLine()方法进行读取。
            return br.readLine();
        } catch (Exception e) {
        }
        return null;
    }

    public static String readPassword() {
        if (StringUtils.isNotBlank(passwordCache)) {
            return WalletHelper.decryptWithPasswordHex(passwordCache, passwordEncrypt);
        }
        String password = "";
        Console console = System.console();
        if (console == null) {
            System.err.println("Couldn't get Console instance, password input will show in console!");
        }
        if (console != null) {
            for (int i = 0; i < 3; i++) {
                char passwordArray[] = console.readPassword("Enter hit config password: ");
                password = new String(passwordArray);
                if (testHitConfigPassword(password, false)) {
                    break;
                }
                System.err.println("Password is incorrect, try again!");
            }
        } else {
            for (int i = 0; i < 3; i++) {
                System.err.println("Enter hit config password:");
                password = readFromSystemInput();
                if (testHitConfigPassword(password, false)) {
                    break;
                }
                System.err.println("Password is incorrect, try again!");
            }
        }
        if (StringUtils.isBlank(password)) {
            System.err.println("Password is incorrect!");
            System.exit(0);
            return null;
        }
        {
            passwordCache = WalletHelper.encryptWithPasswordHex(password, passwordEncrypt);
        }
        return password;
    }
}
