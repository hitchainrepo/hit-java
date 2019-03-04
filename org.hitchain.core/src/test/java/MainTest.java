/*******************************************************************************
 * Copyright (c) 2019-01-16 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * MainTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-16
 * auto generate by qdp.
 */
public class MainTest {
    public static void main(String[] args) throws Exception {
        File dir = new File("/Users/zhaochen/Desktop/temppath");
        System.setProperty("git_work_tree", "/Users/zhaochen/Desktop/temppath/aatest");
        try {
            System.setProperty("user.dir", dir.getAbsolutePath());
            gitClone();
            File file = new File(new File(dir, "aatest"), "aaa-1547624216414.txt");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(("hello world" + System.currentTimeMillis()).getBytes());
            fos.close();
            exec("git add "+file.getName());
            exec("git commit -m 'hello-"+System.currentTimeMillis()+"'");
            exec("git push");
        } finally {
            FileUtils.deleteDirectory(new File(dir, "aatest"));
        }
    }

    public static void exec(String... args) throws Exception {
        org.eclipse.jgit.pgm.CLIGitCommand.main(args);
    }

    public static void gitClone() throws Exception {
        org.eclipse.jgit.pgm.Main.main(new String[]{
                "clone", "http://localhost:8080/git/root/aatest.git"
        });
    }
}
