package me.hutcwp.remarker.diff

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by huangfan on 2018/3/13.
 */
class DiffPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

//        project.extensions.create("diffConfig", DiffConfig)

//        if (!project.plugins.hasPlugin('com.android.application')) {
//            throw new GradleException('generateDiffApk: Android Application plugin required')
//        }

//        def android = project.extensions.android

        project.afterEvaluate {
            def configuration = new DiffConfig(oldApk: getOldApkPath(project), newApk: getNewApkPath(project), patchFile: getPatchOutput(project))
//            project.diffConfig = configuration

            project.logger.error("---------------------start diff apk-----------------------------")
            project.logger.error("oldApk : " + configuration.oldApk)
            project.logger.error("newApk : " + configuration.newApk)
            project.logger.error("patchDir :" + configuration.patchFile)
            project.logger.error("----------------------------------------------------------------")

            DiffApkTask diffApkTask = project.tasks.create("mobileYYDiff", DiffApkTask)
            diffApkTask.configuration = configuration

//            android.applicationVariants.all { variant ->
//
//                def variantName = variant.name.capitalize()
//
//                DiffApkTask diffApkTask = project.tasks.create("mobileYYDiff${variantName}", DiffApkTask)
//                diffApkTask.configuration = configuration
//                diffApkTask.dependsOn variant.assemble
//            }
        }

    }

    /**
     * 通过命令参数-POLD_APK=""获取旧版本apk的位置
     * @param project
     * @return
     */
    def getOldApkPath(Project project) {
        return project.hasProperty("OLD_APK") ? project.getProperties().get("OLD_APK") : ""
    }

    /**
     * 通过命令参数-PNEW_APK=""获取新版本apk的位置
     * @return
     */
    def getNewApkPath(Project project) {
        return project.hasProperty("NEW_APK") ? project.getProperties().get("NEW_APK") : ""
    }

    /**
     * 通过命令参数-PDIFF_OUT=""生成具体diff文件的目录
     * @return
     */
    def getPatchOutput(Project project) {
        return project.hasProperty("DIFF_OUT") ? project.getProperties().get("DIFF_OUT") : project.buildDir.absolutePath+File.separator+"patch"
    }
}

class DiffConfig{

    /**
     * 需要生成patch旧版本的apk文件地址
     */
    String oldApk

    /**
     * 需要生成patch新版本(基准版本)的apk文件地址
     */
    String newApk

    /**
     * 生成的patch的文件路径
     */
    String patchFile

    /**
     * 检查参数
     */
    void checkParameter(){
        if(oldApk == "" || newApk == ""){
            throw new GradleException('generateDiffApk: oldApk or newApk can not be null or ""')
        }
    }
}