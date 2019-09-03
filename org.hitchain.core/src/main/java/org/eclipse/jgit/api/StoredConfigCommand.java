/*******************************************************************************
 * Copyright (c) 2019-07-12 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package org.eclipse.jgit.api;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;

import java.util.concurrent.Callable;

/**
 * AmCommand
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-07-12
 * auto generate by qdp.
 */
public class StoredConfigCommand implements Callable<String> {

    protected String section;
    protected String subSection;
    protected String name;
    protected String value;
    //
    protected String type;
    protected Repository repository;

    public StoredConfigCommand get(String section, String subSection, String name) {
        return type("get").section(section).subSection(subSection).name(name);
    }

    public StoredConfigCommand set(String section, String subSection, String name, String value) {
        return type("set").section(section).subSection(subSection).name(name).value(value);
    }

    public StoredConfigCommand remove(String section, String subSection, String name) {
        return type("remove").section(section).subSection(subSection).name(name).value(value);
    }

    public StoredConfigCommand getRepositoryAddress() {
        return type("get").section("remote").subSection("origin").name("url");
    }

    public StoredConfigCommand getUserName() {
        return type("get").section("user").subSection(null).name("name");
    }


    public StoredConfigCommand getUserEmail() {
        return type("get").section("user").subSection(null).name("email");
    }

    public StoredConfigCommand getRemoteUrl() {
        return type("get").section("remote").subSection("origin").name("url");
    }

    @Override
    public String call() throws Exception {
        StoredConfig config = repository().getConfig();
        config.load();
        if ("set".equals(type())) {
            config.setString(section(), subSection(), name(), value());
            config.save();
            return "OK";
        }

        if ("remove".equals(type())) {
            config.unset(section(), subSection(), name());
            config.save();
            return "OK";
        }
        if ("get".equals(type())) {
            return config.getString(section(), subSection(), name());
        }
        return null;
    }

    public String value() {
        return value;
    }

    public StoredConfigCommand value(String value) {
        this.value = value;
        return this;
    }

    public String name() {
        return name;
    }

    public StoredConfigCommand name(String name) {
        this.name = name;
        return this;
    }

    public String subSection() {
        return subSection;
    }

    public StoredConfigCommand subSection(String subSection) {
        this.subSection = subSection;
        return this;
    }

    public String section() {
        return section;
    }

    public StoredConfigCommand section(String section) {
        this.section = section;
        return this;
    }

    public String type() {
        return type;
    }

    public StoredConfigCommand type(String type) {
        this.type = type;
        return this;
    }

    public Repository repository() {
        return repository;
    }

    public StoredConfigCommand repository(Repository repository) {
        this.repository = repository;
        return this;
    }
}
