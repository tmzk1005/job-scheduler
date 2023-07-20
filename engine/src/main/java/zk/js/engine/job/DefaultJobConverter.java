/*
 * Copyright 2023 zoukang, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zk.js.engine.job;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;

public class DefaultJobConverter implements JobConverter {

    private final Map<String, String> jobTypes;

    public DefaultJobConverter(Map<String, String> jobTypes) {
        this.jobTypes = jobTypes;
    }

    @Override
    public Job convertJobDefinition(JobDefinition jobDefinition) throws JobConverterException {
        final String className = jobTypes.get(jobDefinition.getType());
        if (Objects.isNull(className)) {
            throw new JobConverterException("Unsupported job type " + jobDefinition.getType());
        }
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor(JobDefinition.class);
            return (Job) constructor.newInstance(jobDefinition);
        } catch (ClassNotFoundException classNotFoundException) {
            throw new JobConverterException("No class named " + className + " found.");
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new JobConverterException("Class " + className + " has no constructor with single JobDefinition parameter.");
        } catch (Exception exception) {
            throw new JobConverterException("Can not build job with given job definition type " + jobDefinition.getType(), exception);
        }
    }

}
