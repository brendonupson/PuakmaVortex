#!/bin/bash
ECLIPSE_INSTALLED_PATH="/d/Downloads/eclipse-committers-2020-09-R-win32-x86_64/eclipse/plugins/"
ECLIPSE_NEW_BUILD="/d/Code/eclipse/test/test2/eclipse/plugins"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.analyzers-common_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.core_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.misc_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
cp "$ECLIPSE_INSTALLED_PATH/org.apache.lucene.queryparser_7.5.0.v20181003-1532.jar" "$ECLIPSE_NEW_BUILD"
