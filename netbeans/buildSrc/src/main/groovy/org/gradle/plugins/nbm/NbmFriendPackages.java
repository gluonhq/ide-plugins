/*
 * Copyright (c) 2018, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gradle.plugins.nbm;

import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class NbmFriendPackages {
    private final List<PackageNameGenerator> packageList;

    public NbmFriendPackages() {
        this.packageList = new LinkedList<>();
    }

    private static void getPackagesInDir(String packageName, File currentDir, List<String> result) {
        boolean hasFile = false;
        for (File file: currentDir.listFiles()) {
            if (file.isDirectory()) {
                String lastPart = file.getName();
                String subPackageName = packageName.isEmpty()
                        ? lastPart
                        : packageName + "." + lastPart;
                getPackagesInDir(subPackageName, file, result);
            }
            else if (!hasFile && file.isFile()) {
                hasFile = true;
            }
        }

        if (hasFile) {
            result.add(packageName);
        }
    }

    private static void findAllPackages(File sourceRoot, String packageName, List<String> result) {
        String[] pathParts = packageName.split(Pattern.quote("."));
        File startDir = sourceRoot;
        for (String part: pathParts) {
            startDir = new File(startDir, part);
        }

        if (!startDir.isDirectory()) {
            return;
        }

        getPackagesInDir(packageName, startDir, result);
    }

    private static void findAllPackages(SourceSet sourceSet, String packageName, List<String> result) {
        for (File sourceRoot: sourceSet.getAllJava().getSrcDirs()) {
            findAllPackages(sourceRoot, packageName, result);
        }
    }

    public void addWithSubPackages(final SourceSet sourceSet, final String packageName) {
        Objects.requireNonNull(sourceSet, "sourceSet");
        Objects.requireNonNull(packageName, "packageName");

        packageList.add(new PackageNameGenerator() {
            @Override
            public void findPackages(List<String> result) {
                findAllPackages(sourceSet, packageName, result);
            }
        });
    }

    public void add(final String packageName) {
        Objects.requireNonNull(packageName, "packageName");

        packageList.add(new PackageNameGenerator() {
            @Override
            public void findPackages(List<String> result) {
                result.add(packageName);
            }
        });
    }

    public List<String> getPackageList() {
        List<String> result = new LinkedList<>();
        for (PackageNameGenerator currentNames: packageList) {
            currentNames.findPackages(result);
        }
        return result;
    }

    public List<String> getPackageListPattern() {
        List<String> packages = getPackageList();
        List<String> result = new ArrayList<>(packages.size());
        for (String packageName: packages) {
            result.add(packageName + ".*");
        }
        return result;
    }

    private interface PackageNameGenerator {
        public void findPackages(List<String> result);
    }
}
