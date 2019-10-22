/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.apache.commons.lang3.StringUtils;
import org.hitchain.hit.util.HitHelper;
import org.hitchain.hit.util.WalletHelper;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * HitConfigCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class HitConfigCommand implements Callable<Map<String, Map<String, String>>> {
    protected ConfigCallable cmd;

    static Map<String, Map<String, String>> updateHitConfig() {
        Map<String, Map<String, String>> hitConfig = HitHelper.getHitConfig();
        HitHelper.hitConfigToFile(hitConfig);
        return hitConfig;
    }

    @Override
    public Map<String, Map<String, String>> call() throws Exception {
        return cmd().call();
    }

    public ConfigCallable cmd() {
        return cmd;
    }

    public HitConfigCommand cmd(ConfigCallable cmd) {
        this.cmd = cmd;
        return this;
    }

    public static interface ConfigCallable extends Callable<Map<String, Map<String, String>>> {
    }

    public static class CreateCommand implements ConfigCallable {
        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.createHitConfig()) {
                return updateHitConfig();
            }
            return null;
        }
    }

    public static class AccountCommand extends KeyValues<AccountCommand> implements ConfigCallable {
        public AccountCommand add(String name, String priKey) {
            return type(HitHelper.ACTION_add).name(name).value(priKey);
        }

        public AccountCommand add(String name) {
            return type(HitHelper.ACTION_add).name(name);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                if (HitHelper.accountAdd(name(), value())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.accountRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.accountSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class RSACommand extends KeyValues<RSACommand> implements ConfigCallable {
        protected String pubKey;

        public String pubKey() {
            return pubKey;
        }

        public RSACommand pubKey(String pubKey) {
            this.pubKey = pubKey;
            return this;
        }

        public RSACommand add(String name) {
            return type(HitHelper.ACTION_add).name(name);
        }

        public RSACommand add(String name, String priKey, String pubKey) {
            return type(HitHelper.ACTION_add).name(name).value(priKey).pubKey(pubKey);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                if (HitHelper.rsaAdd(name(), value(), pubKey())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.rsaRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.rsaSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class StorageCommand extends KeyValues<StorageCommand> implements ConfigCallable {
        public StorageCommand add(String name, String url) {
            return type(HitHelper.ACTION_add).name(name).value(url);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                if (HitHelper.storageAdd(name(), value())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.storageRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.storageSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class ChainCommand extends KeyValues<ChainCommand> implements ConfigCallable {
        public ChainCommand add(String name, String url) {
            return type(HitHelper.ACTION_add).name(name).value(url);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                if (HitHelper.chainAdd(name(), value())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.chainRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.chainSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class ChainApiCommand extends KeyValues<ChainApiCommand> implements ConfigCallable {
        public ChainApiCommand add(String name, String url) {
            return type(HitHelper.ACTION_add).name(name).value(url);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                if (HitHelper.chainApiAdd(name(), value())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.chainApiRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.chainApiSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class GasCommand extends KeyValues<GasCommand> implements ConfigCallable {
        public GasCommand add(String name, String gas) {//deployGas deployGwei writeGas writeGwei
            return type(HitHelper.ACTION_add).name(name).value(gas);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                //gas=deployGas,deployGwei,writeGas,writeGwei
                String[] gas = StringUtils.split(value(), ",");
                if (gas.length == 4) {
                    if (HitHelper.gasAdd(name(), gas[0], gas[1], gas[2], gas[3])) {
                        return updateHitConfig();
                    }
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.gasRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.gasSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class ContractCommand extends KeyValues<ContractCommand> implements ConfigCallable {
        public ContractCommand add(String name, String gas) {
            return type(HitHelper.ACTION_add).name(name).value(gas);
        }

        public ContractCommand deploy(String name) {
            return type(HitHelper.ACTION_deploy).name(name);
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if (HitHelper.ACTION_add.equals(type())) {
                if (HitHelper.contractAdd(name(), value())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_deploy.equals(type())) {
                System.out.println("Start to deploy contract, please waiting for seconds...");
                if (HitHelper.contractDeploy(name())) {
                    System.out.println("Contract deploy successful on address: " + HitHelper.getByName(HitHelper.getHitConfig(), "contract", name()).first());
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_remove.equals(type())) {
                if (HitHelper.contractRemove(name())) {
                    return updateHitConfig();
                }
            } else if (HitHelper.ACTION_set.equals(type())) {
                if (HitHelper.contractSet(name())) {
                    return updateHitConfig();
                }
            }
            return null;
        }
    }

    public static class RecoverCommand extends KeyValues<RecoverCommand> implements ConfigCallable {
        public RecoverCommand password() {
            return type("password");
        }

        public RecoverCommand accountPrivateKey() {
            return type("accountPrivateKey");
        }

        public RecoverCommand rsaPrivateKey() {
            return type("rsaPrivateKey");
        }

        public Map<String, Map<String, String>> call() throws Exception {
            if ("password".equals(type())) {
                System.err.println("Input the mnemonic words:");
                String input = HitHelper.readFromSystemInput();
                String password = WalletHelper.mnemonicToString(input);
                System.out.println("The origin password:" + password);
            }
            if ("accountPrivateKey".equals(type())) {
                System.out.println("The account private key:" + HitHelper.getAccountPriKeyWithPasswordInput());
            }
            if ("rsaPrivateKey".equals(type())) {
                System.out.println("The rsa private key:" + HitHelper.getRsaPriKeyWithPasswordInput());
            }
            return null;
        }
    }

    public static abstract class KeyValues<V extends KeyValues> {
        protected String name;
        protected String value;
        protected String type;

        public String type() {
            return type;
        }

        public V type(String type) {
            this.type = type;
            return (V) this;
        }

        public String value() {
            return value;
        }

        public V value(String value) {
            this.value = value;
            return (V) this;
        }

        public String name() {
            return name;
        }

        public V name(String name) {
            this.name = name;
            return (V) this;
        }

        public V remove(String name) {
            return (V) type(HitHelper.ACTION_remove).name(name);
        }

        public V set(String name) {
            return (V) type(HitHelper.ACTION_set).name(name);
        }
    }
}
