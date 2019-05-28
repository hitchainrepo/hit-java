package org.hitchain.core; /*******************************************************************************
 * Copyright (c) 2019-01-16 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 * Auto Generate By foreveross.com Quick Deliver Platform. 
 ******************************************************************************/

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * MainTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-01-16
 * auto generate by qdp.
 */
public class MainTest {
    public static void main(String[] args) throws Exception {
        //gitPush();
        FileUtils.deleteQuietly(new File("/Users/zhaochen/Desktop/temppath/hello"));
        gitClone();
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
        System.setProperty("user.dir", "/Users/zhaochen/Desktop/temppath");
        org.eclipse.jgit.pgm.Main.main(new String[]{
                "clone", "hit://0xbe8023ef17357028d271ea915100a44b19e726d1.git"
        });
    }
}
