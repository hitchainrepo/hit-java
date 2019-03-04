/*
 * Copyright (C) 2008, Shawn O. Pearce <spearce@spearce.org>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.eclipse.jgit.transport;

import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Repository;
import org.hitchain.core.HitIPFSStorage;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Transport over the non-Git aware Amazon S3 protocol.
 * <p>
 * This transport communicates with the Amazon S3 servers (a non-free commercial
 * hosting service that users must subscribe to). Some users may find transport
 * to and from S3 to be a useful backup service.
 * <p>
 * The transport does not require any specialized Git support on the remote
 * (server side) repository, as Amazon does not provide any such support.
 * Repository files are retrieved directly through the S3 API, which uses
 * extended HTTP/1.1 semantics. This make it possible to read or write Git data
 * from a remote repository that is stored on S3.
 * <p>
 * Unlike the HTTP variant (see
 * {@link TransportHttp}) we rely upon being able to
 * list objects in a bucket, as the S3 API supports this function. By listing
 * the bucket contents we can avoid relying on <code>objects/info/packs</code>
 * or <code>info/refs</code> in the remote repository.
 * <p>
 * Concurrent pushing over this transport is not supported. Multiple concurrent
 * push operations may cause confusion in the repository state.
 *
 * @see WalkFetchConnection
 * @see WalkPushConnection
 */
public class TransportHit extends HttpTransport implements WalkTransport {
    public static final TransportProtocol PROTO_HTTP = new TransportProtocol() {
        private String[] schemeNames = {"http", "https"};

        private Set<String> schemeSet = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(schemeNames)));

        public String getName() {
            return JGitText.get().transportProtoHTTP;
        }

        public Set<String> getSchemes() {
            return schemeSet;
        }

        public Set<URIishField> getRequiredFields() {
            return Collections.unmodifiableSet(EnumSet.of(URIishField.HOST, URIishField.PATH));
        }

        public Set<URIishField> getOptionalFields() {
            return Collections.unmodifiableSet(EnumSet.of(URIishField.USER, URIishField.PASS, URIishField.PORT));
        }

        public int getDefaultPort() {
            return 80;
        }

        public Transport open(URIish uri, Repository local, String remoteName) throws NotSupportedException {
            return new TransportHit(local, uri);
        }

        //TODO
        //        public Transport open(URIish uri) throws NotSupportedException {
        //            return new TransportHit(uri);
        //        }
    };
    public static String HIT_SCHEME = "hit";
    public static final TransportProtocol PROTO_HIT = new TransportProtocol() {
        public String getName() {
            return "HitChain";
        }

        public Set<String> getSchemes() {
            return Collections.singleton(HIT_SCHEME);
        }

        public Set<URIishField> getRequiredFields() {
            return Collections.unmodifiableSet(EnumSet.of(URIishField.USER, URIishField.HOST, URIishField.PATH));
        }

        public Set<URIishField> getOptionalFields() {
            return Collections.unmodifiableSet(EnumSet.of(URIishField.PASS));
        }

        public Transport open(URIish uri, Repository local, String remoteName) throws NotSupportedException {
            return new TransportHit(local, uri);
        }
    };

    /**
     * User information necessary to connect to S3.
     */
    HitIPFSStorage hit;

    /**
     * Bucket the remote repository is stored in.
     */
    //String bucket;

    /**
     * Key prefix which all objects related to the repository start with.
     * <p>
     * The prefix does not start with "/".
     * <p>
     * The prefix does not end with "/". The trailing slash is stripped during
     * the constructor if a trailing slash was supplied in the URIish.
     * <p>
     * All files within the remote repository start with
     * <code>keyPrefix + "/"</code>.
     */
    private String keyPrefix;

    protected TransportHit(Repository local, URIish uri) throws NotSupportedException {
        super(local, uri);
        //Properties props = loadProperties();
        File projectDir = local.getDirectory();
        //if (!props.containsKey("tmpdir") && directory != null) {
        //    props.put("tmpdir", directory.getPath());
        //}

        hit = new HitIPFSStorage(projectDir);
        //bucket = uri.getHost();

        String p = uri.getPath();
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        keyPrefix = p;
    }

//    private static Properties loadPropertiesFile(File propsFile) throws NotSupportedException {
//        try {
//            return AmazonS3.properties(propsFile);
//        } catch (IOException e) {
//            throw new NotSupportedException(MessageFormat.format(JGitText.get().cannotReadFile, propsFile), e);
//        }
//    }

//    private Properties loadProperties() throws NotSupportedException {
//        if (local.getDirectory() != null) {
//            File propsFile = new File(local.getDirectory(), uri.getUser());
//            if (propsFile.isFile()) {
//                return loadPropertiesFile(propsFile);
//            }
//        }
//
//        File propsFile = new File(local.getFS().userHome(), uri.getUser());
//        if (propsFile.isFile()) {
//            return loadPropertiesFile(propsFile);
//        }
//
//        Properties props = new Properties();
//        String user = uri.getUser();
//        String pass = uri.getPass();
//        if (user != null && pass != null) {
//            props.setProperty("accesskey", user);
//            props.setProperty("secretkey", pass);
//        } else {
//            throw new NotSupportedException(MessageFormat.format(JGitText.get().cannotReadFile, propsFile));
//        }
//        return props;
//    }

    /**
     * {@inheritDoc}
     */
    public FetchConnection openFetch() throws TransportException {
        HitIPFSDatabase c = new HitIPFSDatabase(hit, keyPrefix + "/objects");
        WalkFetchConnection r = new WalkFetchConnection(this, c);
        r.available(c.readAdvertisedRefs());
        return r;
    }

    /**
     * {@inheritDoc}
     */
    public PushConnection openPush() throws TransportException {
        HitIPFSDatabase c = new HitIPFSDatabase(hit,keyPrefix + "/objects");
        WalkPushConnection r = new WalkPushConnection(this, c);
        r.available(c.readAdvertisedRefs());
        return r;
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        // No explicit connections are maintained.
    }
}
