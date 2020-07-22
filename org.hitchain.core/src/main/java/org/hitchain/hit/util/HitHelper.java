package org.hitchain.hit.util;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Hit;
import org.eclipse.jgit.api.MigrateCommand;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.hit.api.ProjectInfoFile;
import org.iff.infra.util.FCS;
import org.iff.infra.util.NumberHelper;
import org.iff.infra.util.PreRequiredHelper;
import org.iff.infra.util.SocketHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
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
    public static final String TYPE_help = "help";
    public static final String TYPE_create = "create";
    public static final String TYPE_recover = "recover";
    public static final String TYPE_chain = "chain";
    public static final String TYPE_chainapi = "chainapi";
    public static final String TYPE_gas = "gas";
    public static final String TYPE_contract = "contract";
    //
    public static final String TYPE_member = "member";
    public static final String TYPE_keypair = "keypair";
    //
    public static final String ACTION_add = "add";
    public static final String ACTION_remove = "remove";
    public static final String ACTION_set = "set";
    public static final String ACTION_deploy = "deploy";
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
        {// set hit config to system.
            System.getProperties().put("HitCfg", map);
        }
        String content = null;
        try {// read hit config from file.
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
        // read hit config by line
        String[] lines = StringUtils.split(content, '\n');
        String type = null;
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            line = line.trim();
            // this is the section start, starts with "[" and ends with "]"
            if (line.startsWith("[")) {
                type = line.substring(1, line.length() - 1);
                continue;
            }
            // the key-value line.
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

    /**
     * @param config
     * @param section
     * @param name
     * @return Tuple.Two[String, String: value1, String: value2]
     */
    public static Tuple.Two<String, String/*value1*/, String/*value2*/> getByName(Map<String/*section*/, Map<String/*name*/, String/*value*/>> config, String section, String name) {
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
        {//TYPE_storage, TYPE_repository, TYPE_chain, TYPE_chainapi, TYPE_main, TYPE_gas, TYPE_contract
            if (!kv.containsKey(name)) {
                return null;
            }
            Tuple.Two<String, String, String> two = new Tuple.Two<>(kv.get(name), null);
            two.result(kv.get(NAME_default));
            return two;
        }
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
        {//TYPE_storage, TYPE_repository, TYPE_chain, TYPE_chainapi, TYPE_main, TYPE_gas, TYPE_contract
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
        {//TYPE_storage, TYPE_repository, TYPE_chain, TYPE_chainapi, TYPE_main, TYPE_gas, TYPE_contract
            kv.remove(name);
            if (name.equals(StringUtils.trim(kv.get(NAME_default)))) {
                kv.put(NAME_default, "");
            }
            return true;
        }
    }

    public static boolean testPassword(String password) {
        Pattern compile = Pattern.compile("^[a-zA-Z0-9~`!@#\\$%\\^&\\*\\(\\)-_\\+=\\{\\}\\[\\]|\\\\:;\"'<,>\\.\\?/]{6,15}$");
        boolean matches = compile.matcher(password).matches();
        if (!matches) {
            System.out.println("Password should use the the number, character and special chars and must has 6 to 15 chars!");
        }
        return matches;
    }

    public static boolean createHitConfig() {
        if (!(getHitConfig().isEmpty() || getByName(getHitConfig(), TYPE_main, NAME_default) == null)) {
            System.out.println("Hit config file is exists!");
            return false;
        }
        System.err.println();
        String password = createPassword();
        String mnemonic = WalletHelper.stringToMnemonic(password);
        System.out.println(StringUtils.repeat('!', 50));
        System.out.println("Please write down the mnemonic words(for recover your password)!!! : \n" + mnemonic);
        System.out.println(StringUtils.repeat('!', 50));
        String sign = WalletHelper.sign(password);
        {//init main
            Map<String, String> kv = getHitConfig().get(TYPE_main);
            if (kv == null) {
                getHitConfig().put(TYPE_main, kv = new LinkedHashMap<>());
            }
            kv.put(NAME_default, sign);
        }
        {//init account
            Map<String, String> kv = getHitConfig().get(TYPE_account);
            if (kv == null) {
                accountAdd("main", null);
            }
        }
        {//init rsa
            Map<String, String> kv = getHitConfig().get(TYPE_rsa);
            if (kv == null) {
                rsaAdd("main", null, null);
            }
        }
        {//init storage
            Map<String, String> kv = getHitConfig().get(TYPE_storage);
            if (kv == null) {
                storageAdd("main", "http://121.40.127.45:5001/api/v0/,http://121.40.127.45:8080/ipfs/");
            }
        }
        {//init chain
            Map<String, String> kv = getHitConfig().get(TYPE_chain);
            if (kv == null) {
                chainAdd("test", "https://ropsten.infura.io/v3/c4bf69dfc42d40b5aadf56b4ce19d078");
                chainAdd("main", "https://mainnet.infura.io/v3/c4bf69dfc42d40b5aadf56b4ce19d078");
            }
        }
        {//init chain api
            Map<String, String> kv = getHitConfig().get(TYPE_chainapi);
            if (kv == null) {
                chainApiAdd("test", "http://api-ropsten.etherscan.io/api");
                chainApiAdd("main", "https://api.etherscan.io/api");
            }
        }
        {//init gas
            Map<String, String> kv = getHitConfig().get(TYPE_gas);
            if (kv == null) {
                gasAdd("main", "5000000", "30", "500000", "30");
            }
        }
        return true;
    }

    public static boolean testHitConfigPassword(String password, boolean createNew) {
        if (!testPassword(password)) {
            return false;
        }
        if (getHitConfig().isEmpty()) {
            if (createNew) {
                System.out.println("Hit config is empty, try to create new.");
                createHitConfig();
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

    public static boolean accountAdd(String name, String priKey) {
        if (!isValidName(name)) {
            return false;
        }
        String password = readPassword();
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

    public static boolean rsaAdd(String name, String priKey, String pubKey) {
        if (!isValidName(name)) {
            return false;
        }
        String password = readPassword();
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
        String[] split = StringUtils.split(url, ",");
        if (split.length != 2) {
            return false;
        }
        if (!split[0].endsWith("/")) {
            split[0] += "/";
        }
        if (!split[1].endsWith("/")) {
            split[1] += "/";
        }
        addByName(getHitConfig(), TYPE_storage, name, StringUtils.join(split, ","), null);
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

    public static void chainInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_chain);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_chain, name);
        if (two == null) {
            System.out.println("Can not find the chain name: " + name);
            return;
        }
        System.out.println("The information by chain's name: " + name);
        System.out.println("chain url :" + two.first());
        System.out.println("default    name:" + two.result());
    }

    public static boolean chainAdd(String name, String url) {
        if (!isValidName(name)) {
            return false;
        }
        if (!isValidUrl(url)) {
            return false;
        }
        addByName(getHitConfig(), TYPE_chain, name, url, null);
        return true;
    }

    public static boolean chainRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_chain, name);
    }

    public static boolean chainSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_chain, name) == null || !addByName(getHitConfig(), TYPE_chain, NAME_default, name, null)) {
            System.out.println("Can not find the chain " + name + " config!");
            return false;
        }
        return true;
    }

    public static void chainApiInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_chainapi);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_chainapi, name);
        if (two == null) {
            System.out.println("Can not find the chain api name: " + name);
            return;
        }
        System.out.println("The information by chain api's name: " + name);
        System.out.println("chain api url :" + two.first());
        System.out.println("default   name:" + two.result());
    }

    public static boolean chainApiAdd(String name, String url) {
        if (!isValidName(name)) {
            return false;
        }
        if (!isValidUrl(url)) {
            return false;
        }
        addByName(getHitConfig(), TYPE_chainapi, name, url, null);
        return true;
    }

    public static boolean chainApiRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_chainapi, name);
    }

    public static boolean chainApiSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_chainapi, name) == null || !addByName(getHitConfig(), TYPE_chainapi, NAME_default, name, null)) {
            System.out.println("Can not find the chain api " + name + " config!");
            return false;
        }
        return true;
    }

    public static void gasInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_gas);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_gas, name);
        if (two == null) {
            System.out.println("Can not find the gas name: " + name);
            return;
        }
        System.out.println("The information by gas's name: " + name);
        System.out.println("gas :" + two.first());
        System.out.println("default   name:" + two.result());
    }

    public static boolean gasAdd(String name, String deployGasLimit, String deployGWei, String writeGasLimit, String writeGWei) {
        if (!isValidName(name)) {
            return false;
        }
        if (StringUtils.isBlank(deployGasLimit) || StringUtils.isBlank(deployGWei) || StringUtils.isBlank(writeGasLimit) || StringUtils.isBlank(writeGWei)) {
            System.out.println("deployGasLimit, deployGWei, writeGasLimit, writeGWei must has value.");
            return false;
        }
        long gasDeploy = NumberHelper.getLong(deployGasLimit, 0);
        long gweiDeploy = NumberHelper.getLong(deployGWei, 0);
        long gasWrite = NumberHelper.getLong(writeGasLimit, 0);
        long gweiWrite = NumberHelper.getLong(writeGWei, 0);
        if (gasDeploy == 0 || gweiDeploy == 0 || gasWrite == 0 || gweiWrite == 0) {
            System.out.println("deployGasLimit, deployGWei, writeGasLimit, writeGWei must has value.");
            return false;
        }
        addByName(getHitConfig(), TYPE_gas, name, gasDeploy + "," + gweiDeploy + "," + gasWrite + "," + gweiWrite, null);
        return true;
    }

    public static boolean gasRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_gas, name);
    }

    public static boolean gasSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_gas, name) == null || !addByName(getHitConfig(), TYPE_gas, NAME_default, name, null)) {
            System.out.println("Can not find the gas " + name + " config!");
            return false;
        }
        return true;
    }

    public static void contractInfo(String name) {
        if (name == null) {
            hitConfigInfo(TYPE_contract);
            return;
        }
        Tuple.Two<String, String, String> two = getByName(getHitConfig(), TYPE_contract, name);
        if (two == null) {
            System.out.println("Can not find the contract name: " + name);
            return;
        }
        System.out.println("The information by contract's name: " + name);
        System.out.println("contract :" + two.first());
        System.out.println("default   name:" + two.result());
    }

    public static boolean contractAdd(String name, String address) {
        if (!isValidName(name)) {
            return false;
        }
        if (StringUtils.isBlank(address)) {
            System.out.println("Contract address must has value.");
            return false;
        }
        addByName(getHitConfig(), TYPE_contract, name, address, null);
        return true;
    }

    public static boolean contractDeploy(String name) {
        if (!isValidName(name)) {
            return false;
        }
        String address = null;
        try {
            address = Hit.deployContract().call();
        } catch (Exception e) {
        }
        if (StringUtils.isBlank(address) || ContractApi.isError(address)) {
            System.out.println("Can not deploy contract.");
            return false;
        }
        addByName(getHitConfig(), TYPE_contract, name, address, null);
        return true;
    }

    public static boolean contractRemove(String name) {
        if (!isValidName(name)) {
            return false;
        }
        return removeByName(getHitConfig(), TYPE_contract, name);
    }

    public static boolean contractSet(String name) {
        if (!isValidName(name)) {
            return false;
        }
        if (getByName(getHitConfig(), TYPE_contract, name) == null || !addByName(getHitConfig(), TYPE_contract, NAME_default, name, null)) {
            System.out.println("Can not find the contract " + name + " config!");
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

    public static URL getStoragePersistentUrl() {
        String url = getStorage();
        String[] split = StringUtils.split(url, ",");
        if (split.length > 0) {
            try {
                if (!split[0].endsWith("/")) {
                    split[0] += "/";
                }
                return new URL(split[0]);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static String getStorageViewUrl() {
        String url = getStorage();
        String[] split = StringUtils.split(url, ",");
        if (split.length > 1) {
            if (!split[1].endsWith("/")) {
                split[1] += "/";
            }
            return split[1];
        }
        return null;
    }

    public static String getChain() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_chain);
        return two == null ? null : two.first();
    }

    public static String getChainApi() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_chainapi);
        return two == null ? null : two.first();
    }

    public static String getGas() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_gas);
        return two == null ? null : two.first();
    }

    public static long getGasDeploy() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_gas);
        String gas = two == null ? null : two.first();
        return Long.valueOf(StringUtils.split(gas, ",")[0]);
    }

    public static long getGasDeployGwei() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_gas);
        String gas = two == null ? null : two.first();
        return Long.valueOf(StringUtils.split(gas, ",")[1]);
    }

    public static long getGasWrite() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_gas);
        String gas = two == null ? null : two.first();
        return Long.valueOf(StringUtils.split(gas, ",")[2]);
    }

    public static long getGasWriteGwei() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_gas);
        String gas = two == null ? null : two.first();
        return Long.valueOf(StringUtils.split(gas, ",")[3]);
    }

    public static String getContract() {
        Tuple.Two<String, String, String> two = getDefaultValue(TYPE_contract);
        String contract = two == null ? null : two.first();
        return contract;
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

    public static void checkValidCfg() {
        PreRequiredHelper.requireNotBlank(getAccountAddress(), "Hit account config not exists, add or set default account first: hit cfg account help");
        PreRequiredHelper.requireNotBlank(getChain(), "Hit chain config not exists, add or set default chain first: hit cfg chain help");
        PreRequiredHelper.requireNotBlank(getChainApi(), "Hit chainapi config not exists, add or set default chainapi first: hit cfg chainapi help");
        PreRequiredHelper.requireNotBlank(getContract(), "Hit contract config not exists, add or set default contract first: hit cfg contract help");
        PreRequiredHelper.requireNotBlank(getGas(), "Hit gas config not exists, add or set default gas first: hit cfg gas help");
        PreRequiredHelper.requireNotBlank(getRsaPubKey(), "Hit rsa config not exists, add or set default repository first: hit cfg rsa help");
        PreRequiredHelper.requireNotNull(getStoragePersistentUrl(), "Hit storage config not exists, add or set default storage first: hit cfg storage help");
        PreRequiredHelper.requireNotBlank(getStorageViewUrl(), "Hit storage config not exists, add or set default storage first: hit cfg storage help");
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

    public static String createPassword() {
        String password = "";
        Console console = System.console();
        if (console == null) {
            System.err.println("Couldn't get Console instance, password input will show in console!");
        }
        if (console != null) {
            char passwordArray[] = console.readPassword("Enter hit config password: ");
            password = new String(passwordArray);
        } else {
            System.err.println("Enter hit config password:");
            password = readFromSystemInput();
        }
        {//verify password.
            String rePassword = "";
            if (console != null) {
                char passwordArray[] = console.readPassword("Enter hit config password again: ");
                rePassword = new String(passwordArray);
            } else {
                System.err.println("Enter hit config password again:");
                rePassword = readFromSystemInput();
            }
            if (!StringUtils.equals(password, rePassword)) {
                System.err.println("Password is incorrect!");
                System.exit(0);
                return null;
            }
        }
        if (!testPassword(password)) {
            System.exit(0);
            return null;
        }
        {
            passwordCache = WalletHelper.encryptWithPasswordHex(password, passwordEncrypt);
        }
        return password;
    }

    //
    public static void printMemberInfo(ProjectInfoFile projectInfoFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Team members: \n");
        for (ProjectInfoFile.TeamInfo ti : projectInfoFile.getMembers()) {
            sb.append(StringUtils.rightPad(ti.getMember(), 20)).append(' ')
                    .append(StringUtils.rightPad(ti.getMemberAddressEcc(), 45)).append(' ')
                    .append(ti.getMemberPubKeyRsa()).append('\n');
        }
        System.out.println(sb);
    }

    public static ProjectInfoFile getProjectInfoFile(File projectDir) {
        ProjectInfoFile projectInfoFile = null;
        if (GitHelper.existProjectInfoFile(projectDir)) {
            projectInfoFile = GitHelper.readProjectInfoFile(projectDir);
        }
        if (projectInfoFile == null) {
            System.err.println(projectDir.getAbsolutePath() + " is invalid hit repository.");
            System.exit(1);
        }
        if (!projectInfoFile.verify(null)) {
            System.err.println("Project info file is not verify.");
            System.exit(1);
        }
        return projectInfoFile;
    }

    //
    public static void removeKeyPair(File projectDir, ProjectInfoFile projectInfoFile) {
        projectInfoFile.setRepoPubKey(null);
        projectInfoFile.setRepoPriKey(null);
        for (ProjectInfoFile.TeamInfo ti : projectInfoFile.getMembers()) {
            ti.setMemberRepoPriKey(null);
        }
        GitHelper.updateWholeHitRepository(projectDir, projectInfoFile);
    }

    public static void addKeyPair(File projectDir, ProjectInfoFile projectInfoFile) {
        ECKey key = new ECKey();
        String publicKey = Hex.toHexString(key.getPubKey());
        String privateKey = Hex.toHexString(key.getPrivKeyBytes());
        // Encrypt: private key -(hex decode)-> private key bytes -(encrypt with rsa public key)->  encrypt bytes -(hex encode)-> hex encrypt
        // Decrypt: hex encrypt -(hex decode)-> encrypt bytes     -(decrypt with rsa private key)-> private key bytes -(hex encode) private key
        String privateKeyEncryptByOwnerRsaPubKey = Hex.toHexString(
                RSAHelper.encrypt(
                        Hex.decode(privateKey),
                        RSAHelper.getPublicKeyFromHex(HitHelper.getRsaPubKey())
                ));
        projectInfoFile.setRepoPubKey(publicKey);
        projectInfoFile.setRepoPriKey(privateKeyEncryptByOwnerRsaPubKey);
        for (ProjectInfoFile.TeamInfo ti : projectInfoFile.getMembers()) {
            String memberPubKey = ti.getMemberPubKeyRsa();
            String privateKeyEncryptByMemberRsaPubKey = Hex.toHexString(
                    RSAHelper.encrypt(
                            Hex.decode(privateKey),
                            RSAHelper.getPublicKeyFromHex(memberPubKey)
                    ));
            ti.setMemberRepoPriKey(privateKeyEncryptByMemberRsaPubKey);
        }
        GitHelper.updateWholeHitRepository(projectDir, projectInfoFile);
    }

    public static void printKeyPairInfo(ProjectInfoFile projectInfoFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key Pair: \n");
        sb.append("Is private: ").append(projectInfoFile.isPrivate()).append("\n")
                .append("Public  Key:").append(projectInfoFile.getRepoPubKey()).append("\n")
                .append("Private Key:").append(projectInfoFile.getRepoPriKey()).append("\n");
        System.out.println(sb);
    }

    public static String createRepository(File file, String repositoryName, boolean autoRename) {
        try {
            if (StringUtils.isBlank(repositoryName)) {
                if (file != null) {
                    repositoryName = file.getParentFile().getName();
                }
                if (StringUtils.isBlank(repositoryName)) {
                    return null;
                }
            }
            String repositoryAddress = Hit.createRepository().name(repositoryName).autoRename(autoRename).call();
            String hitUri = "hit://" + repositoryAddress + ".git";
            if (file == null) {
                return hitUri;
            }
            Hit hit = new Hit(new FileRepository(file), true);
            hit.storedConfig().set("remote", "origin", "url", hitUri).call();
            IOUtils.closeQuietly(hit);
            System.out.println("Update remote origin url to " + hitUri);
            GitHelper.onInitHitRepository(file);
            return hitUri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String createPullRequestCmd(File gitDir, String startBranch, String endBranch, String comment) {
        HitHelper.testIpfs();
        // setting system property for transport.
        System.setProperty("GIT_CMD", "pullRequest");
        // comment is required.
        if (StringUtils.isBlank(comment)) {
            System.err.println("PullRequest comment is required, cmd: hit pullRequest create -m 'comment' [startBranch] [endBranch].");
            return null;
        }
        // check if the repository has enabled the pull request.
        Tuple.Two<Object, String, PatchHelper.PatchSummaryInfo> two = HitHelper.createPullRequest(gitDir, startBranch, endBranch, comment);
        String pullRequest = two.first();
        if (StringUtils.isBlank(pullRequest)) {
            System.err.println("Can not create pull request!");
            return null;
        }
        String prInfo = HitHelper.createPullRequestInfo(gitDir, startBranch, endBranch, comment, new Tuple.Two[]{two});
        if (StringUtils.isBlank(prInfo)) {
            System.err.println("Can not create pull request info!");
            return null;
        }
        try (Hit hit = new Hit(new FileRepository(gitDir), true)) {
            return hit.contract().addPullRequest(prInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Tuple.Two<Object, String, PatchHelper.PatchSummaryInfo> createPullRequest(File gitDir, String startBranch, String endBranch, String comment) {
        Tuple.Two<Object, String, PatchHelper.PatchSummaryInfo> two = null;
        {// upload pull request patch
            startBranch = StringUtils.isBlank(startBranch) ? GitHelper.findDefaultRemoteBranch(gitDir) : startBranch;
            endBranch = StringUtils.isBlank(endBranch) ? GitHelper.findDefaultBranch(gitDir) : endBranch;
            System.out.println("Create pull request from start branch " + startBranch + " to end banch " + endBranch + ".");
            PatchHelper.PatchSummaryInfo patch = PatchHelper.createPatch(gitDir, startBranch, endBranch, comment);
            if (patch.patchs().isEmpty()) {
                System.err.println("Nothing changed of pull request from start branch " + startBranch + " to end banch " + endBranch + ".");
                return null;
            }
            System.out.println(patch.patch());
            String pullRequestHash = GitHelper.writeFileToIpfs(ByteHelper.utf8(patch.patch()), "pullRequest.patch");
            System.out.println("PullRequest: " + HitHelper.getStorageViewUrl() + pullRequestHash);
            two = new Tuple.Two<>(pullRequestHash, patch);
        }
        // push repository
        try (Hit hit = new Hit(new FileRepository(gitDir), true)) {
            hit.push().call();
        } catch (Exception e) {
            throw new RuntimeException("HitHelper can not push the repository!", e);
        }
        return two;
    }

    /**
     * create pull request info and update to ipfs and return hash.
     *
     * @param gitDir
     * @param startBranch
     * @param endBranch
     * @param comment
     * @param twos
     * @return
     */
    public static String createPullRequestInfo(File gitDir, String startBranch, String endBranch, String comment, Tuple.Two<Object, String, PatchHelper.PatchSummaryInfo>[] twos) {
        List<Object> infos = new ArrayList<>();
        String url = null, author = null;
        try (Hit hit = new Hit(new FileRepository(gitDir), true)) {
            String name = hit.storedConfig().getUserName().call();
            String email = hit.storedConfig().getUserEmail().call();
            if (name == null || email == null) {
                author = "unknown";
            } else {
                author = name + " <" + email + ">";
            }
            url = hit.storedConfig().getRemoteUrl().call();
        } catch (Exception e) {
            throw new RuntimeException("HitHelper can not push the repository!", e);
        }
        for (Tuple.Two<Object, String, PatchHelper.PatchSummaryInfo> two : twos) {
            Map<String, Object> format = PatchHelper.format(two.second(), url, author, getAccountAddress(), getRsaPubKey());
            format.put("patch_url", HitHelper.getStorageViewUrl() + two.first());
            format.put("patch_hash", two.first());
            infos.add(format);
        }
        String prInfo = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create().toJson(infos);
        String prInfoHash = null;
        {// upload pull request info
            System.out.println(prInfo);
            prInfoHash = GitHelper.writeFileToIpfs(ByteHelper.utf8(prInfo), "pullRequestInfo.json");
            System.out.println("PullRequestInfo: " + HitHelper.getStorageViewUrl() + prInfoHash);
        }
        return prInfoHash;
    }

    public static boolean pullRequestMerge(File gitDir, Map<String, Object> pullRequestSummaryInfo, boolean ignoreSpaceChange, boolean ignoreWhitespace, boolean forceMergeLine, boolean noCommit) {
        File prDir = new File(gitDir, "pullrequest");
        String currentBanch = null;
        String branchName = null;
        Repository repo = null;
        Git git = null;
        try {
            repo = new FileRepository(gitDir);
            git = new Git(repo);
            prDir.mkdir();
            currentBanch = repo.getBranch();
            String commitName = (String) pullRequestSummaryInfo.get("start_commit");
            String currentBranch = repo.exactRef(Constants.HEAD).getTarget().getName();
            branchName = "pr-" + commitName.substring(0, 5);
            git.checkout().setCreateBranch(true).setName(branchName).setStartPoint(commitName).call();
            System.out.println("Branch " + branchName + " is created for pull request.");
            byte[] patchBytes = GitHelper.readFileFromIpfs((String) pullRequestSummaryInfo.get("patch_hash"));
            FileUtils.writeByteArrayToFile(new File(prDir, "pullrequest.patch"), patchBytes);
            {
                List<PatchHelper.PatchFileInfo> patchFileInfos = PatchHelper.parsePatch(new ByteArrayInputStream(patchBytes));
                for (PatchHelper.PatchFileInfo pfi : patchFileInfos) {
                    ApplyResult call = ApplyHelper.createApply(repo)
                            .ignoreSpaceChange(ignoreSpaceChange).ignoreWhitespace(ignoreWhitespace).forceMergeLine(forceMergeLine)
                            .setPatch(new ByteArrayInputStream(pfi.diff().getBytes("UTF-8"))).call();
                    if (!noCommit) {
                        // Stage all files in the repo including new files
                        git.add().addFilepattern(".").call();
                        git.commit().setMessage(pfi.msg()).setAuthor(pfi.author(), pfi.email()).call();
                        System.out.println("commit patched changes " + pfi.author() + " <" + pfi.email() + "> " + pfi.msg());
                    }
                }
                git.checkout().setName(currentBanch).call();
                FileUtils.deleteQuietly(prDir);
            }
            System.out.println("Merge: hit merge " + branchName);
            System.out.println("Remove pull request branch: hit branch -D " + branchName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (branchName != null && git != null) {
                try {
                    git.checkout().setCreateBranch(false).setName(currentBanch).call();
                } catch (Exception e) {
                }
            }
            SocketHelper.closeWithoutError(repo);
        }
        return true;
    }

    private static String httpGet2(String requestUrl, String tryMessage) {
        String content = "";
        String token = System.getProperty(MigrateCommand.PROP_TOKEN, "");
        for (int i = 0; i < 5; i++) {
            if (tryMessage != null && i > 0) {
                System.out.println(FCS.get(tryMessage, i, requestUrl));
            }
            HttpURLConnection connection = null;
            try {
                URL url = new URL(requestUrl);
                {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("charset", "UTF-8");
                    connection.setRequestProperty("accept", "*/*");
                    if (StringUtils.isNotBlank(token)) {
                        connection.setRequestProperty("Authorization", "token " + token);
                    }
                    connection.setConnectTimeout(30 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.connect();
                }
                content = IOUtils.toString(connection.getInputStream(), "UTF-8");
            } catch (Exception e) {
            } finally {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                }
            }
            if (StringUtils.isNotBlank(content)) {
                return content;
            }
        }
        return content;
    }

    public static boolean testIpfs() {
        try {
            URL persistentUrl = getStoragePersistentUrl();
            String s = persistentUrl.toExternalForm() + "/stats/bw";
            HttpURLConnection connection = null;
            String content = null;
            try {
                URL url = new URL(s);
                {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("charset", "UTF-8");
                    connection.setRequestProperty("accept", "*/*");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3100.0 Safari/537.36");
                    connection.setConnectTimeout(30 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.connect();
                }
                //content = IOUtils.toString(connection.getInputStream(), "UTF-8");
                return 200 == connection.getResponseCode();
            } catch (Exception e) {
            } finally {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.err.printf("IPFS is not available.");
            throw new RuntimeException("IPFS is not available.");
        }
        return false;
    }
}
