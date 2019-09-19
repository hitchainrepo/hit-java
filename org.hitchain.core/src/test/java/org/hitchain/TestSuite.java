/*******************************************************************************
 * Copyright (c) 2019-09-19 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain;

import org.hitchain.hit.api.DecryptableFileWrapperTest;
import org.hitchain.hit.api.EncryptableFileWrapperTest;
import org.hitchain.hit.api.ProjectInfoFileTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TestSuite
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-09-19
 * auto generate by qdp.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DecryptableFileWrapperTest.class,
        EncryptableFileWrapperTest.class,
        ProjectInfoFileTest.class
})
public class TestSuite {
}
