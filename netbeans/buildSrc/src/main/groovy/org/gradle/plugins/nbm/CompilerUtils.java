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

import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class CompilerUtils {
    private static final String JAVAC_VERSION_PREFIX = "javac";

    private static String tryGetCompilerVersion(ForkOptions forkOptions) {
        String executable = forkOptions.getExecutable();
        if (executable == null || executable.isEmpty()) {
            return null;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(executable, "-version");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        try {
            Process process = processBuilder.start();

            InputStream input = process.getErrorStream();
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(input, "ISO-8859-1"), 4096);

            String result = null;
            String line = inputReader.readLine();
            while (line != null) {
                if (line.startsWith(JAVAC_VERSION_PREFIX)) {
                    result = line.substring(JAVAC_VERSION_PREFIX.length()).trim();
                    // Continue reading to prevent dead-locking the process if it
                    // prints something else.
                }
                line = inputReader.readLine();
            }
            return result;
        } catch (IOException ex) {
            return null;
        }
    }

    public static String tryGetCompilerVersion(JavaCompile compileTask) {
        CompileOptions options = compileTask.getOptions();
        if (options.isFork()) {
            ForkOptions forkOptions = options.getForkOptions();
            return tryGetCompilerVersion(forkOptions);
        }
        else {
            return System.getProperty("java.version");
        }
    }

    private CompilerUtils() {
        throw new AssertionError();
    }

}
