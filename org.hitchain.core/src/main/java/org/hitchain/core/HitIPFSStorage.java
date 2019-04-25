/*******************************************************************************
 * Copyright (c) 2019-03-01 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/
package org.hitchain.core;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.multihash.Multihash;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.transport.URIish;
import org.hitchain.hit.api.DecryptableFileWrapper;
import org.hitchain.hit.api.EncryptableFileWrapper;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.EthereumHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.HitHelper;
import org.hitchain.hit.util.Tuple.Two;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HitIPFSStorage
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-03-01
 * auto generate by qdp.
 */
public class HitIPFSStorage {

    private URIish uri;
    private File projectDir;
    private ProjectInfoFile projectInfoFile;
    private IPFS ipfs;
    private Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileIndex;
    private Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> uploadedGitFileIndex = new HashMap<>();

    public HitIPFSStorage(File projectDir, URIish uri) {
        this.projectDir = projectDir;
        this.uri = uri;
        if (!GitHelper.isValidHitRepository(projectDir)) {
            String host = uri.getHost();
            String path = uri.getRawPath();
            if (StringUtils.isBlank(path)) {
                path = host;
                host = HitHelper.getRepository();
            } else {
                host = "https://" + host;
            }
            String contractAddress = StringUtils.removeEnd(StringUtils.removeStart(path, "/"), ".git");
            String projectAddress = EthereumHelper.getProjectAddress(host, contractAddress);
            if (StringUtils.isNotBlank(projectAddress) && !EthereumHelper.isError(projectAddress)) {// is valid contract address and have content.
                String gitFileIndexHash = projectAddress;
                ipfs = GitHelper.getIpfs();
                gitFileIndex = GitHelper.readGitFileIndexFromIpfs(ipfs, gitFileIndexHash);
                {
                    gitFileIndex.put(GitHelper.HIT_GITFILE_IDX, new Two<>(gitFileIndexHash, ""));
                }
                Two<Object, String, String> ipfsHashAndSha1 = gitFileIndex.get(GitHelper.HIT_PROJECT_INFO);
                if (StringUtils.isNotBlank(ipfsHashAndSha1.first())) {
                    try {
                        byte[] cat = ipfs.cat(Multihash.fromBase58(ipfsHashAndSha1.first()));
                        projectInfoFile = ProjectInfoFile.fromFile(
                                new HashedFile.FileWrapper(GitHelper.HIT_PROJECT_INFO, new HashedFile.ByteArrayInputStreamCallback(cat)));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (projectInfoFile == null) {
            GitHelper.onInitHitRepository(projectDir);
            projectInfoFile = GitHelper.readProjectInfoFile(projectDir);
            String projectAddress = EthereumHelper.getProjectAddress(projectInfoFile.getEthereumUrl(), projectInfoFile.getRepoAddress());
            String gitFileIndexHash = projectAddress;
            ipfs = GitHelper.getIpfs(projectInfoFile.getFileServerUrl());
            gitFileIndex = GitHelper.readGitFileIndexFromIpfs(ipfs, gitFileIndexHash);
            {
                gitFileIndex.put(GitHelper.HIT_GITFILE_IDX, new Two<>(gitFileIndexHash, ""));
            }
        }
    }

    /**
     * List the path content.
     *
     * @param prefix
     * @return
     * @throws IOException
     */
    public List<String> list(String prefix) throws IOException {
        prefix = StringUtils.defaultString(prefix);
        prefix = StringUtils.appendIfMissing(prefix, "/", "/");
        prefix = StringUtils.removeStart(prefix, "/");
        List<String> filePaths = new ArrayList<>();
        for (Map.Entry<String, Two<Object, String, String>> entry : gitFileIndex.entrySet()) {
            String filePath = entry.getKey();
            if (prefix.length() < 1) {
                filePaths.add(filePath);
            } else if (filePath.startsWith(prefix)) {
                filePaths.add(filePath.substring(prefix.length()));
            }
        }
        return filePaths;
    }

    /**
     * return the file content.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public byte[] get(String filePath) throws IOException {
        System.out.println("get filename:" + filePath);
        Two<Object, String, String> ipfsHashAndSha1 = gitFileIndex.get(filePath);
        if (ipfsHashAndSha1 == null || StringUtils.isBlank(ipfsHashAndSha1.first())) {
            throw new FileNotFoundException("File not found: " + filePath);
        }
        //System.out.println("get filename:" + filePath + ", ipfs:" + ipfsHashAndSha1.first() + ", sha1:" + ipfsHashAndSha1.second());
        byte[] content = ipfs.cat(Multihash.fromBase58(ipfsHashAndSha1.first()));
        //System.out.println("file content:" + new String(content));
        String rsaPriKeyWithPasswordInput = null;
        if (projectInfoFile.isPrivate()) {
            rsaPriKeyWithPasswordInput = HitHelper.getRsaPriKeyWithPasswordInput();
        }
        DecryptableFileWrapper file = new DecryptableFileWrapper(
                new HashedFile.FileWrapper(
                        filePath,
                        new HashedFile.ByteArrayInputStreamCallback(content)
                ),
                projectInfoFile,
                HitHelper.getAccountAddress(),
                rsaPriKeyWithPasswordInput);
        byte[] contents = file.getContents();
        return contents;
    }

    /**
     * delete the file.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public void delete(String path) throws IOException {
        //nothing to do.
    }

    /**
     * put file to ipfs storage and return ipfs hash.
     *
     * @param filePath file name with relative path.
     * @param data     file content.
     * @return ipfs hash.
     */
    public String put(String filePath, byte[] data) {
        System.out.println("Uploading file:" + filePath + "...");
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(data);
            EncryptableFileWrapper file = new EncryptableFileWrapper(
                    new HashedFile.FileWrapper(filePath,
                            new HashedFile.ByteArrayInputStreamCallback(is)),
                    projectInfoFile);
            List<MerkleNode> add = ipfs.add(file);
            String ipfsHash = add.get(add.size() - 1).hash.toBase58();
            {// add ipfs hash to uploadedGitFileIndex.
                if (StringUtils.isBlank(ipfsHash)) {
                    throw new IOException("Can not upload the file: " + filePath);
                }
                uploadedGitFileIndex.put(filePath, new Two(ipfsHash, GitHelper.sha1(data)));
            }
            System.out.println("Uploaded file:" + filePath + ", ipfs:" + ipfsHash);
            return ipfsHash;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * start put.
     *
     * @param filePath
     * @param monitor
     * @param monitorTask
     * @return
     * @throws IOException
     */
    public OutputStream beginPut(String filePath, ProgressMonitor monitor, String monitorTask) throws IOException {
        System.out.println("BeginPut file:" + filePath + "...");
        ByteArrayOutputStream os = new ByteArrayOutputStream() {
            public void close() throws IOException {
                onBeginPutClose(filePath, monitor, monitorTask, toByteArray());
            }
        };
        return os;
    }

    /**
     * return uploaded git file hash: Map<String:relativeFilePath, Two<Object, String:ipfsHash, String:sha1>>.
     *
     * @return
     */
    public Map<String, Two<Object, String, String>> getUploadedGitFileIndex() {
        return uploadedGitFileIndex;
    }

    /**
     * return ipfs git file hash: Map<String:relativeFilePath, Two<Object, String:ipfsHash, String:sha1>>.
     *
     * @return
     */
    public Map<String, Two<Object, String, String>> getGitFileIndex() {
        return gitFileIndex;
    }

    public ProjectInfoFile getProjectInfoFile() {
        return projectInfoFile;
    }

    public File getProjectDir() {
        return projectDir;
    }

    public IPFS getIpfs() {
        return ipfs;
    }

    protected String onBeginPutClose(String filePath, ProgressMonitor monitor, String monitorTask, byte[] bs) throws IOException {
        if (monitor == null) {
            monitor = NullProgressMonitor.INSTANCE;
        }
        if (monitorTask == null) {
            monitorTask = MessageFormat.format(JGitText.get().progressMonUploading, filePath);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(bs);
        EncryptableFileWrapper file = new EncryptableFileWrapper(new HashedFile.FileWrapper(filePath, new HashedFile.ByteArrayInputStreamCallback(bais)), projectInfoFile);
        byte[] content = file.getContents();
        String ipfsHash = put(filePath, content);
        monitor.beginTask(monitorTask, content.length / 1024);
        monitor.endTask();
        return ipfsHash;
    }
}
