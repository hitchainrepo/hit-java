/*******************************************************************************
 * Copyright (c) 2019-02-21 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.hit.util;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.hitchain.hit.api.EncryptableFileWrapper;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.Tuple.Two;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GitHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-02-21
 * auto generate by qdp.
 */
public class GitHelper {

    public static final String URL_IPFS = System.getProperty("URL_IPFS", "121.40.127.45"/*"http://121.40.127.45"*/);
    public static final String URL_ETHER = System.getProperty("URL_ETHER", "https://121.40.127.45:1443");
    public static final String rootPubKeyEcc = "0x837a4bbef0f7235b8fdb03c55d0d98f27f49cda8";
    public static final String rootPriKeyEcc = "448b60044aec0065a08115d7af1038491830f697c36118e046e38cf7002ee45b";
    public static final String rootPubKeyRsa = "30819f300d06092a864886f70d010101050003818d0030818902818100df6c814a1b827317370607e207a8749f12497d4ea339cd4f4a38df3690c9d24eb279852780105ed4f7a493833b0ed27409b74eb58b1a452a66be052146ee1f5fb0fa42231221f22cd73e70026b606862b91365fdbe6b2af79838eaa38db60dddc01ecf78f6881880ad399e65747fe86f5e844f5cd4b40f6de8c3e8e60db343290203010001";
    public static final String rootPriKeyRsa = "30820275020100300d06092a864886f70d01010105000482025f3082025b02010002818100df6c814a1b827317370607e207a8749f12497d4ea339cd4f4a38df3690c9d24eb279852780105ed4f7a493833b0ed27409b74eb58b1a452a66be052146ee1f5fb0fa42231221f22cd73e70026b606862b91365fdbe6b2af79838eaa38db60dddc01ecf78f6881880ad399e65747fe86f5e844f5cd4b40f6de8c3e8e60db343290203010001028180549653eca6b5a0b52d53cf30380e02f926874435bd7e68c8982527fd149c144f4f2acacac5a56d01dc3026d90c46f44e924f2031835492d316cae24e52f85c4fbd1a340b7b4f60f758631f16955d8503a154858f129a0d66268b9a929caaa1e1ff944c861a13c28e2d0869a93f8ffc508450339856de7869b8dbcce66dbde7b1024100f63fb36a8067288e16e2d63b4dda45bf7e8ebf00e34ac4514d169cfe50aba01a1d4785fe38226893814bda6c49a7888aa4a9a108045ac2db9c65ffecdefb5eeb024100e8456b9fb09c93280b3cf1364b00b997bb3293b7f95dba8cd48bd1ef734c64cd51cdb6140948a058f9588b9f4495ce88ba5790e663e711f730f296c7f602693b02407c498e8ef49c1c960aeb16e1fbdb6d54c7d5d885e432ba7fa67f016242e93cf7b14b864fd799565b0ce9722731cdc356e6e14f0bb2d6f47ecfa393d6c47cef5d02401a5b8e5bffc1b4dd4d712bfa3a46a9c8f320492d0e6a397a33c06e215b172735397c3b96487b6a5ece64e2eb3ef03510c4fc9cdfd82467a0827874edda17e9f3024002afe2f48990647b96526c6f2ddfee427a21fafd02ad982981425372587c14f742f1c1133a0f34084436cf78b0a55484fbb547f20077b937da7e5569f63a5ad9";
    public static final String HIT_GITFILE_IDX = "objects/hit/gitfile.idx";
    public static final String HIT_GITFILE_IDX_NAME = "gitfile.idx";
    public static final String HIT_PROJECT_INFO = "objects/hit/projectinfo";


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
    public static void onInitHitRepository(File projectDir) {
        IPFS ipfs = getIpfs();
        boolean isLog = false;
        //#1.Get or create ProjectInfoFile.
        ProjectInfoFile projectInfoFile = readProjectInfoFile(projectDir);
        //#2.Get GitFileIndex from ProjectInfoFile contract address.
        Map<String/*fileName*/, Two<Object, String/* ipfs hash */, String/* sha1 */>> oldGitFileIndex = readGitFileIndexFromLocal(projectDir);
        if (isLog) {
            for (Entry<String, Two<Object, String, String>> entry : oldGitFileIndex.entrySet()) {
                System.out.println("OLD:" + entry.getKey());
            }
        }
        //#3.List all current files.
        Map<String, File> current = listGitFiles(projectDir);
        if (isLog) {
            for (Entry<String, File> entry : current.entrySet()) {
                System.out.println("CURR:" + entry.getKey());
            }
        }
        //#4.Compare current files and GitFileIndex and get the changed files.
        Two<Object, Map<String, File>/*add*/, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>>/*remove*/> tuple = diffGitFiles(current, oldGitFileIndex);
        //#5.Write changed files to ipfs.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndexToIpfs = writeNewFileToIpfs(tuple.first(), projectInfoFile, ipfs);
        if (isLog) {
            for (Entry<String, Two<Object, String, String>> entry : newGitFileIndexToIpfs.entrySet()) {
                System.out.println("ADD:" + entry.getKey());
            }
        }
        //#6.Gen the new GitFileIndex.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndex = generateNewGitFileIndex(current, oldGitFileIndex, newGitFileIndexToIpfs);
        if (isLog) {
            for (Entry<String, Two<Object, String, String>> entry : newGitFileIndex.entrySet()) {
                System.out.println("NEW:" + entry.getKey() + ", ipfsHash:" + entry.getValue().first() + ", sha1:" + entry.getValue().second());
            }
        }
        //#7.Write the new GitFileIndex to disk and ipfs.
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, newGitFileIndex);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + URL_IPFS + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#8.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
    }

    public static void updateHitRepositoryGitFileIndex(File projectDir, ProjectInfoFile projectInfoFile, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> old, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> upload) {
        //#6.Gen the new GitFileIndex.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> combine = new LinkedHashMap<>();
        combine.putAll(old);
        combine.putAll(upload);
        //#7.Write the new GitFileIndex to disk and ipfs.
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, combine);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + URL_IPFS + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#8.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
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
        IPFS ipfs = getIpfs();
        boolean isLog = false;
        //#1.Get or create ProjectInfoFile.
        ProjectInfoFile projectInfoFile = readProjectInfoFile(projectDir);
        //#2.Get GitFileIndex from ProjectInfoFile contract address.
        Map<String/*fileName*/, Two<Object, String/* ipfs hash */, String/* sha1 */>> oldGitFileIndex = readGitFileIndexFromLocal(projectDir);
        if (isLog) {
            for (Entry<String, Two<Object, String, String>> entry : oldGitFileIndex.entrySet()) {
                System.out.println("OLD:" + entry.getKey());
            }
        }
        //#3.List all current files.
        Map<String, File> current = listGitFiles(projectDir);
        if (isLog) {
            for (Entry<String, File> entry : current.entrySet()) {
                System.out.println("CURR:" + entry.getKey());
            }
        }
        //#4.Compare current files and GitFileIndex and get the changed files.
        Two<Object, Map<String, File>/*add*/, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>>/*remove*/> tuple = diffGitFiles(current, oldGitFileIndex);
        //#5.Write changed files to ipfs.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndexToIpfs = writeNewFileToIpfs(tuple.first(), projectInfoFile, ipfs);
        if (isLog) {
            for (Entry<String, Two<Object, String, String>> entry : newGitFileIndexToIpfs.entrySet()) {
                System.out.println("ADD:" + entry.getKey());
            }
        }
        //#6.Gen the new GitFileIndex.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndex = generateNewGitFileIndex(current, oldGitFileIndex, newGitFileIndexToIpfs);
        if (isLog) {
            for (Entry<String, Two<Object, String, String>> entry : newGitFileIndex.entrySet()) {
                System.out.println("NEW:" + entry.getKey() + ", ipfsHash:" + entry.getValue().first() + ", sha1:" + entry.getValue().second());
            }
        }
        //#7.Write the new GitFileIndex to disk and ipfs.
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, newGitFileIndex);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + URL_IPFS + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#8.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
    }

    private static void updateProjectAddress(ProjectInfoFile projectInfoFile, String newProjectAddress) {
        EthereumHelper.updateProjectAddress(URL_ETHER, projectInfoFile.getRepoAddress(), EthereumHelper.encryptPriKeyEcc(URL_ETHER, rootPriKeyEcc), newProjectAddress);
    }

    private static String writeGitFileIndexToIpfs(File projectDir, Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileHash) {
        IPFS ipfs = getIpfs();
        try {
            byte[] gitFileIndexWithCompress = toGitFileIndexWithCompress(gitFileHash);
            File gitFileIndex = new File(projectDir, HIT_GITFILE_IDX);
            writeUpdateFile(gitFileIndex, gitFileIndexWithCompress);
            NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(HIT_GITFILE_IDX_NAME, gitFileIndexWithCompress);
            List<MerkleNode> add = ipfs.add(file);
            return add.get(add.size() - 1).hash.toBase58();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] toGitFileIndexWithCompress(Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileIndex) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Two<Object, String, String>> entry : gitFileIndex.entrySet()) {
            sb.append(entry.getValue().first()).append(',').append(entry.getValue().second()).append(',').append(entry.getKey()).append('\n');
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(sb.toString().getBytes("UTF-8"));
            gzip.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> generateNewGitFileIndex(
            Map<String/* relativePath */, File> current,
            Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> oldGitFileIndex,
            Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndex) {
        Map<String, Two<Object, String, String>> map = new LinkedHashMap();
        for (Entry<String, File> entry : current.entrySet()) {
            String key = entry.getKey();
            Two<Object, String, String> twoNew = newGitFileIndex.get(key);
            Two<Object, String, String> twoOld = oldGitFileIndex.get(key);
            String ipfsHash = StringUtils.defaultString(twoNew == null ? null : twoNew.first(),
                    twoOld == null ? null : twoOld.first());
            String sha1 = StringUtils.defaultString(twoNew == null ? null : twoNew.second(),
                    twoOld == null ? null : twoOld.second());
            map.put(key, new Two(ipfsHash, sha1));
        }
        return map;
    }

    public static IPFS getIpfs() {
        String urlIpfs = URL_IPFS;
        IPFS ipfs = new IPFS(urlIpfs, 5001, "/api/v0/", false);
        return ipfs;
    }

    public static IPFS getIpfs(String ipfsUrl) {
        IPFS ipfs = new IPFS(ipfsUrl, 5001, "/api/v0/", false);
        return ipfs;
    }

    private static Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> writeNewFileToIpfs(Map<String/* relativePath */, File> newGitFile, ProjectInfoFile projectInfoFile, IPFS ipfs) {
        Map<String, Two<Object, String, String>> map = new HashMap();
        Map<String, Two<Object, String, String>> hashMap = new HashMap();
        for (Entry<String, File> entry : newGitFile.entrySet()) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.readFileToByteArray(entry.getValue()));
                EncryptableFileWrapper file = new EncryptableFileWrapper(
                        new HashedFile.FileWrapper(entry.getKey(), new HashedFile.ByteArrayInputStreamCallback(bais)),
                        projectInfoFile);
                List<MerkleNode> add = ipfs.add(file);
                hashMap.put(entry.getKey(), new Two(add.get(add.size() - 1).hash.toBase58(), sha1(entry.getValue())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (Entry<String, File> entry : newGitFile.entrySet()) {
            map.put(entry.getKey(), hashMap.get(entry.getKey()));
        }
        return map;
    }

    private static String sha1(File file) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            return Hex.toHexString(DigestUtils.sha1(is));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    public static String sha1(byte[] data) {
        return Hex.toHexString(DigestUtils.sha1(data));
    }

    private static Two<Object, Map<String, File>/*add*/, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>>/*remove*/> diffGitFiles(
            Map<String/* relativePath */, File> current,
            Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileIndex) {
        Map<String, File> fileAdd = new HashMap<String, File>();
        Map<String, Two<Object, String, String>> fileRemove = new HashMap<>();
        for (Entry<String, File> entry : current.entrySet()) {
            String key = entry.getKey();
            File file = entry.getValue();
            if (!key.startsWith("objects/")) {// is not the hash object, so hash the file and compare the hash.
                String sha1 = sha1(file);
                Two<Object, String, String> two = gitFileIndex.get(key);
                if (two == null || !sha1.equals(two.second())) {
                    fileAdd.put(key, file);
                }
                continue;
            }
            if (!gitFileIndex.containsKey(key)) {
                fileAdd.put(key, file);
            }
        }
        for (Entry<String, Two<Object, String, String>> entry : gitFileIndex.entrySet()) {
            if (!current.containsKey(entry.getKey())) {
                fileRemove.put(entry.getKey(), entry.getValue());
            }
        }
        return new Two<Object, Map<String, File>, Map<String, Two<Object, String, String>>>(fileAdd, fileRemove);
    }

    /**
     * list git files.
     *
     * @param projectDir
     * @return {relativePath/fileName: File}
     */
    private static Map<String/* relativePath */, File> listGitFiles(File projectDir) {
        String basePath = projectDir.getAbsolutePath();
        Collection<File> files = FileUtils.listFiles(projectDir, null, true);
        Map<String, File> map = new HashMap<>();
        for (File file : files) {
            if (file.isFile()) {
                map.put(file.getAbsolutePath().substring(basePath.length() + 1), file);
            }
        }
        return map;
    }

    private static Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> readGitFileIndexFromLocal(File projectDir) {
        try {
            File gitFileIndex = new File(projectDir, HIT_GITFILE_IDX);
            if (!gitFileIndex.exists()) {
                return parseGitFilesIndex(null);
            }
            byte[] contentWithCompress = FileUtils.readFileToByteArray(gitFileIndex);
            return parseGitFilesIndex(contentWithCompress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> readGitFileIndexFromIpfs(IPFS ipfs, String gitFileIndexHash) {
        try {
            byte[] contentWithCompress = StringUtils.isBlank(gitFileIndexHash) ? new byte[0] : ipfs.cat(Multihash.fromBase58(gitFileIndexHash));//objects/hit/gitfile.idx from ipfs.
            return parseGitFilesIndex(contentWithCompress);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> parseGitFilesIndex(byte[] contentWithCompress) {
        Map<String, Two<Object, String, String>> map = new LinkedHashMap<>();
        if (contentWithCompress == null || contentWithCompress.length == 0) {
            return map;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(contentWithCompress);
            GZIPInputStream ungzip = new GZIPInputStream(in);
            IOUtils.copy(ungzip, out);
            ungzip.close();
            String index = new String(out.toByteArray(), "UTF-8");
            String[] lines = StringUtils.split(index, '\n');
            for (String line : lines) {
                String[] nameIpfsSha = StringUtils.split(line, ',');
                if (nameIpfsSha.length != 3) {
                    continue;
                }
                map.put(nameIpfsSha[2], new Two<>(nameIpfsSha[0], nameIpfsSha[1]));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static boolean isValidHitRepository(File projectDir) {
        return existProjectInfoFile(projectDir) && StringUtils.isNotBlank(readProjectInfoFile(projectDir).getRepoAddress());
    }

    public static boolean existProjectInfoFile(File projectDir) {
        File file = new File(projectDir, HIT_PROJECT_INFO);
        return file.exists();
    }

    public static ProjectInfoFile readProjectInfoFile(File projectDir) {
        try {
            File file = new File(projectDir, HIT_PROJECT_INFO);
            if (!file.exists()) {
                // is new project
                ProjectInfoFile info = new ProjectInfoFile();
                {
                    info.setVersion("1");
                    info.setEthereumUrl(URL_ETHER);
                    info.setFileServerUrl(URL_IPFS);
                    info.setRepoName(getProjectName(projectDir));
                    ECKey repoKeyPair = new ECKey();
                    //info.setRepoPubKey(Hex.toHexString(repoKeyPair.getPubKey()));
                    //info.setRepoPriKey(Hex.toHexString(RSAHelper.encrypt(repoKeyPair.getPrivKeyBytes(), RSAHelper.getPublicKeyFromHex(rootPubKeyRsa))));
                    String address = EthereumHelper.createContractForProject(URL_ETHER, rootPubKeyEcc, info.getRepoName());
                    if (address == null) {
                        throw new RuntimeException("Can't not create contract for project!");
                    }
                    System.out.println("Repository contract is created on address:" + address);
                    info.setRepoAddress(address);
                    info.setOwner(getProjectOwner(projectDir));
                    info.setOwnerPubKeyRsa(rootPubKeyRsa);
                    info.setOwnerAddressEcc(rootPubKeyEcc);
                }
                writeUpdateFile(file, ByteHelper.utf8(info.genSignedContent(rootPriKeyRsa)));
            }
            byte[] content = FileUtils.readFileToByteArray(file);
            return ProjectInfoFile.fromFile(
                    new HashedFile.FileWrapper(HIT_PROJECT_INFO, new HashedFile.InputStreamCallback() {
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
        if (".git".equals(dir.getName())) {
            return dir.getParentFile().getName();
        }
        if (path.endsWith(".git")) {
            return StringUtils.removeEnd(dir.getName(), ".git");
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
