/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.hitchain.hit.util.GitHelper;
import org.hitchain.hit.util.HitHelper;

import java.util.Date;
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
    protected String uri;
    protected String name;
    protected boolean autoRename;
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
        HitHelper.migrateWithPullRequest(uri(), repositoryName);
        System.out.println("Migrate repository success.");
        return null;
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
}
