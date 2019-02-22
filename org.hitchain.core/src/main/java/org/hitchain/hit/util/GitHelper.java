/*******************************************************************************
 * Copyright (c) 2019-02-21 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.util;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * GitHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-02-21
 * auto generate by qdp.
 */
public class GitHelper {

    public static final String URL_IPFS = System.getProperty("URL_IPFS", "121.40.127.45"/*"http://121.40.127.45"*/);
    public static final String URL_ETHER = System.getProperty("URL_ETHER", "https://120.26.82.66:1443");
    private static final String rootPubKeyEcc = "0x837a4bbef0f7235b8fdb03c55d0d98f27f49cda8";
    private static final String rootPriKeyEcc = "448b60044aec0065a08115d7af1038491830f697c36118e046e38cf7002ee45b";
    private static final String rootPubKeyRsa = "30819f300d06092a864886f70d010101050003818d0030818902818100df6c814a1b827317370607e207a8749f12497d4ea339cd4f4a38df3690c9d24eb279852780105ed4f7a493833b0ed27409b74eb58b1a452a66be052146ee1f5fb0fa42231221f22cd73e70026b606862b91365fdbe6b2af79838eaa38db60dddc01ecf78f6881880ad399e65747fe86f5e844f5cd4b40f6de8c3e8e60db343290203010001";
    private static final String rootPriKeyRsa = "30820275020100300d06092a864886f70d01010105000482025f3082025b02010002818100df6c814a1b827317370607e207a8749f12497d4ea339cd4f4a38df3690c9d24eb279852780105ed4f7a493833b0ed27409b74eb58b1a452a66be052146ee1f5fb0fa42231221f22cd73e70026b606862b91365fdbe6b2af79838eaa38db60dddc01ecf78f6881880ad399e65747fe86f5e844f5cd4b40f6de8c3e8e60db343290203010001028180549653eca6b5a0b52d53cf30380e02f926874435bd7e68c8982527fd149c144f4f2acacac5a56d01dc3026d90c46f44e924f2031835492d316cae24e52f85c4fbd1a340b7b4f60f758631f16955d8503a154858f129a0d66268b9a929caaa1e1ff944c861a13c28e2d0869a93f8ffc508450339856de7869b8dbcce66dbde7b1024100f63fb36a8067288e16e2d63b4dda45bf7e8ebf00e34ac4514d169cfe50aba01a1d4785fe38226893814bda6c49a7888aa4a9a108045ac2db9c65ffecdefb5eeb024100e8456b9fb09c93280b3cf1364b00b997bb3293b7f95dba8cd48bd1ef734c64cd51cdb6140948a058f9588b9f4495ce88ba5790e663e711f730f296c7f602693b02407c498e8ef49c1c960aeb16e1fbdb6d54c7d5d885e432ba7fa67f016242e93cf7b14b864fd799565b0ce9722731cdc356e6e14f0bb2d6f47ecfa393d6c47cef5d02401a5b8e5bffc1b4dd4d712bfa3a46a9c8f320492d0e6a397a33c06e215b172735397c3b96487b6a5ece64e2eb3ef03510c4fc9cdfd82467a0827874edda17e9f3024002afe2f48990647b96526c6f2ddfee427a21fafd02ad982981425372587c14f742f1c1133a0f34084436cf78b0a55484fbb547f20077b937da7e5569f63a5ad9";


    public static void onClone(File projectDir) {

    }

    public static void onPull(File projectDir) {

    }

    public static void onFetch(File projectDir) {

    }

    /**
     * <pre>
     * #1.Get or create ProjectInfoFile.
     * #2.Get GitFileIndex from ProjectInfoFile contract address.
     * #3.List all current files.
     * #4.Compare current files and GitFileIndex and get the changed files.
     * #5.Write changed files to ipfs.
     * #6.Gen the new GitFileIndex.
     * #7.Write the new GitFileIndex to disk and ipfs.
     * #8.Call contract and update project hash(GitFileIndex hash).
     * </pre>
     *
     * @param projectDir
     */
    public static void onPush(File projectDir) {
        //#1.Get or create ProjectInfoFile.
        //#2.Get GitFileIndex from ProjectInfoFile contract address.
        //#3.List all current files.
        //#4.Compare current files and GitFileIndex and get the changed files.
        //#5.Write changed files to ipfs.
        //#6.Gen the new GitFileIndex.
        //#7.Write the new GitFileIndex to disk and ipfs.
        //#8.Call contract and update project hash(GitFileIndex hash).
    }

    private static ProjectInfoFile readProjectInfoFile(File projectDir) {
        try {
            File file = new File(projectDir, "objects/info/projectinfo");
            if (!file.exists()) {
                // is new project
                ProjectInfoFile info = new ProjectInfoFile();
                {
                    info.setVersion("1");
                    info.setEthereumUrl(URL_ETHER);
                    info.setFileServerUrl(URL_IPFS);
                    info.setRepoName(getProjectName(projectDir));
                    ECKey repoKeyPair = new ECKey();
                    info.setRepoPubKey(Hex.toHexString(repoKeyPair.getPubKey()));
                    info.setRepoPriKey(Hex.toHexString(RSAHelper.encrypt(repoKeyPair.getPrivKeyBytes(),
                            RSAHelper.getPublicKeyFromHex(rootPubKeyRsa))));
                    String address = EthereumHelper.createContractForProject(URL_ETHER, rootPubKeyEcc,
                            info.getRepoName());
                    if (address == null) {
                        throw new RuntimeException("Can't not create contract for project!");
                    }
                    info.setRepoAddress(address);
                    info.setOwner(getProjectOwner(projectDir));
                    info.setOwnerPubKeyRsa(rootPubKeyRsa);
                    info.setOwnerAddressEcc(rootPubKeyEcc);
                }
                writeUpdateFile(file, ByteHelper.utf8(info.genSignedContent(rootPriKeyRsa)));
            }
            byte[] content = FileUtils.readFileToByteArray(file);
            return ProjectInfoFile.fromFile(
                    new HashedFile.FileWrapper("objects/info/projectinfo", new HashedFile.InputStreamCallback() {
                        public InputStream call(HashedFile hashedFile) throws IOException {
                            return new ByteArrayInputStream(content);
                        }
                    }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeUpdateFile(File p, byte[] bin) {
        final LockFile lck = new LockFile(p);
        try {
            if (!lck.lock()) {
                throw new RuntimeException("Can't write " + p);
            }
            lck.write(bin);
        } catch (Exception ioe) {
            throw new RuntimeException("Can't write " + p);
        }
        if (!lck.commit()) {
            throw new RuntimeException("Can't write " + p);
        }
    }

    private static String getProjectName(File dir) {
        String path = dir.getAbsolutePath();
        if (path.endsWith(".git")) {
            return dir.getParentFile().getName() + "/" + dir.getName();
        }
        throw new RuntimeException("GitHelper can not get project name !");
    }

    private static String getProjectOwner(File dir) {
        String path = dir.getAbsolutePath();
        if (path.endsWith(".git")) {
            return dir.getParentFile().getName();
        }
        throw new RuntimeException("GitHelper can not get project owner !");
    }
}
