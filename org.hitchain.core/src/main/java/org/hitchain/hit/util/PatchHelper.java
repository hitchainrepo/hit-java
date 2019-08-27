/*******************************************************************************
 * Copyright (c) 2019-02-19 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.hit.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.iff.infra.util.FCS;
import org.iff.infra.util.HttpHelper;
import org.iff.infra.util.MapHelper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PatchHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-06-21
 */
public class PatchHelper {

    public static void main(String[] args) {
        String url = "https://gitee.com/api/v5/repos/jfinal/jfinal/pulls?per_page=100";
        HttpRequestHelper.RequestResult result = HttpRequestHelper.get(url, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        String content = result.getBody();
        System.out.println(content);
        System.out.println(HttpHelper.get(url));
    }

    public static List<PatchFileInfo> parsePatch(InputStream in) {
        List<PatchFileInfo> list = new ArrayList<>();
        try {
            String content = IOUtils.toString(in, "UTF-8");
            String[] split = StringUtils.split(content, '\n');
            boolean isHead = false, isSubject = false, isDiff = false;
            StringBuilder msg = new StringBuilder();
            StringBuilder diff = new StringBuilder();
            PatchFileInfo pfi = null;
            for (String s : split) {
                if (s.startsWith("From ") && s.endsWith("Mon Sep 17 00:00:00 2001")) {
                    if (isHead) {//new patch section
                        isSubject = isDiff = false;
                        pfi.diff(diff.toString());
                        list.add(pfi);
                        pfi = null;
                        diff.setLength(0);
                        msg.setLength(0);
                    }
                    isHead = true;
                    pfi = new PatchFileInfo();
                    String commit = StringUtils.substringBefore(StringUtils.substringAfter(s, "From ").trim(), " ");
                    String dateStr = StringUtils.substringAfter(s, commit).trim();//Mon Sep 17 00:00:00 2001
                    pfi.commit(commit).from(DateUtils.parseDate(dateStr, new String[]{"EEE MMM dd HH:mm:ss yyyy", "EEE, dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX"}));
                    continue;
                }
                if (isHead && !isDiff && s.startsWith("From: ")) {
                    String author = StringUtils.substringBefore(StringUtils.substringAfter(s, "From: ").trim(), " ");
                    String email = StringUtils.substringBefore(StringUtils.substringAfter(s, "<"), ">").trim();
                    pfi.author(author).email(email);
                    continue;
                }
                if (isHead && !isDiff && s.startsWith("Date: ")) {
                    String dateStr = StringUtils.substringAfter(s, "Date: ").trim();//Thu, 3 Jan 2019 19:24:33 +0800
                    Date date = DateUtils.parseDate(dateStr, new String[]{"EEE MMM dd HH:mm:ss yyyy", "EEE, dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX"});
                    pfi.date(date);
                    continue;
                }
                if (isHead && !isDiff && s.startsWith("Subject: ")) {
                    isSubject = true;
                    String shortMsg = StringUtils.substringAfter(s, "]").trim();
                    pfi.shortMsg(shortMsg);
                    continue;
                }
                if (isHead && isSubject && !isDiff) {
                    if (!s.equals("---")) {
                        msg.append(s).append("\n");
                    } else {
                        isHead = isSubject = false;
                        String message = msg.toString();
                        pfi.msg(StringUtils.isBlank(message) ? pfi.shortMsg() : message.trim());
                    }
                    continue;
                }
                if (!isHead && !isSubject && !isDiff && (s.startsWith("diff --git ") || s.startsWith("diff --cc ") || s.startsWith("diff --combined "))) {
                    isDiff = true;
                    diff.append(s).append('\n');
                    continue;
                }
                if (isDiff) {
                    if (s.equals("-- ")) {//jgit
                        isDiff = false;
                        continue;
                    }
                    if (s.equals("--")) {//libgit2
                        isDiff = false;
                        continue;
                    }
                    diff.append(s).append('\n');
                }
            }
            if (pfi != null) {
                pfi.diff(diff.toString());
                list.add(pfi);
                pfi = null;
                diff.setLength(0);
                msg.setLength(0);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * parse diff content and return PatchInfo{patch: diff, files, insertions, deletions, summary}
     * <pre>
     * Sample data:
     * https://gitee.com/jfinal/jfinal/pulls/40.diff
     * https://gitee.com/jfinal/jfinal/pulls/40.patch
     * https://gitee.com/jfinal/jfinal/pulls/40/commits
     * </pre>
     *
     * @param in
     * @return
     */
    public static PatchInfo parseDiff(InputStream in) {
        try {
            String content = IOUtils.toString(in, "UTF-8");
            String[] split = StringUtils.split(content, '\n');
            StringBuilder diff = new StringBuilder();
            PatchInfo pi = new PatchInfo();
            int files = 0;
            int insertions = 0;
            int deletions = 0;
            for (String s : split) {
                if (s.startsWith("diff --git ") || s.startsWith("diff --cc ") || s.startsWith("diff --combined ")) {
                    diff.append(s).append('\n');
                    files += 1;
                    continue;
                }
                if (s.equals("-- ")) {//jgit
                    continue;
                }
                if (s.equals("--")) {//libgit2
                    continue;
                }
                if (s.startsWith("+")) {
                    insertions += 1;
                }
                if (s.startsWith("-")) {
                    deletions += 1;
                }
                diff.append(s).append('\n');
            }
            {
                //pi.from(DateUtils.parseDate("Mon Sep 17 00:00:00 2001", new String[]{"EEE MMM dd HH:mm:ss yyyy", "EEE, dd MMM yyyy HH:mm:ss Z", "yyyy-MM-dd'T'HH:mm:ssXXX"}));
                pi.patch(diff.toString());
                pi.files(files);
                pi.insertions(insertions);
                pi.deletions(deletions);
                pi.summary(files + " files changed, " + insertions + " insertions(+), " + deletions + " deletions(-)");//TODO missing change details.
            }
            return pi;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PatchSummaryInfo createPatch(File gitDir, String startRev, String endRev, String comment) {
        PatchSummaryInfo summary = new PatchSummaryInfo();
        try (Repository repo = new FileRepository(gitDir)) {
            RevWalk walk = new RevWalk(repo);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
            {
                summary.startRevision(startRev)
                        .endRevision(endRev)
                        .startCommit(base.getName())
                        .endCommit(commits.get(commits.size() - 1).getName())
                        .totalCommit(total)
                        .message(comment)
                        .date(new Date());
            }
            for (RevCommit rev : commits) {
                PatchFormatter patch = new PatchFormatter(baos);
                patch.setRepository(repo);
                patch.format(base.getTree(), rev.getTree());
                PatchInfo patchInfo = patch.writePatch(base, rev, count.incrementAndGet(), total);
                {
                    summary.patchs().add(patchInfo);
                }
                base = rev;
            }
            {
                summary.patch = ByteHelper.utf8(baos.toByteArray());
            }
            return summary;
        } catch (Exception e) {
            throw new RuntimeException("GitHelper can not create patch!", e);
        }
    }

    public static Map<String, Object> format(PatchSummaryInfo info, String url, String author, String accountAddress, String accountRsaPub) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (info == null) {
            return map;
        }
        ArrayList<Object> patches = new ArrayList<>();
        MapHelper.fillMap(map,
                "id", info.endCommit,
                "url", url,
                "date", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).format(info.date),
                "author", StringUtils.defaultString(author),
                "author_address", StringUtils.defaultString(accountAddress),
                "author_rsa_pub", StringUtils.defaultString(accountRsaPub),
                "start_revision", info.startRevision,
                "end_revision", info.endRevision,
                "start_commit", info.startCommit,
                "end_commit", info.endCommit,
                "total_commit", String.valueOf(info.totalCommit),
                "message", info.message,
                "patches", patches
        );
        for (PatchInfo pi : info.patchs) {
            Map<String, Object> tmp = new LinkedHashMap<>();
            MapHelper.fillMap(tmp,
                    "id", pi.endCommit(),
                    "commit_index", String.valueOf(pi.commitIndex),
                    "total_commit", String.valueOf(pi.commitTotal),
                    "start_commit", pi.startCommit(),
                    "end_commit", pi.endCommit(),
                    "short_message", pi.shortMsg,
                    "message", pi.msg,
                    "author", pi.author,
                    "change_files", String.valueOf(pi.files),
                    "insertions", String.valueOf(pi.insertions),
                    "deletions", String.valueOf(pi.deletions),
                    "summary", pi.summary
            );
            patches.add(tmp);
        }
        return map;
    }

    public static class PatchFormatter extends DiffFormatter {
        /**
         * the output stream for real output when call writePatch.
         */
        private final OutputStream os;

        private final ByteArrayOutputStream baos;

        private Map<String, int[/*0:insertions, 1:deletions*/]> changes = new HashMap<>();

        private int[/*0:insertions, 1:deletions*/] currentCount;

        public PatchFormatter(OutputStream os) {
            super(new ByteArrayOutputStream());
            this.os = os;
            this.baos = (ByteArrayOutputStream) super.getOutputStream();
        }

        private static String formatDate(PersonIdent author) {
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            df.setCalendar(Calendar.getInstance(author.getTimeZone(), Locale.US));
            return df.format(author.getWhen());
        }

        @Override
        public void format(DiffEntry entry) throws IOException {
            currentCount = new int[2];
            changes.put(entry.getNewPath(), currentCount);
            super.format(entry);
        }

        @Override
        protected void writeLine(final char prefix, final RawText text, final int cur)
                throws IOException {
            currentCount[0/*insertions*/] += prefix == '+' ? 1 : 0;
            currentCount[1/*deletions*/] += prefix == '-' ? 1 : 0;
            super.writeLine(prefix, text, cur);
        }

        /**
         * Only for one call.
         *
         * @param endCommit
         * @param index
         * @param totalCount
         * @return
         */
        public PatchInfo writePatch(RevCommit startCommit, RevCommit endCommit, int index, int totalCount) {
            PersonIdent author = endCommit.getAuthorIdent();
            String subject = endCommit.getShortMessage();
            String msg = endCommit.getFullMessage().substring(subject.length());
            if (msg.startsWith("\n\n")) {
                msg = msg.substring(2);
            }
            if (msg.endsWith("\n")) {
                msg = msg.substring(0, msg.length() - 1);
            }
            String patchInfo = "" +//
                    "From {commitName} Mon Sep 17 00:00:00 2001\n" +//Fixed timestamp to match output of C Git's format-patch
                    "From: {authorName} <{authorEmail}>\n" +//
                    "Date: {date}\n" +//
                    "Subject: [PATCH {index}/{total}] {subject}\n" +//
                    "\n" +//
                    "{msg}\n" +//
                    "---\n" +//
                    "{changeDetail}\n" +//
                    " {files} files changed, {insertions} insertions(+), {deletions} deletions(-)\n\n" +//
                    "{diff}\n" +//
                    "-- \n";

            List<String> changeDetail = new ArrayList<>();
            int files = 0;
            int insertions = 0;
            int deletions = 0;
            {
                // generate diff stats
                int maxPathLen = 0;
                for (String path : changes.keySet()) {
                    if (path.length() > maxPathLen) {
                        maxPathLen = path.length();
                    }
                    int[] changeCount = changes.get(path);
                    files++;
                    insertions += changeCount[0/*insertions*/];
                    deletions += changeCount[1/*deletions*/];
                }
                int columns = 60;
                int total = insertions + deletions;
                int unit = total / columns + (total % columns > 0 ? 1 : 0);
                if (unit == 0) {
                    unit = 1;
                }
                for (String path : changes.keySet()) {
                    int[] changeCount = changes.get(path);
                    int totalChanges = changeCount[0] + changeCount[1];
                    String line = " {fileName} | {totalChanges} {relativeScale}";
                    changeDetail.add(
                            FCS.get(line,
                                    StringUtils.rightPad(path, maxPathLen, ' '),
                                    StringUtils.leftPad("" + totalChanges, 4, ' '),
                                    relativeScale(unit, changeCount[0], changeCount[1])
                            ).toString()
                    );
                }
            }
            String patchContent = FCS.get(patchInfo,
                    endCommit.getName(),
                    author.getName(),
                    author.getEmailAddress(),
                    formatDate(author),
                    index,
                    totalCount,
                    subject,
                    msg,
                    StringUtils.join(changeDetail, "\n"),
                    files,
                    insertions,
                    deletions,
                    ByteHelper.utf8(baos.toByteArray())
            ).toString();

            try {
                os.write(ByteHelper.utf8(patchContent));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            PatchInfo info = new PatchInfo()
                    .commitIndex(index)
                    .commitTotal(totalCount)
                    .startCommit(startCommit.getName())
                    .endCommit(endCommit.getName())
                    .shortMsg(subject)
                    .msg(msg)
                    .author(author.getName())
                    .email(author.getEmailAddress())
                    .date(author.getWhen())
                    .files(files)
                    .insertions(insertions)
                    .deletions(deletions)
                    .summary(StringUtils.join(changeDetail, "\n") + "\n " + files + " files changed, " + insertions + " insertions(+), " + deletions + " deletions(-)")
                    .patch(patchContent);
            return info;
        }


        private String relativeScale(int unit, int insertions, int deletions) {
            int plus = insertions / unit;
            int minus = deletions / unit;
            return StringUtils.repeat('+', plus) + StringUtils.repeat('-', minus);
        }
    }

    /**
     * Patch information description.
     */
    public static class PatchInfo implements Serializable {
        /*Subject: [PATCH commitIndex/commitTotal] shortMsg*/
        protected int commitIndex;
        /*Subject: [PATCH commitIndex/commitTotal] shortMsg*/
        protected int commitTotal;
        /*User for the merge, to checkout the commit*/
        protected String startCommit;
        /*From b9ec6700e4e0c41849104e9be90c50ad5e731b99 Mon Sep 17 00:00:00 2001*/
        protected String endCommit;
        /*Subject: [PATCH commitIndex/commitTotal] shortMsg*/
        protected String shortMsg;
        /*the full message append to the Subject with new line*/
        protected String msg;
        /*From: author <author@163.com>*/
        protected String author;
        /*From: author <email@163.com>*/
        protected String email;
        /*Date: Thu, 25 Oct 2018 14:59:23 +0800*/
        protected Date date;
        /*3 files changed, 57 insertions(+), 4 deletions(-)*/
        protected int files;
        /*3 files changed, 57 insertions(+), 4 deletions(-)*/
        protected int insertions;
        /*3 files changed, 57 insertions(+), 4 deletions(-)*/
        protected int deletions;
        /*files list, change summary and create mode*/
        protected String summary;
        /*full path content*/
        protected String patch;

        public String genPatch(String diff) {
            String patchInfo = "" +//
                    "From {commitName} Mon Sep 17 00:00:00 2001\n" +//Fixed timestamp to match output of C Git's format-patch
                    "From: {authorName} <{authorEmail}>\n" +//
                    "Date: {date}\n" +//
                    "Subject: [PATCH {index}/{total}] {subject}\n" +//
                    "\n" +//
                    "{msg}\n" +//
                    "---\n" +//
                    "{changeDetail}\n" +//
                    " {files} files changed, {insertions} insertions(+), {deletions} deletions(-)\n\n" +//
                    "{diff}\n" +//
                    "-- \n";
            return FCS.get(patchInfo,
                    endCommit(),
                    author(),
                    email(),
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).format(date()),
                    commitIndex(),
                    commitTotal(),
                    shortMsg(),
                    msg(),
                    summary(),
                    files(),
                    insertions(),
                    deletions(),
                    diff
            ).toString();
        }

        public String email() {
            return email;
        }

        public PatchInfo email(String email) {
            this.email = email;
            return this;
        }

        public Date date() {
            return date;
        }

        public PatchInfo date(Date date) {
            this.date = date;
            return this;
        }

        public int commitIndex() {
            return commitIndex;
        }

        public PatchInfo commitIndex(int commitIndex) {
            this.commitIndex = commitIndex;
            return this;
        }

        public int commitTotal() {
            return commitTotal;
        }

        public PatchInfo commitTotal(int commitTotal) {
            this.commitTotal = commitTotal;
            return this;
        }

        public String startCommit() {
            return startCommit;
        }

        public PatchInfo startCommit(String startCommit) {
            this.startCommit = startCommit;
            return this;
        }

        public String endCommit() {
            return endCommit;
        }

        public PatchInfo endCommit(String endCommit) {
            this.endCommit = endCommit;
            return this;
        }

        public String shortMsg() {
            return shortMsg;
        }

        public PatchInfo shortMsg(String shortMsg) {
            this.shortMsg = shortMsg;
            return this;
        }

        public String msg() {
            return msg;
        }

        public PatchInfo msg(String msg) {
            this.msg = msg;
            return this;
        }

        public String author() {
            return author;
        }

        public PatchInfo author(String author) {
            this.author = author;
            return this;
        }

        public int files() {
            return files;
        }

        public PatchInfo files(int files) {
            this.files = files;
            return this;
        }

        public int insertions() {
            return insertions;
        }

        public PatchInfo insertions(int insertions) {
            this.insertions = insertions;
            return this;
        }

        public int deletions() {
            return deletions;
        }

        public PatchInfo deletions(int deletions) {
            this.deletions = deletions;
            return this;
        }

        public String summary() {
            return summary;
        }

        public PatchInfo summary(String summary) {
            this.summary = summary;
            return this;
        }

        public String patch() {
            return patch;
        }

        public PatchInfo patch(String patch) {
            this.patch = patch;
            return this;
        }

        public String toString() {
            return patch();
        }
    }

    public static class PatchSummaryInfo implements Serializable {
        protected String startRevision;
        protected String endRevision;
        protected String startCommit;
        protected String endCommit;
        protected int totalCommit;
        protected String message;
        protected Date date;
        protected String patch;
        protected List<PatchInfo> patchs;

        public String startRevision() {
            return startRevision;
        }

        public PatchSummaryInfo startRevision(String startRevision) {
            this.startRevision = startRevision;
            return this;
        }

        public String endRevision() {
            return endRevision;
        }

        public PatchSummaryInfo endRevision(String endRevision) {
            this.endRevision = endRevision;
            return this;
        }

        public String startCommit() {
            return startCommit;
        }

        public PatchSummaryInfo startCommit(String startCommit) {
            this.startCommit = startCommit;
            return this;
        }

        public String endCommit() {
            return endCommit;
        }

        public PatchSummaryInfo endCommit(String endCommit) {
            this.endCommit = endCommit;
            return this;
        }

        public int totalCommit() {
            return totalCommit;
        }

        public PatchSummaryInfo totalCommit(int totalCommit) {
            this.totalCommit = totalCommit;
            return this;
        }

        public String message() {
            return message;
        }

        public PatchSummaryInfo message(String message) {
            this.message = message;
            return this;
        }

        public Date date() {
            return date;
        }

        public PatchSummaryInfo date(Date date) {
            this.date = date;
            return this;
        }

        public String patch() {
            return patch;
        }

        public PatchSummaryInfo patch(String patch) {
            this.patch = patch;
            return this;
        }

        public List<PatchInfo> patchs() {
            return patchs;
        }

        public PatchSummaryInfo patchs(List<PatchInfo> patchs) {
            this.patchs = patchs;
            return this;
        }
    }

    public static class PatchFileInfo implements Serializable {
        /*From 303f4fab121188669451c624d073f95488ff1219 Mon Sep 17 00:00:00 2001*/
        protected Date from;
        /*From 303f4fab121188669451c624d073f95488ff1219 Mon Sep 17 00:00:00 2001*/
        protected String commit;
        /*From: jolestar <jolestar@gmail.com>*/
        protected String author;
        /*From: jolestar <jolestar@gmail.com>*/
        protected String email;
        /*Date: Thu, 3 Jan 2019 19:24:33 +0800*/
        protected Date date;
        /*Subject: [PATCH 1/2] fix https://github.com/ethereum/ethereumj/issues/1248*/
        protected String shortMsg;
        protected String msg;
        protected String diff;

        public String commit() {
            return commit;
        }

        public PatchFileInfo commit(String commit) {
            this.commit = commit;
            return this;
        }

        public Date from() {
            return from;
        }

        public PatchFileInfo from(Date from) {
            this.from = from;
            return this;
        }

        public String author() {
            return author;
        }

        public PatchFileInfo author(String author) {
            this.author = author;
            return this;
        }

        public String email() {
            return email;
        }

        public PatchFileInfo email(String email) {
            this.email = email;
            return this;
        }

        public Date date() {
            return date;
        }

        public PatchFileInfo date(Date date) {
            this.date = date;
            return this;
        }

        public String shortMsg() {
            return shortMsg;
        }

        public PatchFileInfo shortMsg(String shortMsg) {
            this.shortMsg = shortMsg;
            return this;
        }

        public String msg() {
            return msg;
        }

        public PatchFileInfo msg(String msg) {
            this.msg = msg;
            return this;
        }

        public String diff() {
            return diff;
        }

        public PatchFileInfo diff(String diff) {
            this.diff = diff;
            return this;
        }

        @Override
        public String toString() {
            String patchInfo = "" +//
                    "From {commitName} Mon Sep 17 00:00:00 2001\n" +//Fixed timestamp to match output of C Git's format-patch
                    "From: {authorName} <{authorEmail}>\n" +//
                    "Date: {date}\n" +//
                    "Subject: [PATCH] {subject}\n" +//
                    "\n" +//
                    "{msg}\n" +//
                    "---\n" +//
                    "{diff}\n" +//
                    "-- \n";
            return FCS.get(patchInfo,
                    commit,
                    author, email,
                    date == null ? "" : new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(date),
                    shortMsg,
                    msg,
                    diff).toString();
        }
    }

}