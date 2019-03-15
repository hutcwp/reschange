/*
 * Copyright (C) 2017 seiginonakama (https://github.com/seiginonakama).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.hutcwp.remarker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskOutputs
import org.gradle.api.tasks.compile.JavaCompile
import org.zeroturnaround.zip.ZipUtil
import org.zeroturnaround.zip.commons.FileUtils

/**
 * 处理processXXXResources的输出
 *
 * 1. 解包resources.ap_，修改resources.arsc和编译后的xml文件中的id，然后重新打包resources.ap_
 * 2. 处理R.java，批量替换0x7f -> customPackageId
 * 3. 处理生成symbols文件，批量替换0x7f -> customPackageId
 */
class ResPkgRemakerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("remaker", ResPkgRemakerExtension)
        ResPkgRemakerExtension extension = project.remaker

        project.afterEvaluate {
            if (extension.enable) {
                final int customPackageId = extension.packageId

                GenerateRProcessor generateRProcessor = new GenerateRProcessor(customPackageId)
                ResourceChunkProcessor resourceChunkProcessor = new ResourceChunkProcessor(customPackageId)
                project.android.applicationVariants.each { variant ->
                    String fullName = variant.getName().capitalize()

                    Task processAndroidResourceTask = project.tasks.findByName("process${fullName}Resources")
                    Task generateSourcesTask = project.tasks.findByName("generate${fullName}Sources")
                    Task customResourceTask = project.task("custom${fullName}Resources")

                    customResourceTask.dependsOn(processAndroidResourceTask)
                    generateSourcesTask.dependsOn(customResourceTask)

                    Set<File> resourceOutputFiles = new HashSet<>()
                    customResourceTask.doLast {
                        TaskOutputs taskOutputs = processAndroidResourceTask.getOutputs()
                        resourceOutputFiles = taskOutputs.files.files
                        println(resourceOutputFiles)
                        for (File output : resourceOutputFiles) {
                            if (output.isDirectory()) {
                                output.eachFileRecurse {
                                    file ->
                                        if (file.isFile() && file.absolutePath.endsWith("ap_")) {
                                            processOutput(resourceChunkProcessor, file)
                                        }
                                }
                            } else {
                                if (output.isFile() && output.absolutePath.endsWith("ap_")) {
                                    processOutput(resourceChunkProcessor, output)
                                }
                            }
                        }
                    }
                    // 替换R.java中的资源开头
                    JavaCompile javaCompile = variant.getJavaCompile()
                    javaCompile.doFirst {
                        for (File f : javaCompile.source.files) {
                            if (f.isFile()) {
                                if (f.name == 'R.java') {
                                    generateRProcessor.process(f)
                                }
                            } else if (f.isDirectory()) {
                                f.eachFileRecurse {
                                    file ->
                                        if (file.name == 'R.java') {
                                            generateRProcessor.process(file)
                                        }
                                }
                            }
                        }
                        for (File f : resourceOutputFiles) {
                            if (f.isDirectory()) {
                                f.eachFileRecurse {
                                    file ->
                                        if (file.name == 'R.txt') {
                                            generateRProcessor.process(file)
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void processOutput(ResourceChunkProcessor processor, File apk) {
        println("processOutput: ")
        println("apk = " + apk.getName())
        File workPath = new File(apk.getParent(), "remake-res-package-tmp")
        ZipUtil.unpack(apk, workPath)
        processor.processChunkFiles(workPath)
        apk.delete()
        ZipUtil.pack(workPath, apk)
//        FileUtils.deleteDirectory(workPath)
        // 为什么要删除掉工作空间
        deleteAllFilesOfDir(workPath)
    }

    /**
     * 删除文件夹（强制删除）
     *
     * @param path
     */
    public static void deleteAllFilesOfDir(File path) {
        if (null != path) {
            if (!path.exists())
                return;
            if (path.isFile()) {
                boolean result = path.delete();
                int tryCount = 0;
                while (!result && tryCount++ < 10) {
                    System.gc(); // 回收资源
                    result = path.delete();
                }
            }
            File[] files = path.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    deleteAllFilesOfDir(files[i]);
                }
            }
            path.delete();
        }
    }
}