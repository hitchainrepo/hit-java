/*******************************************************************************
 * Copyright (c) 2019-07-03 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.hitchain.hit.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.api.errors.PatchFormatException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.FormatError;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.patch.Patch;
import org.eclipse.jgit.util.IO;

import java.io.*;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * ApplyHelper
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-03
 * auto generate by qdp.
 */
public class ApplyHelper {

    public static void main(String[] args) throws Exception {
        //String path = "/Users/zhaochen/Desktop/temppath/mergepr/migrate/.git/pullrequest/pullrequest.patch";
        String path = "/Users/zhaochen/Desktop/1249.patch";
        List<PatchHelper.PatchFileInfo> infos = PatchHelper.parsePatch(new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(path))));
        System.out.println(infos);
        for (PatchHelper.PatchFileInfo pfi : infos) {
            Patch patch = new Patch();
            patch.parse(new ByteArrayInputStream(pfi.diff().getBytes("UTF-8")));
            System.out.println(patch.getErrors());
        }
    }

    public static ApplyCommand createApply(Repository repository) {
        return new ApplyCommand(repository);
    }

    /**
     * Apply a patch to files and/or to the index.
     *
     * @see <a href="http://www.kernel.org/pub/software/scm/git/docs/git-apply.html"
     * >Git documentation about apply</a>
     * @since 2.0
     */
    public static class ApplyCommand extends GitCommand<ApplyResult> {

        /**
         * arg: --ignore-space-change
         * ignore all space change.
         */
        private boolean ignoreSpaceChange;
        /**
         * arg: --ignore-white-sapce
         * ignore white space at line start and end.
         */
        private boolean ignoreWhitespace;
        /**
         * arg: --force-merge
         * force
         */
        private boolean forceMergeLine;

        private InputStream in;

        /**
         * Constructs the command if the patch is to be applied to the index.
         *
         * @param repo
         */
        ApplyCommand(Repository repo) {
            super(repo);
        }

        private static boolean isChanged(List<String> ol, List<String> nl) {
            if (ol.size() != nl.size()) {
                return true;
            }
            for (int i = 0; i < ol.size(); i++) {
                if (!ol.get(i).equals(nl.get(i))) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Set patch
         *
         * @param in the patch to apply
         * @return this instance
         */
        public ApplyCommand setPatch(InputStream in) {
            checkCallable();
            this.in = in;
            return this;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Executes the {@code ApplyCommand} command with all the options and
         * parameters collected by the setter methods (e.g.
         * {@link #setPatch(InputStream)} of this class. Each instance of this class
         * should only be used for one invocation of the command. Don't call this
         * method twice on an instance.
         */
        @Override
        public ApplyResult call() throws GitAPIException, PatchFormatException, PatchApplyException {
            checkCallable();
            ApplyResult r = new ApplyResult();
            try {
                final Patch p = new Patch();
                try {
                    p.parse(in);
                } finally {
                    in.close();
                }
                if (!p.getErrors().isEmpty()) {
                    boolean hasError = false;
                    for (FormatError fe : p.getErrors()) {
                        if (fe.getSeverity() == FormatError.Severity.ERROR) {
                            hasError = true;
                        }
                        System.out.println(fe);
                    }
                    if (hasError) {
                        throw new PatchFormatException(p.getErrors());
                    }
                }
                for (FileHeader fh : p.getFiles()) {
                    DiffEntry.ChangeType type = fh.getChangeType();
                    File f = null;
                    if (DiffEntry.ChangeType.ADD == type) {
                        f = getFile(fh.getNewPath(), true);
                        apply(f, fh);
                    } else if (DiffEntry.ChangeType.MODIFY == type) {
                        f = getFile(fh.getOldPath(), false);
                        apply(f, fh);
                    } else if (DiffEntry.ChangeType.DELETE == type) {
                        f = getFile(fh.getOldPath(), false);
                        if (!f.delete()) {
                            throw new PatchApplyException(MessageFormat.format(JGitText.get().cannotDeleteFile, f));
                        }
                    } else if (DiffEntry.ChangeType.RENAME == type) {
                        f = getFile(fh.getOldPath(), false);
                        File dest = getFile(fh.getNewPath(), false);
                        try {
                            org.eclipse.jgit.util.FileUtils.rename(f, dest, StandardCopyOption.ATOMIC_MOVE);
                        } catch (IOException e) {
                            throw new PatchApplyException(MessageFormat.format(JGitText.get().renameFileFailed, f, dest), e);
                        }
                    } else if (DiffEntry.ChangeType.COPY == type) {
                        f = getFile(fh.getOldPath(), false);
                        byte[] bs = IO.readFully(f);
                        FileOutputStream fos = new FileOutputStream(getFile(fh.getNewPath(), true));
                        try {
                            fos.write(bs);
                        } finally {
                            fos.close();
                        }
                    }
                    r.addUpdatedFile(f);
                }
            } catch (IOException e) {
                throw new PatchApplyException(MessageFormat.format(JGitText.get().patchApplyException, e.getMessage()), e);
            }
            setCallable(false);
            return r;
        }

        private File getFile(String path, boolean create) throws PatchApplyException {
            File f = new File(getRepository().getWorkTree(), path);
            if (create)
                try {
                    File parent = f.getParentFile();
                    org.eclipse.jgit.util.FileUtils.mkdirs(parent, true);
                    org.eclipse.jgit.util.FileUtils.createNewFile(f);
                } catch (IOException e) {
                    throw new PatchApplyException(MessageFormat.format(JGitText.get().createNewFileFailed, f), e);
                }
            return f;
        }

        /**
         * @param f
         * @param fh
         * @throws IOException
         * @throws PatchApplyException
         */
        private void apply(File f, FileHeader fh) throws IOException, PatchApplyException {
            RawText rt = new RawText(f);
            List<String> oldLines = new ArrayList<>(rt.size());
            for (int i = 0; i < rt.size(); i++) {
                oldLines.add(rt.getString(i));
            }
            List<String> newLines = new ArrayList<>(oldLines);
            for (HunkHeader hh : fh.getHunks()) {
                byte[] b = new byte[hh.getEndOffset() - hh.getStartOffset()];
                System.arraycopy(hh.getBuffer(), hh.getStartOffset(), b, 0, b.length);
                RawText hrt = new RawText(b);

                List<String> hunkLines = new ArrayList<>(hrt.size());
                for (int i = 0; i < hrt.size(); i++) {
                    hunkLines.add(hrt.getString(i));
                }
                int pos = 0;
                for (int j = 1; j < hunkLines.size(); j++) {
                    String hunkLine = hunkLines.get(j);
                    char firstChar = hunkLine.charAt(0);
                    int newStartLine = hh.getNewStartLine();
                    String removedFirstChar = hunkLine.substring(1);
                    if (' ' == firstChar) {
                        if (!equalsLine(newLines.get(newStartLine - 1 + pos), removedFirstChar)) {
                            throw new PatchApplyException(MessageFormat.format(JGitText.get().patchApplyException, hh));
                        }
                        pos++;
                    } else if ('-' == firstChar) {
                        if (newStartLine == 0) {
                            newLines.clear();
                        } else {
                            if (!equalsLine(newLines.get(newStartLine - 1 + pos), removedFirstChar)) {
                                throw new PatchApplyException(MessageFormat.format(JGitText.get().patchApplyException, hh));
                            }
                            newLines.remove(newStartLine - 1 + pos);
                        }
                    } else if ('+' == firstChar) {
                        newLines.add(newStartLine - 1 + pos, removedFirstChar);
                        pos++;
                    }
                }
            }
            if (!isNoNewlineAtEndOfFile(fh)) {
                newLines.add(""); //$NON-NLS-1$
            }
            if (!rt.isMissingNewlineAtEnd()) {
                oldLines.add(""); //$NON-NLS-1$
            }
            if (!isChanged(oldLines, newLines)) {
                return; // don't touch the file
            }
            StringBuilder sb = new StringBuilder();
            for (String l : newLines) {
                // don't bother handling line endings - if it was windows, the \r is
                // still there!
                sb.append(l).append('\n');
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            try (Writer fw = new OutputStreamWriter(new FileOutputStream(f), UTF_8)) {
                fw.write(sb.toString());
            }

            getRepository().getFS().setExecute(f, fh.getNewMode() == FileMode.EXECUTABLE_FILE);
        }

        private boolean isNoNewlineAtEndOfFile(FileHeader fh) {
            HunkHeader lastHunk = fh.getHunks().get(fh.getHunks().size() - 1);
            RawText lhrt = new RawText(lastHunk.getBuffer());
            return lhrt.getString(lhrt.size() - 1).equals("\\ No newline at end of file"); //$NON-NLS-1$
        }

        private boolean equalsLine(String str1, String str2) {
            if (ignoreSpaceChange()) {
                str1 = StringUtils.replace(StringUtils.trim(str1), " ", "");
                str2 = StringUtils.replace(StringUtils.trim(str2), " ", "");
                return StringUtils.equals(str1, str2);
            }
            if (ignoreWhitespace()) {
                str1 = StringUtils.trim(str1);
                str2 = StringUtils.trim(str2);
                return StringUtils.equals(str1, str2);
            }
            if (forceMergeLine()) {
                return true;
            }
            return StringUtils.equals(str1, str2);
        }

        public boolean ignoreSpaceChange() {
            return ignoreSpaceChange;
        }

        public ApplyCommand ignoreSpaceChange(boolean ignoreSpaceChange) {
            this.ignoreSpaceChange = ignoreSpaceChange;
            return this;
        }

        public boolean ignoreWhitespace() {
            return ignoreWhitespace;
        }

        public ApplyCommand ignoreWhitespace(boolean ignoreWhitespace) {
            this.ignoreWhitespace = ignoreWhitespace;
            return this;
        }

        public boolean forceMergeLine() {
            return forceMergeLine;
        }

        public ApplyCommand forceMergeLine(boolean forceMergeLine) {
            this.forceMergeLine = forceMergeLine;
            return this;
        }
    }
}
