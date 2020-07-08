package org.hitchain.core; /*******************************************************************************
 * Copyright (c) 2019-01-16 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/

import io.ipfs.multibase.Base58;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.web3j.crypto.Hash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MainTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-16
 * auto generate by qdp.
 */
public class MainTest {
    public static void main(String[] args) throws Exception {
        //System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/testencrypt");
        //System.setProperty("user.dir", "/Users/zhaochen/Desktop/temppath/testencrypt");
        //exec("git push");
        //gitClone();
        //System.exit(0);

        System.out.println(Hash.sha256("test".getBytes()).length);
        System.out.println(Hash.sha3("test").length());
        System.out.println(Base58.encode(Hash.sha3("test".getBytes())).length());
        System.out.println((1024*1024*4*66)/1024);
    }

    public static void main2(String[] args) throws Exception {
        if (2 - 2 == 1) {
            return;
        }
        if (1 - 1 == 1) {
            File dir = new File("/Users/zhaochen/Desktop/temppath");
            System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/hello");
            System.setProperty("user.dir", "/Users/zhaochen/Desktop/temppath/hello");
            for (int i = 0; i < 10; i++) {
                File file = new File(new File(dir, "hello"), "aaa-1547624216414.txt");
                FileOutputStream fos = new FileOutputStream(file);
                fos.write((StringUtils.repeat("hello world", 1 + new Random().nextInt(100)) + System.currentTimeMillis()).getBytes());
                fos.close();
                exec("git add " + file.getName());
                exec("git commit -m 'hello-" + System.currentTimeMillis() + "'");
            }
        }
        Repository repo = new FileRepository("/Users/zhaochen/Desktop/temppath/hello/.git");
        Git git = new Git(repo);
        RevWalk walk = new RevWalk(repo);

        {//test remote branch
            Iterable<RevCommit> commits = new Git(repo).log()
                    .add(repo.resolve("remotes/origin/master"))
                    .call();
            int count = 0;
            for (RevCommit rev : commits) {
                System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() */);
                count++;
            }
            System.out.println("Had " + count + " commits overall on test-branch");
        }
        {//test local branch
            Iterable<RevCommit> commits = new Git(repo).log()
                    .add(repo.resolve("master"))
                    .call();
            int count = 0;
            for (RevCommit rev : commits) {
                System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() */);
                count++;
            }
            System.out.println("Had " + count + " commits overall on test-branch");
        }
        {//test un push local-remote branch
            Iterable<RevCommit> commits = new Git(repo).log()
                    .not(repo.resolve("remotes/origin/master"))
                    .add(repo.resolve("master"))
                    .call();

            int count = 0;
            for (RevCommit rev : commits) {
                System.out.println("Commit: " + rev /* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() */);
                count++;
            }
            System.out.println("Had " + count + " un-push commits overall on test-branch");
        }
        {//test list all branch
            for (Ref ref : git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()) {
                System.out.println("Branch:" + ref.getName());
            }
            System.out.println("HEAD branch:" + repo.exactRef(Constants.HEAD).getTarget().getName());
        }
        {//test bundle:https://github.com/eclipse/jgit/blob/master/org.eclipse.jgit.test/tst/org/eclipse/jgit/transport/BundleWriterTest.java
            //https://gerrit.googlesource.com/gerrit/+/refs/heads/master/java/com/google/gerrit/server/
            //https://gerrit.googlesource.com/gerrit/+/refs/heads/master/java/com/google/gerrit/server/restapi/change/GetPatch.java
            //BundleWriter bw = new BundleWriter(repo);
            //bw.include(repo.exactRef(Constants.HEAD));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //bw.writeBundle(NullProgressMonitor.INSTANCE, baos);
            //FileUtils.writeByteArrayToFile(new File("/Users/zhaochen/Desktop/7.zip"), baos.toByteArray());
            DiffFormatter df = new DiffFormatter(baos);
            df.setRepository(repo);
            Iterable<RevCommit> result = new Git(repo).log()
                    .not(repo.resolve("remotes/origin/master"))
                    .add(repo.resolve("master"))
                    .call();
            List<RevCommit> commits = new ArrayList<>();
            for (RevCommit rev : result) {
                commits.add(rev);
            }
            Collections.reverse(commits);
            RevCommit base = walk.parseCommit(repo.resolve("remotes/origin/master").toObjectId());
            System.out.println("======master content=======");
            {
                RevTree tree = base.getTree();
                try (TreeWalk treeWalk = new TreeWalk(repo)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    if (!treeWalk.next()) {
                        throw new IllegalStateException("Did not find expected file 'README.md'");
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repo.open(objectId);

                    // and then one can the loader to read the file
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    loader.copyTo(out);
                    System.out.println(treeWalk.getPathString());
                    System.out.println(new String(out.toByteArray()));
                }
            }
            AtomicInteger count = new AtomicInteger(0);
            int total = commits.size();
            for (RevCommit rev : commits) {
                baos.write(formatEmailHeader(rev, count.incrementAndGet(), total).getBytes("UTF-8"));
                df.format(base.getTree(), rev.getTree());
                base = rev;
            }
            System.out.println("========diff=======");
            System.out.println(new String(baos.toByteArray()));
            FileUtils.writeByteArrayToFile(new File("/Users/zhaochen/Desktop/temppath/test/test.patch"), baos.toByteArray());
            System.out.println("========diff-end=======");
            Patch patch = new Patch();
            patch.parse(new ByteArrayInputStream(baos.toByteArray()));
            for (FileHeader fh : patch.getFiles()) {
                System.out.println(fh);
                for (HunkHeader hh : fh.getHunks()) {
                    byte[] b = new byte[hh.getEndOffset() - hh.getStartOffset()];
                    System.arraycopy(hh.getBuffer(), hh.getStartOffset(), b, 0, b.length);
                    RawText hrt = new RawText(b);
                    for (int i = 0; i < hrt.size(); i++) {
                        System.out.println("RawText:" + hrt.getString(i));
                    }
                }
            }
            System.out.println("========patch-end=======");
        }

        List<Ref> branches = git.branchList().call();

        for (Ref branch : branches) {
            String branchName = branch.getName();

            System.out.println("Commits of branch: " + branch.getName());
            System.out.println("-------------------------------------");

            Iterable<RevCommit> commits = git.log()
                    .addRange(repo.resolve("refs/remotes/origin/master"), repo.resolve("refs/heads/master")).call();

            for (RevCommit commit : commits) {
                boolean foundInThisBranch = false;

                RevCommit targetCommit = walk.parseCommit(repo.resolve(
                        commit.getName()));
                for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet()) {
                    if (e.getKey().startsWith(Constants.R_HEADS)) {
                        if (walk.isMergedInto(targetCommit, walk.parseCommit(
                                e.getValue().getObjectId()))) {
                            String foundInBranch = e.getValue().getName();
                            if (branchName.equals(foundInBranch)) {
                                foundInThisBranch = true;
                                break;
                            }
                        }
                    }
                }

                if (foundInThisBranch) {
                    System.out.println(commit.getName());
                    System.out.println(commit.getAuthorIdent().getName());
                    System.out.println(new Date(commit.getCommitTime() * 1000L));
                    System.out.println(commit.getFullMessage());
                    RevTree tree = commit.getTree();
                    // now try to find a specific file
                    try (TreeWalk treeWalk = new TreeWalk(repo)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        if (!treeWalk.next()) {
                            throw new IllegalStateException("Did not find expected file 'README.md'");
                        }

                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repo.open(objectId);

                        // and then one can the loader to read the file
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        loader.copyTo(out);
                        System.out.println(treeWalk.getPathString());
                        System.out.println(new String(out.toByteArray()));
                    }
                    System.out.println("====");
                }
            }
        }
    }

    public static void main1(String[] args) throws Exception {
        //gitPush();
        //FileUtils.deleteQuietly(new File("/Users/zhaochen/Desktop/temppath/hello"));
        //gitClone();
    }


    public static void gitPush() throws Exception {
        File dir = new File("/Users/zhaochen/Desktop/temppath");
        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/hello");
        System.setProperty("user.dir", "/Users/zhaochen/Desktop/temppath/hello");
        FileUtils.deleteQuietly(new File("/Users/zhaochen/Desktop/temppath/hello/.git"));
        exec("git init");
        exec("git add .gitignore");
        exec("git add *");
        exec("git commit -m 'init'");
        exec("git remote add origin hit://hello.git");
        for (int i = 0; i < 10; i++) {
            File file = new File(new File(dir, "hello"), "aaa-1547624216414.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write((StringUtils.repeat("hello world", 1 + new Random().nextInt(100)) + System.currentTimeMillis()).getBytes());
            fos.close();
            exec("git add " + file.getName());
            exec("git commit -m 'hello-" + System.currentTimeMillis() + "'");
        }
        exec("git push");
    }


    public static void exec(String... args) throws Exception {
        org.eclipse.jgit.pgm.CLIGitCommand.main(args);
    }

    public static void gitClone() throws Exception {
        System.setProperty("user.dir", "/Users/zhaochen/Desktop/temppath/testencrypt");
        org.eclipse.jgit.pgm.Main.main(new String[]{
                // "clone", "hit://0xd1c935d2b098b52346d6fd7ce713c2eeb09982f9-3.git"
                "clone", "hit://0x48e154cb7040602163236df58a8cc3c0836425e1-6.git"
        });
    }

    public static void gitClone2() throws Exception {
        System.setProperty("user.dir", "/Users/Theodore-Ramsden/Desktop/temppath");
        org.eclipse.jgit.pgm.Main.main(new String[]{
                "clone", "hit://0xd1c935d2b098b52346d6fd7ce713c2eeb09982f9-3.git"
        });
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
