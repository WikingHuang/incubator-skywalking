/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.apm.plugin.configurable.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.apache.skywalking.apm.agent.core.plugin.match.ClassMatch;
import org.apache.skywalking.apm.plugin.configurable.util.HierarchyAndNameMatch;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Created by weijie.huang on 2018/9/12
 */
public class RunnableInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String[] PARENT_CLASS = {"java.lang.Runnable"};
    private static final String[] CLASS_NAME_CONTAINS = {"slick", "s.com.eoi", "scala.concurrent"};
    private static final String INIT_METHOD_INTERCEPTOR = "org.apache.skywalking.apm.plugin.configurable.CallableOrRunnableConstructInterceptor";
    private static final String CALL_METHOD_INTERCEPTOR = "org.apache.skywalking.apm.plugin.configurable.CallableOrRunnableInvokeInterceptor";
    private static final String CALL_METHOD_NAME = "call";
    private static final String RUN_METHOD_NAME = "run";

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[]{
            new ConstructorInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getConstructorMatcher() {
                    return any();
                }

                @Override
                public String getConstructorInterceptor() {
                    return INIT_METHOD_INTERCEPTOR;
                }
            }
        };
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[]{
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return (named(CALL_METHOD_NAME).and(takesArguments(0)))
                            .or(named(RUN_METHOD_NAME).and(takesArguments(0)));
                }

                @Override
                public String getMethodsInterceptor() {
                    return CALL_METHOD_INTERCEPTOR;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return HierarchyAndNameMatch.match(PARENT_CLASS, CLASS_NAME_CONTAINS);
    }

}
