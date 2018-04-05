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
package com.gluonhq.plugin.intellij.util;

import com.intellij.lang.java.lexer.JavaLexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.NotNull;

public class Utils {

    @NotNull
    public static String getDefaultPackageNameByModuleName(@NotNull String moduleName) {
        return "com." + Utils.toIdentifier(moduleName);
    }

    @NotNull
    public static String toIdentifier(@NotNull String s) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0, n = s.length(); i < n; i++) {
            final char c = s.charAt(i);

            if (Character.isJavaIdentifierPart(c)) {
                if (i == 0 && !Character.isJavaIdentifierStart(c)) {
                    result.append('_');
                }
                result.append(c);
            } else {
                result.append('_');
            }
        }
        return result.toString();
    }

    public static boolean isJavaIdentifier(@NotNull String candidate) {
        return StringUtil.isJavaIdentifier(candidate) && !JavaLexer.isKeyword(candidate, LanguageLevel.JDK_1_8);
    }

    public static boolean isValidPackageName(String candidate) {
        String[] tokens = candidate.split("\\.");
        for (String token : tokens) {
            if ("".equals(token) || !Utils.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

}

