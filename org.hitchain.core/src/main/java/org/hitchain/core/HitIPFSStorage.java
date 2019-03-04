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
import org.hitchain.hit.api.EncryptableFileWrapper;
import org.hitchain.hit.api.HashedFile;
import org.hitchain.hit.api.ProjectInfoFile;
import org.hitchain.hit.util.EthereumHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.Tuple.Two;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
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

    private File projectDir;
    private ProjectInfoFile projectInfoFile;
    private IPFS ipfs;
    private Map<String/* filename */, Two<Object, String/* ipfs hash */, String/* sha1 */>> gitFileIndex;

    public HitIPFSStorage(File projectDir) {
        this.projectDir = projectDir;
        projectInfoFile = GitHelper.readProjectInfoFile(projectDir);
        String projectAddress = EthereumHelper.getProjectAddress(projectInfoFile.getEthereumUrl(), projectInfoFile.getRepoAddress());
        String gitFileIndexHash = projectAddress;
        ipfs = GitHelper.getIpfs(projectInfoFile.getFileServerUrl());
        gitFileIndex = GitHelper.readGitFileIndexFromIpfs(ipfs, gitFileIndexHash);
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
        prefix = StringUtils.removeStart(prefix, "/");
        prefix = StringUtils.appendIfMissing(prefix, "/", "/");
        List<String> filePaths = new ArrayList<>();
        for (Map.Entry<String, Two<Object, String, String>> entry : gitFileIndex.entrySet()) {
            String filePath = entry.getKey();
            if (prefix.length() < 1) {
                filePaths.add(filePath);
            } else if (filePath.startsWith(prefix)) {
                filePaths.add(filePath);
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
        Two<Object, String, String> ipfsHashAndSha1 = gitFileIndex.get(filePath);
        if (ipfsHashAndSha1 == null || StringUtils.isBlank(ipfsHashAndSha1.first())) {
            return null;
        }
        return ipfs.cat(Multihash.fromBase58(ipfsHashAndSha1.first()));
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
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            EncryptableFileWrapper file = new EncryptableFileWrapper(new HashedFile.FileWrapper(filePath, new HashedFile.ByteArrayInputStreamCallback(bais)), projectInfoFile);
            List<MerkleNode> add = ipfs.add(file);
            return add.get(add.size() - 1).hash.toBase58();
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
        if (monitor == null) {
            monitor = NullProgressMonitor.INSTANCE;
        }
        if (monitorTask == null) {
            monitorTask = MessageFormat.format(JGitText.get().progressMonUploading, filePath);
        }
        EncryptableFileWrapper file = new EncryptableFileWrapper(new HashedFile.FileSystemWrapper(new File(projectDir, filePath).getAbsolutePath()), projectInfoFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] contents = file.getContents();
        monitor.beginTask(monitorTask, contents.length / 1024);
        baos.write(contents);
        monitor.endTask();
        return baos;
    }
}
