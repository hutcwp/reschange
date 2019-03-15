package me.hutcwp.remarker.diff


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by huangfan on 2018/3/13.
 */
class DiffApkTask extends DefaultTask {
    def configuration

    DiffApkTask() {
    }

    @TaskAction
    def diffApk() {
        configuration.checkParameter()
        BSDiff.bsdiff(new File(configuration.oldApk), new File(configuration.newApk), new File(configuration.patchFile))
    }
}