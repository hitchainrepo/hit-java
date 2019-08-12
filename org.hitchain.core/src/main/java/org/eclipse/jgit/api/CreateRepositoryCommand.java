/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.hitchain.contract.api.ContractApi;
import org.hitchain.hit.util.HitHelper;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * AmCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class CreateRepositoryCommand implements Callable<String> {

    protected String name;
    protected boolean autoRename;

    public boolean autoRename() {
        return autoRename;
    }

    public CreateRepositoryCommand autoRename(boolean autoRename) {
        this.autoRename = autoRename;
        return this;
    }

    public String name() {
        return name;
    }

    public CreateRepositoryCommand name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String call() throws Exception {
        boolean isRepositoryExists = Hit.util().readId(name()) > 0;
        if (isRepositoryExists && !autoRename()) {
            throw new RuntimeException("Can not add repository, repository name is exists: " + name());
        }
        if (isRepositoryExists) {
            for (int i = 0; i < 100; i++) {
                String newName = name() + "-" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
                if (Hit.util().readId(newName) > 0) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                    }
                } else {
                    name(newName);
                    break;
                }
            }
        }
        String result = Hit.util().addRepository(name());
        if (ContractApi.isError(result)) {
            throw new RuntimeException("Can not add repository: " + result);
        }
        int id = Hit.util().readId(name());
        if (id < 1) {
            throw new RuntimeException("Can not add repository by name: " + name());
        }
        return HitHelper.getContract() + "-" + id;
    }
}
