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

import groovy.lang.Closure;
import groovy.lang.GString;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class EvaluateUtils {
    public static String asString(Object obj) {
        if (obj instanceof Closure) {
            return asString(((Closure<?>)obj).call());
        }

        return obj != null ? obj.toString() : null;
    }

    public static Path asPath(Object obj) {
        if (obj instanceof Path || obj == null) {
            return (Path)obj;
        }
        if (obj instanceof File) {
            return ((File)obj).toPath();
        }
        if (obj instanceof URI) {
            return Paths.get((URI)obj);
        }
        if (obj instanceof String || obj instanceof GString) {
            return Paths.get(obj.toString());
        }
        if (obj instanceof Closure) {
            return asPath(((Closure<?>)obj).call());
        }

        throw new IllegalArgumentException("Unexpected file type: " + obj.getClass().getName());
    }

    private EvaluateUtils() {
        throw new AssertionError();
    }

}
