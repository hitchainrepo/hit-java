/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.hitchain.hit.util.HitHelper;

/**
 * HitConfigCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class MigrateCommand extends TransportCommand<MigrateCommand, Hit> {
    protected Hit hit;
    protected String uri;

    public MigrateCommand() {
        super(null);
    }

    @Override
    public Hit call() throws GitAPIException {
        HitHelper.migrateWithPullRequest(uri());
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
}
