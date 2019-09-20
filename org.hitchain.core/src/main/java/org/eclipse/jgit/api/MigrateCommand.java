/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.utils.DateUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.hit.util.ByteHelper;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.HitHelper;
import org.hitchain.hit.util.PatchHelper;
import org.iff.infra.util.FCS;
import org.iff.infra.util.GsonHelper;
import org.iff.infra.util.MapHelper;
import org.iff.infra.util.NumberHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MigrateCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class MigrateCommand extends TransportCommand<MigrateCommand, Hit> {
    public static final String PROP_TOKEN = "--token-for-authorization";
    protected Hit hit;
    /**
     * the repository uri github/gitee.
     */
    protected String uri;
    /**
     * the repository name.
     */
    protected String name;
    /**
     * if true the repository will auto rename if the name exists.
     */
    protected boolean autoRename;
    /**
     * max pull request to fetch, if maxPrSize=0, will fetch all status=open pull request, default 20.
     */
    protected int maxPrSize = 20;
    /**
     * if you migrate repository from github, the request is limiting by default to 60 requests per hour.
     * https://developer.github.com/v3/#rate-limiting
     * https://developer.github.com/v3/#authentication
     * https://developer.github.com/apps/building-oauth-apps/creating-an-oauth-app/
     */
    protected String token;


    public MigrateCommand() {
        super(null);
    }

    @Override
    public Hit call() throws GitAPIException {
        String repositoryName = StringUtils.isNotBlank(name()) ? name() : GitHelper.getRepositoryName(uri());
        if (StringUtils.isBlank(repositoryName)) {
            throw new RuntimeException("uri is invalid: " + uri());
        }
        boolean isRepositoryExists = Hit.util().readId(repositoryName) > 0;
        if (isRepositoryExists && !autoRename()) {
            throw new RuntimeException("repository name exists: " + repositoryName);
        }
        if (isRepositoryExists && autoRename()) {
            for (int i = 0; i < 100; i++) {
                String newName = repositoryName + "-" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
                if (Hit.util().readId(newName) > 0) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                    }
                } else {
                    repositoryName = newName;
                    break;
                }
            }
        }
        if (StringUtils.isNotBlank(token())) {
            System.setProperty(PROP_TOKEN, token());
        }
        try {
            migrateWithPullRequest(uri(), repositoryName, maxPrSize());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Migrate repository success.");
        return null;
    }

    protected String migrateWithPullRequest(String gitUrl, String repositoryName, int maxPrSize) throws Exception {
        System.out.println("0/5 Start to clone repository " + gitUrl + " ...");
        String workDir = System.getProperty("git_work_tree");
        try (Repository repo = Git.cloneRepository()
                .setDirectory(StringUtils.isBlank(workDir) ? null : new File(workDir))
                .setURI(gitUrl)
                .setProgressMonitor(new TextProgressMonitor())
                .call().getRepository()) {
            System.out.println("Check repository name...");
            repositoryName = StringUtils.isBlank(repositoryName) ? repo.getDirectory().getParentFile().getName() : repositoryName;
            if (Hit.util().readId(repositoryName) > 0) {
                throw new Exception("Migrate repository name " + repositoryName + " exists, you should provide a new name for migrate.");
            }
            {
                System.out.println("1/5 Clone repository " + gitUrl + " success.");
            }
            List<PatchHelper.PatchSummaryInfo> summaryInfos = Collections.EMPTY_LIST;
            {
                System.out.println("2/5 Start to fetch pull request...");
                summaryInfos = fetchPullRequest2(repo, gitUrl, maxPrSize);
                System.out.println("2/5 Fetch pull request success.");
            }
            {
                System.out.println("3/5 Start to add repository...");
                HitHelper.createRepository(repo.getDirectory(), repositoryName, false);
                System.out.println("3/5 Add repository success.");
            }
            {
                System.out.println("4/5 Start to push repository to hit...");
                new Git(repo).push().call();
                hit(new Hit(repo));
                System.out.println("4/5 Push repository to hit success.");
            }
            System.out.println("5/5 Start to add pull request...");
            File pullRequestFetch = new File(repo.getDirectory(), "pullrequest_fetch");
            {//
                List<Map<String, Object>> summaries = new ArrayList<>();
                File[] files = pullRequestFetch.listFiles();
                files = files == null ? new File[0] : files;
                Map<String/*commitName*/, String/*ipfsHash*/> map = new HashMap<>();
                for (File f : files) {
                    if (!f.getName().endsWith(".patch")) {
                        continue;
                    }
                    String ipfsHash = GitHelper.writeFileToIpfs(FileUtils.readFileToByteArray(f), f.getName());
                    map.put(StringUtils.substringBefore(f.getName(), ".patch"), ipfsHash);
                }
                String url = null, author = null;
                {
                    String name = hit().storedConfig().getUserName().call();
                    String email = hit().storedConfig().getUserEmail().call();
                    if (name == null || email == null) {
                        author = "unknown";
                    } else {
                        author = name + " <" + email + ">";
                    }
                    url = hit().storedConfig().getRemoteUrl().call();
                }
                for (PatchHelper.PatchSummaryInfo psi : summaryInfos) {
                    Map<String, Object> format = PatchHelper.format(psi, url, author, HitHelper.getAccountAddress(), HitHelper.getRsaPubKey());
                    format.put("patch_url", "http://" + HitHelper.getStorage() + ":8080/ipfs/" + map.get(format.get("id")));
                    format.put("patch_hash", map.get(format.get("id")));
                    summaries.add(format);
                }
                String prInfo = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create().toJson(summaries);
                String prInfoHash = null;
                {// upload pull request info
                    prInfoHash = GitHelper.writeFileToIpfs(ByteHelper.utf8(prInfo), "pullRequestInfo.json");
                    System.out.println("PullRequestInfo: http://" + HitHelper.getStorage() + ":8080/ipfs/" + prInfoHash);
                    String result = hit().contract().addPullRequest(prInfoHash);
                    //String writeAddPullRequest = PullRequestContractEthereumService.getApi().writeAddPullRequest(prInfoHash, getAccountPriKeyWithPasswordInput(), contractAddress, getGasWrite(), getGasWriteGwei());
                    if (ContractApi.isError(result)) {
                        System.err.println("Add pull request faild, error: " + result);
                        return null;
                    }
                }
            }
            FileUtils.deleteQuietly(pullRequestFetch);
            System.out.println("5/5 Add request success.");
        }
        return "";
    }

    protected List<PatchHelper.PatchSummaryInfo> fetchPullRequest2(Repository repo, String gitUrl, int maxPrSize) throws Exception {
        if (StringUtils.contains(gitUrl, "gitee.com")) {
            return fetchPullRequestFromGiteeServer(repo, gitUrl, maxPrSize);
        }
        if (StringUtils.contains(gitUrl, "github.com")) {
            return fetchPullRequestFromGitServer(repo, gitUrl, maxPrSize);
        }
        throw new RuntimeException("Git url not support yet: " + gitUrl);
    }

    protected List<PatchHelper.PatchSummaryInfo> fetchPullRequestFromGitServer(Repository repo, String gitUrl, int maxPrSize) throws Exception {
        List<PatchHelper.PatchSummaryInfo> summaryInfos = new ArrayList<>();
        String[] split = StringUtils.split(gitUrl, "/");
        if (split.length < 2) {
            System.err.println("Git url is invalided.");
            return summaryInfos;
        }
        List<String> paths = Arrays.asList(split);
        Collections.reverse(paths);
        String repoName = StringUtils.remove(paths.get(0), ".git");
        String owner = paths.get(1);
        // pull url sample: https://api.github.com/repos/ethereum/ethereumj/pulls
        String pullUrl = FCS.get("https://api.github.com/repos/{owner}/{repoName}/pulls?per_page=" + maxPrSize, owner, repoName).toString();
        String pullsJson = httpGet2(pullUrl, "Try fetch pull request for {times} times, url {url}.");
        List<Map<String, Object>> pulls = GsonHelper.toJsonList(pullsJson);
        for (Map<String, Object> pull : pulls) {
            System.out.println("Fetching pull request:" + pull.get("url"));
            // sample: https://github.com/ethereum/ethereumj/pull/1278.patch
            String patchUrl = (String) pull.get("patch_url");
            // sample: https://api.github.com/repos/ethereum/ethereumj/pulls/1278/commits
            String commitsUrl = (String) pull.get("commits_url");
            String startRevision = "refs/heads/" + MapHelper.getByPath(pull, "base/ref");
            String endRevision = "refs/heads/" + MapHelper.getByPath(pull, "head/ref");
            String startCommit = (String) MapHelper.getByPath(pull, "base/sha");
            String endCommit = (String) MapHelper.getByPath(pull, "head/sha");
            String message = (String) pull.get("body");
            //"created_at": "2019-05-01T09:59:35Z",
            String dateStr = (String) pull.get("created_at");
            Date date = DateUtils.parseDate(dateStr, new String[]{"yyyy-MM-dd'T'HH:mm:ss'Z'"});
            //
            String patchContent = httpGet2(patchUrl, "Try fetch pull request patch for {times} times, url {url}.");
            PatchHelper.PatchInfo pi = null;
            if (StringUtils.isBlank(patchContent)) {
                // patchs could by empty in some case, such as a merge patch
                // https://gitee.com/jfinal/jfinal/pulls/40.diff
                // https://gitee.com/jfinal/jfinal/pulls/40.patch
                // https://gitee.com/jfinal/jfinal/pulls/40/commits
                String diffUrl = (String) pull.get("diff_url");
                String diff = httpGet2(diffUrl, "Try fetch pull request diff for {times} times, url {url}.");
                if (StringUtils.isBlank(diff)) {
                    System.err.println("Warning can not fetch patch: " + patchUrl);
                    continue;
                }
                pi = PatchHelper.parseDiff(new ByteArrayInputStream(ByteHelper.utf8(diffUrl)))
                        .date(date);
            }
            //
            String commitsJson = httpGet2(commitsUrl, "Try fetch pull request commits for {times} times, url {url}.");
            List<Map<String, Object>> commits = GsonHelper.toJsonList(commitsJson);
            if (commits == null || commits.isEmpty()) {
                System.out.println("commits is empty: " + commitsJson);
            }
            int commitIndex = 0, commitTotal = commits == null ? 0 : commits.size();
            PatchHelper.PatchSummaryInfo summaryInfo = new PatchHelper.PatchSummaryInfo();
            {
                summaryInfos.add(summaryInfo);
                summaryInfo.startRevision(startRevision)
                        .endRevision(endRevision)
                        .startCommit(startCommit)
                        .endCommit(endCommit)
                        .totalCommit(commitTotal)
                        .message(message)
                        .date(date)
                        .patch(patchContent);
            }
            if (pi != null && commits.size() > 0) {
                pi.commitIndex(1)
                        .commitTotal(1)
                        .endCommit((String) commits.get(0).get("sha"))
                        .startCommit((String) ((List<Map<String, Object>>) commits.get(commits.size() - 1).get("parents")).get(0).get("sha"))
                        .author((String) MapHelper.getByPath(commits.get(0), "commit/author/name"))
                        .email((String) MapHelper.getByPath(commits.get(0), "commit/author/email"))
                        .msg((String) MapHelper.getByPath(commits.get(0), "commit/message"))
                        .shortMsg(StringUtils.substringBefore(pi.msg(), "\n"))
                        .patch(pi.genPatch(pi.patch()));
                summaryInfo.patchs().add(pi);
                continue;//no need to process commits.
            }
            for (Map<String, Object> commit : commits) {
                commitIndex += 1;
                String parentCommit = (String) ((List<Map<String, Object>>) commit.get("parents")).get(0).get("sha");
                String currentCommit = (String) commit.get("sha");
                String msg = (String) MapHelper.getByPath(commit, "commit/message");
                String shortMsg = StringUtils.substringBefore(msg, "\n");
                String author = (String) MapHelper.getByPath(commit, "commit/author/name");
                String email = (String) MapHelper.getByPath(commit, "commit/author/email");
                int files = 0, insertions = 0, deletions = 0;
                String summary = "", patch = "";
                try {
                    String[] lines = StringUtils.split(patchContent, "\n");
                    String starts = "From " + currentCommit;
                    int mark = 0;
                    StringBuilder sb = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith(starts)) {
                            mark = 1;
                            continue;
                        }
                        if (mark == 1 && line.startsWith("---")) {
                            mark = 2;
                            continue;
                        }
                        if (mark != 2) {
                            continue;
                        }
                        if (mark == 2 && line.startsWith("diff ")) {
                            break;
                        }
                        sb.append(line).append("\n");
                        if (line.indexOf(" files changed") > 0 && (line.indexOf(" insertions") > 0 || line.indexOf(" deletions") > 0)) {
                            String[] counts = StringUtils.split(line, ",");
                            for (String count : counts) {
                                if (count.indexOf("files changed") > 0) {
                                    files = NumberHelper.getInt(StringUtils.substringBefore(count, "files changed").trim(), 0);
                                } else if (count.indexOf(" insertions") > 0) {
                                    insertions = NumberHelper.getInt(StringUtils.substringBefore(count, " insertions").trim(), 0);
                                } else if (count.indexOf(" deletions") > 0) {
                                    deletions = NumberHelper.getInt(StringUtils.substringBefore(count, " deletions").trim(), 0);
                                }
                            }
                        }
                    }
                    summary = sb.toString().trim();
                } catch (Exception e) {
                    System.err.println("Warning " + e.getMessage());
                }
                {
                    PatchHelper.PatchInfo patchInfo = new PatchHelper.PatchInfo()
                            .commitIndex(commitIndex)
                            .commitTotal(commitTotal)
                            .startCommit(parentCommit)
                            .endCommit(currentCommit)
                            .shortMsg(shortMsg)
                            .msg(msg)
                            .author(author)
                            .email(email)
                            .files(files)
                            .insertions(insertions)
                            .deletions(deletions)
                            .summary(summary)
                            .patch(patch);
                    summaryInfo.patchs().add(patchInfo);
                }
            }
        }
        String url = null, author = null;
        {
            Config config = repo.getConfig();
            String name = config.getString("user", null, "name");
            String email = config.getString("user", null, "email");
            if (name == null || email == null) {
                author = "unknown";
            } else {
                author = name + " <" + email + ">";
            }
            url = config.getString("remote", "origin", "url");
        }
        {
            File pullRequestFetch = new File(repo.getDirectory(), "pullrequest_fetch");
            pullRequestFetch.mkdirs();
            List<Map<String, Object>> summaries = new ArrayList<>();
            for (PatchHelper.PatchSummaryInfo psi : summaryInfos) {
                Map<String, Object> format = PatchHelper.format(psi, url, author, HitHelper.getAccountAddress(), HitHelper.getRsaPubKey());
                summaries.add(format);
                FileUtils.writeStringToFile(new File(pullRequestFetch, format.get("id") + ".patch"), psi.patch());
            }
            String json = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create().toJson(summaries);
            FileUtils.writeStringToFile(new File(pullRequestFetch, "patch-summary-info.json"), json);
        }
        return summaryInfos;
    }

    protected List<PatchHelper.PatchSummaryInfo> fetchPullRequestFromGiteeServer(Repository repo, String gitUrl, int maxPrSize) throws Exception {
        List<PatchHelper.PatchSummaryInfo> summaryInfos = new ArrayList<>();
        String[] split = StringUtils.split(gitUrl, "/");
        if (split.length < 2) {
            System.err.println("Git url is invalided.");
            return summaryInfos;
        }
        List<String> paths = Arrays.asList(split);
        Collections.reverse(paths);
        String repoName = StringUtils.remove(paths.get(0), ".git");
        String owner = paths.get(1);
        // pull url sample: https://gitee.com/api/v5/repos/jfinal/jfinal/pulls
        String pullUrl = FCS.get("https://gitee.com/api/v5/repos/{owner}/{repoName}/pulls?per_page=" + maxPrSize, owner, repoName).toString();
        String pullsJson = httpGet2(pullUrl, "Try fetch pull request for {times} times, url {url}.");
        List<Map<String, Object>> pulls = GsonHelper.toJsonList(pullsJson);
        if (pulls == null) {
            System.err.println("Can not fetch pull request from url: " + pullUrl);
            return summaryInfos;
        }
        for (Map<String, Object> pull : pulls) {
            System.out.println("Fetching pull request:" + pull.get("url"));
            // sample: https://gitee.com/jfinal/jfinal/pulls/40.patch
            String patchUrl = (String) pull.get("patch_url");
            // sample: https://gitee.com/api/v5/repos/jfinal/jfinal/pulls/40/commits
            String commitsUrl = (String) pull.get("commits_url");
            String startRevision = "refs/heads/" + MapHelper.getByPath(pull, "base/ref");
            String endRevision = "refs/heads/" + MapHelper.getByPath(pull, "head/ref");
            String startCommit = (String) MapHelper.getByPath(pull, "base/sha");
            String endCommit = (String) MapHelper.getByPath(pull, "head/sha");
            String message = (String) pull.get("body");
            //created_at:"2019-04-18T13:12:57+08:00"
            String dateStr = (String) pull.get("created_at");
            Date date = DateUtils.parseDate(dateStr, new String[]{"yyyy-MM-dd'T'HH:mm:ssXXX"});
            //
            String patchContent = httpGet2(patchUrl, "Try fetch pull request patch for {times} times, url {url}.");
            PatchHelper.PatchInfo pi = null;
            if (StringUtils.isBlank(patchContent)) {
                // patchs could by empty in some case, such as a merge patch
                // https://gitee.com/jfinal/jfinal/pulls/40.diff
                // https://gitee.com/jfinal/jfinal/pulls/40.patch
                // https://gitee.com/jfinal/jfinal/pulls/40/commits
                String diffUrl = (String) pull.get("diff_url");
                String diff = httpGet2(diffUrl, "Try fetch pull request diff for {times} times, url {url}.");
                if (StringUtils.isBlank(diff)) {
                    System.err.println("Warning can not fetch patch: " + patchUrl);
                    continue;
                }
                pi = PatchHelper.parseDiff(new ByteArrayInputStream(ByteHelper.utf8(diff)))
                        .date(date);
            }
            //
            String commitsJson = httpGet2(commitsUrl, "Try fetch pull request commits for {times} times, url {url}.");
            List<Map<String, Object>> commits = GsonHelper.toJsonList(commitsJson);
            int commitIndex = 0, commitTotal = commits.size();
            PatchHelper.PatchSummaryInfo summaryInfo = new PatchHelper.PatchSummaryInfo();
            {// create summary information
                summaryInfos.add(summaryInfo);
                summaryInfo.startRevision(startRevision)
                        .endRevision(endRevision)
                        .startCommit(startCommit)
                        .endCommit(endCommit)
                        .totalCommit(commitTotal)
                        .message(message)
                        .date(date)
                        .patch(patchContent);
            }
            if (pi != null && commits.size() > 0) {// patch is empty and fetch from diff.
                pi.commitIndex(1)
                        .commitTotal(1)
                        .endCommit((String) commits.get(0).get("sha"))
                        .startCommit((String) MapHelper.getByPath(commits.get(0), "parents/sha"))
                        .author((String) MapHelper.getByPath(commits.get(0), "commit/author/name"))
                        .email((String) MapHelper.getByPath(commits.get(0), "commit/author/email"))
                        .msg((String) MapHelper.getByPath(commits.get(0), "commit/message"))
                        .shortMsg(StringUtils.substringBefore(pi.msg(), "\n"))
                        .patch(pi.genPatch(pi.patch()));

                summaryInfo.patch(pi.patch());// update patch content.
                summaryInfo.patchs().add(pi);
                continue;//no need to process commits.
            }
            // patch is not empty
            for (Map<String, Object> commit : commits) {
                commitIndex += 1;
                String parentCommit = (String) MapHelper.getByPath(commit, "parents/sha");
                String currentCommit = (String) commit.get("sha");
                String msg = (String) MapHelper.getByPath(commit, "commit/message");
                String shortMsg = StringUtils.substringBefore(msg, "\n");
                String author = (String) MapHelper.getByPath(commit, "commit/author/name");
                String email = (String) MapHelper.getByPath(commit, "commit/author/email");
                int files = 0, insertions = 0, deletions = 0;
                String summary = "", patch = "";
                try {
                    String[] lines = StringUtils.split(patchContent, "\n");
                    String starts = "From " + currentCommit;
                    int mark = 0;
                    StringBuilder sb = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith(starts)) {
                            mark = 1;
                            continue;
                        }
                        if (mark == 1 && line.startsWith("---")) {
                            mark = 2;
                            continue;
                        }
                        if (mark != 2) {
                            continue;
                        }
                        if (mark == 2 && line.startsWith("diff ")) {
                            break;
                        }
                        sb.append(line).append("\n");
                        if (line.indexOf(" file changed") > 0 && (line.indexOf(" insertions") > 0 || line.indexOf(" deletions") > 0)) {
                            String[] counts = StringUtils.split(line, ",");
                            for (String count : counts) {
                                if (count.indexOf("file changed") > 0) {
                                    files = NumberHelper.getInt(StringUtils.substringBefore(count, "file changed").trim(), 0);
                                } else if (count.indexOf(" insertions") > 0) {
                                    insertions = NumberHelper.getInt(StringUtils.substringBefore(count, " insertions").trim(), 0);
                                } else if (count.indexOf(" deletions") > 0) {
                                    deletions = NumberHelper.getInt(StringUtils.substringBefore(count, " deletions").trim(), 0);
                                }
                            }
                        }
                    }
                    summary = sb.toString().trim();
                } catch (Exception e) {
                    System.err.println("Warning " + e.getMessage());
                }
                {// create patch information.
                    PatchHelper.PatchInfo patchInfo = new PatchHelper.PatchInfo()
                            .commitIndex(commitIndex)
                            .commitTotal(commitTotal)
                            .startCommit(parentCommit)
                            .endCommit(currentCommit)
                            .shortMsg(shortMsg)
                            .msg(msg)
                            .author(author)
                            .email(email)
                            .files(files)
                            .insertions(insertions)
                            .deletions(deletions)
                            .summary(summary)
                            .patch(patch);
                    summaryInfo.patchs().add(patchInfo);
                }
            }
        }
        String url = null, author = null;
        {
            Config config = repo.getConfig();
            String name = config.getString("user", null, "name");
            String email = config.getString("user", null, "email");
            if (name == null || email == null) {
                author = "unknown";
            } else {
                author = name + " <" + email + ">";
            }
            url = config.getString("remote", "origin", "url");
        }
        {
            File pullRequestFetch = new File(repo.getDirectory(), "pullrequest_fetch");
            pullRequestFetch.mkdir();
            List<Map<String, Object>> summaries = new ArrayList<>();
            for (PatchHelper.PatchSummaryInfo psi : summaryInfos) {
                Map<String, Object> format = PatchHelper.format(psi, url, author, HitHelper.getAccountAddress(), HitHelper.getRsaPubKey());
                summaries.add(format);
                FileUtils.writeStringToFile(new File(pullRequestFetch, format.get("id") + ".patch"), psi.patch());
            }
            String json = new GsonBuilder().setPrettyPrinting().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create().toJson(summaries);
            FileUtils.writeStringToFile(new File(pullRequestFetch, "patch-summary-info.json"), json);
        }
        return summaryInfos;
    }

    protected String httpGet2(String requestUrl, String tryMessage) {
        String content = "";
        String token = System.getProperty(PROP_TOKEN, "");
        for (int i = 0; i < 3; i++) {
            if (tryMessage != null && i > 0) {
                System.out.println(FCS.get(tryMessage, i, requestUrl));
            }
            HttpURLConnection connection = null;
            try {
                URL url = new URL(requestUrl);
                {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    //connection.setDoOutput(true);
                    //connection.setDoInput(true);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("Charset", "UTF-8");
                    connection.setRequestProperty("Accept", "*/*");
                    connection.setRequestProperty("User-Agent", "hit/1.0.0");
                    if (StringUtils.isNotBlank(token)) {
                        connection.setRequestProperty("Authorization", "token " + token);
                    }
                    connection.setConnectTimeout(30 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.connect();
                }
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                System.out.println(responseCode + " " + responseMessage);
                content = IOUtils.toString(connection.getInputStream(), "UTF-8");
                if (responseCode == 200) {
                    return content;
                }
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

    public Hit hit() {
        return hit;
    }

    public MigrateCommand hit(Hit hit) {
        this.hit = hit;
        return this;
    }

    public String uri() {
        return uri;
    }

    public MigrateCommand uri(String uri) {
        this.uri = uri;
        return this;
    }

    public boolean autoRename() {
        return autoRename;
    }

    public MigrateCommand autoRename(boolean autoRename) {
        this.autoRename = autoRename;
        return this;
    }

    public String name() {
        return name;
    }

    public MigrateCommand name(String name) {
        this.name = name;
        return this;
    }

    public String token() {
        return token;
    }

    public MigrateCommand token(String token) {
        this.token = token;
        return this;
    }

    public int maxPrSize() {
        return maxPrSize;
    }

    public MigrateCommand maxPrSize(int maxPrSize) {
        this.maxPrSize = maxPrSize;
        return this;
    }
}
