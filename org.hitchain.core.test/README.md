JGit
====

### 1.Build from source

    mvn clean package

### 2.Unzip and config

    cd target
    tar -xf tc-jgit-project-1.0.0-assembly.tar.gz
    cd tc-jgit-project-1.0.0
    # change jgit.base-path to your git directory
    vi conf/conf.properties
    
### 3.Running

    ./bin/start

