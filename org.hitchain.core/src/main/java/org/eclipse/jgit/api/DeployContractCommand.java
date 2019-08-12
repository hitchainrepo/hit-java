/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.hitchain.contract.api.HitRepositoryContractEthereumApi;
import org.hitchain.contract.ethereum.HitRepositoryContractEthereumService;
import org.hitchain.hit.util.HitHelper;

import java.util.concurrent.Callable;

/**
 * AmCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class DeployContractCommand implements Callable<String> {

    @Override
    public String call() throws Exception {
        HitRepositoryContractEthereumApi api = HitRepositoryContractEthereumService.getApi();
        String contract = api.deployContract(HitHelper.getAccountPriKeyWithPasswordInput(), HitHelper.getGasDeploy(), HitHelper.getGasDeployGwei());
        return contract;
    }
}
