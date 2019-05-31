/*******************************************************************************
 * Copyright (c) 2019-05-30 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.contract.ethereum;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * InputHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-05-30
 * auto generate by qdp.
 */
public class InputHelper {

    public static String getContent(String title) {
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        new Thread(new Runnable() {
            public void run() {
                final Frame frame = new Frame("Input");
                final TextArea field = new TextArea();
                final Button ok = new Button("OK");
                frame.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));
                {
                    frame.add(new Label(title));
                    {
                        //field.setSize(field.getWidth(), 200);
                        field.setColumns(50);
                        field.setRows(2);
                        frame.add(field);
                    }
                    {
                        ok.setActionCommand("ok");
                        frame.add(ok);
                        ok.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                String content = field.getText();
                                frame.setVisible(false);
                                queue.offer(content);
                                frame.dispose();
                            }
                        });
                    }
                }
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        String content = field.getText();
                        frame.setVisible(false);
                        queue.offer(content);
                        frame.dispose();
                    }
                });
                frame.pack();
                frame.setVisible(true);
            }
        }).start();
        try {
            return queue.take();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
