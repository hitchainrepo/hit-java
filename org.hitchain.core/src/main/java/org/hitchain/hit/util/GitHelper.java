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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.internal.storage.file.LockFile;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hitchain.hit.api.EncryptableFileWrapper;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.Tuple.Two;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
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

    public static final String HIT_GITFILE_IDX = "objects/hit/gitfile.idx";
    public static final String HIT_GITFILE_IDX_NAME = "gitfile.idx";
    public static final String HIT_PROJECT_INFO = "objects/hit/projectinfo";

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
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + HitHelper.getStorage() + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
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
    public static void updateWholeHitRepository(File projectDir, ProjectInfoFile projectInfoFile) {
        IPFS ipfs = getIpfs();
        //#1.Get or create ProjectInfoFile.
        String content = projectInfoFile.genSignedContent(HitHelper.getRsaPriKeyWithPasswordInput());
        File newProjectInfoFile = null;
        try {
            newProjectInfoFile = File.createTempFile("projectinfo-" + System.currentTimeMillis(), null);
            //write a temp file.
            FileUtils.writeByteArrayToFile(newProjectInfoFile, ByteHelper.utf8(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //#2.Get GitFileIndex from ProjectInfoFile contract address.
        Map<String/*fileName*/, Two<Object, String/* ipfs hash */, String/* sha1 */>> oldGitFileIndex = new HashMap<>();
        //#3.List all current files.
        Map<String, File> current = listGitFiles(projectDir);
        {// overwrite new project info file
            current.put(HIT_PROJECT_INFO, newProjectInfoFile);
            current.remove(HIT_GITFILE_IDX);
        }
        //#4.Compare current files and GitFileIndex and get the changed files.
        Two<Object, Map<String, File>/*add*/, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>>/*remove*/> tuple = diffGitFiles(current, oldGitFileIndex);
        //#5.Write changed files to ipfs.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndexToIpfs = writeNewFileToIpfs(tuple.first(), projectInfoFile, ipfs);
        //#6.Gen the new GitFileIndex.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> newGitFileIndex = generateNewGitFileIndex(current, oldGitFileIndex, newGitFileIndexToIpfs);
        //#7.Write the new GitFileIndex to disk and ipfs.
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, newGitFileIndex);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + HitHelper.getStorage() + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#8.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
        //#9.write project info file to disk
        writeUpdateFile(new File(projectDir, HIT_PROJECT_INFO), ByteHelper.utf8(content));
        FileUtils.deleteQuietly(newProjectInfoFile);
    }

    public static boolean updateHitRepositoryProjectInfoFile(File projectDir, ProjectInfoFile projectInfoFile) {
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> combine = readGitFileIndexFromLocal(projectDir);
        //#1. write project info file to disk.
        String content = projectInfoFile.genSignedContent(HitHelper.getRsaPriKeyWithPasswordInput());
        File newProjectInfoFile = null;
        try {
            newProjectInfoFile = File.createTempFile("projectinfo-" + System.currentTimeMillis(), null);
            //write a temp file.
            FileUtils.writeByteArrayToFile(newProjectInfoFile, ByteHelper.utf8(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, File> newGitFile = new HashMap<>();
        {
            newGitFile.put(HIT_PROJECT_INFO, newProjectInfoFile);
        }
        Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> twoMap = writeNewFileToIpfs(newGitFile, projectInfoFile, getIpfs());
        Two<Object, String/* ipfs hash */, String/* sha1 */> two = twoMap.isEmpty() || twoMap.values().isEmpty() ? null : twoMap.values().iterator().next();
        if (two == null && StringUtils.isNotBlank(two.first())) {
            return false;
        }
        //#2.Write the new GitFileIndex to disk and ipfs.
        {
            combine.put(HIT_PROJECT_INFO, two);
        }
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, combine);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + HitHelper.getStorage() + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#3.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
        //#9.write project info file to disk
        writeUpdateFile(new File(projectDir, HIT_PROJECT_INFO), ByteHelper.utf8(content));
        FileUtils.deleteQuietly(newProjectInfoFile);
        return true;
    }

    public static void updateHitRepositoryGitFileIndex(File projectDir, ProjectInfoFile projectInfoFile, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> old, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> upload) {
        //#6.Gen the new GitFileIndex.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> combine = new LinkedHashMap<>();
        combine.putAll(old);
        combine.putAll(upload);
        //#7.Write the new GitFileIndex to disk and ipfs.
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, combine);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + HitHelper.getStorage() + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#8.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
    }

    public static void updatePullRequestGitFileIndex(File projectDir, ProjectInfoFile projectInfoFile, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> old, Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> upload) {
        //#6.Gen the new GitFileIndex.
        Map<String, Two<Object, String/* ipfs hash */, String/* sha1 */>> combine = new LinkedHashMap<>();
        combine.putAll(old);
        combine.putAll(upload);
        //#7.Write the new GitFileIndex to disk and ipfs.
        String gitFileIndexHash = writeGitFileIndexToIpfs(projectDir, combine);
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + HitHelper.getStorage() + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
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
        System.out.println("Repository information local directory=" + projectDir.getPath() + ", index=http://" + HitHelper.getStorage() + ":8080/ipfs/" + gitFileIndexHash + ", address=https://ropsten.etherscan.io/address/" + projectInfoFile.getRepoAddress());
        //#8.Call contract and update project hash(GitFileIndex hash).
        updateProjectAddress(projectInfoFile, gitFileIndexHash);
    }

    private static void updateProjectAddress(ProjectInfoFile projectInfoFile, String newProjectAddress) {
        EthereumHelper.updateProjectAddress(HitHelper.getRepository(), projectInfoFile.getRepoAddress(), HitHelper.getAccountPriKeyWithPasswordInput(), newProjectAddress);
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

    public static String writeFileToIpfs(byte[] data, String fileName) {
        IPFS ipfs = getIpfs();
        try {
            NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper(fileName, data);
            List<MerkleNode> add = ipfs.add(file);
            return add.get(add.size() - 1).hash.toBase58();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IPFS getIpfs() {
        String urlIpfs = HitHelper.getStorage();
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
                byte[] buf = FileUtils.readFileToByteArray(entry.getValue());
                ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                String fileName = entry.getKey();
                EncryptableFileWrapper file = new EncryptableFileWrapper(
                        new HashedFile.FileWrapper(fileName, new HashedFile.ByteArrayInputStreamCallback(bais)),
                        projectInfoFile);
//                if (projectInfoFile.isPrivate()) {
//                    String rsaPriKeyWithPasswordInput = null;
//                    if (projectInfoFile.isPrivate()) {
//                        rsaPriKeyWithPasswordInput = HitHelper.getRsaPriKeyWithPasswordInput();
//                    }
//                    DecryptableFileWrapper dfile = new DecryptableFileWrapper(
//                            new HashedFile.FileWrapper(fileName,
//                                    new HashedFile.ByteArrayInputStreamCallback(file.getContents())),
//                            projectInfoFile,
//                            HitHelper.getAccountAddress(),
//                            rsaPriKeyWithPasswordInput);
//                    if (fileName.equals("index") || fileName.startsWith("objects/")) {
//                        System.out.println("GitHelper-Encrypt==" + fileName + "==" + Hex.toHexString(buf));
//                        System.out.println("GitHelper-Decrypt==" + fileName + "==" + Hex.toHexString(dfile.getContents()));
//                    } else {
//                        System.out.println("GitHelper-Encrypt==" + fileName + "==" + ByteHelper.utf8(buf));
//                        System.out.println("GitHelper-Decrypt==" + fileName + "==" + ByteHelper.utf8(dfile.getContents()));
//                    }
//                }
                List<MerkleNode> add = ipfs.add(file);
                hashMap.put(fileName, new Two(add.get(add.size() - 1).hash.toBase58(), sha1(entry.getValue())));
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
                    info.setEthereumUrl(HitHelper.getRepository());
                    info.setFileServerUrl(HitHelper.getStorage());
                    info.setRepoName(getProjectName(projectDir));
                    String accountPubKey = HitHelper.getAccountPubKey();
                    String address = EthereumHelper.createContractForProject(HitHelper.getRepository(), accountPubKey, info.getRepoName());
                    if (address == null) {
                        throw new RuntimeException("Can't not create contract for project!");
                    }
                    System.out.println("Repository contract is created on address:" + address);
                    info.setRepoAddress(address);
                    info.setOwner(getProjectOwner(projectDir));
                    info.setOwnerPubKeyRsa(HitHelper.getRsaPubKey());
                    info.setOwnerAddressEcc(accountPubKey);
                }
                writeUpdateFile(file, ByteHelper.utf8(info.genSignedContent(HitHelper.getRsaPriKeyWithPasswordInput())));
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

    /**
     * find default local branch, is exists only one branch the return the branch name, else return null.
     *
     * @param gitDir
     * @return
     */
    public static String findDefaultBranch(File gitDir) {
        try (Repository repo = new FileRepository(gitDir)) {
            return repo.exactRef(Constants.HEAD).getTarget().getName();
        } catch (Exception e) {
            throw new RuntimeException("GitHelper can not find local branch!", e);
        }
    }

    /**
     * find default remote branch, is exists only one branch the return the branch name, else return null.
     *
     * @param gitDir
     * @return
     */
    public static String findDefaultRemoteBranch(File gitDir) {
        try (Repository repo = new FileRepository(gitDir)) {
            Git git = new Git(repo);
            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            if (branches.size() != 1) {
                return null;
            }
            return branches.get(0).getName();
        } catch (Exception e) {
            throw new RuntimeException("GitHelper can not find retmote branch!", e);
        }
    }

    /**
     * create patch by default branch if branch is not set.
     *
     * @param gitDir
     * @param startRev
     * @param endRev
     * @return
     */
    public static byte[] createPatchByDefaultBranch(File gitDir, String startRev, String endRev) {
        if (StringUtils.isBlank(startRev)) {
            startRev = findDefaultRemoteBranch(gitDir);
        }
        if (StringUtils.isBlank(endRev)) {
            endRev = findDefaultBranch(gitDir);
        }
        if (StringUtils.isBlank(startRev)) {
            throw new IllegalArgumentException("GitHelper start branch name need to specify!");
        }
        if (StringUtils.isBlank(endRev)) {
            throw new IllegalArgumentException("GitHelper end branch name need to specify!");
        }
        return createPatch(gitDir, startRev, endRev);
    }

    /**
     * create patch from start branch name (not include) to end branchName.
     *
     * @param gitDir   git dir
     * @param startRev the start revision (no include)
     * @param endRev   the end revision.
     * @return
     */
    public static byte[] createPatch(File gitDir, String startRev, String endRev) {
        try (Repository repo = new FileRepository(gitDir)) {
            Git git = new Git(repo);
            RevWalk walk = new RevWalk(repo);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(baos);
            df.setRepository(repo);
            Iterable<RevCommit> result = new Git(repo).log()
                    .not(repo.resolve(startRev))
                    .add(repo.resolve(endRev))
                    .call();
            List<RevCommit> commits = new ArrayList<>();
            for (RevCommit rev : result) {
                commits.add(rev);
            }
            Collections.reverse(commits);
            RevCommit base = walk.parseCommit(repo.resolve(startRev).toObjectId());
            AtomicInteger count = new AtomicInteger(0);
            int total = commits.size();
            for (RevCommit rev : commits) {
                baos.write(formatEmailHeader(rev, count.incrementAndGet(), total).getBytes("UTF-8"));
                df.format(base.getTree(), rev.getTree());
                base = rev;
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("GitHelper can not create patch!", e);
        }
    }

    private static String formatEmailHeader(RevCommit commit, int count, int total) {
        StringBuilder b = new StringBuilder();
        PersonIdent author = commit.getAuthorIdent();
        String subject = commit.getShortMessage();
        String msg = commit.getFullMessage().substring(subject.length());
        if (msg.startsWith("\n\n")) {
            msg = msg.substring(2);
        }
        b.append("From ")
                .append(commit.getName())
                .append(' ')
                .append(
                        "Mon Sep 17 00:00:00 2001\n") // Fixed timestamp to match output of C Git's format-patch
                .append("From: ")
                .append(author.getName())
                .append(" <")
                .append(author.getEmailAddress())
                .append(">\n")
                .append("Date: ")
                .append(formatDate(author))
                .append('\n')
                .append("Subject: [PATCH " + count + "/" + total + "] ")
                .append(subject)
                .append('\n')
                .append('\n')
                .append(msg);
        if (!msg.endsWith("\n")) {
            b.append('\n');
        }
        return b.append("---\n\n").toString();
    }

    private static String formatDate(PersonIdent author) {
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        df.setCalendar(Calendar.getInstance(author.getTimeZone(), Locale.US));
        return df.format(author.getWhen());
    }
}
