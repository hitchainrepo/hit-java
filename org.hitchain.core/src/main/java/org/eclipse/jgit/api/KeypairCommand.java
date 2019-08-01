/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.hitchain.hit.util.HitHelper;

import java.util.concurrent.Callable;

/**
 * HitConfigCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class KeypairCommand implements Callable<Hit> {
    protected Hit hit;
    protected String type;

    @Override
    public Hit call() throws Exception {
        if (HitHelper.ACTION_add.equals(type())) {
            if (hit().projectInfoFile().isPrivate()) {
                System.out.println("Repository already has keypair.");
                return hit();
            }
            HitHelper.addKeyPair(hit().getRepository().getDirectory(), hit().projectInfoFile());
            System.out.println("Repository keypair has updated.");
            return hit();
        } else if (HitHelper.ACTION_remove.equals(type())) {
            if (!hit().projectInfoFile().isPrivate()) {
                System.out.println("Repository is public without keypair.");
                return hit();
            }
            HitHelper.removeKeyPair(hit().getRepository().getDirectory(), hit().projectInfoFile());
            System.out.println("Repository keypair has removed.");
            return hit();
        } else if (HitHelper.ACTION_renew.equals(type())) {
            HitHelper.addKeyPair(hit().getRepository().getDirectory(), hit().projectInfoFile());
            System.out.println("Repository keypair has renewed.");
            return hit();
        }
        return hit();
    }

    public String type() {
        return type;
    }

    public KeypairCommand type(String type) {
        this.type = type;
        return this;
    }

    public KeypairCommand add() {
        return type(HitHelper.ACTION_add);
    }

    public KeypairCommand remove() {
        return type(HitHelper.ACTION_remove);
    }

    public KeypairCommand renew() {
        return type(HitHelper.ACTION_renew);
    }

    public Hit hit() {
        return hit;
    }

    public KeypairCommand hit(Hit hit) {
        this.hit = hit;
        return this;
    }
}
